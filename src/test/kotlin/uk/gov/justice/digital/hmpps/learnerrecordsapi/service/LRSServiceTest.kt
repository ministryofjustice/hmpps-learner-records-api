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
import uk.gov.justice.digital.hmpps.learnerrecordsapi.config.AppConfig
import uk.gov.justice.digital.hmpps.learnerrecordsapi.config.HttpClientConfiguration
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.lrsapi.response.*
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.lrsapi.response.exceptions.LRSException
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.request.FindLearnerByDemographicsRequest
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.response.FindLearnerByDemographicsResponse
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.response.LRSResponseType
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
class LRSServiceTest {

  private lateinit var httpClientConfigurationMock: HttpClientConfiguration
  private lateinit var lrsApiServiceInterfaceMock: uk.gov.justice.digital.hmpps.learnerrecordsapi.interfaces.LRSApiServiceInterface
  private lateinit var appConfigMock: AppConfig

  private lateinit var lrsService: LRSService

  @BeforeEach
  fun setup() {
    httpClientConfigurationMock = mock(HttpClientConfiguration::class.java)
    lrsApiServiceInterfaceMock =
      mock(uk.gov.justice.digital.hmpps.learnerrecordsapi.interfaces.LRSApiServiceInterface::class.java)
    appConfigMock = mock(AppConfig::class.java)
    lrsService = LRSService(httpClientConfigurationMock, lrsApiServiceInterfaceMock, appConfigMock)
    `when`(appConfigMock.ukprn()).thenReturn("test")
    `when`(appConfigMock.password()).thenReturn("pass")
  }

  @Test
  fun `should return an LRS request object`(): Unit = runTest {
    val xmlEnv = FindLearnerEnvelope()

    val requestBody = FindLearnerByDemographicsRequest(
      givenName = "Some",
      familyName = "Person",
      dateOfBirth = LocalDate.of(1980, 1, 1),
      gender = 1,
      lastKnownPostCode = "ABCDEF",
    )

    val expectedResult = FindLearnerByDemographicsResponse(
      searchParameters = requestBody,
      responseType = LRSResponseType.UNKNOWN_RESPONSE_TYPE,
      mismatchedFields = null,
      matchedLearners = null,
    )

    `when`(lrsApiServiceInterfaceMock.findLearnerByDemographics(any())).thenReturn(
      Response.success(
        xmlEnv,
      ),
    )

    val result = lrsService.findLearner(requestBody)

    assertEquals(expectedResult, result)
    verify(lrsApiServiceInterfaceMock).findLearnerByDemographics(any())
  }

  @Test
  fun `should return an LRS request object with a learner for exact match`(): Unit = runTest {
    val xmlFindLearnerResponse = FindLearnerResponse(
      responseCode = LRSResponseType.EXACT_MATCH.lrsResponseCode,
      familyName = "Person",
      givenName = "Some",
      dateOfBirth = LocalDate.of(1980, 1, 1).toString(),
      gender = 1,
      lastKnownPostCode = "ABCDEF",
      learners = listOf(Learner(givenName = "Some")),
    )

    val xmlBody = FindLearnerBody(xmlFindLearnerResponse)

    val xmlEnv = FindLearnerEnvelope(xmlBody)

    val requestBody = FindLearnerByDemographicsRequest(
      givenName = "Some",
      familyName = "Person",
      dateOfBirth = LocalDate.of(1980, 1, 1),
      gender = 1,
      lastKnownPostCode = "ABCDEF",
    )

    val expectedResult = FindLearnerByDemographicsResponse(
      searchParameters = requestBody,
      responseType = LRSResponseType.EXACT_MATCH,
      mismatchedFields = null,
      matchedLearners = listOf(Learner(givenName = "Some")),
    )

    `when`(lrsApiServiceInterfaceMock.findLearnerByDemographics(any())).thenReturn(
      Response.success(
        xmlEnv,
      ),
    )

    val result = lrsService.findLearner(requestBody)

    assertEquals(expectedResult, result)
    verify(lrsApiServiceInterfaceMock).findLearnerByDemographics(any())
  }

  @Test
  fun `should return an LRS request object with learners and mismatched fields for possible match`(): Unit = runTest {
    val xmlFindLearnerResponse = FindLearnerResponse(
      responseCode = LRSResponseType.POSSIBLE_MATCH.lrsResponseCode,
      familyName = "Person",
      givenName = "Some",
      dateOfBirth = LocalDate.of(1980, 1, 1).toString(),
      gender = 1,
      lastKnownPostCode = "ABCDEF",
      learners = listOf(Learner(givenName = "Some"), Learner(givenName = "Mismatch")),
    )

    val xmlBody = FindLearnerBody(xmlFindLearnerResponse)

    val xmlEnv = FindLearnerEnvelope(xmlBody)

    val requestBody = FindLearnerByDemographicsRequest(
      givenName = "Some",
      familyName = "Person",
      dateOfBirth = LocalDate.of(1980, 1, 1),
      gender = 1,
      lastKnownPostCode = "ABCDEF",
    )

    val expectedResult = FindLearnerByDemographicsResponse(
      searchParameters = requestBody,
      responseType = LRSResponseType.POSSIBLE_MATCH,
      mismatchedFields = mutableMapOf(
        "givenName" to mutableListOf("Mismatch"),
      ),
      matchedLearners = listOf(Learner(givenName = "Some"), Learner(givenName = "Mismatch")),
    )

    `when`(lrsApiServiceInterfaceMock.findLearnerByDemographics(any())).thenReturn(
      Response.success(
        xmlEnv,
      ),
    )

    val result = lrsService.findLearner(requestBody)

    assertEquals(expectedResult, result)
    verify(lrsApiServiceInterfaceMock).findLearnerByDemographics(any())
  }

  @Test
  fun `should throw an LRSException when the API returns an error`(): Unit = runTest {
    val requestBody = FindLearnerByDemographicsRequest(
      givenName = "Some",
      familyName = "Person",
      dateOfBirth = LocalDate.of(1980, 1, 1),
      gender = 1,
      lastKnownPostCode = "ABCDEF",
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

    val inputStream = javaClass.classLoader.getResourceAsStream("error_ful.xml") ?: throw IllegalArgumentException("File not found in resources: error_ful.xml")

    `when`(lrsApiServiceInterfaceMock.findLearnerByDemographics(any())).thenReturn(
      Response.error(
        500,
        ResponseBody.create("text/xml".toMediaTypeOrNull(), InputStreamReader(inputStream, StandardCharsets.UTF_8).readText()),
      ),
    )

    val actualException = assertThrows<LRSException> {
      lrsService.findLearner(requestBody)
    }

    verify(lrsApiServiceInterfaceMock).findLearnerByDemographics(any())
    assertEquals(expectedException.toString(), actualException.toString())
  }
}
