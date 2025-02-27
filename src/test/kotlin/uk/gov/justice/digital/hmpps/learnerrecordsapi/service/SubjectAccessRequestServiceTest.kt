package uk.gov.justice.digital.hmpps.learnerrecordsapi.service

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.reset
import org.mockito.Mockito.`when`
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.db.MatchEntity
import uk.gov.justice.hmpps.kotlin.sar.HmppsSubjectAccessRequestContent

class SubjectAccessRequestServiceTest {
  private val mockMatchService = mock(MatchService::class.java)
  private val service = SubjectAccessRequestService(mockMatchService)

  @BeforeEach
  fun setup() {
    reset(mockMatchService)
  }

  @Test
  fun `returns null for not found`() {
    `when`(mockMatchService.getDataForSubjectAccessRequest("B12345", null, null)).thenReturn(null)
    assertThat(service.getPrisonContentFor("B12345", null, null)).isNull()
  }

  @Test
  fun `returns data if prisoner found`() {
    val expectedData = listOf(
      MatchEntity(1, "A12345", "abc"),
      MatchEntity(1, "A12345", "def"),
      MatchEntity(1, "A12345", "ghi"),
    )
    `when`(mockMatchService.getDataForSubjectAccessRequest("A12345", null, null)).thenReturn(HmppsSubjectAccessRequestContent(expectedData))
    val actualData = service.getPrisonContentFor("A12345", null, null)?.content
    assertThat(actualData).isEqualTo(expectedData)
  }
}
