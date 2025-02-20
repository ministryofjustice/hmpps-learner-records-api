package uk.gov.justice.digital.hmpps.learnerrecordsapi.service

import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.db.MatchEntity
import uk.gov.justice.digital.hmpps.learnerrecordsapi.repository.MatchRepository

@Service
class MatchService(
  private val matchRepository: MatchRepository,
) {

  fun findMatch(matchEntity: MatchEntity): MatchEntity? = matchRepository.findFirstByNomisIdOrderByIdDesc(
    matchEntity.nomisId,
  )

  fun saveMatch(nomisId: String, uln: String): MatchEntity = matchRepository.save(MatchEntity(nomisId, uln))
}
