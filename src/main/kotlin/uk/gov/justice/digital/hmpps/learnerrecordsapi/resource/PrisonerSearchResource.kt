package uk.gov.justice.digital.hmpps.learnerrecordsapi.resource

import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.learnerrecordsapi.config.Roles.ROLE_LEARNERS_UI
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.response.PrisonerSearchResponse
import uk.gov.justice.digital.hmpps.learnerrecordsapi.service.PrisonerSearchService

@RestController
@RequestMapping("/prisoners", produces = ["application/json"])
class PrisonerSearchResource(
  private val prisonerSearchApiClient: PrisonerSearchService,
) {

  @PreAuthorize("hasRole('$ROLE_LEARNERS_UI')")
  @GetMapping("/prison/{prisonId}")
  suspend fun getPrisoners(
    @PathVariable(name = "prisonId", required = true) prisonId: String,
  ): List<PrisonerSearchResponse> {
    val prisoners = prisonerSearchApiClient.findPrisonersByPrisonId(prisonId, pageSize = 100)
    return prisoners
  }
}
