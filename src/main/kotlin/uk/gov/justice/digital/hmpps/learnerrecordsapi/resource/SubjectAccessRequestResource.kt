package uk.gov.justice.digital.hmpps.learnerrecordsapi.resource

import io.swagger.v3.oas.annotations.*
import io.swagger.v3.oas.annotations.media.*
import io.swagger.v3.oas.annotations.responses.*
import io.swagger.v3.oas.annotations.tags.*
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.db.MatchEntity
import uk.gov.justice.digital.hmpps.learnerrecordsapi.service.MatchService

@RestController
@RequestMapping("/subject-access-request")
@Tag(name = "Subject Access Request", description = "Endpoint specification")
class SubjectAccessRequestResource {

  @Autowired
  lateinit var matchService: MatchService

  @Operation(
    summary = "API call to retrieve SAR data from a product",
    description = "Either NOMIS Prison Number (PRN) or nDelius Case Reference Number (CRN) must be provided as part of the request.\n" +
      "- If the product uses the identifier type transmitted in the request, it can respond with its data and HTTP code 200.\n" +
      "- If the product uses the identifier type transmitted in the request but has no data to respond with, it should respond with HTTP code 204.\n" +
      "- If the product does not use the identifier type transmitted in the request, it should respond with HTTP code 209.",
    responses = [
      ApiResponse(
        responseCode = "200",
        description = "Request successfully processed - content found",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = SuccessResponse::class))],
      ),
      ApiResponse(responseCode = "204", description = "Request successfully processed - no content found"),
      ApiResponse(responseCode = "209", description = "Subject Identifier is not recognised by this service"),
      ApiResponse(
        responseCode = "400",
        description = "The request was not formed correctly",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
      ApiResponse(
        responseCode = "401",
        description = "The client does not have authorisation to make this request",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
      ),
    ],
  )
  @GetMapping
  fun getData(
    @Parameter(description = "NOMIS Prison Reference Number")
    @RequestParam(required = false) prn: String?,
    @Parameter(description = "nDelius Case Reference Number")
    @RequestParam(required = false) crn: String?,
    @Parameter(description = "Optional parameter denoting minimum date of event occurrence which should be returned in the response")
    @RequestParam(required = false) fromDate: String?,
    @Parameter(description = "Optional parameter denoting maximum date of event occurrence which should be returned in the response")
    @RequestParam(required = false) toDate: String?,
  ): ResponseEntity<Any> {
    when {
      prn.isNullOrEmpty() && crn.isNullOrEmpty() -> return ResponseEntity.badRequest().build()
      !crn.isNullOrEmpty() -> return ResponseEntity.status(209).build()
    }

    val foundData = matchService.getDataForSubjectAccessRequest(prn.orEmpty(), fromDate, toDate)
    if (foundData.isEmpty()) {
      return ResponseEntity.noContent().build()
    }
    return ResponseEntity.ok().body(SuccessResponse(content = foundData))
  }
}

data class SuccessResponse(
  @Schema(description = "Response content")
  val content: List<MatchEntity>,
)

data class ErrorResponse(
  @Schema(description = "Developer message")
  val developerMessage: String,

  @Schema(description = "Error code")
  val errorCode: Int,

  @Schema(description = "Status code")
  val status: Int,

  @Schema(description = "User message")
  val userMessage: String,
)
