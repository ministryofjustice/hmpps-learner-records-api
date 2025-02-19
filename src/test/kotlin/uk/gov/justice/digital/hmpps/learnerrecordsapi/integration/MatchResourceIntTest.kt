package uk.gov.justice.digital.hmpps.learnerrecordsapi.integration

import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.doThrow
import org.mockito.Mockito.never
import org.mockito.Mockito.reset
import org.mockito.Mockito.spy
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.kotlin.any
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import uk.gov.justice.digital.hmpps.learnerrecordsapi.config.HmppsBoldLrsExceptionHandler
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.request.ConfirmMatchRequest
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.response.MatchResponse
import uk.gov.justice.digital.hmpps.learnerrecordsapi.repository.MatchRepository
import uk.gov.justice.digital.hmpps.learnerrecordsapi.service.MatchService

class MatchResourceIntTest : IntegrationTestBase() {

  @TestConfiguration
  class MockitoSpyConfig {

    @Autowired
    lateinit var matchRepository: MatchRepository

    @Bean
    fun matchService(): MatchService = spy(MatchService(matchRepository))
  }

  @Autowired
  lateinit var objectMapper: ObjectMapper

  @Autowired
  lateinit var spiedMatchService: MatchService

  @BeforeEach
  fun setUp() {
    reset(spiedMatchService)
  }

  @Test
  fun `POST to confirm match should return 200 with a response confirming a match`() {
    val confirmMatchRequest = ConfirmMatchRequest("A1417AE", "1234567890")

    val actualResponse = objectMapper.readValue(
      webTestClient.post()
        .uri("/match/confirm")
        .headers(setAuthorisation(roles = listOf("ROLE_LEARNER_RECORDS_SEARCH__RO")))
        .header("X-Username", "TestUser")
        .bodyValue(confirmMatchRequest)
        .accept(MediaType.parseMediaType("application/json"))
        .exchange()
        .expectStatus()
        .isOk
        .expectBody()
        .returnResult()
        .responseBody,
      MatchResponse::class.java,
    )

    val expectedSavedMatchEntity = confirmMatchRequest.asMatchEntity().copy(id = 1)
    val expectedResponse = MatchResponse("Match confirmed successfully", expectedSavedMatchEntity)

    verify(spiedMatchService, times(1)).saveMatch(any())
    assertThat(actualResponse).isEqualTo(expectedResponse)
  }

  @Test
  fun `POST to confirm match should return 400 if nomis id is malformed`() {
    val confirmMatchRequest = ConfirmMatchRequest("ABCDEFGH", "1234567890")

    val actualResponse = objectMapper.readValue(
      webTestClient.post()
        .uri("/match/confirm")
        .headers(setAuthorisation(roles = listOf("ROLE_LEARNER_RECORDS_SEARCH__RO")))
        .header("X-Username", "TestUser")
        .bodyValue(confirmMatchRequest)
        .accept(MediaType.parseMediaType("application/json"))
        .exchange()
        .expectStatus()
        .isBadRequest
        .expectBody()
        .returnResult()
        .responseBody,
      HmppsBoldLrsExceptionHandler.ErrorResponse::class.java,
    )

    val expectedError = HmppsBoldLrsExceptionHandler.ErrorResponse(
      HttpStatus.BAD_REQUEST,
      errorCode = "Validation Failed",
      userMessage = "Please correct the error and retry",
      developerMessage = "Validation(s) failed for [nomisId]",
      moreInfo = "Validation(s) failed for [nomisId] with reason(s): [must match \"^[A-Z]\\d{4}[A-Z]{2}\$\"]",
    )

    verify(spiedMatchService, never()).saveMatch(any())
    assertThat(actualResponse).isEqualTo(expectedError)
  }

  @Test
  fun `POST to confirm match should return 400 if uln is malformed`() {
    val confirmMatchRequest = ConfirmMatchRequest("A1417AE", "1234567890abcdef")

    val actualResponse = objectMapper.readValue(
      webTestClient.post()
        .uri("/match/confirm")
        .headers(setAuthorisation(roles = listOf("ROLE_LEARNER_RECORDS_SEARCH__RO")))
        .header("X-Username", "TestUser")
        .bodyValue(confirmMatchRequest)
        .accept(MediaType.parseMediaType("application/json"))
        .exchange()
        .expectStatus()
        .isBadRequest
        .expectBody()
        .returnResult()
        .responseBody,
      HmppsBoldLrsExceptionHandler.ErrorResponse::class.java,
    )

    val expectedError = HmppsBoldLrsExceptionHandler.ErrorResponse(
      HttpStatus.BAD_REQUEST,
      errorCode = "Validation Failed",
      userMessage = "Please correct the error and retry",
      developerMessage = "Validation(s) failed for [matchingUln]",
      moreInfo = "Validation(s) failed for [matchingUln] with reason(s): [must match \"^[0-9]{1,10}\$\"]",
    )

    verify(spiedMatchService, never()).saveMatch(any())
    assertThat(actualResponse).isEqualTo(expectedError)
  }

  @Test
  fun `POST to confirm match should return 500 if match service fails to save`() {
    val confirmMatchRequest = ConfirmMatchRequest("A1417AE", "1234567890")

    doThrow(RuntimeException("Database error")).`when`(spiedMatchService).saveMatch(any())

    val actualResponse = objectMapper.readValue(
      webTestClient.post()
        .uri("/match/confirm")
        .headers(setAuthorisation(roles = listOf("ROLE_LEARNER_RECORDS_SEARCH__RO")))
        .header("X-Username", "TestUser")
        .bodyValue(confirmMatchRequest)
        .accept(MediaType.parseMediaType("application/json"))
        .exchange()
        .expectStatus()
        .is5xxServerError
        .expectBody()
        .returnResult()
        .responseBody,
      HmppsBoldLrsExceptionHandler.ErrorResponse::class.java,
    )

    val expectedError = HmppsBoldLrsExceptionHandler.ErrorResponse(
      HttpStatus.INTERNAL_SERVER_ERROR,
      errorCode = "Unexpected error",
      userMessage = "Unexpected error: Database error",
      developerMessage = "Unexpected error: Database error",
      moreInfo = "Unexpected error",
    )

    verify(spiedMatchService, times(1)).saveMatch(any())
    assertThat(actualResponse).isEqualTo(expectedError)
  }
}
