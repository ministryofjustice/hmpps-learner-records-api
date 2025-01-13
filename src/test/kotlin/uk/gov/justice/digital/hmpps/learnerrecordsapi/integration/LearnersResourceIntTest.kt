package uk.gov.justice.digital.hmpps.learnerrecordsapi.integration

import com.google.gson.GsonBuilder
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import uk.gov.justice.digital.hmpps.learnerrecordsapi.integration.wiremock.FindLearnerByDemographicsApiExtension.Companion.findLearnerByDemographicsApiMock
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.gsonadapters.LocalDateAdapter
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.gsonadapters.ResponseTypeAdapter
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.lrsapi.response.Learner
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.response.FindLearnerByDemographicsResponse
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.response.ResponseType
import java.time.LocalDate

class LearnersResourceIntTest : IntegrationTestBase() {

  @Nested
  @DisplayName("POST /learners")
  inner class LearnersEndpoint {

    val gson = GsonBuilder()
      .registerTypeAdapter(LocalDate::class.java, LocalDateAdapter().nullSafe())
      .registerTypeAdapter(ResponseType::class.java, ResponseTypeAdapter().nullSafe())
      .create()

    @Test
    fun `should return 500 with an appropriate error response if LRS returns a BadRequest`() {
      findLearnerByDemographicsApiMock.stubPostBadRequest()

      val actualResponse = webTestClient.post()
        .uri("/learners")
        .headers(setAuthorisation(roles = listOf("ROLE_TEMPLATE_KOTLIN__UI")))
        .bodyValue(findLearnerByDemographicsRequest)
        .accept(MediaType.parseMediaType("application/json"))
        .exchange()
        .expectStatus()
        .is5xxServerError
        .expectBody()
        .returnResult()
        .responseBody

      val actualResponseString = actualResponse?.toString(Charsets.UTF_8)
      assertThat(actualResponseString).contains("There was an error with an upstream service. Please try again later.")
    }

    @Test
    fun `should return 500 with an appropriate error response if LRS returns an InternalServerError`() {
      findLearnerByDemographicsApiMock.stubPostServerError()

      val actualResponse = webTestClient.post()
        .uri("/learners")
        .headers(setAuthorisation(roles = listOf("ROLE_TEMPLATE_KOTLIN__UI")))
        .bodyValue(findLearnerByDemographicsRequest)
        .accept(MediaType.parseMediaType("application/json"))
        .exchange()
        .expectStatus()
        .is5xxServerError
        .expectBody()
        .returnResult()
        .responseBody

      val actualResponseString = actualResponse?.toString(Charsets.UTF_8)
      assertThat(actualResponseString).contains("There was an error with an upstream service. Please try again later.")
    }

    @Test
    fun `should return OK and the correct response when LRS returns an exact match`() {
      findLearnerByDemographicsApiMock.stubExactMatch()

      val expectedResponse = FindLearnerByDemographicsResponse(
        searchParameters = findLearnerByDemographicsRequest,
        responseType = ResponseType.EXACT_MATCH,
        matchedLearners = listOf(learner),
      )

      val actualResponse = webTestClient.post()
        .uri("/learners")
        .headers(setAuthorisation(roles = listOf("ROLE_TEMPLATE_KOTLIN__UI")))
        .bodyValue(findLearnerByDemographicsRequest)
        .accept(MediaType.parseMediaType("application/json"))
        .exchange()
        .expectStatus()
        .isOk
        .expectBody()
        .returnResult()
        .responseBody

      val actualResponseString = actualResponse?.toString(Charsets.UTF_8)
      assertThat(actualResponseString).isEqualTo(gson.toJson(expectedResponse))
    }

    @Test
    fun `should return OK and the correct response when LRS returns a possible match with two learners`() {
      findLearnerByDemographicsApiMock.stubPossibleMatchTwoLearners()

      val expectedResponse = FindLearnerByDemographicsResponse(
        searchParameters = findLearnerByDemographicsRequest,
        responseType = ResponseType.POSSIBLE_MATCH,
        mismatchedFields = mutableMapOf(
          ("familyName" to mutableListOf("FN", "FN")),
          ("gender" to mutableListOf("2", "2")),
          ("givenName" to mutableListOf("GN", "GN")),
          ("lastKnownPostCode" to mutableListOf("CV49EA", "CV49EA")),
        ),
        matchedLearners = listOf(learner, learner),
      )

      val actualResponse = webTestClient.post()
        .uri("/learners")
        .headers(setAuthorisation(roles = listOf("ROLE_TEMPLATE_KOTLIN__UI")))
        .bodyValue(findLearnerByDemographicsRequest)
        .accept(MediaType.parseMediaType("application/json"))
        .exchange()
        .expectStatus()
        .isOk
        .expectBody()
        .returnResult()
        .responseBody

      val actualResponseString = actualResponse?.toString(Charsets.UTF_8)
      assertThat(actualResponseString).isEqualTo(gson.toJson(expectedResponse))
    }

    @Test
    fun `should return appropriate mismatched fields when there is a possible match`() {
      findLearnerByDemographicsApiMock.stubPossibleMatchTwoLearners()

      val requestWithTwoMismatches = findLearnerByDemographicsRequest.copy(
        givenName = "Mismatch",
        familyName = "Mismatch",
        lastKnownPostCode = "CV49EA",
        gender = 2,
      )

      val expectedResponse = FindLearnerByDemographicsResponse(
        searchParameters = findLearnerByDemographicsRequest.copy(
          givenName = "Mismatch",
          familyName = "Mismatch",
          lastKnownPostCode = "CV49EA",
          gender = 2,
        ),
        responseType = ResponseType.POSSIBLE_MATCH,
        mismatchedFields = mutableMapOf(
          ("familyName" to mutableListOf("FN", "FN")),
          ("givenName" to mutableListOf("GN", "GN")),
        ),
        matchedLearners = listOf(learner, learner),
      )

      val actualResponse = webTestClient.post()
        .uri("/learners")
        .headers(setAuthorisation(roles = listOf("ROLE_TEMPLATE_KOTLIN__UI")))
        .bodyValue(requestWithTwoMismatches)
        .accept(MediaType.parseMediaType("application/json"))
        .exchange()
        .expectStatus()
        .isOk
        .expectBody()
        .returnResult()
        .responseBody

      val actualResponseString = actualResponse?.toString(Charsets.UTF_8)
      assertThat(actualResponseString).isEqualTo(gson.toJson(expectedResponse))
    }

    @Test
    fun `should return OK and the correct response when LRS returns a no match response`() {
      findLearnerByDemographicsApiMock.stubNoMatch()

      val expectedResponse = FindLearnerByDemographicsResponse(
        searchParameters = findLearnerByDemographicsRequest,
        responseType = ResponseType.NO_MATCH,
      )

      val actualResponse = webTestClient.post()
        .uri("/learners")
        .headers(setAuthorisation(roles = listOf("ROLE_TEMPLATE_KOTLIN__UI")))
        .bodyValue(findLearnerByDemographicsRequest)
        .accept(MediaType.parseMediaType("application/json"))
        .exchange()
        .expectStatus()
        .isOk
        .expectBody()
        .returnResult()
        .responseBody

      val actualResponseString = actualResponse?.toString(Charsets.UTF_8)
      assertThat(actualResponseString).isEqualTo(gson.toJson(expectedResponse))
    }
  }

