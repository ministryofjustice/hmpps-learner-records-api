package uk.gov.justice.digital.hmpps.learnerrecordsapi.models.lrsapi.response

import javax.xml.bind.annotation.XmlElement
import javax.xml.bind.annotation.XmlRootElement

@XmlRootElement(name = "LearningEvent", namespace = "http://api.lrs.miap.gov.uk/findmsg")
data class LearningEvent(
  @get:XmlElement(name = "ID", namespace = "http://api.lrs.qcf.gov.uk/model")
  var id: String? = null,

  @get:XmlElement(name = "AchievementProviderUkprn", namespace = "http://api.lrs.qcf.gov.uk/model")
  var achievementProviderUkprn: String? = null,

  @get:XmlElement(name = "AchievementProviderName", namespace = "http://api.lrs.qcf.gov.uk/model")
  var achievementProviderName: String? = null,

  @get:XmlElement(name = "AwardingOrganisationName", namespace = "http://api.lrs.qcf.gov.uk/model")
  var awardingOrganisationName: String? = null,

  @get:XmlElement(name = "QualificationType", namespace = "http://api.lrs.qcf.gov.uk/model")
  var qualificationType: String? = null,

  @get:XmlElement(name = "SubjectCode", namespace = "http://api.lrs.qcf.gov.uk/model")
  var subjectCode: String? = null,

  @get:XmlElement(name = "AchievementAwardDate", namespace = "http://api.lrs.qcf.gov.uk/model")
  var achievementAwardDate: String? = null,

  @get:XmlElement(name = "Credits", namespace = "http://api.lrs.qcf.gov.uk/model")
  var credits: String? = null,

  @get:XmlElement(name = "Source", namespace = "http://api.lrs.qcf.gov.uk/model")
  var source: String? = null,

  @get:XmlElement(name = "DateLoaded", namespace = "http://api.lrs.qcf.gov.uk/model")
  var dateLoaded: String? = null,

  @get:XmlElement(name = "UnderDataChallenge", namespace = "http://api.lrs.qcf.gov.uk/model")
  var underDataChallenge: String? = null,

  @get:XmlElement(name = "Level", namespace = "http://api.lrs.qcf.gov.uk/model")
  var level: String? = null,

  @get:XmlElement(name = "Status", namespace = "http://api.lrs.qcf.gov.uk/model")
  var status: String? = null,

  @get:XmlElement(name = "Subject", namespace = "http://api.lrs.qcf.gov.uk/model")
  var subject: String? = null,

  @get:XmlElement(name = "Grade", namespace = "http://api.lrs.qcf.gov.uk/model")
  var grade: String? = null,

  @get:XmlElement(name = "AwardingOrganisationUkprn", namespace = "http://api.lrs.qcf.gov.uk/model")
  var awardingOrganisationUkprn: String? = null,

  // Fields when source is ILR / NPD

  @get:XmlElement(name = "CollectionType", namespace = "http://api.lrs.qcf.gov.uk/model")
  var collectionType: String? = null,

  @get:XmlElement(name = "ReturnNumber", namespace = "http://api.lrs.qcf.gov.uk/model")
  var returnNumber: String? = null,

  @get:XmlElement(name = "ParticipationStartDate", namespace = "http://api.lrs.qcf.gov.uk/model")
  var participationStartDate: String? = null,

  @get:XmlElement(name = "ParticipationEndDate", namespace = "http://api.lrs.qcf.gov.uk/model")
  var participationEndDate: String? = null,

  // Fields for awarding organisation achievements

  @get:XmlElement(name = "LanguageForAssessment", namespace = "http://api.lrs.qcf.gov.uk/model")
  var languageForAssessment: String? = null,
)

