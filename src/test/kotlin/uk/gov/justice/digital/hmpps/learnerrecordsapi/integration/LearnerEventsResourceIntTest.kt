package uk.gov.justice.digital.hmpps.learnerrecordsapi.integration

import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import uk.gov.justice.digital.hmpps.learnerrecordsapi.config.HmppsBoldLrsExceptionHandler
import uk.gov.justice.digital.hmpps.learnerrecordsapi.config.Roles.ROLE_LEARNERS_UI
import uk.gov.justice.digital.hmpps.learnerrecordsapi.integration.wiremock.LRSApiExtension.Companion.lrsApiMock
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.lrsapi.response.LearningEvent
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.request.Gender
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.request.LearnerEventsRequest
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.response.LRSResponseType
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.response.LearnerEventsResponse
import uk.gov.justice.digital.hmpps.learnerrecordsapi.repository.MatchRepository
import uk.gov.justice.hmpps.sqs.HmppsQueueService
import uk.gov.justice.hmpps.sqs.MissingQueueException

class LearnerEventsResourceIntTest : IntegrationTestBase() {

  @Autowired
  lateinit var objectMapper: ObjectMapper

  @Autowired
  protected lateinit var hmppsQueueService: HmppsQueueService

  @Autowired
  lateinit var matchRepository: MatchRepository

  private val auditQueue by lazy {
    hmppsQueueService.findByQueueId("audit") ?: throw MissingQueueException("HmppsQueue audit not found")
  }
  protected val auditSqsClient by lazy { auditQueue.sqsClient }
  protected val auditQueueUrl by lazy { auditQueue.queueUrl }

