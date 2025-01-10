package uk.gov.justice.digital.hmpps.learnerrecordsapi.models.lrsapi.response

import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlRootElement

@XmlRootElement(name = "GetLearnerLearningEventsResponse", namespace = "http://tempuri.org/")
data class LearningEventsResponse(
  @get:XmlElement(name = "GetLearnerLearningEventsResult", namespace = "http://tempuri.org/")
  var learningEventsResult: LearningEventsResult = LearningEventsResult()
)