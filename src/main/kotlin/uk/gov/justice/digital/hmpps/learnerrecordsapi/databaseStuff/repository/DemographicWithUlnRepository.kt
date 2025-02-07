package uk.gov.justice.digital.hmpps.learnerrecordsapi.databaseStuff.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.learnerrecordsapi.databaseStuff.entities.DemographicWithUln

// This is the spring data JpaRepository.
// It helps us interface with our database without manually writing queries.
// Things like .findById() are already provided by default.
// We can do .save() to save an entity to it.
// Note that we may in the future have more than one repository, each for a separate entity type.

@Repository
interface DemographicWithUlnRepository : JpaRepository<DemographicWithUln, Long> {

  // Refactor to perhaps use an indexed column containing a hash of the given demographic details.
  // Or, index all the demographic columns.
  fun findFirstByGivenNameAndFamilyNameAndDateOfBirthAndGenderOrderByIdDesc(
    givenName: String?,
    familyName: String,
    dateOfBirth: String,
    gender: String,
  ): DemographicWithUln?

}
