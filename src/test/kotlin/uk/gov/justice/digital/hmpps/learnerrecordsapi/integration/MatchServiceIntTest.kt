package uk.gov.justice.digital.hmpps.learnerrecordsapi.integration

import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.db.MatchEntity
import uk.gov.justice.digital.hmpps.learnerrecordsapi.repository.MatchRepository
import uk.gov.justice.digital.hmpps.learnerrecordsapi.service.MatchService

@Transactional
class MatchServiceIntTest : IntegrationTestBase() {

  @PersistenceContext
  lateinit var entityManager: EntityManager

  @Autowired
  lateinit var matchService: MatchService

  @Autowired
  lateinit var matchRepository: MatchRepository

  @AfterEach
  fun clearDatabase() {
    entityManager.createNativeQuery("DELETE FROM matches").executeUpdate()
  }

  val matchWithUln = MatchEntity(
    nomisId = "ABCDEFGH",
    matchedUln = "123456789",
    givenName = "John",
    familyName = "Smith",
    dateOfBirth = "1990-01-01",
    gender = "MALE",
  )

  @Test
  fun `can save a MatchEntity with a uln`() {
    val savedMatchWithUln = matchService.saveMatch(matchWithUln)
    assertEquals(matchWithUln, savedMatchWithUln)
  }

  @Test
  fun `can retrieve a MatchEntity without providing its uln property`() {
    val matchWithoutUln = matchWithUln.copy(matchedUln = null)
    val expectedMatchWithUln = matchWithoutUln.copy(matchedUln = "0987654321")
    matchRepository.save(expectedMatchWithUln)
    val foundMatchWithUln = matchService.findMatch(matchWithoutUln)
    assertEquals(expectedMatchWithUln, foundMatchWithUln)
  }

  @Test
  fun `only the latest MatchEntity is retrieved`() {
    val matchWithoutUln = matchWithUln.copy(matchedUln = null)

    for (i in 0..9) {
      val matchToSaveWithUln = matchWithoutUln.copy(matchedUln = "112233445$i")
      matchRepository.save(matchToSaveWithUln)
    }

    val expectedMatchWithUln = matchWithoutUln.copy(matchedUln = "0099887766")
    matchRepository.save(expectedMatchWithUln)
    val foundMatchWithUln = matchService.findMatch(matchWithoutUln)
    assertEquals(expectedMatchWithUln, foundMatchWithUln)
  }
}
