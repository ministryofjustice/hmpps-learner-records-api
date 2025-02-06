package uk.gov.justice.digital.hmpps.learnerrecordsapi.databaseStuff.entities

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "UNIQUE_LEARNER_NUMBER")
data class UniqueLearnerNumber(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  val id: Long? = null,

  @Column(nullable = false) // Not unique because we might attempt to create it twice.
  val uln: String,

) {
  constructor(uln: String) : this(null, uln)
}