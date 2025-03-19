package uk.gov.justice.digital.hmpps.learnerrecordsapi.resource

import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.learnerrecordsapi.config.AuditHelper
import uk.gov.justice.digital.hmpps.learnerrecordsapi.config.Roles.ROLE_LEARNERS_RO
import uk.gov.justice.digital.hmpps.learnerrecordsapi.config.Roles.ROLE_LEARNERS_UI
import uk.gov.justice.digital.hmpps.learnerrecordsapi.logging.LoggerUtil
import uk.gov.justice.digital.hmpps.learnerrecordsapi.logging.LoggerUtil.log
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.lrsapi.response.exceptions.MatchNotFoundException
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.lrsapi.response.exceptions.MatchNotPossibleException
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.request.ConfirmMatchRequest
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.request.ConfirmNoMatchRequest
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.response.CheckMatchResponse
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.response.CheckMatchStatus
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.response.LearnerEventsResponse
import uk.gov.justice.digital.hmpps.learnerrecordsapi.openapi.LearnerEventsByNomisIdApi
import uk.gov.justice.digital.hmpps.learnerrecordsapi.openapi.MatchCheckApi
import uk.gov.justice.digital.hmpps.learnerrecordsapi.openapi.MatchConfirmApi
import uk.gov.justice.digital.hmpps.learnerrecordsapi.openapi.NoMatchConfirmApi
import uk.gov.justice.digital.hmpps.learnerrecordsapi.service.LearnerEventsService
import uk.gov.justice.digital.hmpps.learnerrecordsapi.service.MatchService
import uk.gov.justice.hmpps.kotlin.auth.HmppsAuthenticationHolder
import uk.gov.justice.hmpps.sqs.audit.HmppsAuditService

@RestController
@RequestMapping(value = ["/match"], produces = ["application/json"])
class MatchResource(
  private val matchService: MatchService,
  private val learnerEventsService: LearnerEventsService,
  auditService: HmppsAuditService,
  private val authHolder: HmppsAuthenticationHolder,
) {

  val logger = LoggerUtil.getLogger<MatchResource>()
  private val auditHelper = AuditHelper(auditService)

  @PreAuthorize("hasAnyRole('$ROLE_LEARNERS_UI', '$ROLE_LEARNERS_RO')")
  @GetMapping("/{nomisId}")
  @Tag(name = "Match")
  @MatchCheckApi
  suspend fun findMatch(
    @PathVariable(name = "nomisId", required = true) nomisId: String,
    @RequestHeader("X-Username", required = true) userName: String,
  ): ResponseEntity<CheckMatchResponse> {
    val roles = authHolder.roles.map { it?.authority ?: "" }
    if (roles.contains(ROLE_LEARNERS_RO)) {
      auditHelper.publishMatchAuditEvent(userName, nomisId)
    }
    logger.log("Received a get request to match endpoint", nomisId)
    return matchService.findMatch(nomisId)?.let {
      ResponseEntity.status(HttpStatus.OK).body(it)
    } ?: ResponseEntity.status(HttpStatus.NOT_FOUND).body(
      CheckMatchResponse(
        status = CheckMatchStatus.NotFound,
      ),
    )
  }

  @PreAuthorize("hasRole('$ROLE_LEARNERS_UI')")
  @PostMapping(value = ["/{nomisId}"])
  @Tag(name = "Match")
  @MatchConfirmApi
  suspend fun confirmMatch(
    @RequestHeader("X-Username", required = true) userName: String,
    @PathVariable(name = "nomisId", required = true) nomisId: String,
    @RequestBody @Valid confirmMatchRequest: ConfirmMatchRequest,
  ): ResponseEntity<Void> {
    logger.log("Received a post request to confirm match endpoint", confirmMatchRequest)
    matchService.saveMatch(nomisId, confirmMatchRequest)
    return ResponseEntity.status(HttpStatus.CREATED).build()
  }

  @PreAuthorize("hasRole('$ROLE_LEARNERS_UI')")
  @PostMapping(value = ["/{nomisId}/no-match"])
  @Tag(name = "Match")
  @NoMatchConfirmApi
  suspend fun confirmNoMatch(
    @RequestHeader("X-Username", required = true) userName: String,
    @PathVariable(name = "nomisId", required = true) nomisId: String,
    @RequestBody @Valid confirmNoMatchRequest: ConfirmNoMatchRequest,
  ): ResponseEntity<Void> {
    logger.log("Received a post request to confirm no match endpoint")
    matchService.saveNoMatch(nomisId, confirmNoMatchRequest)
    return ResponseEntity.status(HttpStatus.CREATED).build()
  }

  @PreAuthorize("hasRole('$ROLE_LEARNERS_UI')")
  @PostMapping(value = ["/{nomisId}/unmatch"])
  @Tag(name = "Match")
  @NoMatchConfirmApi
  suspend fun confirmUnmatch(
    @RequestHeader("X-Username", required = true) userName: String,
    @PathVariable(name = "nomisId", required = true) nomisId: String,
  ): ResponseEntity<Void> {
    logger.log("Received a post request to confirm un-match endpoint")
    matchService.unMatch(nomisId)
    return ResponseEntity.status(HttpStatus.CREATED).build()
  }

  @PreAuthorize("hasRole('$ROLE_LEARNERS_RO')")
  @GetMapping(value = ["/{nomisId}/learner-events"], produces = ["application/json"])
  @Tag(name = "Learning Events By Nomis ID")
  @LearnerEventsByNomisIdApi
  suspend fun findLearnerEventsByNomisId(
    @PathVariable(name = "nomisId", required = true) nomisId: String,
    @RequestHeader("X-Username", required = true) userName: String,
  ): ResponseEntity<LearnerEventsResponse> {
    auditHelper.publishLearnerEventsAuditEvent(userName, nomisId)
    logger.log("Received a post request to learner events by Nomis ID endpoint", nomisId)
    val checkMatchResponse = matchService.findMatch(nomisId)
    if (checkMatchResponse == null) {
      throw MatchNotFoundException(nomisId)
    } else if (checkMatchResponse.status == CheckMatchStatus.NoMatch) {
      throw MatchNotPossibleException(nomisId)
    } else {
      val learnerEventsResponse = learnerEventsService.getLearningEvents(
        checkMatchResponse.asLearnerEventsRequest(),
        userName,
      )
      return ResponseEntity.status(HttpStatus.OK).body(learnerEventsResponse)
    }
  }
}
