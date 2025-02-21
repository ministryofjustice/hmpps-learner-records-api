package uk.gov.justice.digital.hmpps.learnerrecordsapi.models.request

import jakarta.validation.constraints.Pattern

class ConfirmMatchRequest(

  @field:Pattern(regexp = "^[0-9]{1,10}\$")
  val matchingUln: String,

)
