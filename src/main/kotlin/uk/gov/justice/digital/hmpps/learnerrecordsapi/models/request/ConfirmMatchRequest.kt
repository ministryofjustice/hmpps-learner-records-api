package uk.gov.justice.digital.hmpps.learnerrecordsapi.models.request

import com.google.gson.annotations.SerializedName
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.db.MatchEntity

data class ConfirmMatchRequest(

  @SerializedName("nomisId")
  val nomisId: String,

  @SerializedName("uln")
  val matchingUln: String,
) {
  fun asMatchEntity() = MatchEntity(nomisId, matchingUln)
}
