package uk.gov.justice.digital.hmpps.learnerrecordsapi.resource

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.springframework.http.HttpStatus
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.db.MatchEntity
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.request.CheckMatchRequest
import uk.gov.justice.digital.hmpps.learnerrecordsapi.service.MatchService

@ExtendWith(MockitoExtension::class)
class MatchResourceTest {
  private val NOMIS_ID = "NOMIS_ID"
  private val LEARNER_ID = "LEARNER_ID"

  private lateinit var mockMatchService: MatchService
  private lateinit var matchResource: MatchResource

  @BeforeEach
  fun setup() {
    mockMatchService = mock(MatchService::class.java)
    matchResource = MatchResource(mockMatchService)
  }

  @Test
  fun `should return NOT_FOUND if no record found`() {
    `when`(mockMatchService.findMatch(any())).thenReturn(null)

    val actual = matchResource.findMatch(CheckMatchRequest(
      nomisId = NOMIS_ID
    ), "")
    assertThat(actual.statusCode).isEqualTo(HttpStatus.NOT_FOUND)
  }

  @Test
  fun `should return entity if record found`() {
    `when`(mockMatchService.findMatch(any())).thenReturn(MatchEntity(
      nomisId = NOMIS_ID,
      matchedUln = LEARNER_ID,
    ))

    val actual = matchResource.findMatch(CheckMatchRequest(
      nomisId = NOMIS_ID
    ), "")
    assertThat(actual.statusCode).isEqualTo(HttpStatus.OK)
    assertThat(actual.body?.matchedUln ?: "").isEqualTo(LEARNER_ID)
  }
}
