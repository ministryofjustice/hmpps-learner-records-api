package uk.gov.justice.digital.hmpps.learnerrecordsapi.databaseStuff.entities

import jakarta.persistence.*

// This is the entity that we can save and retrieve from our database.
// Note that it has a relatedUln property.

// See the comments on jira for how this will differ when we come to implementing.

@Entity
@Table(name = "DEMOGRAPHIC_ULN")
data class DemographicWithUln(
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

  @Column(nullable = false)
  val relatedUln: String?,

) {
  constructor(
    givenName: String?,
    familyName: String,
    dateOfBirth: String,
    gender: String,
    relatedUln: String? = null,
  ) : this(null, givenName, familyName, dateOfBirth, gender, relatedUln)
}
