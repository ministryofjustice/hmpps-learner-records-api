package uk.gov.justice.digital.hmpps.learnerrecordsapi.openapi
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.parameters.RequestBody
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.request.FindLearnerByDemographicsRequest
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.response.FindLearnerByDemographicsResponse
import uk.gov.justice.hmpps.kotlin.common.ErrorResponse

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@Operation(
  summary = "Find learners by demographic information",
  description = "Find possible or exact matches for learners by demographic information",
  requestBody = RequestBody(
    description = "Demographic details of the learner",
    required = true,
    content = [
      Content(
        mediaType = "application/json",
        schema = Schema(implementation = FindLearnerByDemographicsRequest::class),
        examples = [
          ExampleObject(
            name = "Example Request",
            value = """
              {
                "givenName": "Anna",
                "familyName": "Cheng",
                "dateOfBirth": "1995-06-28",
                "gender": "2",
                "lastKnownPostCode": "ZZ12ZZ"
              }
            """,
          ),
        ],
      ),
    ],
  ),
  security = [SecurityRequirement(name = "template-kotlin-ui-role")],
  responses = [
    ApiResponse(
      responseCode = "200",
      description = "Successful response, response type may vary - e.g Possible Match.",
      content = [Content(mediaType = "application/json", schema = Schema(implementation = FindLearnerByDemographicsResponse::class))],
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
annotation class FindByDemographicApi
