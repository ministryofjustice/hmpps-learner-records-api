package uk.gov.justice.digital.hmpps.learnerrecordsapi.integration

import com.fasterxml.jackson.databind.ObjectMapper
import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.db.MatchEntity
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.response.CheckMatchResponse
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.response.CheckMatchStatus
import uk.gov.justice.digital.hmpps.learnerrecordsapi.repository.MatchRepository

@Transactional
class MatchResourceIntTest : IntegrationTestBase() {

  @PersistenceContext
  lateinit var entityManager: EntityManager

  @Autowired
  protected lateinit var objectMapper: ObjectMapper

  @Autowired
  lateinit var matchRepository: MatchRepository

  val found = "A1234BC"
  val noMatch = "X1234YZ"
  val matchedUln = "A"

  fun setUpDatabase(nomisId: String, matchedUln: String) {
    entityManager.createNativeQuery("INSERT INTO matches (nomis_id, matched_uln) VALUES ('$nomisId', '$matchedUln')").executeUpdate()
  }

  private fun checkWebCall(
    nomisId: String,
    expectedResponseStatus: Int,
    expectedStatus: CheckMatchStatus,
    expectedUln: String? = null,
  ) {
    val executedRequest = webTestClient.get()
      .uri("/match/check?nomisId=$nomisId")
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

  @Test
  fun `should find a match by id`() {
    setUpDatabase(found, matchedUln)
    val entity = matchRepository.findFirstByNomisIdOrderByIdDesc(found)
    assertThat(entity).isNotNull
    checkWebCall(
      found,
      200,
      CheckMatchStatus.Found,
      matchedUln,
    )
  }
}
