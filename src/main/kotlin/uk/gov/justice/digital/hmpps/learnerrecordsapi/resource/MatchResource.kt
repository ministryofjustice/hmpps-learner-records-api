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
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.db.MatchEntity
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.request.ConfirmMatchRequest
import uk.gov.justice.digital.hmpps.learnerrecordsapi.service.MatchService

@RestController
@PreAuthorize("hasRole('ROLE_LEARNER_RECORDS_SEARCH__RO')")
@RequestMapping(value = ["/"], produces = ["application/json"])
class MatchResource(
  private val matchService: MatchService
) {

  val logger = LoggerUtil.getLogger<MatchResource>()

  @PostMapping(value = ["/confirm-match"])
  @Tag(name = "Match")
  suspend fun confirmMatch(
    @RequestBody @Valid matchRequest: ConfirmMatchRequest,
    @RequestHeader("X-Username", required = true) userName: String,
  ): ResponseEntity<MatchEntity> {
    logger.log("Received a post request to match endpoint", matchRequest)
    val savedMatchEntity = matchService.saveMatch(matchRequest.asMatchEntity())
    return ResponseEntity.status(HttpStatus.OK).body(savedMatchEntity)
  }
}
