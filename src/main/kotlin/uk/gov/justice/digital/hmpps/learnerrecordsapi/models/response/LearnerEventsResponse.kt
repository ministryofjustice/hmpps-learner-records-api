package uk.gov.justice.digital.hmpps.learnerrecordsapi.models.response

import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.lrsapi.response.LearningEvent
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.request.LearnerEventsRequest

data class LearnerEventsResponse(
  val searchParameters: LearnerEventsRequest,
  val responseType: LRSResponseType,
  var foundUln: String,
  var incomingUln: String,
  var learnerRecord: List<LearningEvent> = mutableListOf(),
)
