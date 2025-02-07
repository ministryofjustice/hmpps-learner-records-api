package uk.gov.justice.digital.hmpps.learnerrecordsapi.resource

import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import jakarta.validation.constraints.NotEmpty
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.request.LearnerEventsRequest
import uk.gov.justice.digital.hmpps.learnerrecordsapi.openapi.LearnerEventsApi
import uk.gov.justice.digital.hmpps.learnerrecordsapi.service.LearnerEventsService

@RestController
//TODO: should ROLE_LEARNER_RECORDS_SEARCH__RO be a constant instead?
@PreAuthorize("hasRole('ROLE_LEARNER_RECORDS_SEARCH__RO')")
@RequestMapping(value = ["/learner-events"], produces = ["application/json"])
class LearnerEventsResource(
  private val learnerEventsService: LearnerEventsService,
) : BaseResource() {

  @PostMapping
  @Tag(name = "Learning Events")
  @LearnerEventsApi
  suspend fun findByUln(
    @RequestBody @Valid learnerEventsRequest: LearnerEventsRequest,
    //TODO: what if userName empty?
    @RequestHeader("X-Username", required = true) userName: String,
  ): String {
    //TODO: use local logging instead
    log.inboundRequest(requestModelObject = learnerEventsRequest)
    //TODO: I am guessing that gson.toJson is redundant - ?? expect spring to handle this gracefully use ResponseEntity.ok(learners) instead??
    //TODO: try /catch
    return gson.toJson(learnerEventsService.getLearningEvents(learnerEventsRequest, userName))
  }
}
