package uk.gov.justice.digital.hmpps.learnerrecordsapi.databaseStuff

import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceContext
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional
import uk.gov.justice.digital.hmpps.learnerrecordsapi.databaseStuff.entities.DemographicDetails
import uk.gov.justice.digital.hmpps.learnerrecordsapi.databaseStuff.entities.UniqueLearnerNumber
import uk.gov.justice.digital.hmpps.learnerrecordsapi.databaseStuff.repository.DemographicRepository
import uk.gov.justice.digital.hmpps.learnerrecordsapi.databaseStuff.repository.UniqueLearnerNumberRepository
import uk.gov.justice.digital.hmpps.learnerrecordsapi.databaseStuff.service.DatabaseService

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class DatabaseServiceTest {

  @PersistenceContext
  lateinit var entityManager: EntityManager

  @Autowired
  lateinit var databaseService: DatabaseService

  @Autowired
  lateinit var demographicRepository: DemographicRepository

  @Autowired
  lateinit var ulnRepository: UniqueLearnerNumberRepository

  @AfterEach
  fun clearDatabase() {
    entityManager.createNativeQuery("DELETE FROM DEMOGRAPHIC_DETAILS").executeUpdate()
    entityManager.createNativeQuery("DELETE FROM UNIQUE_LEARNER_NUMBER").executeUpdate()
  }

  @Test
  fun `demographic details and unique learner number are saved and linked together`() {

    val demographicUserWantsLinked = DemographicDetails("test", "test", "test","test")
    val ulnUserWantsLinked = UniqueLearnerNumber("1234567890")

    val savedDemographicWithUlnLink = databaseService.saveDemographicDetailsUlnRelationship(demographicUserWantsLinked, ulnUserWantsLinked)
    val ulnInDatabase = savedDemographicWithUlnLink.id?.let { ulnRepository.getReferenceById(it) }

    assertNotNull(savedDemographicWithUlnLink)
    assertNotNull(ulnInDatabase)
    assertEquals(savedDemographicWithUlnLink.relatedUln, ulnInDatabase)
  }

  @Test
  fun `unique learner numbers are possible to retrieve via demographics`() {
    val ulnInDatabase = ulnRepository.save(UniqueLearnerNumber( "1234567890"))
    val demographicUserWillSearchWith = DemographicDetails("test", "test", "test","test")

    demographicRepository.save(demographicUserWillSearchWith.copy(relatedUln = ulnInDatabase))

    val retrievedUln = databaseService.getUlnFromDemographicDetails(demographicUserWillSearchWith)?.uln

    assertNotNull(retrievedUln)
    assertEquals(retrievedUln, "1234567890")
  }

  @Test
  fun `only the latest uln linked to some demographic details is retrieved`() {
    val demographicUserWillSearchWith = DemographicDetails("test", "test", "test","test")

    val olderUlnInDatabase = ulnRepository.save(UniqueLearnerNumber( "1234567890"))
    demographicRepository.save(demographicUserWillSearchWith.copy(relatedUln = olderUlnInDatabase))

    val newerUlnInDatabase = ulnRepository.save(UniqueLearnerNumber( "0987654321"))
    demographicRepository.save(demographicUserWillSearchWith.copy(relatedUln = newerUlnInDatabase))

    val retrievedUln = databaseService.getUlnFromDemographicDetails(demographicUserWillSearchWith)?.uln

    assertNotNull(retrievedUln)
    assertEquals(retrievedUln, "0987654321")
  }

}
