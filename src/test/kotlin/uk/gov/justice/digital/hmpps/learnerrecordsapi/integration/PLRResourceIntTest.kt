package uk.gov.justice.digital.hmpps.learnerrecordsapi.integration

import com.google.gson.GsonBuilder
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import uk.gov.justice.digital.hmpps.learnerrecordsapi.integration.wiremock.LRSApiExtension.Companion.lrsApiMock
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.gsonadapters.LocalDateAdapter
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.gsonadapters.ResponseTypeAdapter
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.lrsapi.response.LearningEvent
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.request.GetPLRByULNRequest
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.response.GetPLRByULNResponse
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.response.LRSResponseType
import java.time.LocalDate

class PLRResourceIntTest : IntegrationTestBase() {

  @Nested
  @DisplayName("POST /plr")
  inner class LearnersEndpoint {

    private val gson = GsonBuilder()
      .registerTypeAdapter(LocalDate::class.java, LocalDateAdapter().nullSafe())
      .registerTypeAdapter(LRSResponseType::class.java, ResponseTypeAdapter().nullSafe())
      .create()

    private val getLearningEventsRequest = GetPLRByULNRequest(
      "Some Given Name",
      "Some Family Name",
      "1234567890",
      null,
      null,
    )

    private fun actualResponse(
      request: GetPLRByULNRequest = getLearningEventsRequest,
      requestAsJson: String? = null,
      expectedStatus: Int = 200,
    ): String? {
      val executedRequest = webTestClient.post()
        .uri("/plr")
        .headers(setAuthorisation(roles = listOf("ROLE_TEMPLATE_KOTLIN__UI")))
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(requestAsJson ?: request)
        .accept(MediaType.parseMediaType("application/json"))
        .exchange()
        .expectStatus()

      return when (expectedStatus) {
        200 -> executedRequest.isOk.expectBody().returnResult().responseBody?.toString(Charsets.UTF_8)
        500 -> executedRequest.is5xxServerError.expectBody().returnResult().responseBody?.toString(Charsets.UTF_8)
        400 -> executedRequest.isBadRequest.expectBody().returnResult().responseBody?.toString(Charsets.UTF_8)
        else -> throw RuntimeException("Unhandled status code")
      }
    }

    @Test
    fun `should return 500 with an appropriate error response if LRS returns an InternalServerError`() {
      lrsApiMock.stubPostServerError()
      assertThat(actualResponse(expectedStatus = 500)).contains("LRS returned an error: MIAPAPIException")
    }

    @Test
    fun `should return 400 with an appropriate error response if any mandatory field is missing`() {
      val request = """
        {
          "givenName": "Some Given Name",
          "familyName": "Some Family Name",
          "uln": "1234567890",
        }
      """

      val scenarios = listOf(
        "\"givenName\": \"Some Given Name\",",
        "\"familyName\": \"Some Family Name\",",
        "\"uln\": \"1234567890\",",
      )

      for (jsonToRemove in scenarios) {
        assertThat(actualResponse(requestAsJson = request.replace(jsonToRemove, ""), expectedStatus = 400)).contains("JSON parse error")
      }
    }

    @Test
    fun `should return 400 with an appropriate error response if any field is invalid`() {
      val validRequest = """
        {
          "givenName": "Some",
          "familyName": "Person",
          "uln": "1234567890",
        }
      """

      val scenarios = listOf(
        "InvalidNameInvalidNameInvalidNameInvalidNameInvalidName" to "Some",
        "InvalidNameInvalidNameInvalidNameInvalidNameInvalidName" to "Person",
        "1234" to "1234567890",
      )

      for ((invalidValue, valueToReplace) in scenarios) {
        val requestWithInvalidField = validRequest.replace(valueToReplace, invalidValue)
        val response = actualResponse(requestAsJson = requestWithInvalidField, expectedStatus = 400).orEmpty()
        assertThat(response.contains("JSON parse error"))
      }
    }

    @Test
    fun `should return OK and the correct response when LRS returns an exact match for FULL type`() {
      lrsApiMock.stubLearningEventsExactMatchFull()

      val expectedResponse = GetPLRByULNResponse(
        getLearningEventsRequest,
        LRSResponseType.EXACT_MATCH,
        "1234567890",
        "1234567890",
        listOf(
          LearningEvent(
            id = "28538264",
            achievementProviderUkprn = "90000051",
            achievementProviderName = "TEST90000051",
            awardingOrganisationName = "Pearson Education Ltd",
            qualificationType = "",
            subjectCode = "K/501/5773",
            achievementAwardDate = "2010-01-01",
            credits = "2",
            source = "QCFU",
            dateLoaded = "2014-05-21 14:49:01",
            underDataChallenge = "N",
            level = "Entry Level",
            status = "F",
            subject = "Introduction to Construction Work: Entry 3",
            grade = "Pass",
            awardingOrganisationUkprn = "90000051",
          ),
        ),
      )

      assertThat(actualResponse()).isEqualTo(gson.toJson(expectedResponse))
    }

    @Test
    fun `should return OK and the correct response when LRS returns a linked learner response`() {
      lrsApiMock.stubLearningEventsLinkedMatchFull()

      val expectedResponse = GetPLRByULNResponse(
        getLearningEventsRequest,
        LRSResponseType.LINKED_LEARNER,
        "6666666666",
        "1234567890",
        listOf(
          LearningEvent(
            id = "4284",
            achievementProviderUkprn = "10032743",
            achievementProviderName = "TEST90000051",
            awardingOrganisationName = "UNKNOWN",
            qualificationType = "NVQ/GNVQ Key Skills Unit",
            subjectCode = "1000323X",
            achievementAwardDate = "2010-09-26",
            credits = "0",
            source = "ILR",
            dateLoaded = "2012-05-31 16:47:04",
            underDataChallenge = "N",
            level = "",
            status = "F",
            subject = "Key Skills in Application of Number - level 1",
            grade = "9999999999",
            awardingOrganisationUkprn = "UNKNWN",
            collectionType = "W",
            returnNumber = "02",
            participationStartDate = "2010-09-01",
            participationEndDate = "2010-09-26",
          ),
        ),
      )

      assertThat(actualResponse()).isEqualTo(gson.toJson(expectedResponse))
    }

    @Test
    fun `should return OK and the correct response when LRS returns a not shared response`() {
      lrsApiMock.stubLearningEventsNotShared()

      val expectedResponse = GetPLRByULNResponse(
        getLearningEventsRequest,
        LRSResponseType.NOT_SHARED,
        "",
        "1234567890",
        emptyList(),
      )

      assertThat(actualResponse()).isEqualTo(gson.toJson(expectedResponse))
    }

    @Test
    fun `should return OK and the correct response when LRS returns a not verified response`() {
      lrsApiMock.stubLearningEventsNotVerified()

      val expectedResponse = GetPLRByULNResponse(
        getLearningEventsRequest,
        LRSResponseType.NOT_VERIFIED,
        "",
        "1234567890",
        emptyList(),
      )

      assertThat(actualResponse()).isEqualTo(gson.toJson(expectedResponse))
    }
  }
}
