package uk.gov.justice.digital.hmpps.learnerrecordsapi.models.request

import com.google.gson.annotations.SerializedName
import jakarta.validation.constraints.Pattern
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.lrsapi.request.LearnerEventsLRSRequest
import uk.gov.justice.digital.hmpps.learnerrecordsapi.utils.toLocalDate

class LearnerEventsRequest(
  @field:Pattern(regexp = "^[A-Za-z' ,.-]{3,35}$")
  @SerializedName("givenName")
  val givenName: String,

  @field:Pattern(regexp = "^[A-Za-z' ,.-]{3,35}$")
  @SerializedName("familyName")
  val familyName: String,

  @field:Pattern(regexp = "^[0-9]{1,10}\$")
  @SerializedName("uln")
  val uln: String,

  @field:Pattern(regexp = "^[0-9]{4}-[0-9]{2}-[0-9]{2}$")
  @SerializedName("dateOfBirth")
  val dateOfBirth: String? = null,

  @SerializedName("gender")
  val gender: Gender? = null,
) {
  fun extractFromRequest(): LearnerEventsLRSRequest = LearnerEventsLRSRequest(
    givenName = givenName,
    familyName = familyName,
    uln = uln,
    dateOfBirth = dateOfBirth?.toLocalDate(),
    gender = gender?.code,
  )
}
