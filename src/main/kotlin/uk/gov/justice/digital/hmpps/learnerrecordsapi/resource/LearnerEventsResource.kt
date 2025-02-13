package uk.gov.justice.digital.hmpps.learnerrecordsapi.resource

import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.learnerrecordsapi.logging.LoggerUtil
import uk.gov.justice.digital.hmpps.learnerrecordsapi.logging.LoggerUtil.log
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.request.LearnerEventsRequest
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.response.LearnerEventsResponse
import uk.gov.justice.digital.hmpps.learnerrecordsapi.openapi.LearnerEventsApi
import uk.gov.justice.digital.hmpps.learnerrecordsapi.service.LearnerEventsService
import uk.gov.justice.hmpps.sqs.audit.HmppsAuditEvent
import uk.gov.justice.hmpps.sqs.audit.HmppsAuditService
import java.time.Instant
import java.util.*

@RestController
@PreAuthorize("hasRole('ROLE_LEARNER_RECORDS_SEARCH__RO')")
@RequestMapping(value = ["/learner-events"], produces = ["application/json"])
class LearnerEventsResource(
  private val learnerEventsService: LearnerEventsService,
  private val auditService: HmppsAuditService,
) {

  val logger = LoggerUtil.getLogger<LearnerEventsResource>()

  val learnerRecordsApi = "learner-records-api"
  val subjectTypeRead = "Read"
  val readRequestReceived = "Read Request Received"

  @PostMapping
  @Tag(name = "Learning Events")
  @LearnerEventsApi
  suspend fun findByUln(
    @RequestBody @Valid learnerEventsRequest: LearnerEventsRequest,
    @RequestHeader("X-Username", required = true) userName: String,
  ): ResponseEntity<LearnerEventsResponse> {
    val hmppsLRSEvent = HmppsAuditEvent(
      readRequestReceived,
      "From $userName",
      subjectTypeRead,
      UUID.randomUUID().toString(),
      Instant.now(),
      userName,
      learnerRecordsApi,
      learnerEventsRequest.toString(),
    )
    auditService.publishEvent(hmppsLRSEvent)
    logger.log("Received a post request to learner events endpoint", learnerEventsRequest)
    val learnerEventsResponse = learnerEventsService.getLearningEvents(learnerEventsRequest, userName)
    return ResponseEntity.status(HttpStatus.OK).body(learnerEventsResponse)
  }
}
