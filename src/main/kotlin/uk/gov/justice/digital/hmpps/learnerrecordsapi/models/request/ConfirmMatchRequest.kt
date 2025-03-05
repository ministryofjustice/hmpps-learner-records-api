package uk.gov.justice.digital.hmpps.learnerrecordsapi.models.request

import jakarta.validation.constraints.Pattern
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.db.MatchEntity

class ConfirmMatchRequest(

  @field:Pattern(regexp = "^[0-9]{1,10}\$")
  val matchingUln: String,

  @field:Pattern(regexp = "^[A-Za-z' ,.-]{3,35}$")
  val givenName: String,

  @field:Pattern(regexp = "^[A-Za-z' ,.-]{3,35}$")
  val familyName: String,

  @field:Pattern(regexp = "^[0-9]{4}-[0-9]{2}-[0-9]{2}$")
  val dateOfBirth: String = "",

  @field:Pattern(regexp = "^(MALE|FEMALE|NOT_KNOWN|NOT_SPECIFIED)\$")
  val gender: String = "",

) {
  fun asMatchEntity(nomisId: String): MatchEntity = MatchEntity(
    null,
    nomisId,
    matchingUln,
    givenName,
    familyName,
    dateOfBirth,
    gender,
  )
}
