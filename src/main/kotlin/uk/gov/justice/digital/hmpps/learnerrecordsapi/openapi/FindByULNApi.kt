package uk.gov.justice.digital.hmpps.learnerrecordsapi.openapi

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.parameters.RequestBody
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.request.GetPLRByULNRequest
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.response.FindLearnerByDemographicsResponse
import uk.gov.justice.hmpps.kotlin.common.ErrorResponse

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@Operation(
  summary = "Get PLR by ULN",
  description = "Get personal learning records and events by a ULN",
  requestBody = RequestBody(
    description = "ULN and demographic details of the learner",
    required = true,
    content = [
      Content(
        mediaType = "application/json",
        schema = Schema(implementation = uk.gov.justice.digital.hmpps.learnerrecordsapi.models.request.GetPLRByULNRequest::class),
        examples = [
          ExampleObject(
            name = "Example Request",
            value = """
              {
                "givenName": "Connor",
                "familyName": "Carroll",
                "uln": "4444599390"
              }
            """
          )
        ]
      )
    ]
  ),
  security = [SecurityRequirement(name = "template-kotlin-ui-role")],
  responses = [
    ApiResponse(
      responseCode = "200",
      description = "The request was successful and a response was returned.",
      content = [Content(mediaType = "application/json", schema = Schema(implementation = FindLearnerByDemographicsResponse::class))]
    ),
    ApiResponse(
      responseCode = "401",
      description = "Unauthorized to access this endpoint",
      content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]
    ),
    ApiResponse(
      responseCode = "403",
      description = "Forbidden to access this endpoint",
      content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))]
    ),
  ]
)
annotation class FindByULNApi
