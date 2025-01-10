package uk.gov.justice.digital.hmpps.learnerrecordsapi.models.lrsapi.response

import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlRootElement

@XmlRootElement(name = "Body", namespace = "http://schemas.xmlsoap.org/soap/envelope/")
data class FindLearnerBody(
  @get:XmlElement(name = "FindLearnerResponse", namespace = "http://api.lrs.miap.gov.uk/findmsg")
  var findLearnerResponse: FindLearnerResponse = FindLearnerResponse()
)