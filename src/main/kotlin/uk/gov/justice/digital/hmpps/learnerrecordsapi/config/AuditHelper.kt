package uk.gov.justice.digital.hmpps.learnerrecordsapi.config

import uk.gov.justice.digital.hmpps.learnerrecordsapi.config.AuditEvent.MATCH_LE
import uk.gov.justice.digital.hmpps.learnerrecordsapi.config.AuditEvent.MATCH_ULN
import uk.gov.justice.digital.hmpps.learnerrecordsapi.config.AuditEvent.createAuditEvent
import uk.gov.justice.hmpps.sqs.audit.HmppsAuditEvent
import uk.gov.justice.hmpps.sqs.audit.HmppsAuditService
import java.time.Instant
import java.util.UUID

object AuditEvent {
  const val MATCH_ULN = "match-uln"
  const val MATCH_LE = "match-learner-events"

  fun createAuditEvent(whatEvent: String, userName: String, requestParams: String): HmppsAuditEvent = HmppsAuditEvent(
    what = whatEvent,
    correlationId = UUID.randomUUID().toString(),
    `when` = Instant.now(),
    who = userName,
    service = "hmpps-learner-records-api",
    details = requestParams,
  )
}

class AuditHelper(private val auditService: HmppsAuditService) {

  private suspend fun publishAuditEvent(whatEvent: String, userName: String, requestParams: String) {
    val hmppsLRSEvent = createAuditEvent(
      whatEvent,
      userName,
      requestParams,
    )
    auditService.publishEvent(hmppsLRSEvent)
  }

  suspend fun publishMatchAuditEvent(userName: String, nomisId: String) {
    publishAuditEvent(MATCH_ULN, userName, nomisId)
  }

  suspend fun publishLearnerEventsAuditEvent(userName: String, nomisId: String) {
    publishAuditEvent(MATCH_LE, userName, nomisId)
  }
}
