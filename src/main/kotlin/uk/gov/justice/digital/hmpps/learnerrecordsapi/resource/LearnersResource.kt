package uk.gov.justice.digital.hmpps.learnerrecordsapi.resource

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.request.LearnersRequest
import uk.gov.justice.digital.hmpps.learnerrecordsapi.openapi.FindByDemographicApi
import uk.gov.justice.digital.hmpps.learnerrecordsapi.service.LearnersService

@RestController
@PreAuthorize("hasRole('ROLE_LEARNER_RECORDS_SEARCH__RO')")
@RequestMapping(value = ["/learners"], produces = ["application/json"])
class LearnersResource(
  private val learnersService: LearnersService,
) : BaseResource() {

  @PostMapping
  @Tag(name = "Learners")
  @FindByDemographicApi
  //TODO: Add Operation / ApiResponse
  @Operation(
    summary = "Returns completed response",
    responses = [
      ApiResponse(responseCode = "200", description = "Learners found"),
      ApiResponse(responseCode = "400", description = "Bad Request"),
      ApiResponse(responseCode = "500", description = "Internal Server Error"),
    ],
  )
  suspend fun findByDemographic(
    @RequestBody @Valid findLearnerByDemographicsRequest: LearnersRequest,
    @RequestHeader("X-Username", required = true) userName: String,
  ): String {
    log.inboundRequest(requestModelObject = findLearnerByDemographicsRequest)
    return gson.toJson(learnersService.getLearners(findLearnerByDemographicsRequest, userName))
  }
}
