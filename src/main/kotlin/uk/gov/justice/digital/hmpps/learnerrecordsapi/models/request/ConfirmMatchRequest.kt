package uk.gov.justice.digital.hmpps.learnerrecordsapi.models.request

import jakarta.validation.constraints.Pattern
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.db.MatchEntity

class ConfirmMatchRequest(
  nomisId: String,
  @field:Pattern(regexp = "^[0-9]{1,10}\$")
  val matchingUln: String,
) : CheckMatchRequest(nomisId) {
  fun asMatchEntity() = MatchEntity(nomisId, matchingUln)
}
