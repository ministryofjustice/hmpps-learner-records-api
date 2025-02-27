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
import uk.gov.justice.digital.hmpps.learnerrecordsapi.config.AuditEvent.createAuditEvent
import uk.gov.justice.digital.hmpps.learnerrecordsapi.config.Roles.ROLE_LEARNER_RECORDS__LEARNER_RECORDS_MATCH_UI
import uk.gov.justice.digital.hmpps.learnerrecordsapi.logging.LoggerUtil
import uk.gov.justice.digital.hmpps.learnerrecordsapi.logging.LoggerUtil.log
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.request.LearnerEventsRequest
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.response.LearnerEventsResponse
import uk.gov.justice.digital.hmpps.learnerrecordsapi.openapi.LearnerEventsApi
import uk.gov.justice.digital.hmpps.learnerrecordsapi.service.LearnerEventsService
import uk.gov.justice.hmpps.sqs.audit.HmppsAuditService

@RestController
@PreAuthorize("hasRole('$ROLE_LEARNER_RECORDS__LEARNER_RECORDS_MATCH_UI')")
@RequestMapping(value = ["/learner-events"], produces = ["application/json"])
class LearnerEventsResource(
  private val learnerEventsService: LearnerEventsService,
  private val auditService: HmppsAuditService,
) {

  val logger = LoggerUtil.getLogger<LearnerEventsResource>()
  val searchLearnerEventsByULN = "SEARCH_LEARNER_EVENTS_BY_ULN"

  @PostMapping
  @Tag(name = "Learning Events")
  @LearnerEventsApi
  suspend fun findByUln(
    @RequestBody @Valid learnerEventsRequest: LearnerEventsRequest,
    @RequestHeader("X-Username", required = true) userName: String,
  ): ResponseEntity<LearnerEventsResponse> {
    auditService.publishEvent(createAuditEvent(searchLearnerEventsByULN, userName, learnerEventsRequest.toString()))
    logger.log("Received a post request to learner events endpoint", learnerEventsRequest)
    val learnerEventsResponse = learnerEventsService.getLearningEvents(learnerEventsRequest, userName)
    return ResponseEntity.status(HttpStatus.OK).body(learnerEventsResponse)
  }
}
