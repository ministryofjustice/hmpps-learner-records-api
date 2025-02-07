package uk.gov.justice.digital.hmpps.learnerrecordsapi.databaseStuff.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.learnerrecordsapi.databaseStuff.entities.DemographicWithUln
import uk.gov.justice.digital.hmpps.learnerrecordsapi.databaseStuff.repository.DemographicWithUlnRepository

// Our database service will be the co-ordinating point for everything.
// It's nicer than calling the repository directly because we might need to refactor with extra logic.
// Also nicer to unit test. See test/databaseStuff for an example test of this service.

@Service
class DatabaseService(
  private val demographicWithUlnRepository: DemographicWithUlnRepository,
) {

  fun findDemographicWithUln(demographicWithUln: DemographicWithUln): DemographicWithUln? = demographicWithUlnRepository.findFirstByGivenNameAndFamilyNameAndDateOfBirthAndGenderOrderByIdDesc(
    demographicWithUln.givenName,
    demographicWithUln.familyName,
    demographicWithUln.dateOfBirth,
    demographicWithUln.gender,
  )

  fun saveDemographicWithUln(demographicWithUln: DemographicWithUln): DemographicWithUln = demographicWithUlnRepository.save(demographicWithUln)
}
