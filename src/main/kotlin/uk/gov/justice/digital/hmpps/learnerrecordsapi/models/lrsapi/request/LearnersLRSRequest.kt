package uk.gov.justice.digital.hmpps.learnerrecordsapi.models.lrsapi.request

import jakarta.validation.constraints.Size
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.time.LocalDate

// We supply the ukprn and orgPassword - need to store this somewhere safe and call it
// When user first uses microservice the username will be in the header of the request, we need to store this value and then input this value in the request to the LRS API
// Need to investigate logs - everytime a request is made we want to store some logging information (i.e. username, request made). Need to speak to hmpps devs to understand what is the norm.
data class LearnersLRSRequest(
  @field:Size(max = 35)
  val userName: String = "TEST",

  @field:Size(max = 35)
  val givenName: String,

  @field:Size(max = 35)
  val familyName: String,

  val dateOfBirth: LocalDate? = null,

  val gender: Int? = null,

  @field:Size(max = 9)
  val lastKnownPostCode: String? = null,

  @field:Size(max = 35)
  val previousFamilyName: String? = null,

  @field:Size(max = 254)
  val schoolAtAge16: String? = null,

  @field:Size(max = 35)
  val placeOfBirth: String? = null,

  @field:Size(max = 254)
  val emailAddress: String? = null,

  @field:Size(max = 10)
  val uln: String? = null,
) {
  fun transformToLRSRequest(ukprn: String, password: String): RequestBody = """
      <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:fin="http://api.lrs.miap.gov.uk/findmsg">
        <soapenv:Header/>
        <soapenv:Body>
          <fin:FindLearnerByDemographics>
            <FindType>FUL</FindType>
            <UKPRN>$ukprn</UKPRN>
            <OrgPassword>$password</OrgPassword>
            <UserName>$userName</UserName>
            <FamilyName>$familyName</FamilyName>
            <GivenName>$givenName</GivenName>
            <DateOfBirth>$dateOfBirth</DateOfBirth>
            <Gender>$gender</Gender>
            <LastKnownPostCode>${lastKnownPostCode.orEmpty()}</LastKnownPostCode>
            <PreviousFamilyName>${previousFamilyName.orEmpty()}</PreviousFamilyName>
            <SchoolAtAge16>${schoolAtAge16.orEmpty()}</SchoolAtAge16>
            <PlaceOfBirth>${placeOfBirth.orEmpty()}</PlaceOfBirth>
            <EmailAddress>${emailAddress.orEmpty()}</EmailAddress>
          </fin:FindLearnerByDemographics>
        </soapenv:Body>
      </soapenv:Envelope>
      """.toRequestBody("text/xml".toMediaTypeOrNull())
}
