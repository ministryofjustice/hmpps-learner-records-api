package uk.gov.justice.digital.hmpps.learnerrecordsapi.models.lrsapi.response

import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.request.Gender
import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlRootElement

@XmlRootElement(name = "FindLearnerResponse", namespace = "http://api.lrs.miap.gov.uk/findmsg")
data class FindLearnerResponse(
  @get:XmlElement(name = "ResponseCode")
  var responseCode: String = "",

  @get:XmlElement(name = "FamilyName")
  var familyName: String = "",

  @get:XmlElement(name = "GivenName")
  var givenName: String = "",

  @get:XmlElement(name = "DateOfBirth")
  var dateOfBirth: String = "",

  @get:XmlElement(name = "Gender")
  var gender: Gender = Gender.NOT_SPECIFIED,

  @get:XmlElement(name = "LastKnownPostCode")
  var lastKnownPostCode: String = "",

  @get:XmlElement(name = "Learner")
  var learners: List<Learner>? = null,
)
