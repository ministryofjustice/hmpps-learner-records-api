package uk.gov.justice.digital.hmpps.learnerrecordsapi.integration

import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.db.MatchEntity
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.response.CheckMatchResponse
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.response.CheckMatchStatus
import uk.gov.justice.digital.hmpps.learnerrecordsapi.repository.MatchRepository

class MatchResourceIntTest : IntegrationTestBase() {

  @Autowired
  protected lateinit var objectMapper: ObjectMapper

  @Autowired
  protected lateinit var matchRepository: MatchRepository

  val nomisId = "A1234BC"
  val matchedUln = "A"

  private fun checkWebCall(
    nomisId: String,
    expectedResponseStatus: Int,
    expectedStatus: CheckMatchStatus,
    expectedUln: String? = null,
  ) {
    val executedRequest = webTestClient.get()
      .uri("/match/$nomisId")
      .headers(setAuthorisation(roles = listOf("ROLE_LEARNER_RECORDS_SEARCH__RO")))
      .header("X-Username", "TestUser")
      .accept(MediaType.parseMediaType("application/json"))
      .exchange()
      .expectStatus()

    val checkMatchResponse = objectMapper.readValue(
      executedRequest
        .isEqualTo(expectedResponseStatus)
        .expectBody()
        .returnResult()
        .responseBody?.toString(Charsets.UTF_8),
      CheckMatchResponse::class.java,
    )
    assertThat(checkMatchResponse.status).isEqualTo(expectedStatus)
    if (expectedUln != null) {
      assertThat(checkMatchResponse.matchedUln).isEqualTo(expectedUln)
    }
  }

  @AfterEach
  fun cleanup() {
    matchRepository.deleteAll()
  }

  @Test
  fun `should find a match by id`() {
    matchRepository.save(MatchEntity(nomisId, matchedUln))
    checkWebCall(
      nomisId,
      200,
      CheckMatchStatus.Found,
      matchedUln,
    )
  }

  @Test
  fun `should return NOT_FOUND if no match`() {
    checkWebCall(
      nomisId,
      404,
      CheckMatchStatus.NotFound,
    )
  }

  @Test
  fun `should return no match if record marked as such`() {
    matchRepository.save(MatchEntity(nomisId, ""))
    checkWebCall(
      nomisId,
      200,
      CheckMatchStatus.NoMatch,
    )
  }
}
