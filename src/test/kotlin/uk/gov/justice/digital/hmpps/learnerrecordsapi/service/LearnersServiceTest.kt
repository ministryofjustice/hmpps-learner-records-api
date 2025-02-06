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
import uk.gov.justice.digital.hmpps.learnerrecordsapi.config.AppConfig
import uk.gov.justice.digital.hmpps.learnerrecordsapi.config.HttpClientConfiguration
import uk.gov.justice.digital.hmpps.learnerrecordsapi.interfaces.LRSApiInterface
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.lrsapi.response.FindLearnerBody
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.lrsapi.response.FindLearnerEnvelope
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.lrsapi.response.FindLearnerResponse
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.lrsapi.response.Learner
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.lrsapi.response.MIAPAPIException
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.lrsapi.response.exceptions.LRSException
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.request.Gender
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.request.LearnersRequest
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.response.LRSResponseType
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.response.LearnersResponse
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets
import java.time.LocalDate

@ExtendWith(MockitoExtension::class)
class LearnersServiceTest {

  private lateinit var httpClientConfigurationMock: HttpClientConfiguration
  private lateinit var retrofitMock: Retrofit
  private lateinit var lrsApiInterfaceMock: LRSApiInterface
  private lateinit var appConfigMock: AppConfig

  private lateinit var learnersService: LearnersService

  @BeforeEach
  fun setup() {
    httpClientConfigurationMock = mock(HttpClientConfiguration::class.java)
    retrofitMock = mock(Retrofit::class.java)
    lrsApiInterfaceMock =
      mock(LRSApiInterface::class.java)
    `when`(httpClientConfigurationMock.lrsClient()).thenReturn(lrsApiInterfaceMock)
    appConfigMock = mock(AppConfig::class.java)
    learnersService = LearnersService(httpClientConfigurationMock, appConfigMock)
    `when`(appConfigMock.ukprn()).thenReturn("test")
    `when`(appConfigMock.password()).thenReturn("pass")
  }

  @Test
  fun `should return an LRS request object`(): Unit = runTest {
    val xmlEnv = FindLearnerEnvelope()

    val requestBody = LearnersRequest(
      givenName = "Some",
      familyName = "Person",
      dateOfBirth = LocalDate.of(1980, 1, 1),
      gender = Gender.MALE,
      lastKnownPostCode = "ABCDEF",
      previousFamilyName = "Test",
      schoolAtAge16 = "Test High School",
      placeOfBirth = "Some place",
      emailAddress = "test_email@test.com",
    )

    val expectedResult = LearnersResponse(
      searchParameters = requestBody,
      responseType = LRSResponseType.UNKNOWN_RESPONSE_TYPE,
      mismatchedFields = null,
      matchedLearners = null,
    )

    `when`(lrsApiInterfaceMock.findLearnerByDemographics(any())).thenReturn(
      Response.success(
        xmlEnv,
      ),
    )

    val result = learnersService.getLearners(requestBody, "SomePerson")

    assertEquals(expectedResult, result)
    verify(lrsApiInterfaceMock).findLearnerByDemographics(any())
  }

