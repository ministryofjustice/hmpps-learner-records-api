package uk.gov.justice.digital.hmpps.learnerrecordsapi.integration

import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.never
import org.mockito.kotlin.reset
import org.mockito.kotlin.spy
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.test.web.reactive.server.WebTestClient
import software.amazon.awssdk.services.sqs.model.PurgeQueueRequest
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest
import uk.gov.justice.digital.hmpps.learnerrecordsapi.config.HmppsBoldLrsExceptionHandler
import uk.gov.justice.digital.hmpps.learnerrecordsapi.config.Roles.ROLE_LEARNERS_RO
import uk.gov.justice.digital.hmpps.learnerrecordsapi.config.Roles.ROLE_LEARNERS_UI
import uk.gov.justice.digital.hmpps.learnerrecordsapi.integration.wiremock.LRSApiExtension.Companion.lrsApiMock
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.db.MatchEntity
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.request.ConfirmMatchRequest
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.request.Gender
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.response.CheckMatchResponse
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.response.CheckMatchStatus
import uk.gov.justice.digital.hmpps.learnerrecordsapi.repository.MatchRepository
import uk.gov.justice.digital.hmpps.learnerrecordsapi.service.MatchService
import uk.gov.justice.hmpps.sqs.HmppsQueueService
import uk.gov.justice.hmpps.sqs.MissingQueueException
import uk.gov.justice.hmpps.sqs.audit.HmppsAuditEvent
import java.time.Instant

@TestConfiguration
class MockitoSpyConfig {
  @Autowired
  lateinit var matchRepository: MatchRepository

  @Bean
  fun matchService(): MatchService = spy(MatchService(matchRepository))
}

@Import(MockitoSpyConfig::class)
class MatchResourceIntTest : IntegrationTestBase() {

  @Autowired
  protected lateinit var objectMapper: ObjectMapper

  @Autowired
  lateinit var matchRepository: MatchRepository

  @Autowired
  protected lateinit var matchService: MatchService

  @Autowired
  protected lateinit var hmppsQueueService: HmppsQueueService

  private val auditQueue by lazy {
    hmppsQueueService.findByQueueId("audit") ?: throw MissingQueueException("HmppsQueue audit not found")
  }
  protected val auditSqsClient by lazy { auditQueue.sqsClient }
  protected val auditQueueUrl by lazy { auditQueue.queueUrl }

  val nomisId = "A1234BC"
  val matchedUln = "A"
  val givenName = "John"
  val familyName = "Smith"
  val dateOfBirth = "1990-01-01"
  val gender = "MALE"

  private fun checkGetWebCall(
    nomisId: String,
    expectedResponseStatus: Int,
    expectedStatus: CheckMatchStatus,
    expectedUln: String? = null,
    expectedGivenName: String? = null,
    expectedFamilyName: String? = null,
    expectedDateOfBirth: String? = null,
    expectedGender: String? = null,
  ) {
    listOf(ROLE_LEARNERS_RO, ROLE_LEARNERS_UI).forEach { role ->
      val executedRequest = webTestClient.get()
        .uri("/match/$nomisId")
        .headers(setAuthorisation(roles = listOf(role)))
        .header("X-Username", "TestUser")
        .accept(MediaType.parseMediaType("application/json"))
        .exchange()
        .expectStatus()

      val checkMatchResponse = objectMapper.readValue(
        executedRequest
          .isEqualTo(expectedResponseStatus)
          .expectBody()
          .returnResult()
          .responseBody?.toString(Charsets.UTF_8),
        CheckMatchResponse::class.java,
      )
      assertThat(checkMatchResponse.status).isEqualTo(expectedStatus)
      if (expectedUln != null) {
        assertThat(checkMatchResponse.matchedUln).isEqualTo(expectedUln)
        assertThat(checkMatchResponse.givenName).isEqualTo(expectedGivenName)
        assertThat(checkMatchResponse.familyName).isEqualTo(expectedFamilyName)
        assertThat(checkMatchResponse.dateOfBirth).isEqualTo(expectedDateOfBirth)
        assertThat(checkMatchResponse.gender).isEqualTo(expectedGender)
      }
    }
  }

  private fun postMatch(nomisId: String, uln: String, expectedStatus: Int): WebTestClient.ResponseSpec = webTestClient.post()
    .uri("/match/$nomisId")
    .headers(setAuthorisation(roles = listOf(ROLE_LEARNERS_UI)))
    .header("X-Username", "TestUser")
    .bodyValue(ConfirmMatchRequest(uln, givenName, familyName, dateOfBirth, gender))
    .accept(MediaType.parseMediaType("application/json"))
    .exchange()
    .expectStatus()
    .isEqualTo(expectedStatus)

