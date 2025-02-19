package uk.gov.justice.digital.hmpps.learnerrecordsapi.models.lrsapi.response.exceptions

class DFEApiDownException(responseBody: String) : RuntimeException() {
  override val message: String = responseBody
}
