package uk.gov.justice.digital.hmpps.learnerrecordsapi.resource

import kotlinx.coroutines.test.runTest
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.springframework.http.HttpStatus
import org.springframework.security.core.GrantedAuthority
import uk.gov.justice.digital.hmpps.learnerrecordsapi.config.Roles.ROLE_LEARNERS_RO
import uk.gov.justice.digital.hmpps.learnerrecordsapi.config.Roles.ROLE_LEARNERS_UI
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.MatchStatus
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.lrsapi.response.exceptions.MatchNotFoundException
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.lrsapi.response.exceptions.MatchNotPossibleException
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.request.ConfirmMatchRequest
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.request.ConfirmNoMatchRequest
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.request.MatchType
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.response.CheckMatchResponse
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.response.CheckMatchStatus
import uk.gov.justice.digital.hmpps.learnerrecordsapi.service.LearnerEventsService
import uk.gov.justice.digital.hmpps.learnerrecordsapi.service.MatchService
import uk.gov.justice.hmpps.kotlin.auth.HmppsAuthenticationHolder
import uk.gov.justice.hmpps.sqs.audit.HmppsAuditService
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
class MatchResourceTest {
  private val nomisId = "A1234BC"
  private val matchedUln = "q1234"
  private val familyName = "Smith"
  private val dateOfBirth = LocalDate.of(1980, 1, 1).toString()
  private val gender = "Male"
  private val givenName = "John Smith"

  private lateinit var mockMatchService: MatchService
  private lateinit var matchResource: MatchResource
  private lateinit var learnerEventsResource: LearnerEventsResource
  private lateinit var mockAuditService: HmppsAuditService
  private lateinit var mockLearnerEventsService: LearnerEventsService
  private lateinit var mockAuthHolder: HmppsAuthenticationHolder

  @BeforeEach
  fun setup() {
    mockMatchService = mock(MatchService::class.java)
    mockLearnerEventsService = mock(LearnerEventsService::class.java)
    mockAuditService = mock(HmppsAuditService::class.java)
    mockAuthHolder = mock(HmppsAuthenticationHolder::class.java)
    matchResource = MatchResource(mockMatchService, mockLearnerEventsService, mockAuditService, mockAuthHolder)
    learnerEventsResource = LearnerEventsResource(mockLearnerEventsService)
  }

  private fun setRoleAndUln(role: String, uln: String?) {
    `when`(mockAuthHolder.roles).thenReturn(
      listOf(GrantedAuthority { role }),
    )
    `when`(mockMatchService.findMatch(any())).thenReturn(
      uln?.let {
        CheckMatchResponse(
          matchedUln = it,
        ).setStatus()
      },
    )
  }

  @Test
  fun `should return NOT_FOUND if no record found`() = runTest {
    setRoleAndUln(ROLE_LEARNERS_UI, null)

    val actual = matchResource.findMatch(nomisId, "")
    assertThat(actual.statusCode).isEqualTo(HttpStatus.NOT_FOUND)
    assertThat(actual.body?.status ?: "").isEqualTo(CheckMatchStatus.NotFound)
    verify(mockAuditService, times(0)).publishEvent(any())
  }

  @Test
  fun `should return entity if record found`() = runTest {
    setRoleAndUln(ROLE_LEARNERS_RO, matchedUln)

    val actual = matchResource.findMatch(nomisId, "")
    assertThat(actual.statusCode).isEqualTo(HttpStatus.OK)
    assertThat(actual.body?.matchedUln ?: "").isEqualTo(matchedUln)
    assertThat(actual.body?.status ?: "").isEqualTo(CheckMatchStatus.Found)
    verify(mockAuditService, times(1)).publishEvent(any())
  }

  @Test
  fun `should return NO_MATCH if id cannot be matched`() = runTest {
    setRoleAndUln(ROLE_LEARNERS_UI, "")

    val actual = matchResource.findMatch(nomisId, "")
    assertThat(actual.statusCode).isEqualTo(HttpStatus.OK)
    assertThat(actual.body?.status ?: "").isEqualTo(CheckMatchStatus.NoMatch)
    verify(mockAuditService, times(0)).publishEvent(any())
  }

