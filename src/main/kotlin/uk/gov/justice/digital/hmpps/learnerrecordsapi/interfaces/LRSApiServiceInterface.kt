package uk.gov.justice.digital.hmpps.learnerrecordsapi.interfaces

import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.lrsapi.response.FindLearnerEnvelope
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.lrsapi.response.LearningEventsEnvelope

interface LRSApiServiceInterface {
  @Headers("Accept: application/xml", "Content-Type: text/xml")
// This is the endpoint we want to call on the base url of the api
// Want to figure out how to use the config for this instead of hardcoding
// Need to change this endpoint to lrs when the profile is local
  @POST("LearnerService.svc")
  suspend fun findLearnerByDemographics(@Body body: RequestBody): Response<FindLearnerEnvelope>

  @Headers("Accept: application/xml", "SOAPAction: http://tempuri.org/ILearnerServiceR9/GetLearnerLearningEvents", "Content-Type: text/xml")
  @POST("LearnerServiceR9.svc")
  suspend fun getLearnerLearningEvents(@Body body: RequestBody): Response<LearningEventsEnvelope>
}