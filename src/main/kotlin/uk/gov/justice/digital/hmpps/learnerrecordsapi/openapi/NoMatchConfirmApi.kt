package uk.gov.justice.digital.hmpps.learnerrecordsapi.openapi

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.parameters.RequestBody
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import uk.gov.justice.digital.hmpps.learnerrecordsapi.config.Roles.ROLE_LEARNERS_UI
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.request.ConfirmNoMatchRequest
import uk.gov.justice.hmpps.kotlin.common.ErrorResponse

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@Operation(
  summary = "Confirm that a match is not possible",
  description = "Confirm a no match for a NomisID",
  parameters = [
    Parameter(name = "X-Username", `in` = ParameterIn.HEADER, required = true),
    Parameter(name = "nomisId", `in` = ParameterIn.PATH, required = true),
  ],
  requestBody = RequestBody(
    description = "Match Type, and Count of Returned ULNs to match with the NomisID in the path",
    required = true,
    content = [
      Content(
        mediaType = "application/json",
        schema = Schema(implementation = ConfirmNoMatchRequest::class),
        examples = [
          ExampleObject(
            name = "Confirm no match Request",
            value = """
              {
                "matchType": "NO_MATCH_RETURNED_FROM_LRS",
                "countOfReturnedUlns": "0"
              }
              """,
          ),
        ],
      ),
    ],
  ),
  security = [SecurityRequirement(name = ROLE_LEARNERS_UI)],
  responses = [
    ApiResponse(
      responseCode = "201",
      description = "The request was successful and the no match was recorded.",
    ),
    ApiResponse(
      responseCode = "401",
      description = "Unauthorized to access this endpoint",
      content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
    ),
    ApiResponse(
      responseCode = "403",
      description = "Forbidden to access this endpoint",
      content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
    ),
  ],
)
annotation class NoMatchConfirmApi
