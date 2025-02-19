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
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.request.CheckMatchRequest
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.response.CheckMatchResponse
import uk.gov.justice.digital.hmpps.learnerrecordsapi.openapi.FindByDemographicApi
import uk.gov.justice.digital.hmpps.learnerrecordsapi.service.MatchService

@RestController
@PreAuthorize("hasRole('ROLE_LEARNER_RECORDS_SEARCH__RO')")
@RequestMapping(value = ["/match"], produces = ["application/json"])
class MatchResource(
  private val matchService: MatchService,
) {

  val logger = LoggerUtil.getLogger<MatchResource>()

  @PostMapping(value = ["/check"])
  @Tag(name = "Check")
  @FindByDemographicApi
  fun findMatch(
    @RequestBody @Valid checkMatchRequest: CheckMatchRequest,
    @RequestHeader("X-Username", required = true) userName: String,
  ): ResponseEntity<CheckMatchResponse> {
    logger.log("Received a post request to match endpoint", checkMatchRequest)
    val entity = matchService.findMatch(
      MatchEntity(
        nomisId = checkMatchRequest.nomisId,
      )
    )
    if (entity == null) {
      return ResponseEntity(HttpStatus.NOT_FOUND)
    }
    return ResponseEntity.status(HttpStatus.OK).body(
      CheckMatchResponse(
        matchedUln = entity.matchedUln ?: ""
      )
    )
  }
}
