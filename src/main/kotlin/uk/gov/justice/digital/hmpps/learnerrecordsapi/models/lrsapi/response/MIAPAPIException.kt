package uk.gov.justice.digital.hmpps.learnerrecordsapi.models.lrsapi.response

import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlRootElement

@XmlRootElement(name = "MIAPAPIException", namespace = "http://api.lrs.miap.gov.uk/exceptions")
data class MIAPAPIException(
  @field:XmlElement(name = "ErrorCode")
  val errorCode: String? = null,

  @field:XmlElement(name = "ErrorActor")
  val errorActor: String? = null,

  @field:XmlElement(name = "Description")
  val description: String? = null,

  @field:XmlElement(name = "FurtherDetails")
  val furtherDetails: String? = null,

  @field:XmlElement(name = "ErrorTimestamp")
  val errorTimestamp: String? = null,
)
