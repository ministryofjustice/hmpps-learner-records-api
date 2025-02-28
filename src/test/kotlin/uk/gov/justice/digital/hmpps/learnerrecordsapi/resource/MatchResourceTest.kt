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
import org.springframework.http.HttpStatus
import org.testcontainers.shaded.org.bouncycastle.asn1.isismtt.x509.DeclarationOfMajority.dateOfBirth
import org.testcontainers.shaded.org.bouncycastle.asn1.x500.style.RFC4519Style.givenName
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.lrsapi.response.exceptions.MatchNotFoundException
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.response.CheckMatchResponse
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.response.CheckMatchStatus
import uk.gov.justice.digital.hmpps.learnerrecordsapi.service.LearnerEventsService
import uk.gov.justice.digital.hmpps.learnerrecordsapi.service.MatchService
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

  @BeforeEach
  fun setup() {
    mockMatchService = mock(MatchService::class.java)
    mockLearnerEventsService = mock(LearnerEventsService::class.java)
    mockAuditService = mock(HmppsAuditService::class.java)
    matchResource = MatchResource(mockMatchService, mockLearnerEventsService, mockAuditService)
    learnerEventsResource = LearnerEventsResource(mockLearnerEventsService, mockAuditService)
  }

  @Test
  fun `should return NOT_FOUND if no record found`() {
    `when`(mockMatchService.findMatch(any())).thenReturn(null)

    val actual = matchResource.findMatch(nomisId, "")
    assertThat(actual.statusCode).isEqualTo(HttpStatus.NOT_FOUND)
    assertThat(actual.body?.status ?: "").isEqualTo(CheckMatchStatus.NotFound)
  }

  @Test
  fun `should return entity if record found`() {
    `when`(mockMatchService.findMatch(any())).thenReturn(
      CheckMatchResponse(
        matchedUln = matchedUln,
      ),
    )

    val actual = matchResource.findMatch(nomisId, "")
    assertThat(actual.statusCode).isEqualTo(HttpStatus.OK)
    assertThat(actual.body?.matchedUln ?: "").isEqualTo(matchedUln)
    assertThat(actual.body?.status ?: "").isEqualTo(CheckMatchStatus.Found)
  }

  @Test
  fun `should return NO_MATCH if id cannot be matched`() {
    `when`(mockMatchService.findMatch(any())).thenReturn(
      CheckMatchResponse(
        matchedUln = "",
      ),
    )

    val actual = matchResource.findMatch(nomisId, "")
    assertThat(actual.statusCode).isEqualTo(HttpStatus.OK)
    assertThat(actual.body?.status ?: "").isEqualTo(CheckMatchStatus.NoMatch)
  }

  @Test
  fun `should throw MatchNotFound Exception if no match found`(): Unit = runTest {
    `when`(mockLearnerEventsService.getMatchEntityForNomisId(any())).thenReturn(null)
    val exception = assertThrows<MatchNotFoundException> {
      matchResource.findLearnerEventsByNomisId(nomisId, "")
    }
    assertThat(exception.message).isEqualTo(nomisId)
  }

  @Test
  fun `should return Match entity if record found`(): Unit = runTest {
    `when`(mockLearnerEventsService.getMatchEntityForNomisId(any())).thenReturn(
      CheckMatchResponse(
        matchedUln = matchedUln,
        familyName = familyName,
        givenName = givenName,
        dateOfBirth = dateOfBirth,
        gender = gender,
        status = CheckMatchStatus.Found,
      ),
    )
    val actual = matchResource.findLearnerEventsByNomisId(nomisId, "")
    assertThat(actual.statusCode).isEqualTo(HttpStatus.OK)
  }
}
