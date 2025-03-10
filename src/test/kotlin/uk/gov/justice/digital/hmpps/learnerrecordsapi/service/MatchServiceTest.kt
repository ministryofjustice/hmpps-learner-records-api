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
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.request.ConfirmMatchRequest
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.request.ConfirmNoMatchRequest
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.request.MatchType
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

    val actual = matchService.findMatch(nomisId)
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

    val actual = matchService.findMatch(nomisId)
    assertThat(actual).isNotEqualTo(null)
    assertThat(actual?.matchedUln).isEqualTo(matchedUln)
    assertThat(actual?.givenName).isEqualTo(givenName)
    assertThat(actual?.familyName).isEqualTo(familyName)
  }

  @Test
  fun `saveMatch should return id of the saved entity`() {
    val id = 1L

    `when`(mockMatchRepository.save(any())).thenReturn(
      MatchEntity(
        id = id,
        nomisId = nomisId,
        matchedUln = matchedUln,
        givenName = givenName,
        familyName = familyName,
      ),
    )

    val savedId = matchService.saveMatch(
      nomisId,
      ConfirmMatchRequest(
        matchingUln = matchedUln,
        givenName = givenName,
        familyName = familyName,
        matchType = MatchType.EXACT_MATCH,
        countOfReturnedUlns = "1",
      ),
    )
    assertThat(savedId).isEqualTo(id)
  }

  @Test
  fun `saveNoMatch should return id of the saved entity`() {
    val id = 1L

    `when`(mockMatchRepository.save(any())).thenReturn(
      MatchEntity(
        id = id,
        nomisId = nomisId,
        matchedUln = "",
      ),
    )

    val savedId = matchService.saveNoMatch(
      nomisId,
      ConfirmNoMatchRequest(
        matchType = MatchType.NO_MATCH_RETURNED_FROM_LRS,
        countOfReturnedUlns = "1",
      ),
    )
    assertThat(savedId).isEqualTo(id)
  }
}
