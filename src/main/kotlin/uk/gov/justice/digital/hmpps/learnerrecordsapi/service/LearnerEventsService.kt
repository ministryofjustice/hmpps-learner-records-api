package uk.gov.justice.digital.hmpps.learnerrecordsapi.service

import org.slf4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.learnerrecordsapi.config.HttpClientConfiguration
import uk.gov.justice.digital.hmpps.learnerrecordsapi.config.LRSConfiguration
import uk.gov.justice.digital.hmpps.learnerrecordsapi.logging.LoggerUtil
import uk.gov.justice.digital.hmpps.learnerrecordsapi.logging.LoggerUtil.debugLog
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.lrsapi.response.LearningEventsResponse
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.lrsapi.response.exceptions.DFEApiDownException
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.lrsapi.response.exceptions.LRSException
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.request.LearnerEventsRequest
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.response.LRSResponseType
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.response.LearnerEventsResponse

@Service
class LearnerEventsService(
  @Autowired
  private val httpClientConfiguration: HttpClientConfiguration,
  @Autowired
  private val lrsConfiguration: LRSConfiguration,
) : BaseService() {
  private val logger: Logger = LoggerUtil.getLogger<LearnerEventsService>()

  suspend fun getLearningEvents(learnerEventsRequest: LearnerEventsRequest, userName: String): LearnerEventsResponse {
    logger.debugLog("Transforming inbound request object to LRS request object")
    val requestBody = learnerEventsRequest.extractFromRequest()
      .transformToLRSRequest(lrsConfiguration.ukprn, lrsConfiguration.orgPassword, lrsConfiguration.vendorId, userName)
    logger.debug("Calling LRS API")

    val learningEventsResponse = httpClientConfiguration.lrsClient().getLearnerLearningEvents(requestBody)
    val learningEventsObject = learningEventsResponse.body()?.body?.learningEventsResponse
    val errorBody = learningEventsResponse.errorBody()?.string().toString()

    if (learningEventsResponse.isSuccessful && learningEventsObject != null) {
      return formatLRSResponse(learnerEventsRequest, learningEventsObject)
    }

    if (errorBody.contains("UnsupportedHttpVerb")) {
      throw DFEApiDownException(errorBody)
    }

    throw LRSException(parseError(errorBody))
  }

  private fun formatLRSResponse(
    request: LearnerEventsRequest,
    response: LearningEventsResponse,
  ): LearnerEventsResponse {
    val learningEventsResult = response.learningEventsResult
    val responseType = LRSResponseType.fromLrsResponseCode(learningEventsResult.responseCode)
    return LearnerEventsResponse(
      searchParameters = request,
      responseType = responseType,
      incomingUln = learningEventsResult.incomingUln,
      foundUln = learningEventsResult.foundUln,
      learnerRecord = learningEventsResult.learnerRecord,
    )
  }
}