  @Nested
  @DisplayName("POST /learner-events")
  inner class LearnersEndpoint {

    @Test
    fun `should return 500 with an appropriate error response if LRS returns an InternalServerError`() {
      lrsApiMock.stubPostServerError()

      val actualResponse = objectMapper.readValue(
        webTestClient.post()
          .uri("/learner-events")
          .headers(setAuthorisation(roles = listOf(ROLE_LEARNERS_UI)))
          .header("X-Username", "TestUser")
          .bodyValue(getLearningEventsRequest)
          .accept(MediaType.parseMediaType("application/json"))
          .exchange()
          .expectStatus()
          .is5xxServerError
          .expectBody()
          .returnResult()
          .responseBody,
        HmppsBoldLrsExceptionHandler.ErrorResponse::class.java,
      )

      assertThat(actualResponse.toString()).contains("LRS returned an error: MIAPAPIException")
    }

    @Test
    fun `should return OK and the correct response when LRS returns an exact match for FULL type`() {
      lrsApiMock.stubLearningEventsExactMatchFull()

      val expectedResponse = LearnerEventsResponse(
        getLearningEventsRequest,
        LRSResponseType.EXACT_MATCH,
        "1234567890",
        "1234567890",
        listOf(
          LearningEvent(
            id = "28538264",
            achievementProviderUkprn = "90000051",
            achievementProviderName = "TEST90000051",
            awardingOrganisationName = "Test Provider Ltd",
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

      val actualResponse = objectMapper.readValue(
        webTestClient.post()
          .uri("/learner-events")
          .headers(setAuthorisation(roles = listOf(ROLE_LEARNERS_UI)))
          .header("X-Username", "TestUser")
          .bodyValue(getLearningEventsRequest)
          .accept(MediaType.parseMediaType("application/json"))
          .exchange()
          .expectStatus()
          .isOk
          .expectBody()
          .returnResult()
          .responseBody,
        LearnerEventsResponse::class.java,
      )

      assertThat(actualResponse).usingRecursiveComparison().isEqualTo(expectedResponse)
    }

    @Test
    fun `should return OK and the correct response when LRS returns a linked learner response`() {
      lrsApiMock.stubLearningEventsLinkedMatchFull()

      val expectedResponse = LearnerEventsResponse(
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

      val actualResponse = objectMapper.readValue(
        webTestClient.post()
          .uri("/learner-events")
          .headers(setAuthorisation(roles = listOf(ROLE_LEARNERS_UI)))
          .header("X-Username", "TestUser")
          .bodyValue(getLearningEventsRequest)
          .accept(MediaType.parseMediaType("application/json"))
          .exchange()
          .expectStatus()
          .isOk
          .expectBody()
          .returnResult()
          .responseBody,
        LearnerEventsResponse::class.java,
      )

      assertThat(actualResponse).usingRecursiveComparison().isEqualTo(expectedResponse)
    }

    @Test
    fun `should return OK and the correct response when LRS returns a not shared response`() {
      lrsApiMock.stubLearningEventsNotShared()

      val expectedResponse = LearnerEventsResponse(
        getLearningEventsRequest,
        LRSResponseType.NOT_SHARED,
        "",
        "1234567890",
        emptyList(),
      )

      val actualResponse = objectMapper.readValue(
        webTestClient.post()
          .uri("/learner-events")
          .headers(setAuthorisation(roles = listOf(ROLE_LEARNERS_UI)))
          .header("X-Username", "TestUser")
          .bodyValue(getLearningEventsRequest)
          .accept(MediaType.parseMediaType("application/json"))
          .exchange()
          .expectStatus()
          .isOk
          .expectBody()
          .returnResult()
          .responseBody,
        LearnerEventsResponse::class.java,
      )

      assertThat(actualResponse).usingRecursiveComparison().isEqualTo(expectedResponse)
    }

    @Test
    fun `should return OK and the correct response when LRS returns a not verified response`() {
      lrsApiMock.stubLearningEventsNotVerified()

      val expectedResponse = LearnerEventsResponse(
        getLearningEventsRequest,
        LRSResponseType.NOT_VERIFIED,
        "",
        "1234567890",
        emptyList(),
      )

      val actualResponse = objectMapper.readValue(
        webTestClient.post()
          .uri("/learner-events")
          .headers(setAuthorisation(roles = listOf(ROLE_LEARNERS_UI)))
          .header("X-Username", "TestUser")
          .bodyValue(getLearningEventsRequest)
          .accept(MediaType.parseMediaType("application/json"))
          .exchange()
          .expectStatus()
          .isOk
          .expectBody()
          .returnResult()
          .responseBody,
        LearnerEventsResponse::class.java,
      )

      assertThat(actualResponse).usingRecursiveComparison().isEqualTo(expectedResponse)
    }

    @Test
    fun `should return 400 with an appropriate error response if X-Username header is missing`() {
      lrsApiMock.stubLearningEventsExactMatchFull()

      val actualResponse = objectMapper.readValue(
        webTestClient.post()
          .uri("/learner-events")
          .headers(setAuthorisation(roles = listOf(ROLE_LEARNERS_UI)))
          .bodyValue(getLearningEventsRequest)
          .accept(MediaType.parseMediaType("application/json"))
          .exchange()
          .expectStatus()
          .is4xxClientError
          .expectBody()
          .returnResult()
          .responseBody,
        HmppsBoldLrsExceptionHandler.ErrorResponse::class.java,
      )

      val actualResponseString = actualResponse?.toString()
      assertThat(actualResponseString).contains("Missing X-Username Header")
    }

    @Test
    fun `should return 400 with an appropriate error response if additional unknown parameters are passed`() {
      lrsApiMock.stubLearningEventsExactMatchFull()
      val extendedRequestBody = mutableMapOf<String, String>()
      extendedRequestBody["givenName"] = "TestOne"
      extendedRequestBody["familyName"] = "TesterOne"
      extendedRequestBody["uln"] = "1174112637"
      extendedRequestBody["unknownValue"] = "1234"

      val actualResponse = objectMapper.readValue(
        webTestClient.post()
          .uri("/learner-events")
          .headers(setAuthorisation(roles = listOf(ROLE_LEARNERS_UI)))
          .header("X-Username", "TestUser")
          .bodyValue(extendedRequestBody)
          .accept(MediaType.parseMediaType("application/json"))
          .exchange()
          .expectStatus()
          .is4xxClientError
          .expectBody()
          .returnResult()
          .responseBody,
        HmppsBoldLrsExceptionHandler.ErrorResponse::class.java,
      )

      val actualResponseString = actualResponse?.toString()
      assertThat(actualResponseString).contains("Unrecognized field \"unknownValue\"")
    }
  }

  val getLearningEventsRequest = LearnerEventsRequest(
    "Some Given Name",
    "Some Family Name",
    "1234567890",
    null,
    Gender.MALE,
  )
}
