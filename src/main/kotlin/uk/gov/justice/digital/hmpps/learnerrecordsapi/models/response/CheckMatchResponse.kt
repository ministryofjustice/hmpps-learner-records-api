package uk.gov.justice.digital.hmpps.learnerrecordsapi.models.response

import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.request.LearnerEventsRequest

data class CheckMatchResponse(

  val matchedUln: String? = null,
  val givenName: String? = null,
  val familyName: String? = null,
  val status: CheckMatchStatus = CheckMatchStatus.Found,
) {
  fun asLearnerEventsRequest(): LearnerEventsRequest = LearnerEventsRequest(
    givenName.orEmpty(),
    familyName.orEmpty(),
    matchedUln.orEmpty(),
  )
}

enum class CheckMatchStatus {
  Found,
  NotFound,
  NoMatch,
}
