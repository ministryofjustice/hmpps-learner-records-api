package uk.gov.justice.digital.hmpps.learnerrecordsapi.models.lrsapi.response

import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlRootElement

@XmlRootElement(name = "Envelope", namespace = "http://schemas.xmlsoap.org/soap/envelope/")
//@XmlAccessorType(XmlAccessType.get)
data class ErrorEnvelope(
  @get:XmlElement(name = "Body", namespace = "http://schemas.xmlsoap.org/soap/envelope/")
  var body: ErrorBody? = null
)

//@XmlAccessorType(XmlAccessType.get)
data class ErrorBody(
  @get:XmlElement(name = "Fault", namespace = "http://schemas.xmlsoap.org/soap/envelope/")
  var fault: Fault? = null
)

//@XmlAccessorType(XmlAccessType.get)
data class Fault(
  @get:XmlElement(name = "faultcode", namespace = "")
  var faultCode: String? = null,

  @get:XmlElement(name = "faultstring", namespace = "")
  var faultString: String? = null,

  @get:XmlElement(name = "detail", namespace = "")
  var detail: FaultDetail? = null
)

//@XmlAccessorType(XmlAccessType.get)
data class FaultDetail(
  @get:XmlElement(name = "MIAPAPIException", namespace = "http://api.lrs.miap.gov.uk/exceptions")
  var miapApiException: MIAPAPIException? = null
)

//@XmlAccessorType(XmlAccessType.get)
data class MIAPAPIException(
  @get:XmlElement(name = "ErrorCode", namespace = "")
  var errorCode: String? = null,

  @get:XmlElement(name = "ErrorActor", namespace = "")
  var errorActor: String? = null,

  @get:XmlElement(name = "Description", namespace = "")
  var description: String? = null,

  @get:XmlElement(name = "FurtherDetails", namespace = "")
  var furtherDetails: String? = null,

  @get:XmlElement(name = "ErrorTimestamp", namespace = "")
  var errorTimestamp: String? = null
)