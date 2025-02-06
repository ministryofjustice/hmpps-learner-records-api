package uk.gov.justice.digital.hmpps.learnerrecordsapi.databaseStuff.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.learnerrecordsapi.databaseStuff.entities.UniqueLearnerNumber

// This is the spring data JpaRepository.
// It helps us interface with our database without manually writing queries.
// Things like .findById() are already provided by default.
// We can do .save() to save an entity to it.

@Repository
interface UniqueLearnerNumberRepository : JpaRepository<UniqueLearnerNumber, Long>
