package uk.gov.justice.digital.hmpps.learnerrecordsapi.service

import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import retrofit2.Response
import retrofit2.Retrofit
import uk.gov.justice.digital.hmpps.learnerrecordsapi.config.HttpClientConfiguration
import uk.gov.justice.digital.hmpps.learnerrecordsapi.config.LRSConfiguration
import uk.gov.justice.digital.hmpps.learnerrecordsapi.interfaces.LRSApiInterface
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.lrsapi.response.LearningEventsEnvelope
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.lrsapi.response.MIAPAPIException
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.lrsapi.response.exceptions.LRSException
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.request.Gender
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.request.LearnerEventsRequest
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.response.LRSResponseType
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.response.LearnerEventsResponse
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
class LearnerEventsServiceTest {

  private lateinit var httpClientConfigurationMock: HttpClientConfiguration
  private lateinit var retrofitMock: Retrofit
  private lateinit var lrsApiInterfaceMock: LRSApiInterface
  private lateinit var lrsConfiguration: LRSConfiguration
  private lateinit var learnerEventsService: LearnerEventsService

  @BeforeEach
  fun setup() {
    httpClientConfigurationMock = mock(HttpClientConfiguration::class.java)
    retrofitMock = mock(Retrofit::class.java)
    lrsApiInterfaceMock =
      mock(LRSApiInterface::class.java)
    `when`(httpClientConfigurationMock.lrsClient()).thenReturn(lrsApiInterfaceMock)
    lrsConfiguration = mock(LRSConfiguration::class.java)
    learnerEventsService = LearnerEventsService(httpClientConfigurationMock, lrsConfiguration)
    `when`(lrsConfiguration.ukprn).thenReturn("test")
    `when`(lrsConfiguration.orgPassword).thenReturn("pass")
    `when`(lrsConfiguration.vendorId).thenReturn("01")
  }

  @Test
  fun `should return LRS request object`(): Unit = runTest {
    val env = LearningEventsEnvelope()
    val body = LearnerEventsRequest(
      givenName = "test",
      familyName = "test",
      uln = "test",
      dateOfBirth = LocalDate.of(1980, 1, 1),
      gender = Gender.MALE,
    )
    val expectedResult = LearnerEventsResponse(
      searchParameters = body,
      responseType = LRSResponseType.UNKNOWN_RESPONSE_TYPE,
      foundUln = env.body.learningEventsResponse.learningEventsResult.foundUln,
      incomingUln = env.body.learningEventsResponse.learningEventsResult.incomingUln,
      learnerRecord = env.body.learningEventsResponse.learningEventsResult.learnerRecord,
    )

    `when`(lrsApiInterfaceMock.getLearnerLearningEvents(any())).thenReturn(
      Response.success(
        env,
      ),
    )

    val result = learnerEventsService.getLearningEvents(body, "TestTest")

    assertEquals(expectedResult, result)
    verify(lrsApiInterfaceMock).getLearnerLearningEvents(any())
  }

  @Test
  fun `should throw an LRSException when the API returns an error`(): Unit = runTest {
    val body = LearnerEventsRequest(
      givenName = "test",
      familyName = "test",
      uln = "test",
      dateOfBirth = LocalDate.of(1980, 1, 1),
      gender = Gender.MALE,
    )

    val expectedException = LRSException(
      MIAPAPIException(
        errorCode = "WSEC0001",
        errorActor = "uk.gov.miap.lrs.api.learner.utils.MSGValidator.validateSOAPMSG()",
        description = "Invalid request, The request was badly formed - this may include missing mandatory parameters, or invalid data types.",
        furtherDetails = "cvc-pattern-valid: Value '' is not facet-valid with respect to pattern '[\\s]*[\\S][\\s\\S]*' for type 'MIAPNameNonEmpty'.",
        errorTimestamp = "2025-01-14 15:23:47",
      ),
    )

    val inputStream = javaClass.classLoader.getResourceAsStream("error_ful.xml")
      ?: throw IllegalArgumentException("File not found in resources: error_ful.xml")

    `when`(lrsApiInterfaceMock.getLearnerLearningEvents(any())).thenReturn(
      Response.error(
        500,
        ResponseBody.create(
          "text/xml".toMediaTypeOrNull(),
          InputStreamReader(inputStream, StandardCharsets.UTF_8).readText(),
        ),
      ),
    )

    val actualException = assertThrows<LRSException> {
      learnerEventsService.getLearningEvents(body, "TestTest")
    }

    verify(lrsApiInterfaceMock).getLearnerLearningEvents(any())
    assertEquals(expectedException.toString(), actualException.toString())
  }
}
