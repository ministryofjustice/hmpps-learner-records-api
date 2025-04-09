package uk.gov.justice.digital.hmpps.learnerrecordsapi.utils

import jakarta.annotation.PostConstruct
import kotlinx.coroutines.runBlocking
import org.slf4j.Logger
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import uk.gov.justice.digital.hmpps.learnerrecordsapi.logging.LoggerUtil
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.request.ConfirmMatchRequest
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.request.Gender
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.request.LearnersRequest
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.request.MatchType
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.response.LRSResponseType
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.response.LearnersResponse
import uk.gov.justice.digital.hmpps.learnerrecordsapi.service.LearnersService
import uk.gov.justice.digital.hmpps.learnerrecordsapi.service.MatchService
import uk.gov.justice.digital.hmpps.learnerrecordsapi.service.PrisonerSearchService

@Component
class BatchMatcher(
  private val matchService: MatchService,
  private val learnersService: LearnersService,
  val prisonerSearchService: PrisonerSearchService,
  @Value("\${feature.enable-batch-matching:false}")
  private val batchMatchingEnabled: Boolean = false,
  @Value("\${feature.prisonCode}")
  private val prisonCode: String,
  @Value("\${feature.pageSize}")
  private val pageSize: Int,
) {

  val logger: Logger = LoggerUtil.getLogger<BatchMatcher>()

  @PostConstruct
  fun matchFromPrisonerSearchAPI() = runBlocking {
    if (batchMatchingEnabled) {
      logger.info("Batch matching started...")
      val prisonerList = prisonerSearchService.findPrisonersByPrisonId(prisonCode, pageSize)

      for (prisoner in prisonerList) {
        val nomisId = prisoner.prisonerNumber
        val givenName = prisoner.firstName
        val familyName = prisoner.lastName
        val dateOfBirth = prisoner.dateOfBirth
        val gender = prisoner.genderTransformed
        val lastKnownPostCode = prisoner.primaryPostalCode.orEmpty()

        if (matchService.findMatch(nomisId) != null) continue

        val searchRequest = LearnersRequest(givenName, familyName, dateOfBirth, Gender.valueOf(gender), lastKnownPostCode)
        val result = learnersService.getLearners(searchRequest, "hmpps-learner-records-api-batch-match")

        if (shouldCreateMatch(result, lastKnownPostCode)) {
          try {
            createMatch(result, nomisId)
          } catch (e: Exception) {
            logger.error("Failed to auto-match match prisoner: $nomisId")
          }
        }
      }
      logger.info("Batch matching complete.")
    } else {
      logger.info("Batch matching is disabled.")
    }
  }

  private fun shouldCreateMatch(result: LearnersResponse, lastKnownPostCode: String): Boolean {
    val hasOneMatchedLearner = result.matchedLearners?.size == 1
    return when {
      result.responseType in arrayOf(LRSResponseType.EXACT_MATCH, LRSResponseType.LINKED_LEARNER) && hasOneMatchedLearner -> true
      result.responseType == LRSResponseType.POSSIBLE_MATCH &&
        hasOneMatchedLearner &&
        (
          result.matchedLearners?.get(0)?.lastKnownPostCode == lastKnownPostCode ||
            result.matchedLearners?.get(0)?.lastKnownPostCode == "ZZ99 9ZZ"
          ) -> true
      else -> false
    }
  }

  private fun createMatch(
    result: LearnersResponse,
    nomisId: String,
  ) {
    val confirmMatchRequest = ConfirmMatchRequest(
      matchingUln = result.matchedLearners?.get(0)?.uln.orEmpty(),
      givenName = result.matchedLearners?.get(0)?.givenName.orEmpty(),
      familyName = result.matchedLearners?.get(0)?.familyName.orEmpty(),
      matchType = MatchType.fromString(result.responseType.englishName),
      countOfReturnedUlns = "1",
    )

    matchService.saveMatch(nomisId, confirmMatchRequest)
  }
}
