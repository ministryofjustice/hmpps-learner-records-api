package uk.gov.justice.digital.hmpps.learnerrecordsapi.models.request

import com.google.gson.annotations.SerializedName
import jakarta.validation.constraints.Pattern
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.lrsapi.request.LearnersLRSRequest
import java.time.LocalDate

// Example model for the request
// Checking number of characters
// Error handling for bad requests need to be added - add specific message and giving a 400 instead of a 500
// annotations support message = ''
data class LearnersRequest(
  // Mandatory
  @field:Pattern(regexp = "^[A-Za-z' ,.-]{3,35}$")
  @SerializedName("givenName")
  val givenName: String,

  @field:Pattern(regexp = "^[A-Za-z' ,.-]{3,35}$")
  @SerializedName("familyName")
  val familyName: String,

  @SerializedName("dateOfBirth")
  val dateOfBirth: LocalDate,

  val gender: Gender,

  @field:Pattern(regexp = "^[A-Z]{1,2}[0-9R][0-9A-Z]? ?[0-9][ABDEFGHJLNPQRSTUWXYZ]{2}|BFPO ?[0-9]{1,4}|([AC-FHKNPRTV-Y]\\d{2}|D6W)? ?[0-9AC-FHKNPRTV-Y]{4}\$")
  @SerializedName("lastKnownPostcode")
  val lastKnownPostCode: String,
) {
  fun extractFromRequest(): LearnersLRSRequest = LearnersLRSRequest(
    givenName = givenName,
    familyName = familyName,
    dateOfBirth = dateOfBirth,
    gender = gender.value,
    lastKnownPostCode = lastKnownPostCode,
  )
}

enum class Gender(val value: Int) {
  MALE(1),
  FEMALE(2),
  NOT_KNOWN(0),
  NOT_SPECIFIED(9),
}
