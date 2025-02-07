package uk.gov.justice.digital.hmpps.learnerrecordsapi.databaseStuff

import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.learnerrecordsapi.databaseStuff.entities.DemographicWithUln
import uk.gov.justice.digital.hmpps.learnerrecordsapi.databaseStuff.repository.DemographicWithUlnRepository
import uk.gov.justice.digital.hmpps.learnerrecordsapi.databaseStuff.service.DatabaseService

// This tests the service's methods, and injects the repositories so that only a single service method is tested at once.

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class DatabaseServiceTest {

  @PersistenceContext
  lateinit var entityManager: EntityManager

  @Autowired
  lateinit var databaseService: DatabaseService

  @Autowired
  lateinit var demographicWithUlnRepository: DemographicWithUlnRepository

  @AfterEach
  fun clearDatabase() {
    // Replace this with a more standard way of clearing down H2 database.
    entityManager.createNativeQuery("DELETE FROM DEMOGRAPHIC_ULN").executeUpdate()
  }

  @Test
  fun `can save a DemographicUlnLinkEntity`() {
    val demographicWithUln = DemographicWithUln("test", "test", "test", "test", "123456789")
    val savedDemographicWithUln = databaseService.saveDemographicWithUln(demographicWithUln)
    assertEquals(demographicWithUln, savedDemographicWithUln)
  }

  @Test
  fun `can retrieve a DemographicUlnLinkEntity without providing its uln property`() {
    val demographicWithoutUln = DemographicWithUln("test", "test", "test", "test")
    val expectedDemographicWithUln = demographicWithoutUln.copy(relatedUln = "0987654321")
    demographicWithUlnRepository.save(expectedDemographicWithUln)
    val foundDemographicWithUln = databaseService.findDemographicWithUln(demographicWithoutUln)
    assertEquals(expectedDemographicWithUln, foundDemographicWithUln)
  }

  @Test
  fun `only the latest DemographicUlnLinkEntity is retrieved`() {
    val demographicWithoutUln = DemographicWithUln("test", "test", "test", "test")

    for (i in 0..9) {
      val demographicWithUln = demographicWithoutUln.copy(relatedUln = "112233445$i")
      demographicWithUlnRepository.save(demographicWithUln)
    }

    val expectedDemographicWithUln = demographicWithoutUln.copy(relatedUln = "0099887766")
    demographicWithUlnRepository.save(expectedDemographicWithUln)
    val foundDemographicWithUln = databaseService.findDemographicWithUln(demographicWithoutUln)
    assertEquals(expectedDemographicWithUln, foundDemographicWithUln)
  }
}
