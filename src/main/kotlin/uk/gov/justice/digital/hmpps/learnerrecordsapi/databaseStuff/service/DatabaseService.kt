package uk.gov.justice.digital.hmpps.learnerrecordsapi.databaseStuff.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.learnerrecordsapi.databaseStuff.entities.DemographicDetails
import uk.gov.justice.digital.hmpps.learnerrecordsapi.databaseStuff.entities.UniqueLearnerNumber
import uk.gov.justice.digital.hmpps.learnerrecordsapi.databaseStuff.repository.DemographicRepository
import uk.gov.justice.digital.hmpps.learnerrecordsapi.databaseStuff.repository.UniqueLearnerNumberRepository

// Our database service will be the co-ordinating point for everything.
// It's nicer than calling the repository directly because we might need to refactor with extra logic.

@Service
class DatabaseService(
  private val demographicRepository: DemographicRepository,
  private val uniqueLearnerNumberRepository: UniqueLearnerNumberRepository,
) {

  fun getUlnFromDemographicDetails(demographicDetails: DemographicDetails): UniqueLearnerNumber? {
    val foundDemographicDetails = demographicRepository.findFirstByGivenNameAndFamilyNameAndDateOfBirthAndGenderOrderByIdDesc(
      demographicDetails.givenName,
      demographicDetails.familyName,
      demographicDetails.dateOfBirth,
      demographicDetails.gender
    )
    if (foundDemographicDetails != null && foundDemographicDetails.relatedUln?.id != null) {
      return uniqueLearnerNumberRepository.getReferenceById(foundDemographicDetails.relatedUln.id)
    } else
      return null
  }

  fun saveDemographicDetailsUlnRelationship(demographicDetails: DemographicDetails, uniqueLearnerNumber: UniqueLearnerNumber): DemographicDetails {
    val savedUlnWithId = saveUniqueLearnerNumber(uniqueLearnerNumber)
    val demographicDetailsWithRelatedUln = demographicDetails.copy(relatedUln = savedUlnWithId)
    return saveDemographicDetails(demographicDetailsWithRelatedUln)
  }

  private fun saveUniqueLearnerNumber(uniqueLearnerNumber: UniqueLearnerNumber): UniqueLearnerNumber {
    return uniqueLearnerNumberRepository.save(uniqueLearnerNumber)
  }

  private fun saveDemographicDetails(demographicDetails: DemographicDetails): DemographicDetails {
    return demographicRepository.save(demographicDetails)
  }
}
