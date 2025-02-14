package uk.gov.justice.digital.hmpps.learnerrecordsapi.models.lrsapi.request

import jakarta.validation.constraints.Size
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.time.LocalDate

data class LearnersLRSRequest(
  @field:Size(max = 35)
  val givenName: String,

  @field:Size(max = 35)
  val familyName: String,

  val dateOfBirth: LocalDate?,

  val gender: Int?,

  @field:Size(max = 9)
  val lastKnownPostCode: String?,

  @field:Size(min = 3, max = 4)
  val findType: String = "FUL",

  @field:Size(max = 35)
  val previousFamilyName: String?,

  @field:Size(max = 254)
  val schoolAtAge16: String?,

  @field:Size(max = 35)
  val placeOfBirth: String?,

  @field:Size(max = 254)
  val emailAddress: String?,
) {
  fun transformToLRSRequest(ukprn: String, password: String, userName: String): RequestBody = """
      <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:fin="http://api.lrs.miap.gov.uk/findmsg">
        <soapenv:Header/>
        <soapenv:Body>
          <fin:FindLearnerByDemographics>
            <FindType>$findType</FindType>
            <UKPRN>$ukprn</UKPRN>
            <OrgPassword>$password</OrgPassword>
            <UserName>$userName</UserName>
            <FamilyName>$familyName</FamilyName>
            <GivenName>$givenName</GivenName>
            <DateOfBirth>$dateOfBirth</DateOfBirth>
            <Gender>$gender</Gender>
            <LastKnownPostCode>$lastKnownPostCode</LastKnownPostCode>
            <PreviousFamilyName>${previousFamilyName ?: ""}</PreviousFamilyName>
            <SchoolAtAge16>${schoolAtAge16 ?: ""}</SchoolAtAge16>
            <PlaceOfBirth>${placeOfBirth ?: ""}</PlaceOfBirth>
            <EmailAddress>${emailAddress ?: ""}</EmailAddress>
          </fin:FindLearnerByDemographics>
        </soapenv:Body>
      </soapenv:Envelope>
      """.toRequestBody("text/xml".toMediaTypeOrNull())
}
