package uk.gov.justice.digital.hmpps.learnerrecordsapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.hmpps.kotlin.sar.HmppsPrisonSubjectAccessRequestService
import uk.gov.justice.hmpps.kotlin.sar.HmppsSubjectAccessRequestContent
import java.time.LocalDate

@Service
class SubjectAccessRequestService(
  var matchService: MatchService,
) : HmppsPrisonSubjectAccessRequestService {
  override fun getPrisonContentFor(
    prn: String,
    fromDate: LocalDate?,
    toDate: LocalDate?,
  ): HmppsSubjectAccessRequestContent? = matchService.getDataForSubjectAccessRequest(prn, fromDate, toDate)
}
