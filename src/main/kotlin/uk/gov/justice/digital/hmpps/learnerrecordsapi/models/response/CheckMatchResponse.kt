package uk.gov.justice.digital.hmpps.learnerrecordsapi.models.response

data class CheckMatchResponse(

  val matchedUln: String? = null,
  val givenName: String? = null,
  val familyName: String? = null,
  val status: CheckMatchStatus = CheckMatchStatus.Found,
) {
  fun setStatus(): CheckMatchResponse = this.copy(
    status = if (this.matchedUln.isNullOrEmpty() || this.matchedUln.isBlank()) {
      CheckMatchStatus.NoMatch
    } else {
      CheckMatchStatus.Found
    },
  )
}

enum class CheckMatchStatus {
  Found,
  NotFound,
  NoMatch,
}
