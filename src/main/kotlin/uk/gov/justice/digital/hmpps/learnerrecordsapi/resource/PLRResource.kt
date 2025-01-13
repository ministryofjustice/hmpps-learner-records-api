package uk.gov.justice.digital.hmpps.learnerrecordsapi.resource

import jakarta.validation.Valid
import org.springframework.web.bind.annotation.*
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.request.GetPLRByULNRequest
import uk.gov.justice.digital.hmpps.learnerrecordsapi.openapi.FindByULNApi
import uk.gov.justice.digital.hmpps.learnerrecordsapi.service.PLRService

@RestController
//@PreAuthorize("hasRole('ROLE_TEMPLATE_KOTLIN__UI')")
@RequestMapping(value = ["/plr"], produces = ["application/json"])
class PLRResource(
  private val plrService: PLRService,
) : BaseResource() {

  @PostMapping
  @FindByULNApi
  suspend fun findByUln(
    @RequestBody @Valid getPLRByULNRequest: GetPLRByULNRequest
  ): String {
    log.inboundRequest(requestModelObject = getPLRByULNRequest)
    return gson.toJson(plrService.getPLR(getPLRByULNRequest))
  }
}
