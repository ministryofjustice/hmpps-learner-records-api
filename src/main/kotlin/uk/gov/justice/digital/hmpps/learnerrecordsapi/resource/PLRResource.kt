package uk.gov.justice.digital.hmpps.learnerrecordsapi.resource

import jakarta.validation.Valid
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.learnerrecordsapi.openapi.FindByULNApi
import uk.gov.justice.digital.hmpps.learnerrecordsapi.service.PLRService

@RestController
@PreAuthorize("hasRole('ROLE_LEARNER_RECORDS_SEARCH__RW')")
@RequestMapping(value = ["/plr"], produces = ["application/json"])
class PLRResource(
  private val plrService: PLRService,
) : BaseResource() {

  @PostMapping
  @FindByULNApi
  suspend fun findByUln(
    @RequestBody @Valid getPLRByULNRequest: uk.gov.justice.digital.hmpps.learnerrecordsapi.models.request.GetPLRByULNRequest,
  ): String {
    log.inboundRequest(requestModelObject = getPLRByULNRequest)
    return gson.toJson(plrService.getPLR(getPLRByULNRequest))
  }
}
