package uk.gov.justice.digital.hmpps.learnerrecordsapi.models.request

import com.google.gson.annotations.SerializedName
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.Pattern
import uk.gov.justice.digital.hmpps.bold.lrs.models.lrsapi.request.FindLearnerByDemographicsLRSRequest
import java.time.LocalDate

// Example model for the request
// Checking number of characters
// Error handling for bad requests need to be added - add specific message and giving a 400 instead of a 500
// annotations support message = ''
data class FindLearnerByDemographicsRequest(
  // Mandatory
  @field:Pattern(regexp = "^[A-Za-z]{3,35}\$")
  @SerializedName("givenName")
  val givenName: String,

  @field:Pattern(regexp = "^[A-Za-z]{3,35}\$")
  @SerializedName("familyName")
  val familyName: String,

  @SerializedName("dateOfBirth")
  val dateOfBirth: LocalDate,

  // TODO: Validate gender
  @SerializedName("gender")
  @field:Min(0)
  @field:Max(2)
  val gender: Int,

  @field:Pattern(regexp = "^[A-Z]{1,2}[0-9R][0-9A-Z]? ?[0-9][ABDEFGHJLNPQRSTUWXYZ]{2}|BFPO ?[0-9]{1,4}|([AC-FHKNPRTV-Y]\\d{2}|D6W)? ?[0-9AC-FHKNPRTV-Y]{4}\$")
  @SerializedName("lastKnownPostcode")
  val lastKnownPostCode: String,
) {
  fun extractFromRequest(): FindLearnerByDemographicsLRSRequest {
    return FindLearnerByDemographicsLRSRequest(
      givenName = givenName,
      familyName = familyName,
      dateOfBirth = dateOfBirth,
      gender = gender,
      lastKnownPostCode = lastKnownPostCode,
    )
  }
}
