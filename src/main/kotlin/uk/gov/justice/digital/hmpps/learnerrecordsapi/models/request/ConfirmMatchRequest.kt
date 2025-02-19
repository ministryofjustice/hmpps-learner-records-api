package uk.gov.justice.digital.hmpps.learnerrecordsapi.models.request

import com.google.gson.annotations.SerializedName
import jakarta.validation.constraints.Pattern
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.db.MatchEntity

data class ConfirmMatchRequest(

  @field:Pattern(regexp = "^[A-Z]\\d{4}[A-Z]{2}\$")
  @SerializedName("nomisId")
  val nomisId: String,

  @field:Pattern(regexp = "^[0-9]{1,10}\$")
  @SerializedName("uln")
  val matchingUln: String,
) {
  fun asMatchEntity() = MatchEntity(nomisId, matchingUln)
}