  @AfterEach
  fun cleanup() {
    matchRepository.deleteAll()
    reset(matchService)
  }

  @Test
  fun `GET match should find a match by id`() {
    matchRepository.save(
      MatchEntity(
        null,
        nomisId,
        matchedUln,
        givenName,
        familyName,
        dateOfBirth,
        gender,
      ),
    )

    checkGetWebCall(
      nomisId,
      200,
      CheckMatchStatus.Found,
      matchedUln,
      givenName,
      familyName,
      dateOfBirth,
      gender,
    )
  }

  @Test
  fun `GET match should return NOT_FOUND if no match`() {
    checkGetWebCall(
      nomisId,
      404,
      CheckMatchStatus.NotFound,
    )
  }

  @Test
  fun `GET match should return no match if record marked as such`() {
    matchRepository.save(MatchEntity(null, nomisId, "", "John", "Smith"))
    checkGetWebCall(
      nomisId,
      200,
      CheckMatchStatus.NoMatch,
    )
  }

  @Test
  fun `POST to confirm match should return 201 CREATED with a response confirming a match`() {
    val (nomisId, uln) = arrayOf("A1417AE", "1234567890")
    val actualResponse = postMatch(nomisId, uln, 201)
    verify(matchService, times(1)).saveMatch(any(), any())
    actualResponse.expectStatus().isCreated
  }

  @Test
  fun `POST to confirm match should return 400 if ULN is malformed`() {
    val (nomisId, uln) = arrayOf("A1417AE", "1234567890abcdef")
    val actualResponse = objectMapper.readValue(
      postMatch(nomisId, uln, 400).expectBody().returnResult().responseBody,
      HmppsBoldLrsExceptionHandler.ErrorResponse::class.java,
    )

    val expectedError = HmppsBoldLrsExceptionHandler.ErrorResponse(
      HttpStatus.BAD_REQUEST,
      errorCode = "Validation Failed",
      userMessage = "Please correct the error and retry",
      developerMessage = "Validation(s) failed for [matchingUln]",
      moreInfo = "Validation(s) failed for [matchingUln] with reason(s): [must match \"^[0-9]{1,10}\$\"]",
    )

    verify(matchService, never()).saveMatch(any(), any())
    assertThat(actualResponse).isEqualTo(expectedError)
  }

  @Test
  fun `POST to confirm match should return 500 if match service fails to save`() {
    val (nomisId, uln) = arrayOf("A1417AE", "1234567890")
    doThrow(RuntimeException("Database error")).`when`(matchService).saveMatch(any(), any())
    val actualResponse = objectMapper.readValue(
      postMatch(nomisId, uln, 500).expectBody().returnResult().responseBody,
      HmppsBoldLrsExceptionHandler.ErrorResponse::class.java,
    )

    val expectedError = HmppsBoldLrsExceptionHandler.ErrorResponse(
      HttpStatus.INTERNAL_SERVER_ERROR,
      errorCode = "Unexpected error",
      userMessage = "Unexpected error: Database error",
      developerMessage = "Unexpected error: Database error",
      moreInfo = "Unexpected error",
    )

    verify(matchService, times(1)).saveMatch(any(), any())
    assertThat(actualResponse).isEqualTo(expectedError)
  }

  @Test
  fun `should emit an event that request is received for find Learning Events by Nomis ID `() {
    lrsApiMock.stubLearningEventsLinkedMatchFull()

    auditSqsClient.purgeQueue(
      PurgeQueueRequest.builder()
        .queueUrl(auditQueueUrl)
        .build(),
    )

    matchRepository.save(
      MatchEntity(
        null,
        "123456",
        "1234567890",
        "Some Given Name",
        "Some Family Name",
        null,
        Gender.MALE.toString(),
      ),
    )

    webTestClient.get()
      .uri("/match/{nomisId}/learner-events", "123456")
      .headers(setAuthorisation(roles = listOf(ROLE_LEARNERS_RO)))
      .header("X-Username", "TestUser")
      .accept(MediaType.parseMediaType("application/json"))
      .exchange()
      .expectStatus()
      .is2xxSuccessful
      .expectBody()
      .returnResult()
      .responseBody

    val receivedEvent = objectMapper.readValue(
      auditSqsClient.receiveMessage(
        ReceiveMessageRequest.builder().queueUrl(auditQueueUrl).build(),
      ).get().messages()[0].body(),
      HmppsAuditEvent::class.java,
    )

    assertThat(receivedEvent.what).isEqualTo("SEARCH_LEARNER_EVENTS_BY_NOMISID")
    assertThat(receivedEvent.who).isEqualTo("TestUser")
    assertThat(receivedEvent.service).isEqualTo("hmpps-learner-records-api")
    assertThat(receivedEvent.`when`).isBeforeOrEqualTo(Instant.now())
  }
}
