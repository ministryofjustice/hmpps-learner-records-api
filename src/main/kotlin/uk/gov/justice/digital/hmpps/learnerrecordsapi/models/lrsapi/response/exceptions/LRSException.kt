package uk.gov.justice.digital.hmpps.learnerrecordsapi.models.lrsapi.response.exceptions

class LRSException(
  val errorCode: String? = null,
  val errorActor: String? = null,
  val description: String? = null,
  val furtherDetails: String? = null,
  val errorTimestamp: String? = null
) : RuntimeException("LRS returned an error") {
  override fun toString(): String {
    return "MIAPAPIException(errorCode=$errorCode, errorActor=$errorActor, description=$description, furtherDetails=$furtherDetails, errorTimestamp=$errorTimestamp)"
  }
}
