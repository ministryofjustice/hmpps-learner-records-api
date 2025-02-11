package uk.gov.justice.digital.hmpps.learnerrecordsapi.models.request

import com.google.gson.annotations.SerializedName
import jakarta.validation.constraints.Past
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

  @field:Past
  @SerializedName("dateOfBirth")
  val dateOfBirth: LocalDate,

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
    dateOfBirth = dateOfBirth,
    gender = gender.value,
    lastKnownPostCode = lastKnownPostCode,
    previousFamilyName = previousFamilyName,
    schoolAtAge16 = schoolAtAge16,
    placeOfBirth = placeOfBirth,
    emailAddress = emailAddress,
  )
}

enum class Gender(val value: Int) {
  MALE(1),
  FEMALE(2),
  NOT_KNOWN(0),
  NOT_SPECIFIED(9),
}