  @Test
  fun `should throw MatchNotFound Exception if no match found`(): Unit = runTest {
    `when`(mockMatchService.findMatch(any())).thenReturn(null)
    val exception = assertThrows<MatchNotFoundException> {
      matchResource.findLearnerEventsByNomisId(nomisId, "")
    }
    assertThat(exception.nomisId).isEqualTo(nomisId)
    assertThat(exception.status).isEqualTo(CheckMatchStatus.NotFound)
    verify(mockAuditService, times(1)).publishEvent(any())
  }

  @Test
  fun `should throw MatchNotPossible Exception if match not possible`(): Unit = runTest {
    `when`(mockMatchService.findMatch(any())).thenReturn(
      CheckMatchResponse(
        status = CheckMatchStatus.NoMatch,
      ),
    )
    val exception = assertThrows<MatchNotPossibleException> {
      matchResource.findLearnerEventsByNomisId(nomisId, "")
    }
    assertThat(exception.nomisId).isEqualTo(nomisId)
    assertThat(exception.status).isEqualTo(CheckMatchStatus.NoMatch)
    verify(mockAuditService, times(1)).publishEvent(any())
  }

  @Test
  fun `should return Match entity if record found`(): Unit = runTest {
    `when`(mockMatchService.findMatch(any())).thenReturn(
      CheckMatchResponse(
        matchedUln = matchedUln,
        familyName = familyName,
        givenName = givenName,
        status = CheckMatchStatus.Found,
      ),
    )
    val actual = matchResource.findLearnerEventsByNomisId(nomisId, "")
    assertThat(actual.statusCode).isEqualTo(HttpStatus.OK)
  }

  @Test
  fun `should save the Match`(): Unit = runTest {
    `when`(mockMatchService.isUnmatched(any(), any())).thenReturn(true)
    `when`(mockMatchService.saveMatch(any(), any())).thenReturn(MatchStatus.MATCHED)
    val actual = matchResource.confirmMatch(
      "",
      nomisId,
      ConfirmMatchRequest(
        matchType = MatchType.EXACT_MATCH,
        countOfReturnedUlns = "1",
        matchingUln = "1234567890",
        givenName = "John",
        familyName = "Smith",
      ),
    )
    assertThat(actual.statusCode).isEqualTo(HttpStatus.CREATED)
    verify(mockMatchService, times(1)).saveMatch(any(), any())
  }

  @Test
  fun `should return COINFLICT and not save the Match if uln already in use`(): Unit = runTest {
    `when`(mockMatchService.isUnmatched(any(), any())).thenReturn(false)
    val actual = matchResource.confirmMatch(
      "",
      nomisId,
      ConfirmMatchRequest(
        matchType = MatchType.EXACT_MATCH,
        countOfReturnedUlns = "1",
        matchingUln = "1234567890",
        givenName = "John",
        familyName = "Smith",
      ),
    )
    assertThat(actual.statusCode).isEqualTo(HttpStatus.CONFLICT)
    verify(mockMatchService, times(0)).saveMatch(any(), any())
  }

  @Test
  fun `should save No Match`(): Unit = runTest {
    `when`(mockMatchService.saveNoMatch(any(), any())).thenReturn(MatchStatus.MATCH_NOT_POSSIBLE)
    val actual = matchResource.confirmNoMatch(
      "",
      nomisId,
      ConfirmNoMatchRequest(
        matchType = MatchType.NO_MATCH_RETURNED_FROM_LRS,
        countOfReturnedUlns = "1",
      ),
    )
    assertThat(actual.statusCode).isEqualTo(HttpStatus.CREATED)
    verify(mockMatchService, times(1)).saveNoMatch(any(), any())
  }

  @Test
  fun `should save Unmatch`(): Unit = runTest {
    `when`(mockMatchService.unMatch(any())).thenReturn(MatchStatus.UNMATCHED)
    val actual = matchResource.confirmUnmatch(
      "",
      nomisId,
    )
    assertThat(actual.statusCode).isEqualTo(HttpStatus.CREATED)
    verify(mockMatchService, times(1)).unMatch(any())
  }
}

fun CheckMatchResponse.setStatus() = this.copy(
  status = if (this.matchedUln.isNullOrEmpty() || this.matchedUln!!.isBlank()) {
    CheckMatchStatus.NoMatch
  } else {
    CheckMatchStatus.Found
  },
)
