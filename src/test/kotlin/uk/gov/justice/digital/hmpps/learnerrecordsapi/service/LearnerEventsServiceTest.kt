package uk.gov.justice.digital.hmpps.learnerrecordsapi.service

import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
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
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.lrsapi.response.LearningEvent
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.lrsapi.response.LearningEventsBody
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.lrsapi.response.LearningEventsEnvelope
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.lrsapi.response.LearningEventsResponse
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.lrsapi.response.LearningEventsResult
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.lrsapi.response.MIAPAPIException
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.lrsapi.response.exceptions.LRSException
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.request.Gender
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.request.LearnerEventsRequest
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.response.LRSResponseType
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.response.LearnerEventsResponse
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

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
      dateOfBirth = "1990-01-01",
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
      dateOfBirth = "1990-01-01",
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
        InputStreamReader(inputStream, StandardCharsets.UTF_8).readText()
          .toResponseBody("text/xml".toMediaTypeOrNull()),
      ),
    )

    val actualException = assertThrows<LRSException> {
      learnerEventsService.getLearningEvents(body, "TestTest")
    }

    verify(lrsApiInterfaceMock).getLearnerLearningEvents(any())
    assertEquals(expectedException.toString(), actualException.toString())
  }

  private fun checkLearnerEvents(keyword: String?, shouldInclude: Boolean): Unit = runTest {
    val env = LearningEventsEnvelope(
      body = LearningEventsBody(
        learningEventsResponse = LearningEventsResponse(
          learningEventsResult = LearningEventsResult(
            learnerRecord = listOf(
              LearningEvent(
                subject = "GCSE in Mathematics",
              ),
            ),
          ),
        ),
      ),
    )
    val body = LearnerEventsRequest(
      givenName = "test",
      familyName = "test",
      uln = "test",
      dateOfBirth = "1990-01-01",
      gender = Gender.MALE,
      keywords = keyword?.let { listOf(it) } ?: emptyList(),
    )
    val expectedResult = LearnerEventsResponse(
      searchParameters = body,
      responseType = LRSResponseType.UNKNOWN_RESPONSE_TYPE,
      foundUln = env.body.learningEventsResponse.learningEventsResult.foundUln,
      incomingUln = env.body.learningEventsResponse.learningEventsResult.incomingUln,
      learnerRecord = if (shouldInclude) {
        env.body.learningEventsResponse.learningEventsResult.learnerRecord
      } else {
        emptyList()
      },
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
  fun `should return learner event as keyword match`() {
    checkLearnerEvents("mathematics", true)
  }

  @Test
  fun `should return no learner events as keyword does not match`() {
    checkLearnerEvents("english", false)
  }

  @Test
  fun `should return learner event as no keyword specified`() {
    checkLearnerEvents(null, true)
  }

  @Test
  fun `should return learner event as keyword synonym match`() {
    checkLearnerEvents("maths", true)
  }

  @Test
  fun `should return learner event as keyword at start`() {
    checkLearnerEvents("gcse", true)
  }

  @Test
  fun `should return no learner events as keyword not whole word`() {
    checkLearnerEvents("gc", false)
  }
}
