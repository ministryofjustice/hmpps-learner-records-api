package uk.gov.justice.digital.hmpps.learnerrecordsapi.models.lrsapi.response

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
  var gender: String = "",

  @get:XmlElement(name = "LastKnownPostCode")
  var lastKnownPostCode: String = "",

  @get:XmlElement(name = "PreviousFamilyName")
  var previousFamilyName: String? = null,

  @get:XmlElement(name = "SchoolAtAge16")
  var schoolAtAge16: String? = null,

  @get:XmlElement(name = "PlaceOfBirth")
  var placeOfBirth: String? = null,

  @get:XmlElement(name = "EmailAddress")
  var emailAddress: String? = null,

  @get:XmlElement(name = "Learner")
  var learners: List<Learner>? = null,
)
