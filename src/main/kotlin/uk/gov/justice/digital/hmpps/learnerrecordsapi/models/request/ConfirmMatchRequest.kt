package uk.gov.justice.digital.hmpps.learnerrecordsapi.models.request

import jakarta.validation.constraints.Pattern
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.MatchStatus
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.db.MatchEntity

open class ConfirmNoMatchRequest(
  open val matchType: MatchType,

  open val countOfReturnedUlns: String,

) {

  open fun asMatchEntity(nomisId: String): MatchEntity = MatchEntity(
    nomisId = nomisId,
    matchType = matchType.toString(),
    countOfReturnedUlns = countOfReturnedUlns,
    matchStatus = MatchStatus.MATCH_NOT_POSSIBLE.toString(),
  )
}

class ConfirmMatchRequest(

  @field:Pattern(regexp = "^[0-9]{1,10}\$")
  val matchingUln: String,

  @field:Pattern(regexp = "^[A-Za-z' ,.-]{3,35}$")
  val givenName: String,

  @field:Pattern(regexp = "^[A-Za-z' ,.-]{3,35}$")
  val familyName: String,

  override val matchType: MatchType,

  override val countOfReturnedUlns: String,

) : ConfirmNoMatchRequest(matchType, countOfReturnedUlns) {

  override fun asMatchEntity(nomisId: String): MatchEntity = super.asMatchEntity(nomisId).copy(
    matchedUln = matchingUln,
    givenName = givenName,
    familyName = familyName,
    matchStatus = MatchStatus.MATCHED.toString(),
  )
}
