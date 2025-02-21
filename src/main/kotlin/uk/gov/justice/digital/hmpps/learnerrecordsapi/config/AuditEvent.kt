package uk.gov.justice.digital.hmpps.learnerrecordsapi.config

import uk.gov.justice.hmpps.sqs.audit.HmppsAuditEvent
import java.time.Instant
import java.util.UUID

object AuditEvent {

  val learnerRecordsApi = "learner-records-api"
  val subjectTypeRead = "Read"
  val readRequestReceived = "Read Request Received"

  fun createAuditEvent(userName: String, requestParams: String): HmppsAuditEvent {
    val hmppsLRSEvent = HmppsAuditEvent(
      readRequestReceived,
      "From $userName",
      subjectTypeRead,
      UUID.randomUUID().toString(),
      Instant.now(),
      userName,
      learnerRecordsApi,
      requestParams,
    )
    return hmppsLRSEvent
  }
}
