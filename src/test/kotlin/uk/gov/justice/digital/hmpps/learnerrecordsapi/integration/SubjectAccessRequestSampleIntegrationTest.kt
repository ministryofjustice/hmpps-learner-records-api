package uk.gov.justice.digital.hmpps.learnerrecordsapi.integration

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.db.MatchEntity
import uk.gov.justice.digital.hmpps.learnerrecordsapi.repository.MatchRepository
import java.time.LocalDate

class SubjectAccessRequestIntegrationTest : IntegrationTestBase() {
  @Nested
  @DisplayName("/subject-access-request")
  inner class SubjectAccessRequestEndpoint {

    @Nested
    inner class Security {
      @Test
      fun `access forbidden when no authority`() {
        webTestClient.get().uri("/subject-access-request?prn=A12345")
          .exchange()
          .expectStatus().isUnauthorized
      }

      @Test
      fun `access forbidden when no role`() {
        webTestClient.get().uri("/subject-access-request?prn=A12345")
          .headers(setAuthorisation(roles = listOf()))
          .exchange()
          .expectStatus().isForbidden
      }

      @Test
      fun `access forbidden with wrong role`() {
        webTestClient.get().uri("/subject-access-request?prn=A12345")
          .headers(setAuthorisation(roles = listOf("ROLE_BANANAS")))
          .exchange()
          .expectStatus().isForbidden
      }
    }

    @Nested
    inner class HappyPath {

      @Autowired
      lateinit var matchRepository: MatchRepository

      @BeforeEach
      fun clearRepository() {
        matchRepository.deleteAll()
      }

      @Test
      fun `should return all data if prisoner exists and no timeframe is specified`() {
        val entitiesToSave = listOf(
          MatchEntity(null, "A12345", "765", "Jon", "Test"),
          MatchEntity(null, "A12345", "3213", "John", "Test"),
          MatchEntity(null, "B54321", "3213", "Someone", "Else"),
        )

        matchRepository.saveAll(entitiesToSave)

        webTestClient.get().uri("/subject-access-request?prn=A12345")
          .headers(setAuthorisation(roles = listOf("ROLE_SAR_DATA_ACCESS")))
          .exchange()
          .expectStatus().isOk
          .expectBody()
          .jsonPath("$.content").isArray
          .jsonPath("$.content[0].nomisId").isEqualTo("A12345")
          .jsonPath("$.content[1].nomisId").isEqualTo("A12345")
          .jsonPath("$.content[2].nomisId").doesNotExist()
      }

      @Test
      fun `should return data if prisoner exists and a timeframe is specified`() {
        val today = LocalDate.now()
        val fromDate = today.minusDays(10)
        val toDate = today.plusDays(20)

        val validExact = today.atStartOfDay()
        val invalidBefore = fromDate.minusDays(1).atStartOfDay()
        val invalidAfter = toDate.plusDays(1).atStartOfDay()

        val entitiesToSave = listOf(
          MatchEntity(null, "A12345", "765", "Jon", "Test", dateCreated = invalidBefore),
          MatchEntity(null, "A12345", "3213", "Jonathan", "Test", dateCreated = validExact),
          MatchEntity(null, "A12345", "3213", "John", "Test", dateCreated = invalidAfter),
          MatchEntity(null, "B54321", "3213", "Someone", "Else", dateCreated = validExact),
        )

        matchRepository.saveAll(entitiesToSave)

        webTestClient.get().uri("/subject-access-request?prn=A12345&fromDate=$fromDate&toDate=$toDate")
          .headers(setAuthorisation(roles = listOf("ROLE_SAR_DATA_ACCESS")))
          .exchange()
          .expectStatus().isOk
          .expectBody()
          .jsonPath("$.content").isArray
          .jsonPath("$.content[0].nomisId").isEqualTo("A12345")
          .jsonPath("$.content[1].nomisId").doesNotExist()
      }
    }
  }
}

// Try using the old code, with the param fixed