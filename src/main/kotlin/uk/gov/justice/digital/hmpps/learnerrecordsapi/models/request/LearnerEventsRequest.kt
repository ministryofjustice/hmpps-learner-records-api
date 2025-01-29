package uk.gov.justice.digital.hmpps.learnerrecordsapi.models.request

import com.google.gson.annotations.SerializedName
import jakarta.validation.constraints.Past
import jakarta.validation.constraints.Pattern
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.lrsapi.request.LearnerEventsLRSRequest
import java.time.LocalDate

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

  @field:Past
  @SerializedName("dateOfBirth")
  val dateOfBirth: LocalDate?,

  @SerializedName("gender")
  val gender: Gender,
) {
  fun extractFromRequest(): LearnerEventsLRSRequest = LearnerEventsLRSRequest(
    givenName = givenName,
    familyName = familyName,
    uln = uln,
    dateOfBirth = dateOfBirth,
    gender = gender.value,
  )
}
