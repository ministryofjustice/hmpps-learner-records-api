package uk.gov.justice.digital.hmpps.learnerrecordsapi.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.db.MatchEntity

@Repository
interface MatchRepository : JpaRepository<MatchEntity, Long> {

  fun findFirstByNomisIdOrderByIdDesc(
    nomisId: String,
  ): MatchEntity?

  fun findAllByNomisId(nomisId: String): List<MatchEntity>
}
