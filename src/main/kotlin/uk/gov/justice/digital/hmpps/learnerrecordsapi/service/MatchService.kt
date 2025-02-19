package uk.gov.justice.digital.hmpps.learnerrecordsapi.service

import org.hibernate.internal.util.collections.ConcurrentReferenceHashMap.Option
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.db.MatchEntity
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.request.CheckMatchRequest
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.response.CheckMatchResponse
import uk.gov.justice.digital.hmpps.learnerrecordsapi.repository.MatchRepository
import java.util.Optional

@Service
class MatchService(
  private val matchRepository: MatchRepository,
) {

  fun findMatch(matchEntity: MatchEntity): MatchEntity? = matchRepository.findFirstByNomisIdOrderByIdDesc(
    matchEntity.nomisId,
  )

  fun saveMatch(matchEntity: MatchEntity): MatchEntity = matchRepository.save(matchEntity)
}
