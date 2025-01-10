package uk.gov.justice.digital.hmpps.learnerrecordsapi.models.lrsapi.response

import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlElementWrapper
import javax.xml.bind.annotation.XmlRootElement

@XmlRootElement(name = "GetLearnerLearningEventsResult", namespace = "http://tempuri.org/")
data class LearningEventsResult(
  @get:XmlElement(name = "ResponseCode", namespace = "http://api.lrs.qcf.gov.uk/model")
  var responseCode: String = "",

  @get:XmlElement(name = "FoundULN", namespace = "http://api.lrs.qcf.gov.uk/model")
  var foundUln: String = "",

  @get:XmlElement(name = "IncomingULN", namespace = "http://api.lrs.qcf.gov.uk/model")
  var incomingUln: String = "",

  @get:XmlElementWrapper(name = "LearnerRecord", namespace = "http://api.lrs.qcf.gov.uk/model")
  @get:XmlElement(name = "LearningEvent", namespace = "http://api.lrs.qcf.gov.uk/model")
  var learnerRecord: List<LearningEvent> = mutableListOf(),
)
