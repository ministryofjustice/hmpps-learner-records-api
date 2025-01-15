package uk.gov.justice.digital.hmpps.learnerrecordsapi.models.response

import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.lrsapi.response.LearningEvent
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.request.GetPLRByULNRequest

data class GetPLRByULNResponse(
  val searchParameters: GetPLRByULNRequest,
  val responseType: LRSResponseType,
  var foundUln: String,
  var incomingUln: String,
  var learnerRecord: List<LearningEvent> = mutableListOf(),
)
