package uk.gov.justice.digital.hmpps.learnerrecordsapi.models.lrsapi.response

import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlRootElement

@XmlRootElement(name = "Envelope", namespace = "http://schemas.xmlsoap.org/soap/envelope/")
data class LearningEventsEnvelope(
  @get:XmlElement(name = "Body", namespace = "http://schemas.xmlsoap.org/soap/envelope/")
  var body: LearningEventsBody = LearningEventsBody(),
)
