package uk.gov.justice.digital.hmpps.learnerrecordsapi.models.request

import jakarta.validation.constraints.Pattern
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.db.MatchEntity

class ConfirmMatchRequest(

  @field:Pattern(regexp = "^[0-9]{1,10}\$")
  val matchingUln: String? = null,

  @field:Pattern(regexp = "^[A-Za-z' ,.-]{3,35}$")
  val givenName: String? = null,

  @field:Pattern(regexp = "^[A-Za-z' ,.-]{3,35}$")
  val familyName: String? = null,

  val matchType: MatchType,

  val countOfReturnedUlns: String,

) {
  fun asMatchEntity(nomisId: String): MatchEntity = MatchEntity(
    null,
    nomisId,
    matchingUln.orEmpty(),
    givenName.orEmpty(),
    familyName.orEmpty(),
    matchType.toString(),
    countOfReturnedUlns,
  )
}
