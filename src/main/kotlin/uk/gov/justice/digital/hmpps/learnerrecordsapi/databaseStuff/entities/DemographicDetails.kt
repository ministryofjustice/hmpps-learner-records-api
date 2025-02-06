package uk.gov.justice.digital.hmpps.learnerrecordsapi.databaseStuff.entities

import jakarta.persistence.*

// This is the entity that we can save and retrieve from our database.
// We will need to confirm exactly what to save.

@Entity
@Table(name = "DEMOGRAPHIC_DETAILS")
data class DemographicDetails(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  val id: Long? = null,

  @Column(nullable = false)
  val givenName: String?,

  @Column(nullable = false)
  val familyName: String,

  @Column(nullable = false)
  val dateOfBirth: String,

  @Column(nullable = false)
  val gender: String,

  @ManyToOne
  @JoinColumn(name = "related_uln_id", nullable = false)
  val relatedUln: UniqueLearnerNumber?

) {
  // Default needed by JPA
  constructor() : this(null, "", "", "", "", null)

  constructor(
    givenName: String?,
    familyName: String,
    dateOfBirth: String,
    gender: String,
    relatedUln: UniqueLearnerNumber? = null
  ) : this(null, givenName, familyName, dateOfBirth, gender, relatedUln)
}
