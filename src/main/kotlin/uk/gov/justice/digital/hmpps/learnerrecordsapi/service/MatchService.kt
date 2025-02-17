package uk.gov.justice.digital.hmpps.learnerrecordsapi.service

import jakarta.annotation.PostConstruct
import org.springframework.stereotype.Service
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.db.MatchEntity
import uk.gov.justice.digital.hmpps.learnerrecordsapi.repository.MatchRepository

@Service
class MatchService(
  private val matchRepository: MatchRepository,
) {

  @PostConstruct
  fun onStartup() {
    println("Running a test call")
    saveMatch(MatchEntity("ABCDEFGH", "1234567890"))
  }

  fun findMatch(matchEntity: MatchEntity): MatchEntity? = matchRepository.findFirstByNomisIdOrderByIdDesc(
    matchEntity.nomisId,
  )

  fun saveMatch(matchEntity: MatchEntity): MatchEntity = matchRepository.save(matchEntity)
}