  val findLearnerByDemographicsRequest =
    uk.gov.justice.digital.hmpps.learnerrecordsapi.models.request.FindLearnerByDemographicsRequest(
      "Some",
      "Person",
      LocalDate.parse("2024-01-01"),
      1,
      "CV49EE",
    )

  val learner = Learner(
    createdDate = "2024-01-01",
    lastUpdatedDate = "2024-01-01",
    uln = "1234567890",
    versionNumber = "1",
    title = "Ms",
    givenName = "GN",
    middleOtherName = "MON",
    familyName = "FN",
    preferredGivenName = "PGN",
    previousFamilyName = "PFN",
    familyNameAtAge16 = "FNAA16",
    schoolAtAge16 = "SAA16",
    lastKnownAddressLine1 = "LKAL1",
    lastKnownAddressLine2 = "LKAL2",
    lastKnownAddressTown = "LKAT",
    lastKnownAddressCountyOrCity = "LKACOC",
    lastKnownPostCode = "CV49EA",
    dateOfAddressCapture = "2024-01-01",
    dateOfBirth = "2024-01-01",
    placeOfBirth = "POB",
    gender = "2",
    emailAddress = "email@example.com",
    nationality = "N",
    scottishCandidateNumber = "123456789",
    abilityToShare = "1",
    learnerStatus = "1",
    verificationType = "1",
    tierLevel = "0",
  )
}
