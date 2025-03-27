package uk.gov.justice.digital.hmpps.learnerrecordsapi.util

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.never
import org.mockito.kotlin.spy
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.lrsapi.response.Learner
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.request.Gender
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.request.LearnersRequest
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.response.CheckMatchResponse
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.response.LRSResponseType
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.response.LearnersResponse
import uk.gov.justice.digital.hmpps.learnerrecordsapi.service.LearnersService
import uk.gov.justice.digital.hmpps.learnerrecordsapi.service.MatchService
import uk.gov.justice.digital.hmpps.learnerrecordsapi.utils.BatchMatcher

@ExtendWith(MockitoExtension::class)
class BatchMatcherTest {

  @Mock
  lateinit var matchService: MatchService

  @Mock
  lateinit var learnersService: LearnersService

  @InjectMocks
  lateinit var batchMatcher: BatchMatcher

  lateinit var spyBatchMatcher: BatchMatcher

  @BeforeEach
  fun setUp() {
    spyBatchMatcher = spy(batchMatcher)
  }

  @Test
  fun `creates match when LRS returns exact match`(): Unit = runBlocking {
    val prisonerList = listOf(
      arrayOf("nomisId123", "John", "Doe", "1980-01-01", "MALE", "AB1 2CD"),
    )

    doReturn(prisonerList).`when`(spyBatchMatcher).loadPrisonersFromCSV()

    whenever(matchService.findMatch(any())).thenReturn(null)

    val learnersResponse = LearnersResponse(
      searchParameters = LearnersRequest("", "", "", Gender.NOT_SPECIFIED, ""),
      matchedLearners = listOf(
        Learner(
          uln = "1234567890",
          lastKnownPostCode = "AB1 2CD",
        ),
      ),
      responseType = LRSResponseType.EXACT_MATCH,
    )

    whenever(learnersService.getLearners(any(), any())).thenReturn(learnersResponse)
    spyBatchMatcher.matchFromCSV()
    verify(matchService).saveMatch(any(), any())
  }

  @Test
  fun `creates match when LRS returns single linked learner`(): Unit = runBlocking {
    val prisonerList = listOf(
      arrayOf("nomisId123", "John", "Doe", "1980-01-01", "MALE", "AB1 2CD"),
    )

    doReturn(prisonerList).`when`(spyBatchMatcher).loadPrisonersFromCSV()

    whenever(matchService.findMatch(any())).thenReturn(null)

    val learnersResponse = LearnersResponse(
      searchParameters = LearnersRequest("", "", "", Gender.NOT_SPECIFIED, ""),
      matchedLearners = listOf(
        Learner(
          uln = "1234567890",
          lastKnownPostCode = "AB1 2CD",
        ),
      ),
      responseType = LRSResponseType.LINKED_LEARNER,
    )

    whenever(learnersService.getLearners(any(), any())).thenReturn(learnersResponse)
    spyBatchMatcher.matchFromCSV()
    verify(matchService).saveMatch(any(), any())
  }

  @Test
  fun `creates match when LRS returns single possible match with matching postcode`(): Unit = runBlocking {
    val prisonerList = listOf(
      arrayOf("nomisId123", "John", "Doe", "1980-01-01", "MALE", "AB1 2CD"),
    )

    doReturn(prisonerList).`when`(spyBatchMatcher).loadPrisonersFromCSV()

    whenever(matchService.findMatch(any())).thenReturn(null)

    val learnersResponse = LearnersResponse(
      searchParameters = LearnersRequest("", "", "", Gender.NOT_SPECIFIED, ""),
      matchedLearners = listOf(
        Learner(
          uln = "1234567890",
          lastKnownPostCode = "AB1 2CD",
        ),
      ),
      responseType = LRSResponseType.POSSIBLE_MATCH,
    )

    whenever(learnersService.getLearners(any(), any())).thenReturn(learnersResponse)
    spyBatchMatcher.matchFromCSV()
    verify(matchService).saveMatch(any(), any())
  }

  @Test
  fun `does not create match when LRS returns single possible match with mismatched postcode`(): Unit = runBlocking {
    val prisonerList = listOf(
      arrayOf("nomisId123", "John", "Doe", "1980-01-01", "MALE", "AB1 2CD"),
    )

    doReturn(prisonerList).`when`(spyBatchMatcher).loadPrisonersFromCSV()

    whenever(matchService.findMatch(any())).thenReturn(null)

    val learnersResponse = LearnersResponse(
      searchParameters = LearnersRequest("", "", "", Gender.NOT_SPECIFIED, ""),
      matchedLearners = listOf(
        Learner(
          uln = "1234567890",
          lastKnownPostCode = "WX9 8YZ",
        ),
      ),
      responseType = LRSResponseType.POSSIBLE_MATCH,
    )

    whenever(learnersService.getLearners(any(), any())).thenReturn(learnersResponse)
    spyBatchMatcher.matchFromCSV()
    verify(matchService, never()).saveMatch(any(), any())
  }

  @Test
  fun `does not create match when LRS returns multiple possible matches`(): Unit = runBlocking {
    val prisonerList = listOf(
      arrayOf("nomisId123", "John", "Doe", "1980-01-01", "MALE", "AB1 2CD"),
    )

    doReturn(prisonerList).`when`(spyBatchMatcher).loadPrisonersFromCSV()

    whenever(matchService.findMatch(any())).thenReturn(null)

    val learnersResponse = LearnersResponse(
      searchParameters = LearnersRequest("", "", "", Gender.NOT_SPECIFIED, ""),
      matchedLearners = listOf(
        Learner(
          uln = "1234567890",
          lastKnownPostCode = "AB1 2CD",
        ),
        Learner(
          uln = "0987654321",
          lastKnownPostCode = "AB1 2CD",
        ),
      ),
      responseType = LRSResponseType.POSSIBLE_MATCH,
    )

    whenever(learnersService.getLearners(any(), any())).thenReturn(learnersResponse)
    spyBatchMatcher.matchFromCSV()
    verify(matchService, never()).saveMatch(any(), any())
  }

  @Test
  fun `skips prisoners that are already matched`(): Unit = runBlocking {
    val prisonerList = listOf(
      arrayOf("nomisId123", "John", "Doe", "1980-01-01", "MALE", "A1B 2C3"),
    )
    doReturn(prisonerList).`when`(spyBatchMatcher).loadPrisonersFromCSV()
    whenever(matchService.findMatch("nomisId123")).thenReturn(CheckMatchResponse())
    spyBatchMatcher.matchFromCSV()
    verify(matchService, never()).saveMatch(any(), any())
  }
}
