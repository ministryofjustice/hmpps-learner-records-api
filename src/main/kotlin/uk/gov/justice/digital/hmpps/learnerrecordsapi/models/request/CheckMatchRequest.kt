package uk.gov.justice.digital.hmpps.learnerrecordsapi.models.request

import jakarta.validation.constraints.Pattern

open class CheckMatchRequest (

  @field:Pattern(regexp = "^[A-Z]\\d{4}[A-Z]{2}\$")
  val nomisId: String,

)
