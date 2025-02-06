package uk.gov.justice.digital.hmpps.learnerrecordsapi.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.learnerrecordsapi.config.AppConfig
import uk.gov.justice.digital.hmpps.learnerrecordsapi.config.HttpClientConfiguration
import uk.gov.justice.digital.hmpps.learnerrecordsapi.logging.LoggerUtil
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.lrsapi.response.FindLearnerResponse
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.lrsapi.response.Learner
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.lrsapi.response.MIAPAPIException
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.lrsapi.response.exceptions.LRSException
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.request.LearnersRequest
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.response.LRSResponseType
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.response.LearnersResponse
import java.io.StringReader
import javax.xml.bind.JAXBContext
import kotlin.reflect.full.declaredMemberProperties

@Service
class LearnersService(
  @Autowired
  private val httpClientConfiguration: HttpClientConfiguration,
  @Autowired
  private val appConfig: AppConfig,
) {

  private val log: LoggerUtil = LoggerUtil(javaClass)

  private fun parseError(xmlString: String): MIAPAPIException? {
    val regex = Regex("<ns10:MIAPAPIException[\\s\\S]*?</ns10:MIAPAPIException>")
    val match = regex.find(xmlString)
    val relevantXml = match?.value ?: throw IllegalArgumentException("Unparsable LRS Error")
    val jaxbContext = JAXBContext.newInstance(MIAPAPIException::class.java)
    val unmarshaller = jaxbContext.createUnmarshaller()
    return unmarshaller.unmarshal(StringReader(relevantXml)) as MIAPAPIException
  }

  suspend fun getLearners(findLearnerByDemographicsRequest: LearnersRequest, userName: String): LearnersResponse {
    log.debug("Transforming inbound request object to LRS request object")
    val requestBody = findLearnerByDemographicsRequest.extractFromRequest()
      .transformToLRSRequest(appConfig.ukprn(), appConfig.password(), userName)

    log.debug("Calling LRS API")

    val lrsResponse = httpClientConfiguration.lrsClient().findLearnerByDemographics(requestBody)
    val lrsResponseBody = lrsResponse.body()?.body?.findLearnerResponse

    if (lrsResponse.isSuccessful && lrsResponseBody != null) {
      return convertLrsResponseToOurResponse(findLearnerByDemographicsRequest, lrsResponseBody)
    } else {
      throw LRSException(parseError(lrsResponse.errorBody()?.string().toString()))
    }
  }

  private fun computeMismatchedFields(
    request: LearnersRequest,
    lrsResponse: FindLearnerResponse,
  ): MutableMap<String, MutableList<String>> {
    val requestFieldNames =
      LearnersRequest::class.declaredMemberProperties.associateBy { it.name }
    val learnerFieldNames =
      Learner::class.declaredMemberProperties.associateBy { it.name }
    val sharedFieldNames = requestFieldNames.keys.intersect(learnerFieldNames.keys)

    val mismatchedFields = mutableMapOf<String, MutableList<String>>()

    lrsResponse.learners?.forEach { learner ->
      sharedFieldNames.forEach { fieldName ->
        val requestValue = requestFieldNames[fieldName]?.call(request)?.toString()
        val learnerValue = learnerFieldNames[fieldName]?.call(learner)?.toString()
        val neitherAreNull = (learnerValue != null && requestValue != null)
        if (requestValue.orEmpty().trim().lowercase() != learnerValue.orEmpty().trim().lowercase() && neitherAreNull) {
          mismatchedFields.computeIfAbsent(fieldName) { mutableListOf() }
            .add(learnerValue.orEmpty())
        }
      }
    }

    return mismatchedFields
  }

  private fun convertLrsResponseToOurResponse(
    request: LearnersRequest,
    response: FindLearnerResponse,
  ): LearnersResponse {
    val responseType = LRSResponseType.fromLrsResponseCode(response.responseCode)
    val isPossibleMatch = responseType == LRSResponseType.POSSIBLE_MATCH
    response.learners?.forEach { learner: Learner ->
      val correctGender = when (learner.gender) {
        "1" -> "MALE"
        "2" -> "FEMALE"
        "0" -> "NOT_KNOWN"
        "9" -> "NOT_SPECIFIED"
        else -> "Unknown"
      }
      learner.gender = correctGender
    }

    return LearnersResponse(
      searchParameters = request,
      responseType = responseType,
      mismatchedFields = if (isPossibleMatch) {
        computeMismatchedFields(request, response)
      } else {
        null
      },
      matchedLearners = response.learners,
    )
  }
}
