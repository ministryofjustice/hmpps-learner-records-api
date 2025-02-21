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
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.response.CheckMatchStatus
import uk.gov.justice.digital.hmpps.learnerrecordsapi.service.MatchService

@ExtendWith(MockitoExtension::class)
class MatchResourceTest {
  private val nomisId = "A1234BC"
  private val matchedUln = "q1234"

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

    val actual = matchResource.findMatch(nomisId, "")
    assertThat(actual.statusCode).isEqualTo(HttpStatus.NOT_FOUND)
    assertThat(actual.body?.status ?: "").isEqualTo(CheckMatchStatus.NotFound)
  }

  @Test
  fun `should return entity if record found`() {
    `when`(mockMatchService.findMatch(any())).thenReturn(
      MatchEntity(
        nomisId = nomisId,
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
      MatchEntity(
        nomisId = nomisId,
        matchedUln = "",
      ),
    )

    val actual = matchResource.findMatch(nomisId, "")
    assertThat(actual.statusCode).isEqualTo(HttpStatus.OK)
    assertThat(actual.body?.status ?: "").isEqualTo(CheckMatchStatus.NoMatch)
  }
}
