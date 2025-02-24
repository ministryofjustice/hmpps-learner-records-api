package uk.gov.justice.digital.hmpps.learnerrecordsapi.models.response

data class CheckMatchResponse(

  val matchedUln: String? = null,
  val givenName: String? = null,
  val familyName: String? = null,
  val status: CheckMatchStatus = CheckMatchStatus.Found,

)

enum class CheckMatchStatus {
  Found,
  NotFound,
  NoMatch,
}
