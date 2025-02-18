package uk.gov.justice.digital.hmpps.learnerrecordsapi.models.request

import com.google.gson.annotations.SerializedName
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.db.MatchEntity

data class CreateMatchRequest(

  @SerializedName("nomisId")
  val nomisId: String,

  @SerializedName("uln")
  val uln: String,
) {
  fun asMatchEntity() = MatchEntity(nomisId, uln)
}
