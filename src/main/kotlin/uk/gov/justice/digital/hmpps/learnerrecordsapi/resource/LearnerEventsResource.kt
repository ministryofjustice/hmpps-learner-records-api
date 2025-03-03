package uk.gov.justice.digital.hmpps.learnerrecordsapi.resource

import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import net.minidev.json.JSONObject
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.learnerrecordsapi.config.AuditEvent.createAuditEvent
import uk.gov.justice.digital.hmpps.learnerrecordsapi.config.Roles.ROLE_LEARNERS_RO
import uk.gov.justice.digital.hmpps.learnerrecordsapi.config.Roles.ROLE_LEARNERS_UI
import uk.gov.justice.digital.hmpps.learnerrecordsapi.logging.LoggerUtil
import uk.gov.justice.digital.hmpps.learnerrecordsapi.logging.LoggerUtil.log
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.lrsapi.response.exceptions.MatchNotFoundException
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.request.LearnerEventsRequest
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.response.CheckMatchResponse
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.response.LearnerEventsResponse
import uk.gov.justice.digital.hmpps.learnerrecordsapi.openapi.LearnerEventsApi
import uk.gov.justice.digital.hmpps.learnerrecordsapi.openapi.LearnerEventsByNomisIdApi
import uk.gov.justice.digital.hmpps.learnerrecordsapi.service.LearnerEventsService
import uk.gov.justice.hmpps.sqs.audit.HmppsAuditService

@RestController
class LearnerEventsResource(
  private val learnerEventsService: LearnerEventsService,
  private val auditService: HmppsAuditService,
) {

  val logger = LoggerUtil.getLogger<LearnerEventsResource>()
  val searchLearnerEventsByULN = "SEARCH_LEARNER_EVENTS_BY_ULN"
  val searchLearnerEventsByNomisId = "SEARCH_LEARNER_EVENTS_BY_NOMISID"

  @PreAuthorize("hasRole('$ROLE_LEARNERS_UI')")
  @RequestMapping(value = ["/learner-events"], produces = ["application/json"])
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

  @PreAuthorize("hasRole('$ROLE_LEARNERS_RO')")
  @RequestMapping(value = ["/learner-events/nomisId"], produces = ["application/json"])
  @Tag(name = "Learning Events By Nomis ID")
  @LearnerEventsByNomisIdApi
  suspend fun findLearnerEventsByNomisId(
    @RequestBody nomisId: JSONObject,
    @RequestHeader("X-Username", required = true) userName: String,
  ): ResponseEntity<LearnerEventsResponse> {
    val nomisId = nomisId.getAsString("nomisId")
    auditService.publishEvent(createAuditEvent(searchLearnerEventsByNomisId, userName, nomisId))
    logger.log("Received a post request to learner events by Nomis ID endpoint", nomisId)
    val checkMatchResponse: CheckMatchResponse? = learnerEventsService.getMatchEntityForNomisId(nomisId)
    if (checkMatchResponse == null) {
      throw MatchNotFoundException(nomisId)
    } else {
      val learnerEventsRequest = learnerEventsService.formLearningEventRequestFromMatchEntity(checkMatchResponse)
      val learnerEventsResponse = learnerEventsService.getLearningEvents(learnerEventsRequest, userName)
      return ResponseEntity.status(HttpStatus.OK).body(learnerEventsResponse)
    }
  }
}
