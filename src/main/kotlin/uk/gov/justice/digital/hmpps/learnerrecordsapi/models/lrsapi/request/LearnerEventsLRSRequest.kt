package uk.gov.justice.digital.hmpps.learnerrecordsapi.models.lrsapi.request

import jakarta.validation.constraints.Size
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.time.LocalDate

data class LearnerEventsLRSRequest(
  @field:Size(max = 10)
  val uln: String = "TEST",

  @field:Size(max = 35)
  val givenName: String,

  @field:Size(max = 35)
  val familyName: String,

  @field:Size(min = 4, max = 5)
  val getType: String = "FULL",

  val dateOfBirth: LocalDate? = null,

  // TODO: Validate gender
  val gender: Int? = null,
) {
  fun transformToLRSRequest(ukprn: String, password: String, vendorId: String, userName: String): RequestBody {
// is it best/common practice to move the xml into another file and pull it in? investigate existing kotlin repos
    return """
      <soapenv:Envelope xmlns:soapenv="http://schemas.xmlsoap.org/soap/envelope/" xmlns:tem="http://tempuri.org/" xmlns="http://schemas.datacontract.org/2004/07/Amor.Qcf.Service.Interface">
         <soapenv:Header />
         <soapenv:Body>
            <tem:GetLearnerLearningEvents>
               <tem:invokingOrganisation>
                  <Password>$password</Password>
                  <Ukprn>$ukprn</Ukprn>
                  <Username>$userName</Username>
               </tem:invokingOrganisation>
               <tem:userType>ORG</tem:userType>
               <tem:vendorID>$vendorId</tem:vendorID>
               <tem:language>ENG</tem:language>
               <tem:uln>$uln</tem:uln>
               <tem:givenName>$givenName</tem:givenName>
               <tem:familyName>$familyName</tem:familyName>
               <tem:dateOfBirth>${dateOfBirth ?: ""}</tem:dateOfBirth>
               <tem:gender>${gender ?: ""}</tem:gender>
               <tem:getType>$getType</tem:getType>
            </tem:GetLearnerLearningEvents>
         </soapenv:Body>
      </soapenv:Envelope>
      """.toRequestBody("text/xml".toMediaTypeOrNull())
  }
}
