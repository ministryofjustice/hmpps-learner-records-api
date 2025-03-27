package uk.gov.justice.digital.hmpps.learnerrecordsapi.utils

import com.opencsv.CSVReader
import jakarta.annotation.PostConstruct
import org.slf4j.Logger
import org.springframework.core.io.ClassPathResource
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
import java.nio.charset.StandardCharsets

@Component
class BatchMatcher(
  private val matchService: MatchService,
  private val learnersService: LearnersService,
) {

  val logger: Logger = LoggerUtil.getLogger<LearnersService>()

  // Runs once at startup
  @PostConstruct
  suspend fun matchFromCSV() {
    // Load CSV
    val prisonerList = loadPrisonersFromCSV()

    // Loop through CSV data
    for (prisoner in prisonerList) {
      val nomisId = prisoner[0]
      val (givenName, familyName, dateOfBirth, gender, lastKnownPostCode) = prisoner.slice(1..5).map { it.trim() }

      // Skip if already matched
      if (matchService.findMatch(nomisId) != null) continue

      // Perform search for matching learners
      val searchRequest = LearnersRequest(givenName, familyName, dateOfBirth, Gender.valueOf(gender), lastKnownPostCode)
      val result = learnersService.getLearners(searchRequest, "hmpps-learner-records-api-batch-match")

      // Create match if exact or linked, if possible then only when the postcode matches.
      if (shouldCreateMatch(result, lastKnownPostCode)) {
        createMatch(result, givenName, familyName, nomisId)
      }
    }
  }

  private fun shouldCreateMatch(result: LearnersResponse, lastKnownPostCode: String): Boolean {
    val hasOneMatchedLearner = result.matchedLearners?.size == 1
    return when {
      result.responseType in arrayOf(LRSResponseType.EXACT_MATCH, LRSResponseType.LINKED_LEARNER) && hasOneMatchedLearner -> true
      result.responseType == LRSResponseType.POSSIBLE_MATCH && hasOneMatchedLearner && result.matchedLearners?.get(0)?.lastKnownPostCode == lastKnownPostCode -> true
      else -> false
    }
  }

  private fun createMatch(
    result: LearnersResponse,
    givenName: String,
    familyName: String,
    nomisId: String,
  ) {
    val confirmMatchRequest = ConfirmMatchRequest(
      matchingUln = result.matchedLearners?.get(0)?.uln.orEmpty(),
      givenName = givenName,
      familyName = familyName,
      matchType = MatchType.fromString(result.responseType.englishName),
      countOfReturnedUlns = "1",
    )

    matchService.saveMatch(nomisId, confirmMatchRequest)
  }

  fun loadPrisonersFromCSV(): List<Array<String>> = try {
    val resource = ClassPathResource("prisoners.csv")
    resource.inputStream.bufferedReader(StandardCharsets.UTF_8).use { reader ->
      CSVReader(reader).readAll()
        .filter { it.size == 6 }
    }
  } catch (e: Exception) {
    logger.error("Error reading prisoners CSV: ${e.message}")
    emptyList()
  }
}
