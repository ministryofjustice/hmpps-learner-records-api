package uk.gov.justice.digital.hmpps.learnerrecordsapi.models.db

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "matches")
data class MatchEntity(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  val id: Long? = null,

  @Column(nullable = false)
  val nomisId: String,

  @Column(nullable = false)
  val matchedUln: String? = null,

) {
  constructor(
    nomisId: String,
    matchedUln: String?,
  ) : this(null, nomisId, matchedUln)

  constructor() : this("", "")
}
