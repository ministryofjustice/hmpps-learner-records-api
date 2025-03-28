package uk.gov.justice.digital.hmpps.learnerrecordsapi.utils

import com.opencsv.CSVReader
import jakarta.annotation.PostConstruct
import kotlinx.coroutines.runBlocking
import org.slf4j.Logger
import org.springframework.beans.factory.annotation.Value
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
  @Value("\${feature.enable-batch-matching:false}")
  private val batchMatchingEnabled: Boolean,
) {

  val logger: Logger = LoggerUtil.getLogger<BatchMatcher>()

  @PostConstruct
  fun matchFromCSV() = runBlocking {
    if (batchMatchingEnabled) {
      logger.info("Batch matching started...")
      val prisonerList = loadPrisonersFromCSV()

      for (prisoner in prisonerList) {
        val nomisId = prisoner[0]
        val (givenName, familyName, dateOfBirth, gender, lastKnownPostCode) = prisoner.slice(1..5).map { it.trim() }

        if (matchService.findMatch(nomisId) != null) continue

        val searchRequest = LearnersRequest(givenName, familyName, dateOfBirth, Gender.valueOf(gender), lastKnownPostCode)
        val result = learnersService.getLearners(searchRequest, "hmpps-learner-records-api-batch-match")

        if (shouldCreateMatch(result, lastKnownPostCode)) {
          try {
            createMatch(result, givenName, familyName, nomisId)
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
    val resource = ClassPathResource("example.prisoners.csv")
    resource.inputStream.bufferedReader(StandardCharsets.UTF_8).use { reader ->
      CSVReader(reader).readAll()
        .map { row -> row.map { it.trim() }.toTypedArray() }
        .filter { row -> row.isNotEmpty() && row.any { it.isNotBlank() } }
        .onEach { row ->
          if (row.size != 6) {
            throw IllegalArgumentException("CSV contains a row with ${row.size} columns instead of 6: ${row.contentToString()}")
          }
          if (row.any { it.isBlank() }) {
            throw IllegalArgumentException("CSV contains a row with an empty column: ${row.contentToString()}")
          }
        }
    }
  } catch (e: Exception) {
    logger.error("Error reading prisoners CSV: ${e.message}")
    emptyList()
  }
}
