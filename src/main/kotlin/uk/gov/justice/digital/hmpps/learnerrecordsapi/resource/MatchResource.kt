package uk.gov.justice.digital.hmpps.learnerrecordsapi.resource

import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.learnerrecordsapi.logging.LoggerUtil
import uk.gov.justice.digital.hmpps.learnerrecordsapi.logging.LoggerUtil.log
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.request.ConfirmMatchRequest
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.response.MatchResponse
import uk.gov.justice.digital.hmpps.learnerrecordsapi.service.MatchService

@RestController
@PreAuthorize("hasRole('ROLE_LEARNER_RECORDS_SEARCH__RO')")
@RequestMapping(value = ["/match"], produces = ["application/json"])
class MatchResource(
  private val matchService: MatchService,
) {

  val logger = LoggerUtil.getLogger<MatchResource>()

  @PostMapping(value = ["/confirm"])
  @Tag(name = "Match")
  suspend fun confirmMatch(
    @RequestBody @Valid confirmMatchRequest: ConfirmMatchRequest,
  ): ResponseEntity<MatchResponse> {
    logger.log("Received a post request to confirm match endpoint", confirmMatchRequest)
    val savedMatchEntity = matchService.saveMatch(confirmMatchRequest.asMatchEntity())
    val matchResponse = MatchResponse("Match confirmed successfully", savedMatchEntity)
    return ResponseEntity.status(HttpStatus.OK).body(matchResponse)
  }
}
