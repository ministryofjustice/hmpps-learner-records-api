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
) {
  val currentlyInPrison: Boolean
    get() = status == "ACTIVE IN"
}