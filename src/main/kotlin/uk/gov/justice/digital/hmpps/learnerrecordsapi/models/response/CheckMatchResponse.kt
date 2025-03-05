package uk.gov.justice.digital.hmpps.learnerrecordsapi.models.response

import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.request.Gender
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.request.LearnerEventsRequest

data class CheckMatchResponse(

  val matchedUln: String? = null,
  val givenName: String? = null,
  val familyName: String? = null,
  val dateOfBirth: String? = null,
  val gender: String? = null,
  val status: CheckMatchStatus = CheckMatchStatus.Found,
) {
  fun setStatus(): CheckMatchResponse = this.copy(
    status = if (this.matchedUln.isNullOrEmpty() || this.matchedUln.isBlank()) {
      CheckMatchStatus.NoMatch
    } else {
      CheckMatchStatus.Found
    },
  )

  fun toLearnerEventsRequest(): LearnerEventsRequest = LearnerEventsRequest(
    givenName.orEmpty(),
    familyName.orEmpty(),
    matchedUln.orEmpty(),
    dateOfBirth,
    Gender.valueOf(gender.orEmpty().uppercase()),
  )
}

enum class CheckMatchStatus {
  Found,
  NotFound,
  NoMatch,
}
