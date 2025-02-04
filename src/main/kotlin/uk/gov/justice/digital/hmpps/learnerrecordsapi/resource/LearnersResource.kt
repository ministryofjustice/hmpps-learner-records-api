package uk.gov.justice.digital.hmpps.learnerrecordsapi.resource

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
  suspend fun findByDemographic(
    @RequestBody @Valid findLearnerByDemographicsRequest: LearnersRequest,
    @RequestHeader("X-Username", required = true) username: String,
  ): String {
    log.inboundRequest(requestModelObject = findLearnerByDemographicsRequest)
    return gson.toJson(learnersService.getLearners(findLearnerByDemographicsRequest, username))
  }
}
