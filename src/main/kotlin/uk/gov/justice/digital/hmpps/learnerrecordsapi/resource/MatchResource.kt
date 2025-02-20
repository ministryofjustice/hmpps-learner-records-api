package uk.gov.justice.digital.hmpps.learnerrecordsapi.resource

import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.ConstraintViolationException
import jakarta.validation.Valid
import jakarta.validation.constraints.Pattern
import org.springframework.core.MethodParameter
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.validation.BindingResult
import org.springframework.validation.ObjectError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.context.request.WebRequest
import org.springframework.web.method.annotation.HandlerMethodValidationException
import uk.gov.justice.digital.hmpps.learnerrecordsapi.config.HmppsBoldLrsExceptionHandler.ErrorResponse
import uk.gov.justice.digital.hmpps.learnerrecordsapi.logging.LoggerUtil
import uk.gov.justice.digital.hmpps.learnerrecordsapi.logging.LoggerUtil.errorLog
import uk.gov.justice.digital.hmpps.learnerrecordsapi.logging.LoggerUtil.log
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.request.MatchRequest
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.response.MatchResponse
import uk.gov.justice.digital.hmpps.learnerrecordsapi.openapi.ConfirmMatchApi
import uk.gov.justice.digital.hmpps.learnerrecordsapi.service.MatchService

@RestController
@PreAuthorize("hasRole('ROLE_LEARNER_RECORDS_SEARCH__RO')")
@RequestMapping(value = ["/match"], produces = ["application/json"])
class MatchResource(
  private val matchService: MatchService,
) {

  val logger = LoggerUtil.getLogger<MatchResource>()

  @PostMapping(value = ["/confirm/{nomisId}"])
  @Tag(name = "Match")
  @ConfirmMatchApi
  suspend fun confirmMatch(
    @PathVariable("nomisId") nomisId: String,
    @RequestBody @Valid confirmMatchRequest: MatchRequest,
  ): ResponseEntity<MatchResponse> {
    logger.log("Received a post request to confirm match endpoint", confirmMatchRequest)
    val savedMatchEntity = matchService.saveMatch(nomisId, confirmMatchRequest.matchingUln)
    val matchResponse = MatchResponse("Match confirmed successfully", savedMatchEntity)
    return ResponseEntity.status(HttpStatus.OK).body(matchResponse)
  }
}
