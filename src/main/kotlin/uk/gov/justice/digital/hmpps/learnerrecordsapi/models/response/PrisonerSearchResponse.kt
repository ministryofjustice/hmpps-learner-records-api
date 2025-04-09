package uk.gov.justice.digital.hmpps.learnerrecordsapi.models.response

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class PrisonerSearchResponse(
  @JsonProperty("prisonerNumber") val prisonerNumber: String,
  @JsonProperty("firstName") val firstName: String,
  @JsonProperty("lastName") val lastName: String,
  @JsonProperty("dateOfBirth") val dateOfBirth: String,
  @JsonProperty("gender") val gender: String,
  @JsonProperty("status") val status: String,
  @JsonProperty("addresses") val addresses: List<Address>,
) {
  val currentlyInPrison: Boolean
    get() = status == "ACTIVE IN"

  val primaryPostalCode: String?
    get() = addresses.firstOrNull { it.primaryAddress }?.postalCode

  val genderTransformed: String
    get() = when (gender) {
      "Male" -> "MALE"
      "Female" -> "FEMALE"
      "Not Specified (Indeterminate)" -> "NOT_SPECIFIED"
      else -> "UNKNOWN"
    }
}

@JsonIgnoreProperties(ignoreUnknown = true)
data class Address(
  @JsonProperty("fullAddress") val fullAddress: String,
  @JsonProperty("postalCode") val postalCode: String?,
  @JsonProperty("primaryAddress") val primaryAddress: Boolean,
)
