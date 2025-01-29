package uk.gov.justice.digital.hmpps.learnerrecordsapi.models.response

import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.lrsapi.response.Learner

data class LearnersResponse(
  val searchParameters: uk.gov.justice.digital.hmpps.learnerrecordsapi.models.request.LearnersRequest,
  val responseType: LRSResponseType,
  val mismatchedFields: MutableMap<String, MutableList<String>>? = null,
  val matchedLearners: List<Learner>? = null,
)
