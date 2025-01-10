package uk.gov.justice.digital.hmpps.learnerrecordsapi.resource

import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.request.FindLearnerByDemographicsRequest
import uk.gov.justice.digital.hmpps.learnerrecordsapi.openapi.FindByDemographicApi
import uk.gov.justice.digital.hmpps.learnerrecordsapi.service.LRSService

@RestController
@PreAuthorize("hasRole('ROLE_TEMPLATE_KOTLIN__UI')")
@RequestMapping(value = ["/learners"], produces = ["application/json"])
class LearnersResource(
  private val lrsService: LRSService,
) : BaseResource() {

  @PostMapping
  @Tag(name = "Learners")
  @FindByDemographicApi
  suspend fun findByDemographic(
    @RequestBody @Valid findLearnerByDemographicsRequest: FindLearnerByDemographicsRequest,
  ): String {
    log.inboundRequest(requestModelObject = findLearnerByDemographicsRequest)
    return gson.toJson(lrsService.findLearner(findLearnerByDemographicsRequest))
  }
}