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
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.request.ConfirmMatchRequest
import uk.gov.justice.hmpps.kotlin.common.ErrorResponse

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@Operation(
  summary = "Confirm a match",
  description = "Confirm a match between a NomisID and a ULN",
  parameters = [
    Parameter(name = "X-Username", `in` = ParameterIn.HEADER, required = true),
    Parameter(name = "nomisId", `in` = ParameterIn.PATH, required = true),
  ],
  requestBody = RequestBody(
    description = "ULN, Given Name, Family Name, Match Type, and Count of Returned ULNs to match with the NomisID in the path",
    required = true,
    content = [
      Content(
        mediaType = "application/json",
        schema = Schema(implementation = ConfirmMatchRequest::class),
        examples = [
          ExampleObject(
            name = "Confirm match Request",
            value = """
              {
                "matchingUln": "1026893096",
                "givenName": "Darcie",
                "familyName": "Tucker",
                "matchType": "Exact Match",
                "countOfReturnedUlns": "1"
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
      description = "The request was successful and the match was created.",
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
    ApiResponse(
      responseCode = "409",
      description = "ULN is already matched",
    ),
  ],
)
annotation class MatchConfirmApi
