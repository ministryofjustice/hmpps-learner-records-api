package uk.gov.justice.digital.hmpps.learnerrecordsapi.models.request

import com.google.gson.annotations.SerializedName
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.lrsapi.request.LearnersLRSRequest
import java.time.LocalDate

data class LearnersRequest(
  @field:Pattern(regexp = "^[A-Za-z' ,.-]{3,35}$")
  @SerializedName("givenName")
  val givenName: String,

  @field:Pattern(regexp = "^[A-Za-z' ,.-]{3,35}$")
  @SerializedName("familyName")
  val familyName: String,

  @field:Pattern(regexp = "^[0-9]{4}-[0-9]{2}-[0-9]{2}$")
  @SerializedName("dateOfBirth")
  val dateOfBirth: String? = null,

  val gender: Gender,

  @field:Pattern(regexp = "^[A-Z]{1,2}[0-9R][0-9A-Z]? ?[0-9][ABDEFGHJLNPQRSTUWXYZ]{2}|BFPO ?[0-9]{1,4}|([AC-FHKNPRTV-Y]\\d{2}|D6W)? ?[0-9AC-FHKNPRTV-Y]{4}\$")
  @SerializedName("lastKnownPostcode")
  val lastKnownPostCode: String,

  @field:Pattern(regexp = "^[A-Za-z' ,.-]{3,35}$")
  @SerializedName("previousFamilyName")
  val previousFamilyName: String? = null,

  @field:Size(max = 254)
  @SerializedName("schoolAtAge16")
  val schoolAtAge16: String? = null,

  @field:Size(max = 35)
  @SerializedName("placeOfBirth")
  val placeOfBirth: String? = null,

  @field:Pattern(regexp = "^[A-Za-z0-9._'%+-]{1,64}@(?:(?=[A-Za-z0-9-]{1,63}\\.)[A-Za-z0-9]+(?:-[A-Za-z0-9]+)*\\.){1,8}[A-Za-z]{2,63}\$")
  @SerializedName("emailAddress")
  val emailAddress: String? = null,
) {
  fun extractFromRequest(): LearnersLRSRequest = LearnersLRSRequest(
    givenName = givenName,
    familyName = familyName,
    dateOfBirth = dateOfBirth?.let { LocalDate.parse(it) },
    gender = gender.code,
    lastKnownPostCode = lastKnownPostCode,
    previousFamilyName = previousFamilyName,
    schoolAtAge16 = schoolAtAge16,
    placeOfBirth = placeOfBirth,
    emailAddress = emailAddress,
  )
}
