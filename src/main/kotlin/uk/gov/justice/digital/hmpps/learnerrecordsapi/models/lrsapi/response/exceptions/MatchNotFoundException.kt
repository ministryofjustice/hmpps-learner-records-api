package uk.gov.justice.digital.hmpps.learnerrecordsapi.models.lrsapi.response.exceptions

class MatchNotFoundException(nomisId: String) : RuntimeException() {
  override val message: String = nomisId
}
