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
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.request.CreateMatchRequest
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.request.LearnersRequest
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.response.LearnersResponse
import uk.gov.justice.digital.hmpps.learnerrecordsapi.openapi.FindByDemographicApi
import uk.gov.justice.digital.hmpps.learnerrecordsapi.service.LearnersService
import uk.gov.justice.digital.hmpps.learnerrecordsapi.service.MatchService
import uk.gov.justice.hmpps.sqs.audit.HmppsAuditEvent
import uk.gov.justice.hmpps.sqs.audit.HmppsAuditService
import java.time.Instant
import java.util.UUID

@RestController
@PreAuthorize("hasRole('ROLE_LEARNER_RECORDS_SEARCH__RO')")
@RequestMapping(value = ["/match"], produces = ["application/json"])
class MatchResource(
  private val matchService: MatchService
) {

  val logger = LoggerUtil.getLogger<MatchResource>()

  @PostMapping(value = ["/create"])
  @Tag(name = "Match")
  suspend fun createMatch(
    @RequestBody @Valid matchRequest: CreateMatchRequest,
    @RequestHeader("X-Username", required = true) userName: String,
  ): ResponseEntity<String> {
    logger.log("Received a post request to match endpoint", matchRequest)
    val learnersResponse = matchService.saveMatch(matchRequest.asMatchEntity())
    return ResponseEntity.status(HttpStatus.OK).body("Match Created")
  }
}
