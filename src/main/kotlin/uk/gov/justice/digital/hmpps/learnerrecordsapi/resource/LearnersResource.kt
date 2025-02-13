package uk.gov.justice.digital.hmpps.learnerrecordsapi.resource

import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.learnerrecordsapi.logging.LoggerUtil
import uk.gov.justice.digital.hmpps.learnerrecordsapi.logging.LoggerUtil.log
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.request.LearnersRequest
import uk.gov.justice.digital.hmpps.learnerrecordsapi.openapi.FindByDemographicApi
import uk.gov.justice.digital.hmpps.learnerrecordsapi.service.LearnersService
import uk.gov.justice.hmpps.sqs.audit.HmppsAuditEvent
import uk.gov.justice.hmpps.sqs.audit.HmppsAuditService
import java.time.Instant
import java.util.UUID

@RestController
@PreAuthorize("hasRole('ROLE_LEARNER_RECORDS_SEARCH__RO')")
@RequestMapping(value = ["/learners"], produces = ["application/json"])
class LearnersResource(
  private val learnersService: LearnersService,
  private val auditService: HmppsAuditService,
) : BaseResource() {

  val logger = LoggerUtil.getLogger<LearnersResource>()
  val learnerRecordsApi = "learner-records-api"
  val subjectTypeRead = "Read"
  val readRequestReceived = "Read Request Received"

  @PostMapping
  @Tag(name = "Learners")
  @FindByDemographicApi
  suspend fun findByDemographic(
    @RequestBody @Valid findLearnerByDemographicsRequest: LearnersRequest,
    @RequestHeader("X-Username", required = true) userName: String,
  ): String {
    logger.log("Received a post request to learners endpoint", findLearnerByDemographicsRequest)
    val hmppsLRSEvent = HmppsAuditEvent(
      readRequestReceived,
      "From $userName",
      subjectTypeRead,
      UUID.randomUUID().toString(),
      Instant.now(),
      userName,
      learnerRecordsApi,
      findLearnerByDemographicsRequest.toString(),
    )
    auditService.publishEvent(hmppsLRSEvent)
    return gson.toJson(learnersService.getLearners(findLearnerByDemographicsRequest, userName))
  }
}
