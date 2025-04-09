package uk.gov.justice.digital.hmpps.learnerrecordsapi.util

import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
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
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.response.Address
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.response.CheckMatchResponse
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.response.LRSResponseType
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.response.LearnersResponse
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.response.PrisonerSearchResponse
import uk.gov.justice.digital.hmpps.learnerrecordsapi.service.LearnersService
import uk.gov.justice.digital.hmpps.learnerrecordsapi.service.MatchService
import uk.gov.justice.digital.hmpps.learnerrecordsapi.service.PrisonerSearchService
import uk.gov.justice.digital.hmpps.learnerrecordsapi.utils.BatchMatcher

@ExtendWith(MockitoExtension::class)
class BatchMatcherTest {

  @Mock
  lateinit var matchService: MatchService

  @Mock
  lateinit var learnersService: LearnersService

  @Mock
  lateinit var prisonerSearchService: PrisonerSearchService

  lateinit var spyBatchMatcher: BatchMatcher

  @BeforeEach
  fun setUp() {
    spyBatchMatcher = spy(BatchMatcher(matchService, learnersService, prisonerSearchService, true, "BXI", 100))
    doReturn(prisonerList).`when`(prisonerSearchService).findPrisonersByPrisonId("BXI", 100)
    whenever(matchService.findMatch(any())).thenReturn(null)
  }

  private val prisonerList = listOf(
    PrisonerSearchResponse(
      prisonerNumber = "nomisId123",
      firstName = "John",
      lastName = "Doe",
      dateOfBirth = "1980-01-01",
      gender = "Male",
      status = "ACTIVE IN",
      addresses = listOf(
        Address(
          fullAddress = "AB1 2CD, England",
          postalCode = "AB1 2CD",
          primaryAddress = true,
        ),
      ),
    ),
  )

  private fun learnersResponse(responseType: LRSResponseType, postcode: String = "AB1 2CD", count: Int = 1): LearnersResponse = LearnersResponse(
    searchParameters = LearnersRequest("", "", "", Gender.NOT_SPECIFIED, ""),
    matchedLearners = List(count) {
      Learner(
        uln = "1234567890",
        lastKnownPostCode = postcode,
      )
    },
    responseType = responseType,
  )

  @Test
  fun `creates match when LRS returns exact match`(): Unit = runBlocking {
    whenever(learnersService.getLearners(any(), any())).thenReturn(learnersResponse(LRSResponseType.EXACT_MATCH))
    spyBatchMatcher.matchFromPrisonerSearchAPI()
    verify(matchService).saveMatch(any(), any())
  }

  @Test
  fun `creates match when LRS returns single linked learner`(): Unit = runBlocking {
    whenever(learnersService.getLearners(any(), any())).thenReturn(learnersResponse(LRSResponseType.LINKED_LEARNER))
    spyBatchMatcher.matchFromPrisonerSearchAPI()
    verify(matchService).saveMatch(any(), any())
  }

  @Test
  fun `creates match when LRS returns single possible match with matching postcode`(): Unit = runBlocking {
    whenever(learnersService.getLearners(any(), any())).thenReturn(learnersResponse(LRSResponseType.POSSIBLE_MATCH))
    spyBatchMatcher.matchFromPrisonerSearchAPI()
    verify(matchService).saveMatch(any(), any())
  }

  @Test
  fun `creates match when LRS returns single possible match with default postcode`(): Unit = runBlocking {
    whenever(learnersService.getLearners(any(), any())).thenReturn(learnersResponse(LRSResponseType.POSSIBLE_MATCH, postcode = "ZZ99 9ZZ"))
    spyBatchMatcher.matchFromPrisonerSearchAPI()
    verify(matchService).saveMatch(any(), any())
  }

  @Test
  fun `does not create match when LRS returns single possible match with mismatched non-default postcode`(): Unit = runBlocking {
    whenever(learnersService.getLearners(any(), any())).thenReturn(learnersResponse(LRSResponseType.POSSIBLE_MATCH, postcode = "XY2 Z91"))
    spyBatchMatcher.matchFromPrisonerSearchAPI()
    verify(matchService, never()).saveMatch(any(), any())
  }

  @Test
  fun `does not create match when LRS returns multiple possible matches`(): Unit = runBlocking {
    whenever(learnersService.getLearners(any(), any())).thenReturn(learnersResponse(LRSResponseType.POSSIBLE_MATCH, count = 2))
    spyBatchMatcher.matchFromPrisonerSearchAPI()
    verify(matchService, never()).saveMatch(any(), any())
  }

  @Test
  fun `skips prisoners that are already matched`(): Unit = runBlocking {
    whenever(matchService.findMatch("nomisId123")).thenReturn(CheckMatchResponse())
    spyBatchMatcher.matchFromPrisonerSearchAPI()
    verify(matchService, never()).saveMatch(any(), any())
  }
}
