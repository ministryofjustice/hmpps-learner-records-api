package uk.gov.justice.digital.hmpps.learnerrecordsapi.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.learnerrecordsapi.config.AppConfig
import uk.gov.justice.digital.hmpps.learnerrecordsapi.config.HttpClientConfiguration
import uk.gov.justice.digital.hmpps.learnerrecordsapi.logging.LoggerUtil
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.lrsapi.response.LearningEventsResponse
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.request.GetPLRByULNRequest
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.response.GetPLRByULNResponse
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.response.LRSResponseType

@Service
class PLRService(
  @Autowired
  private val httpClientConfiguration: HttpClientConfiguration,
  private val lrsClient: uk.gov.justice.digital.hmpps.learnerrecordsapi.interfaces.LRSApiServiceInterface = httpClientConfiguration.retrofit()
    .create(uk.gov.justice.digital.hmpps.learnerrecordsapi.interfaces.LRSApiServiceInterface::class.java),
  @Autowired
  private val appConfig: AppConfig,
) {
  private val log: LoggerUtil = LoggerUtil(javaClass)

  suspend fun getPLR(getPLRByULNRequest: GetPLRByULNRequest): GetPLRByULNResponse {
    log.debug("Transforming inbound request object to LRS request object")
    val requestBody = getPLRByULNRequest.extractFromRequest()
      .transformToLRSRequest(appConfig.ukprn(), appConfig.password(), appConfig.vendorId())
    log.debug("Calling LRS API")
    val plrResponse =
      requireNotNull(lrsClient.getLearnerLearningEvents(requestBody).body()?.body?.learningEventsResponse) {
        "There was an error with an upstream service. Please try again later."
      }

    return formatLRSResponse(getPLRByULNRequest, plrResponse)
  }

  private fun formatLRSResponse(
    request: GetPLRByULNRequest,
    response: LearningEventsResponse,
  ): GetPLRByULNResponse {
    val learningEventsResult = response.learningEventsResult
    val responseType = LRSResponseType.fromLrsResponseCode(learningEventsResult.responseCode)
    return GetPLRByULNResponse(
      searchParameters = request,
      responseType = responseType,
      incomingUln = learningEventsResult.incomingUln,
      foundUln = learningEventsResult.foundUln,
      learnerRecord = learningEventsResult.learnerRecord,
    )
  }
}