  @Test
  fun `should return an LRS request object with a learner for exact match`(): Unit = runTest {
    val xmlFindLearnerResponse = FindLearnerResponse(
      responseCode = LRSResponseType.EXACT_MATCH.lrsResponseCode,
      familyName = "Person",
      givenName = "Some",
      dateOfBirth = LocalDate.of(1980, 1, 1).toString(),
      "1",
      lastKnownPostCode = "ABCDEF",
      learners = listOf(Learner(givenName = "Some", gender = "1")),
    )

    val xmlBody = FindLearnerBody(xmlFindLearnerResponse)

    val xmlEnv = FindLearnerEnvelope(xmlBody)

    val requestBody = LearnersRequest(
      givenName = "Some",
      familyName = "Person",
      dateOfBirth = LocalDate.of(1980, 1, 1),
      gender = Gender.MALE,
      lastKnownPostCode = "ABCDEF",
      previousFamilyName = "Test",
      schoolAtAge16 = "Test High School",
      placeOfBirth = "Some place",
      emailAddress = "test_email@test.com",
    )

    val expectedResult = LearnersResponse(
      searchParameters = requestBody,
      responseType = LRSResponseType.EXACT_MATCH,
      mismatchedFields = null,
      matchedLearners = listOf(Learner(givenName = "Some", gender = "MALE")),
    )

    `when`(lrsApiInterfaceMock.findLearnerByDemographics(any())).thenReturn(
      Response.success(
        xmlEnv,
      ),
    )

    val result = learnersService.getLearners(requestBody, "SomePerson")

    assertEquals(expectedResult, result)
    verify(lrsApiInterfaceMock).findLearnerByDemographics(any())
  }

  @Test
  fun `should return an LRS request object with learners and mismatched fields for possible match`(): Unit = runTest {
    val xmlFindLearnerResponse = FindLearnerResponse(
      responseCode = LRSResponseType.POSSIBLE_MATCH.lrsResponseCode,
      familyName = "Person",
      givenName = "Some",
      dateOfBirth = LocalDate.of(1980, 1, 1).toString(),
      "1",
      lastKnownPostCode = "ABCDEF",
      learners = listOf(Learner(givenName = "Some", gender = "1"), Learner(givenName = "Mismatch", gender = "1")),
    )

    val xmlBody = FindLearnerBody(xmlFindLearnerResponse)

    val xmlEnv = FindLearnerEnvelope(xmlBody)

    val requestBody = LearnersRequest(
      givenName = "Some",
      familyName = "Person",
      dateOfBirth = LocalDate.of(1980, 1, 1),
      gender = Gender.MALE,
      lastKnownPostCode = "ABCDEF",
      previousFamilyName = "TeSt",
      schoolAtAge16 = "Test High SchOol",
      placeOfBirth = "Some plAce",
      emailAddress = "test_email@test.com",
    )

    val expectedResult = LearnersResponse(
      searchParameters = requestBody,
      responseType = LRSResponseType.POSSIBLE_MATCH,
      mismatchedFields = mutableMapOf(
        "givenName" to mutableListOf("Mismatch"),
      ),
      matchedLearners = listOf(Learner(givenName = "Some", gender = "MALE"), Learner(givenName = "Mismatch", gender = "MALE")),
    )

    `when`(lrsApiInterfaceMock.findLearnerByDemographics(any())).thenReturn(
      Response.success(
        xmlEnv,
      ),
    )

    val result = learnersService.getLearners(requestBody, "SomePerson")

    assertEquals(expectedResult, result)
    verify(lrsApiInterfaceMock).findLearnerByDemographics(any())
  }

  @Test
  fun `should throw an LRSException when the API returns an error`(): Unit = runTest {
    val requestBody = LearnersRequest(
      givenName = "Some",
      familyName = "Person",
      dateOfBirth = LocalDate.of(1980, 1, 1),
      gender = Gender.MALE,
      lastKnownPostCode = "ABCDEF",
      previousFamilyName = "Test",
      schoolAtAge16 = "Test High School",
      placeOfBirth = "Some place",
      emailAddress = "test_email@test.com",
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

    `when`(lrsApiInterfaceMock.findLearnerByDemographics(any())).thenReturn(
      Response.error(
        500,
        ResponseBody.create(
          "text/xml".toMediaTypeOrNull(),
          InputStreamReader(inputStream, StandardCharsets.UTF_8).readText(),
        ),
      ),
    )

    val actualException = assertThrows<LRSException> {
      learnersService.getLearners(requestBody, "SomePerson")
    }

    verify(lrsApiInterfaceMock).findLearnerByDemographics(any())
    assertEquals(expectedException.toString(), actualException.toString())
  }
}
