package uk.gov.justice.digital.hmpps.learnerrecordsapi.integration

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.db.MatchEntity
import uk.gov.justice.digital.hmpps.learnerrecordsapi.repository.MatchRepository

class MatchRepositoryIntTest : IntegrationTestBase() {
  @Autowired
  lateinit var matchRepository: MatchRepository

  @Test
  fun `existsMatchWithDifferentId should return true when different nomisid has uln`() {
    val uln = "1234567890"
    matchRepository.save(
      MatchEntity(nomisId = "B1234CD", matchedUln = uln),
    )

    assertThat(matchRepository.existsMatchWithDifferentId("A1417AE", uln)).isTrue()
  }

  @Test
  fun `existsMatchWithDifferentId should return false when no other nomisid has uln`() {
    assertThat(matchRepository.existsMatchWithDifferentId("A1417AE", "1234567890")).isFalse()
  }

  @AfterEach
  fun cleanup() {
    matchRepository.deleteAll()
  }
}
