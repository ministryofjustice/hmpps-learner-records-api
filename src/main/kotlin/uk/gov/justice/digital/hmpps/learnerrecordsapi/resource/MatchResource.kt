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
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.learnerrecordsapi.logging.LoggerUtil
import uk.gov.justice.digital.hmpps.learnerrecordsapi.logging.LoggerUtil.log
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.db.MatchEntity
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.request.ConfirmMatchRequest
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.response.CheckMatchResponse
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.response.CheckMatchStatus
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.response.ConfirmMatchResponse
import uk.gov.justice.digital.hmpps.learnerrecordsapi.openapi.MatchCheckApi
import uk.gov.justice.digital.hmpps.learnerrecordsapi.openapi.MatchConfirmApi
import uk.gov.justice.digital.hmpps.learnerrecordsapi.service.MatchService

@RestController
@PreAuthorize("hasRole('ROLE_LEARNER_RECORDS_SEARCH__RO')")
@RequestMapping(value = ["/match"], produces = ["application/json"])
class MatchResource(
  private val matchService: MatchService,
) {

  val logger = LoggerUtil.getLogger<MatchResource>()

  @GetMapping("/{nomisId}")
  @Tag(name = "Match")
  @MatchCheckApi
  fun findMatch(
    @PathVariable(name = "nomisId", required = true) nomisId: String,
  ): ResponseEntity<CheckMatchResponse> {
    logger.log("Received a get request to match endpoint", nomisId)
    val entity = matchService.findMatch(
      MatchEntity(
        nomisId = nomisId,
      ),
    )
    if (entity == null) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
        CheckMatchResponse(
          status = CheckMatchStatus.NotFound,
        ),
      )
    }
    val matchedUln = entity.matchedUln ?: ""
    return ResponseEntity.status(HttpStatus.OK).body(
      CheckMatchResponse(
        matchedUln = matchedUln,
        status = if (matchedUln.isNotBlank()) {
          CheckMatchStatus.Found
        } else {
          CheckMatchStatus.NoMatch
        },
      ),
    )
  }

  @PostMapping(value = ["/{nomisId}"])
  @Tag(name = "Match")
  @MatchConfirmApi
  suspend fun confirmMatch(
    @PathVariable(name = "nomisId", required = true) nomisId: String,
    @RequestBody @Valid confirmMatchRequest: ConfirmMatchRequest,
  ): ResponseEntity<ConfirmMatchResponse> {
    logger.log("Received a post request to confirm match endpoint", confirmMatchRequest)
    val savedMatchEntity = matchService.saveMatch(MatchEntity(nomisId, confirmMatchRequest.matchingUln))
    val confirmMatchResponse = ConfirmMatchResponse("Match confirmed successfully", savedMatchEntity)
    return ResponseEntity.status(HttpStatus.OK).body(confirmMatchResponse)
  }
}
