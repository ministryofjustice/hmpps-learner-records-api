package uk.gov.justice.digital.hmpps.learnerrecordsapi.models.db

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.PrePersist
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "matches")
data class MatchEntity(
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  val id: Long? = null,

  @Column(nullable = false)
  val nomisId: String? = null,

  @Column(nullable = false)
  val matchedUln: String? = null,

  @Column(nullable = false)
  val givenName: String? = null,

  @Column(nullable = false)
  val familyName: String? = null,

  @Column(nullable = false)
  val matchType: String? = null,

  @Column(nullable = false)
  val countofmatchedUlns: String? = null,

  @Column(nullable = true)
  val dateOfBirth: String? = null,

  @Column(nullable = true)
  val gender: String? = null,

  @Column(nullable = false, updatable = false)
  var dateCreated: LocalDateTime? = null,
) {
  @PrePersist
  fun beforeSavingSetTimeCreated() {
    if (dateCreated == null) {
      dateCreated = LocalDateTime.now()
    }
  }
}
