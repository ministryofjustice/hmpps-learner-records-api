package uk.gov.justice.digital.hmpps.learnerrecordsapi.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.learnerrecordsapi.config.AppConfig
import uk.gov.justice.digital.hmpps.learnerrecordsapi.config.HttpClientConfiguration
import uk.gov.justice.digital.hmpps.learnerrecordsapi.interfaces.LRSApiServiceInterface
import uk.gov.justice.digital.hmpps.learnerrecordsapi.logging.LoggerUtil
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.lrsapi.response.LearningEventsResponse
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.lrsapi.response.MIAPAPIException
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.lrsapi.response.exceptions.LRSException
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.request.GetPLRByULNRequest
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.response.GetPLRByULNResponse
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.response.LRSResponseType
import java.io.StringReader
import javax.xml.bind.JAXBContext

@Service
class PLRService(
  @Autowired
  private val httpClientConfiguration: HttpClientConfiguration,
  @Autowired
  private val appConfig: AppConfig,
) {
  private val log: LoggerUtil = LoggerUtil(javaClass)

  private fun lrsClient(): LRSApiServiceInterface = httpClientConfiguration.retrofit().create(LRSApiServiceInterface::class.java)

  private fun parseError(xmlString: String): MIAPAPIException? {
    val regex = Regex("<ns10:MIAPAPIException[\\s\\S]*?</ns10:MIAPAPIException>")
    val match = regex.find(xmlString)
    val relevantXml = match?.value ?: throw IllegalArgumentException("Unparsable LRS Error")
    val jaxbContext = JAXBContext.newInstance(MIAPAPIException::class.java)
    val unmarshaller = jaxbContext.createUnmarshaller()
    return unmarshaller.unmarshal(StringReader(relevantXml)) as MIAPAPIException
  }

  suspend fun getPLR(getPLRByULNRequest: GetPLRByULNRequest): GetPLRByULNResponse {
    log.debug("Transforming inbound request object to LRS request object")
    val requestBody = getPLRByULNRequest.extractFromRequest()
      .transformToLRSRequest(appConfig.ukprn(), appConfig.password(), appConfig.vendorId())
    log.debug("Calling LRS API")

    val plrResponse = lrsClient().getLearnerLearningEvents(requestBody)
    val learningEvents = plrResponse.body()?.body?.learningEventsResponse

    if (plrResponse.isSuccessful && learningEvents != null) {
      return formatLRSResponse(getPLRByULNRequest, learningEvents)
    } else {
      throw LRSException(parseError(plrResponse.errorBody()?.string().toString()))
    }
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
