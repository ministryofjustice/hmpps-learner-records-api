package uk.gov.justice.digital.hmpps.learnerrecordsapi.models.response

import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.lrsapi.response.Learner
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.request.FindLearnerByDemographicsRequest

data class FindLearnerByDemographicsResponse(
  val searchParameters: FindLearnerByDemographicsRequest,
  val responseType: ResponseType,
  val mismatchedFields: MutableMap<String, MutableList<String>>? = null,
  val matchedLearners: List<Learner>? = null,
)
