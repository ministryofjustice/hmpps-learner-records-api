package uk.gov.justice.digital.hmpps.learnerrecordsapi.openapi

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import uk.gov.justice.digital.hmpps.learnerrecordsapi.config.Roles.ROLE_LEARNERS_UI
import uk.gov.justice.hmpps.kotlin.common.ErrorResponse

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@Operation(
  summary = "Confirm an un-match",
  description = "Confirm that a NomisID is to be un-matched",
  parameters = [
    Parameter(name = "X-Username", `in` = ParameterIn.HEADER, required = true),
    Parameter(name = "nomisId", `in` = ParameterIn.PATH, required = true),
  ],
  security = [SecurityRequirement(name = ROLE_LEARNERS_UI)],
  responses = [
    ApiResponse(
      responseCode = "201",
      description = "The request was successful and the un-match was recorded.",
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
annotation class UnmatchConfirmApi
