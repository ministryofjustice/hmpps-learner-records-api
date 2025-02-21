package uk.gov.justice.digital.hmpps.learnerrecordsapi.config

import uk.gov.justice.hmpps.sqs.audit.HmppsAuditEvent
import java.time.Instant
import java.util.UUID

object AuditEvent {

  fun createAuditEvent(whatEvent: String, userName: String, requestParams: String): HmppsAuditEvent {
    val hmppsLRSEvent = HmppsAuditEvent(
      what = whatEvent,
      correlationId = UUID.randomUUID().toString(),
      `when` = Instant.now(),
      who = userName,
      service = "hmpps-learner-records-api",
      details = requestParams,
    )
    return hmppsLRSEvent
  }
}
