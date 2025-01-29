package uk.gov.justice.digital.hmpps.learnerrecordsapi.models.request

import com.google.gson.annotations.SerializedName
import jakarta.validation.constraints.Past
import jakarta.validation.constraints.Size
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.lrsapi.request.LearnerEventsLRSRequest
import java.time.LocalDate

class LearnerEventsRequest(
  @field:Size(max = 35)
  @SerializedName("givenName")
  val givenName: String,

  @field:Size(max = 35)
  @SerializedName("familyName")
  val familyName: String,

  @field:Size(max = 10)
  @SerializedName("uln")
  val uln: String,

  @field:Past
  @SerializedName("dateOfBirth")
  val dateOfBirth: LocalDate?,

  @SerializedName("gender")
  val gender: Int?,
) {
  fun extractFromRequest(): LearnerEventsLRSRequest = LearnerEventsLRSRequest(
    givenName = givenName,
    familyName = familyName,
    uln = uln,
    dateOfBirth = dateOfBirth,
    gender = gender,
  )
}
