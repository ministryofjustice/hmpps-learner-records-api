package uk.gov.justice.digital.hmpps.learnerrecordsapi.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.learnerrecordsapi.config.AppConfig
import uk.gov.justice.digital.hmpps.learnerrecordsapi.config.HttpClientConfiguration
import uk.gov.justice.digital.hmpps.learnerrecordsapi.interfaces.LRSApiServiceInterface
import uk.gov.justice.digital.hmpps.learnerrecordsapi.logging.LoggerUtil
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.lrsapi.response.LearningEventsResult
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.request.GetPLRByULNRequest

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

  suspend fun getPLR(getPLRByULNRequest: uk.gov.justice.digital.hmpps.learnerrecordsapi.models.request.GetPLRByULNRequest): LearningEventsResult {
    log.debug("Transforming inbound request object to LRS request object")
    val requestBody = getPLRByULNRequest.extractFromRequest()
      .transformToLRSRequest(appConfig.ukprn(), appConfig.password(), appConfig.vendorId())
    log.debug("Calling LRS API")
    val plrResponse = lrsClient.getLearnerLearningEvents(requestBody)

    // TODO: Appropriately handle error cases
    return requireNotNull(plrResponse.body()?.body?.learningEventsResponse?.learningEventsResult) { "There was an error with an upstream service. Please try again later." }
  }
}
