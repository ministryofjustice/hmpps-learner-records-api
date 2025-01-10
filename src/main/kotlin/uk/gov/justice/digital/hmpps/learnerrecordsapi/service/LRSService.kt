package uk.gov.justice.digital.hmpps.learnerrecordsapi.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.learnerrecordsapi.config.AppConfig
import uk.gov.justice.digital.hmpps.learnerrecordsapi.config.HttpClientConfiguration
import uk.gov.justice.digital.hmpps.learnerrecordsapi.interfaces.LRSApiServiceInterface
import uk.gov.justice.digital.hmpps.learnerrecordsapi.logging.LoggerUtil
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.lrsapi.response.FindLearnerResponse
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.lrsapi.response.Learner
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.request.FindLearnerByDemographicsRequest
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.response.FindLearnerByDemographicsResponse
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.response.ResponseType
import kotlin.reflect.full.declaredMemberProperties

@Service
class LRSService(
  @Autowired
  private val httpClientConfiguration: HttpClientConfiguration,
  private val lrsClient: LRSApiServiceInterface = httpClientConfiguration.retrofit()
    .create(LRSApiServiceInterface::class.java),
  @Autowired
  private val appConfig: AppConfig,
) {

  private val log: LoggerUtil = LoggerUtil(javaClass)

  suspend fun findLearner(findLearnerByDemographicsRequest: FindLearnerByDemographicsRequest): FindLearnerByDemographicsResponse {
    log.debug("Transforming inbound request object to LRS request object")
    val requestBody = findLearnerByDemographicsRequest.extractFromRequest().transformToLRSRequest(appConfig.ukprn(), appConfig.password())

    log.debug("Calling LRS API")
    val lrsResponse = requireNotNull(lrsClient.findLearnerByDemographics(requestBody).body()?.body?.findLearnerResponse) {
      "There was an error with an upstream service. Please try again later."
    }

    // TODO: Appropriately handle error cases
    return convertLrsResponseToOurResponse(findLearnerByDemographicsRequest, lrsResponse)
  }

  private fun computeMismatchedFields(request: FindLearnerByDemographicsRequest, lrsResponse: FindLearnerResponse): MutableMap<String, MutableList<String>> {
    val requestFieldNames = FindLearnerByDemographicsRequest::class.declaredMemberProperties.associateBy { it.name }
    val learnerFieldNames = Learner::class.declaredMemberProperties.associateBy { it.name }
    val sharedFieldNames = requestFieldNames.keys.intersect(learnerFieldNames.keys)

    val mismatchedFields = mutableMapOf<String, MutableList<String>>()

    lrsResponse.learners?.forEach { learner ->
      sharedFieldNames.forEach { fieldName ->
        val requestValue = requestFieldNames[fieldName]?.call(request)?.toString()
        val learnerValue = learnerFieldNames[fieldName]?.call(learner)?.toString()

        if (requestValue != learnerValue && learnerValue != null) {
          mismatchedFields.computeIfAbsent(fieldName) { mutableListOf() }
            .add(learnerValue)
        }
      }
    }

    return mismatchedFields
  }

  private fun convertLrsResponseToOurResponse(request: FindLearnerByDemographicsRequest, response: FindLearnerResponse): FindLearnerByDemographicsResponse {
    val responseType = ResponseType.fromLrsResponseCode(response.responseCode)
    val isPossibleMatch = responseType == ResponseType.POSSIBLE_MATCH
    return FindLearnerByDemographicsResponse(
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