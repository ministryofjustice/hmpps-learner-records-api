package uk.gov.justice.digital.hmpps.learnerrecordsapi.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.db.MatchEntity
import uk.gov.justice.digital.hmpps.learnerrecordsapi.repository.MatchRepository

@ExtendWith(MockitoExtension::class)
class MatchServiceTest {
  private val nomisId = "A1234BC"
  private val matchedUln = "a1234"
  private val givenName = "John"
  private val familyName = "Smith"

  private lateinit var mockMatchRepository: MatchRepository
  private lateinit var matchService: MatchService

  @BeforeEach
  fun setup() {
    mockMatchRepository = mock(MatchRepository::class.java)
    matchService = MatchService(mockMatchRepository)
  }

  @Test
  fun `findMatch should return null if no record found`() {
    `when`(mockMatchRepository.findFirstByNomisIdOrderByIdDesc(any())).thenReturn(null)

    val actual = matchService.findMatch(
      MatchEntity(
        nomisId = nomisId,
      ),
    )
    assertThat(actual).isEqualTo(null)
  }

  @Test
  fun `findMatch should return entity if record found`() {
    `when`(mockMatchRepository.findFirstByNomisIdOrderByIdDesc(any())).thenReturn(
      MatchEntity(
        nomisId = nomisId,
        matchedUln = matchedUln,
        givenName = givenName,
        familyName = familyName,
      ),
    )

    val actual = matchService.findMatch(
      MatchEntity(
        nomisId = nomisId,
      ),
    )
    assertThat(actual).isNotEqualTo(null)
    assertThat(actual?.matchedUln).isEqualTo(matchedUln)
    assertThat(actual?.givenName).isEqualTo(givenName)
    assertThat(actual?.familyName).isEqualTo(familyName)
  }

  @Test
  fun `saveMatch should return the entity the it saves via the repository`() {
    `when`(mockMatchRepository.save(any())).thenReturn(
      MatchEntity(
        nomisId = nomisId,
        matchedUln = matchedUln,
      ),
    )

    val saved = matchService.saveMatch(
      MatchEntity(
        nomisId = nomisId,
        matchedUln = matchedUln,
      ),
    )
    assertThat(saved).isNotEqualTo(null)
    assertThat(saved.nomisId).isEqualTo(nomisId)
    assertThat(saved.matchedUln).isEqualTo(matchedUln)
  }
}
