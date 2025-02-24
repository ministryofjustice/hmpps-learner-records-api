@file:Suppress("ktlint:standard:filename")

package uk.gov.justice.digital.hmpps.learnerrecordsapi.openapi

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.response.CheckMatchResponse
import uk.gov.justice.hmpps.kotlin.common.ErrorResponse

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@Operation(
  summary = "Find match for nomisId",
  description = "Checks if the given nomisId has already been matched and returns ULN if found",
  parameters = [
    Parameter(name = "X-Username", `in` = ParameterIn.HEADER, required = true),
    Parameter(name = "nomisId", `in` = ParameterIn.PATH, required = true),
  ],
  security = [SecurityRequirement(name = "learner-records-search-read-only-role")],
  responses = [
    ApiResponse(
      responseCode = "200",
      description = "Successful response, found a match or no match.",
      content = [
        Content(
          mediaType = "application/json",
          schema = Schema(implementation = CheckMatchResponse::class),
          examples = [
            ExampleObject(
              name = "Exact Match Response",
              value = """
                {
                  "matchedUln": "a1234",
                  "givenName": "Charlie",
                  "familyName": "Brown",
                  "status": "Found"
                }
                """,
            ),
            ExampleObject(
              name = "Can't match",
              value = """
                {
                  "status": "NoMatch"
                }
                """,
            ),
          ],
        ),
      ],
    ),
    ApiResponse(
      responseCode = "404",
      description = "Successful response, never checked.",
      content = [
        Content(
          mediaType = "application/json",
          schema = Schema(implementation = CheckMatchResponse::class),
          examples = [
            ExampleObject(
              name = "Never been matched",
              value = """
                {
                  "status": "NotFound"
                }
                """,
            ),
          ],
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
annotation class MatchCheckApi
