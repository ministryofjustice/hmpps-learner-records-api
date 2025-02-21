package uk.gov.justice.digital.hmpps.learnerrecordsapi.models.response

import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.db.MatchEntity

data class MatchResponse(
  val message: String,
  val entity: MatchEntity,
)
