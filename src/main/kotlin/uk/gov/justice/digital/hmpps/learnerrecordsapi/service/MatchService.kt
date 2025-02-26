package uk.gov.justice.digital.hmpps.learnerrecordsapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.db.MatchEntity
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.request.ConfirmMatchRequest
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.response.CheckMatchResponse
import uk.gov.justice.digital.hmpps.learnerrecordsapi.repository.MatchRepository
import java.time.LocalDate

@Service
class MatchService(
  private val matchRepository: MatchRepository,
) {

  fun findMatch(nomisId: String): CheckMatchResponse? {
    val entity = matchRepository.findFirstByNomisIdOrderByIdDesc(nomisId)
    return entity?.let {
      CheckMatchResponse(
        matchedUln = it.matchedUln,
        givenName = it.givenName,
        familyName = it.familyName,
        dateOfBirth = it.dateOfBirth,
        gender = it.gender,
      )
    }
  }

  fun saveMatch(nomisId: String, confirmMatchRequest: ConfirmMatchRequest): Long? {
    val entity = confirmMatchRequest.asMatchEntity(nomisId)
    return matchRepository.save(entity).id
  }

  fun getDataForSubjectAccessRequest(
    nomisId: String,
    fromDate: LocalDate?,
    toDate: LocalDate?,
  ): List<MatchEntity> = matchRepository.findForSubjectAccessRequest(
    nomisId,
    fromDate?.atStartOfDay(),
    toDate?.plusDays(1)?.atStartOfDay()?.minusNanos(1L),
  )
}
