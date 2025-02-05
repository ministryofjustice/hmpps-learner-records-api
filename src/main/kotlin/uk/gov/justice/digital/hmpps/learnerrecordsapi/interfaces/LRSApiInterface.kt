package uk.gov.justice.digital.hmpps.learnerrecordsapi.interfaces

import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.lrsapi.response.FindLearnerEnvelope
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.lrsapi.response.LearningEventsEnvelope

interface LRSApiInterface {
  @Headers("Accept: application/xml", "Content-Type: text/xml")
  @POST("LearnerService.svc")
  suspend fun findLearnerByDemographics(@Body body: RequestBody): Response<FindLearnerEnvelope>

  @Headers(
    "Accept: application/xml",
    "SOAPAction: http://tempuri.org/ILearnerServiceR9/GetLearnerLearningEvents",
    "Content-Type: text/xml",
  )
  @POST("LearnerServiceR9.svc")
  suspend fun getLearnerLearningEvents(@Body body: RequestBody): Response<LearningEventsEnvelope>
}
