package uk.gov.justice.digital.hmpps.learnerrecordsapi.service

import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import retrofit2.Response
import uk.gov.justice.digital.hmpps.learnerrecordsapi.config.AppConfig
import uk.gov.justice.digital.hmpps.learnerrecordsapi.config.HttpClientConfiguration
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.lrsapi.response.LearningEventsEnvelope
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
class PLRServiceTest {

  private lateinit var httpClientConfigurationMock: HttpClientConfiguration
  private lateinit var lrsApiServiceInterfaceMock: uk.gov.justice.digital.hmpps.learnerrecordsapi.interfaces.LRSApiServiceInterface
  private lateinit var appConfigMock: AppConfig

  private lateinit var plrService: PLRService

  @BeforeEach
  fun setup() {
    httpClientConfigurationMock = mock(HttpClientConfiguration::class.java)
    lrsApiServiceInterfaceMock =
      mock(uk.gov.justice.digital.hmpps.learnerrecordsapi.interfaces.LRSApiServiceInterface::class.java)
    appConfigMock = mock(AppConfig::class.java)

    plrService = PLRService(httpClientConfigurationMock, lrsApiServiceInterfaceMock, appConfigMock)
  }

  @Test
  fun `should return LRS request object`(): Unit = runTest {
    val env = LearningEventsEnvelope()
    val expectedResult = env.body.learningEventsResponse.learningEventsResult
    val body = uk.gov.justice.digital.hmpps.learnerrecordsapi.models.request.GetPLRByULNRequest(
      givenName = "test",
      familyName = "test",
      uln = "test",
      dateOfBirth = LocalDate.of(1980, 1, 1),
      gender = 1,
    )

    `when`(appConfigMock.ukprn()).thenReturn("test")
    `when`(appConfigMock.password()).thenReturn("pass")
    `when`(appConfigMock.vendorId()).thenReturn("01")
    `when`(lrsApiServiceInterfaceMock.getLearnerLearningEvents(any())).thenReturn(
      Response.success(
        env,
      ),
    )

    val result = plrService.getPLR(body)

    assertEquals(expectedResult, result)
    verify(lrsApiServiceInterfaceMock).getLearnerLearningEvents(any())
  }
}
