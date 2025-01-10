package uk.gov.justice.digital.hmpps.learnerrecordsapi.models.lrsapi.response

import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlRootElement

@XmlRootElement(name = "Body", namespace = "http://schemas.xmlsoap.org/soap/envelope/")
data class LearningEventsBody(
  @get:XmlElement(name = "GetLearnerLearningEventsResponse", namespace = "http://tempuri.org/")
  var learningEventsResponse: uk.gov.justice.digital.hmpps.learnerrecordsapi.models.lrsapi.response.LearningEventsResponse = uk.gov.justice.digital.hmpps.learnerrecordsapi.models.lrsapi.response.LearningEventsResponse()
)