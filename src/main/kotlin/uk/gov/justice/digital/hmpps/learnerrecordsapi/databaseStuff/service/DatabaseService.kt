package uk.gov.justice.digital.hmpps.learnerrecordsapi.databaseStuff.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.learnerrecordsapi.databaseStuff.entities.DemographicWithUln
import uk.gov.justice.digital.hmpps.learnerrecordsapi.databaseStuff.repository.DemographicWithUlnRepository

// Our database service will be the co-ordinating point for everything.
// It's nicer than calling the repository directly because we might need to refactor with extra logic.

@Service
class DatabaseService(
  private val demographicWithUlnRepository: DemographicWithUlnRepository,
) {

  fun findDemographicWithUln(demographicWithUln: DemographicWithUln): DemographicWithUln? {
    return demographicWithUlnRepository.findFirstByGivenNameAndFamilyNameAndDateOfBirthAndGenderOrderByIdDesc(
      demographicWithUln.givenName,
      demographicWithUln.familyName,
      demographicWithUln.dateOfBirth,
      demographicWithUln.gender
    )
  }

  fun saveDemographicWithUln(demographicWithUln: DemographicWithUln): DemographicWithUln {
    return demographicWithUlnRepository.save(demographicWithUln)
  }

}
