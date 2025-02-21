package uk.gov.justice.digital.hmpps.learnerrecordsapi.openapi

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.parameters.RequestBody
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.response.ConfirmMatchResponse
import uk.gov.justice.hmpps.kotlin.common.ErrorResponse

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@Operation(
  summary = "Confirm a match",
  description = "Confirm a match between a nomis id and a ULN",
  requestBody = RequestBody(
    description = "a ULN to match with the nomis id in the path",
    required = true,
    content = [
      Content(
        mediaType = "application/json",
        schema = Schema(implementation = uk.gov.justice.digital.hmpps.learnerrecordsapi.models.request.ConfirmMatchRequest::class),
        examples = [
          ExampleObject(
            name = "Example Request",
            value = """
              {
                "matchingUln": "1964986809"
              }
            """,
          ),
        ],
      ),
    ],
  ),
  security = [SecurityRequirement(name = "learner-records-search-read-only-role")],
  responses = [
    ApiResponse(
      responseCode = "200",
      description = "The request was successful and a response was returned.",
      content = [
        Content(
          mediaType = "application/json",
          schema = Schema(implementation = ConfirmMatchResponse::class),
        ),
      ],
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
annotation class ConfirmMatchApi
