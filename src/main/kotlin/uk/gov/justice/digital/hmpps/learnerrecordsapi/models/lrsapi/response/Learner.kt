package uk.gov.justice.digital.hmpps.learnerrecordsapi.models.lrsapi.response

import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlRootElement

@XmlRootElement(name = "Learner", namespace = "http://api.lrs.miap.gov.uk/findmsg")
data class Learner(
  @get:XmlElement(name = "CreatedDate")
  var createdDate: String? = null,

  @get:XmlElement(name = "LastUpdatedDate")
  var lastUpdatedDate: String? = null,

  @get:XmlElement(name = "ULN")
  var uln: String? = null,

  @get:XmlElement(name = "VersionNumber")
  var versionNumber: String? = null,

  @get:XmlElement(name = "MasterSubstituted")
  var masterSubstituted: String? = null,

  @get:XmlElement(name = "Title")
  var title: String? = null,

  @get:XmlElement(name = "GivenName")
  var givenName: String? = null,

  @get:XmlElement(name = "MiddleOtherName")
  var middleOtherName: String? = null,

  @get:XmlElement(name = "FamilyName")
  var familyName: String? = null,

  @get:XmlElement(name = "PreferredGivenName")
  var preferredGivenName: String? = null,

  @get:XmlElement(name = "PreviousFamilyName")
  var previousFamilyName: String? = null,

  @get:XmlElement(name = "FamilyNameAtAge16")
  var familyNameAtAge16: String? = null,

  @get:XmlElement(name = "SchoolAtAge16")
  var schoolAtAge16: String? = null,

  @get:XmlElement(name = "LastKnownAddressLine1")
  var lastKnownAddressLine1: String? = null,

  @get:XmlElement(name = "LastKnownAddressLine2")
  var lastKnownAddressLine2: String? = null,

  @get:XmlElement(name = "LastKnownAddressTown")
  var lastKnownAddressTown: String? = null,

  @get:XmlElement(name = "LastKnownAddressCountyOrCity")
  var lastKnownAddressCountyOrCity: String? = null,

  @get:XmlElement(name = "LastKnownPostCode")
  var lastKnownPostCode: String? = null,

  @get:XmlElement(name = "DateOfAddressCapture")
  var dateOfAddressCapture: String? = null,

  @get:XmlElement(name = "DateOfBirth")
  var dateOfBirth: String? = null,

  @get:XmlElement(name = "PlaceOfBirth")
  var placeOfBirth: String? = null,

  @get:XmlElement(name = "Gender")
  var gender: String? = null,

  @get:XmlElement(name = "EmailAddress")
  var emailAddress: String? = null,

  @get:XmlElement(name = "Nationality")
  var nationality: String? = null,

  @get:XmlElement(name = "ScottishCandidateNumber")
  var scottishCandidateNumber: String? = null,

  @get:XmlElement(name = "AbilityToShare")
  var abilityToShare: String? = null,

  @get:XmlElement(name = "LearnerStatus")
  var learnerStatus: String? = null,

  @get:XmlElement(name = "VerificationType")
  var verificationType: String? = null,

  @get:XmlElement(name = "OtherVerificationDescription")
  var otherVerificationDescription: String? = null,

  @get:XmlElement(name = "TierLevel")
  var tierLevel: String? = null,

  @get:XmlElement(name = "LinkedULNs")
  var linkedULNs: String? = null
)

