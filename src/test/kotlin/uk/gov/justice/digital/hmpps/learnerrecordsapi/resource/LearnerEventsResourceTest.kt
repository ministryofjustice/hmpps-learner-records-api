package uk.gov.justice.digital.hmpps.learnerrecordsapi.resource

import kotlinx.coroutines.test.runTest
import net.minidev.json.JSONObject
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
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.db.MatchEntity
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.lrsapi.response.exceptions.MatchNotFoundException
import uk.gov.justice.digital.hmpps.learnerrecordsapi.service.LearnerEventsService
import uk.gov.justice.digital.hmpps.learnerrecordsapi.service.MatchService
import uk.gov.justice.hmpps.sqs.audit.HmppsAuditService
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
class LearnerEventsResourceTest {
  private val nomisId = "A1234BC"
  private val matchedUln = "q1234"
  private val familyName = "Smith"
  private val dateOfBirth = LocalDate.of(1980, 1, 1).toString()
  private val gender = "Male"
  private val givenName = "John Smith"

  private lateinit var mockMatchService: MatchService
  private lateinit var mockLearnerEventsService: LearnerEventsService
  private lateinit var mockAuditService: HmppsAuditService
  private lateinit var learnerEventsResource: LearnerEventsResource

  @BeforeEach
  fun setup() {
    mockMatchService = mock(MatchService::class.java)
    mockLearnerEventsService = mock(LearnerEventsService::class.java)
    mockAuditService = mock(HmppsAuditService::class.java)
    learnerEventsResource = LearnerEventsResource(mockLearnerEventsService, mockAuditService)
  }

  @Test
  fun `should throw MatchNotFound Exception if no match found`(): Unit = runTest {
    `when`(mockLearnerEventsService.getMatchEntityForNomisId(any())).thenReturn(null)
    val requestJson = JSONObject()
    requestJson.put("nomisId", nomisId)
    val exception = assertThrows<MatchNotFoundException> {
      learnerEventsResource.findLearnerEventsByNomisId(requestJson, "")
    }
    assertThat(exception.message).isEqualTo(nomisId)
  }

  @Test
  fun `should return entity if record found`(): Unit = runTest {
    `when`(mockLearnerEventsService.getMatchEntityForNomisId(any())).thenReturn(
      MatchEntity(
        nomisId = nomisId,
        matchedUln = matchedUln,
        familyName = familyName,
        givenName = givenName,
        dateOfBirth = dateOfBirth,
        gender = gender,
      ),
    )
    val requestJson = JSONObject()
    requestJson.put("nomisId", nomisId)
    val actual = learnerEventsResource.findLearnerEventsByNomisId(requestJson, "")
    assertThat(actual.statusCode).isEqualTo(HttpStatus.OK)
  }
}
