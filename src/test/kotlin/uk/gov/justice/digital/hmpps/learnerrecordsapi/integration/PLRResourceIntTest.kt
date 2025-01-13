package uk.gov.justice.digital.hmpps.learnerrecordsapi.integration

import com.google.gson.GsonBuilder
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import uk.gov.justice.digital.hmpps.learnerrecordsapi.integration.wiremock.GetLearningEventsApiExtension.Companion.getLearningEventsApiMock
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.gsonadapters.LocalDateAdapter
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.gsonadapters.ResponseTypeAdapter
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.lrsapi.response.LearningEvent
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.lrsapi.response.LearningEventsResult
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.request.GetPLRByULNRequest
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.response.ResponseType
import java.time.LocalDate

class PLRResourceIntTest : IntegrationTestBase() {

  @Nested
  @DisplayName("POST /plr")
  inner class LearnersEndpoint {

    val gson = GsonBuilder()
      .registerTypeAdapter(LocalDate::class.java, LocalDateAdapter().nullSafe())
      .registerTypeAdapter(ResponseType::class.java, ResponseTypeAdapter().nullSafe())
      .create()

    @Test
    fun `should return 500 with an appropriate error response if LRS returns a BadRequest`() {
      getLearningEventsApiMock.stubPostBadRequest()

      val actualResponse = webTestClient.post()
        .uri("/plr")
        .headers(setAuthorisation(roles = listOf("ROLE_TEMPLATE_KOTLIN__UI")))
        .bodyValue(getLearningEventsRequest)
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
      getLearningEventsApiMock.stubPostServerError()

      val actualResponse = webTestClient.post()
        .uri("/plr")
        .headers(setAuthorisation(roles = listOf("ROLE_TEMPLATE_KOTLIN__UI")))
        .bodyValue(getLearningEventsRequest)
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
    fun `should return OK and the correct response when LRS returns an exact match for FULL type`() {
      getLearningEventsApiMock.stubExactMatchFull()

      val expectedResponse = LearningEventsResult(
        "WSRC0004",
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

      val actualResponse = webTestClient.post()
        .uri("/plr")
        .headers(setAuthorisation(roles = listOf("ROLE_TEMPLATE_KOTLIN__UI")))
        .bodyValue(getLearningEventsRequest)
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
    fun `should return OK and the correct response when LRS returns a linked learner response`() {
      getLearningEventsApiMock.stubLinkedMatchFull()

      val expectedResponse = LearningEventsResult(
        "WSRC0022",
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

      val actualResponse = webTestClient.post()
        .uri("/plr")
        .headers(setAuthorisation(roles = listOf("ROLE_TEMPLATE_KOTLIN__UI")))
        .bodyValue(getLearningEventsRequest)
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
    fun `should return OK and the correct response when LRS returns a not shared response`() {
      getLearningEventsApiMock.stubNotShared()

      val expectedResponse = LearningEventsResult(
        "WSEC0206",
        "",
        "1234567890",
        emptyList(),
      )

      val actualResponse = webTestClient.post()
        .uri("/plr")
        .headers(setAuthorisation(roles = listOf("ROLE_TEMPLATE_KOTLIN__UI")))
        .bodyValue(getLearningEventsRequest)
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
    fun `should return OK and the correct response when LRS returns a not verified response`() {
      getLearningEventsApiMock.stubNotVerified()

      val expectedResponse = LearningEventsResult(
        "WSEC0208",
        "",
        "1234567890",
        emptyList(),
      )

      val actualResponse = webTestClient.post()
        .uri("/plr")
        .headers(setAuthorisation(roles = listOf("ROLE_TEMPLATE_KOTLIN__UI")))
        .bodyValue(getLearningEventsRequest)
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

  val getLearningEventsRequest = GetPLRByULNRequest(
    "Some Given Name",
    "Some Family Name",
    "1234567890",
    null,
    null,
  )
}
