package uk.gov.justice.digital.hmpps.learnerrecordsapi.databaseStuff.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.learnerrecordsapi.databaseStuff.entities.DemographicWithUln

// This is the spring data JpaRepository.
// It helps us interface with our database without manually writing queries.
// Things like .findById() are already provided by default.
// We can do .save() to save an entity to it.

@Repository
interface DemographicWithUlnRepository : JpaRepository<DemographicWithUln, Long> {

  // refactor to perhaps use an indexed column with the details in a single string or hashed or something.
  // eg findByHashOrderByIdDesc
  // method name is long because of DSL, but it specifies how to find the most recent one.
  fun findFirstByGivenNameAndFamilyNameAndDateOfBirthAndGenderOrderByIdDesc(
    givenName: String?,
    familyName: String,
    dateOfBirth: String,
    gender: String,
  ): DemographicWithUln?

}
