package uk.gov.justice.digital.hmpps.learnerrecordsapi.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.learnerrecordsapi.config.AppConfig
import uk.gov.justice.digital.hmpps.learnerrecordsapi.config.HttpClientConfiguration
import uk.gov.justice.digital.hmpps.learnerrecordsapi.interfaces.LRSApiInterface
import uk.gov.justice.digital.hmpps.learnerrecordsapi.logging.LoggerUtil
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.lrsapi.response.LearningEventsResponse
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.lrsapi.response.MIAPAPIException
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.lrsapi.response.exceptions.LRSException
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.request.LearnerEventsRequest
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.response.LRSResponseType
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.response.LearnerEventsResponse
import java.io.StringReader
import javax.xml.bind.JAXBContext

@Service
class LearnerEventsService(
  @Autowired
  private val httpClientConfiguration: HttpClientConfiguration,
  @Autowired
  private val appConfig: AppConfig,
) {
  //TODO: use default logger
  private val log: LoggerUtil = LoggerUtil(javaClass)

  private fun lrsClient(): LRSApiInterface = httpClientConfiguration.retrofit().create(LRSApiInterface::class.java)

  //TODO: test if xmlString == null. Logging for null & IllegalArgumentException
  private fun parseError(xmlString: String): MIAPAPIException? {
    val regex = Regex("<ns10:MIAPAPIException[\\s\\S]*?</ns10:MIAPAPIException>")
    val match = regex.find(xmlString)
    val relevantXml = match?.value ?: throw IllegalArgumentException("Unparsable LRS Error")
    //TODO:?? try/catch error logging if relevantXml fails
    val jaxbContext = JAXBContext.newInstance(MIAPAPIException::class.java)
    val unmarshaller = jaxbContext.createUnmarshaller()
    return unmarshaller.unmarshal(StringReader(relevantXml)) as MIAPAPIException
  }

  suspend fun getLearningEvents(learnerEventsRequest: LearnerEventsRequest, userName: String): LearnerEventsResponse {
    log.debug("Transforming inbound request object to LRS request object")
    val requestBody = learnerEventsRequest.extractFromRequest()
      .transformToLRSRequest(appConfig.ukprn(), appConfig.password(), appConfig.vendorId(), userName)
    log.debug("Calling LRS API")

    val learningEventsResponse = lrsClient().getLearnerLearningEvents(requestBody)
    val learningEventsObject = learningEventsResponse.body()?.body?.learningEventsResponse

    if (learningEventsResponse.isSuccessful && learningEventsObject != null) {
      return formatLRSResponse(learnerEventsRequest, learningEventsObject)
    } else {
      throw LRSException(parseError(learningEventsResponse.errorBody()?.string().toString()))
    }
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
