package uk.gov.justice.digital.hmpps.learnerrecordsapi.service

import org.slf4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.learnerrecordsapi.config.HttpClientConfiguration
import uk.gov.justice.digital.hmpps.learnerrecordsapi.config.LRSConfiguration
import uk.gov.justice.digital.hmpps.learnerrecordsapi.logging.LoggerUtil
import uk.gov.justice.digital.hmpps.learnerrecordsapi.logging.LoggerUtil.debugLog
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.lrsapi.response.FindLearnerResponse
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.lrsapi.response.Learner
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.lrsapi.response.exceptions.DFEApiDownException
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.lrsapi.response.exceptions.LRSException
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.request.LearnersRequest
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.response.LRSResponseType
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.response.LearnersResponse
import kotlin.reflect.full.declaredMemberProperties

@Service
class LearnersService(
  @Autowired
  private val httpClientConfiguration: HttpClientConfiguration,
  @Autowired
  private val lrsConfiguration: LRSConfiguration,
) : BaseService() {

  private val logger: Logger = LoggerUtil.getLogger<LearnersService>()

  suspend fun getLearners(findLearnerByDemographicsRequest: LearnersRequest, userName: String): LearnersResponse {
    logger.debugLog("Transforming inbound request object to LRS request object")
    val requestBody = findLearnerByDemographicsRequest.extractFromRequest()
      .transformToLRSRequest(lrsConfiguration.ukprn, lrsConfiguration.orgPassword, userName)

    logger.debugLog("Calling LRS API")

    val lrsResponse = httpClientConfiguration.lrsClient().findLearnerByDemographics(requestBody)
    val lrsResponseBody = lrsResponse.body()?.body?.findLearnerResponse
    val errorBody = lrsResponse.errorBody()?.string().toString()

    if (lrsResponse.isSuccessful && lrsResponseBody != null) {
      return convertLrsResponseToOurResponse(findLearnerByDemographicsRequest, lrsResponseBody)
    }

    if (errorBody.contains("UnsupportedHttpVerb")) {
      throw DFEApiDownException(errorBody)
    }

    throw LRSException(parseError(errorBody))
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
