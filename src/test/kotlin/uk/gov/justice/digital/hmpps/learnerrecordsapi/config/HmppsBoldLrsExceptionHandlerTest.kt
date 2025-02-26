package uk.gov.justice.digital.hmpps.learnerrecordsapi.config

import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import uk.gov.justice.digital.hmpps.learnerrecordsapi.integration.IntegrationTestBase
import java.time.Duration

// Tests that when exceptions are thrown, the exception handler will pick them up and behave correctly.
// Test endpoints that throw exceptions are found in TestExceptionResource in this same package.

class HmppsBoldLrsExceptionHandlerTest : IntegrationTestBase() {

  @Autowired
  lateinit var objectMapper: ObjectMapper

  @BeforeEach
  fun setUp() {
    webTestClient = webTestClient.mutate()
      .responseTimeout(Duration.ofMillis(180000))
      .build()
  }

  private fun testExceptionHandling(
    uri: String,
    expectedResponse: HmppsBoldLrsExceptionHandler.ErrorResponse,
    expectedStatus: HttpStatus,
  ) {
    val actualResponse = objectMapper.readValue(
      webTestClient.post()
        .uri(uri)
        .headers(setAuthorisation(roles = listOf("ROLE_LEARNER_RECORDS_SEARCH__RO")))
        .exchange()
        .expectStatus()
        .isEqualTo(expectedStatus)
        .expectBody()
        .returnResult()
        .responseBody,
      HmppsBoldLrsExceptionHandler.ErrorResponse::class.java,
    )

    assertThat(actualResponse).isEqualTo(expectedResponse)
  }

  @Test
  fun `should catch validation exceptions (MethodArgumentNotValidException) and return BadRequest`() {
    val expectedResponse = HmppsBoldLrsExceptionHandler.ErrorResponse(
      HttpStatus.BAD_REQUEST,
      "Validation Failed",
      "Please correct the error and retry",
      "Validation(s) failed for [testField]",
      "Validation(s) failed for [testField] with reason(s): [Validation failed]",
    )

    testExceptionHandling("/test/validation", expectedResponse, expectedStatus = HttpStatus.BAD_REQUEST)
  }

  @Test
  fun `should catch No Resource exceptions (NoResourceFoundException) and return Not Found`() {
    val expectedResponse = HmppsBoldLrsExceptionHandler.ErrorResponse(
      HttpStatus.NOT_FOUND,
      "No Resource Found",
      "No resource found failure: No static resource someUnknownEndpoint.",
      "Requested Resource not found on the server",
      moreInfo = "Requested Resource not found on the server",
    )

    testExceptionHandling("/someUnknownEndpoint", expectedResponse, expectedStatus = HttpStatus.NOT_FOUND)
  }

  @Test
  fun `should catch missing mandatory field exceptions (HttpMessageNotReadableException) and return BadRequest`() {
    val expectedResponse = HmppsBoldLrsExceptionHandler.ErrorResponse(
      HttpStatus.BAD_REQUEST,
      "Unreadable HTTP message",
      "Unreadable HTTP message",
      "JSON parse error: Missing Field",
      "Unreadable HTTP message",
    )

    testExceptionHandling("/test/missing-mandatory-field", expectedResponse, expectedStatus = HttpStatus.BAD_REQUEST)
  }

  @Test
  fun `should catch exceptions thrown when communicating with LRS (LRSException) and return Internal Server Error`() {
    val expectedResponse = HmppsBoldLrsExceptionHandler.ErrorResponse(
      HttpStatus.INTERNAL_SERVER_ERROR,
      "LRS Error",
      "LRS returned an error without detail",
      "LRS returned an error without detail",
      "LRS Error",
    )

    testExceptionHandling("/test/lrs-error", expectedResponse, expectedStatus = HttpStatus.INTERNAL_SERVER_ERROR)
  }

  @Test
  fun `should catch forbidden exceptions (AccessDeniedException) and return FORBIDDEN`() {
    val expectedResponse = HmppsBoldLrsExceptionHandler.ErrorResponse(
      HttpStatus.FORBIDDEN,
      "Forbidden - Access Denied",
      "Forbidden: ",
      "Forbidden - Access Denied",
      "Forbidden - Access Denied",
    )

    testExceptionHandling("/test/forbidden", expectedResponse, expectedStatus = HttpStatus.FORBIDDEN)
  }

  @Test
  fun `should catch forbidden exceptions (AuthorizationDeniedException) and return FORBIDDEN`() {
    val expectedResponse = HmppsBoldLrsExceptionHandler.ErrorResponse(
      HttpStatus.FORBIDDEN,
      "Forbidden - Authorization Denied",
      "Forbidden: ",
      "Forbidden - Authorization Denied",
      "Forbidden - Authorization Denied",
    )

    testExceptionHandling("/test/authorization-forbidden", expectedResponse, expectedStatus = HttpStatus.FORBIDDEN)
  }

  @Test
  fun `should catch timeout exceptions (SocketTimeoutException) and return Request Timeout`() {
    val expectedResponse = HmppsBoldLrsExceptionHandler.ErrorResponse(
      status = HttpStatus.REQUEST_TIMEOUT,
      errorCode = "Request Timeout",
      userMessage = "A request to an upstream service timed out.",
      developerMessage = "dev message can vary",
      moreInfo = "A request timed out while waiting for a response from an upstream service.",
    )

    val actualResponse = objectMapper.readValue(
      webTestClient.post()
        .uri("/test/okhttp-timeout")
        .headers(setAuthorisation(roles = listOf("ROLE_LEARNER_RECORDS_SEARCH__RO")))
        .exchange()
        .expectStatus()
        .isEqualTo(HttpStatus.REQUEST_TIMEOUT)
        .expectBody()
        .returnResult()
        .responseBody,
      HmppsBoldLrsExceptionHandler.ErrorResponse::class.java,
    )

    assertThat(actualResponse.copy(developerMessage = "dev message can vary")).isEqualTo(expectedResponse)
  }

  @Test
  fun `should catch generic exceptions and return Internal Server Error`() {
    val expectedResponse = HmppsBoldLrsExceptionHandler.ErrorResponse(
      HttpStatus.INTERNAL_SERVER_ERROR,
      "Unexpected error",
      "Unexpected error: null",
      "Unexpected error: null",
      "Unexpected error",
    )

    testExceptionHandling("/test/generic-exception", expectedResponse, expectedStatus = HttpStatus.INTERNAL_SERVER_ERROR)
  }

  @Test
  fun `should catch Method Not Supported exception and return Method Not Supported Error`() {
    val expectedResponse = HmppsBoldLrsExceptionHandler.ErrorResponse(
      HttpStatus.METHOD_NOT_ALLOWED,
      "Method (GET) not allowed",
      "HTTP Method (GET) is not allowed",
      "HTTP Method (GET) is not allowed, use only (POST) method",
      "HTTP Method (GET) is not allowed, use only this (POST) method",
    )
    testExceptionHandling("/test/unsupported-http-verb", expectedResponse, expectedStatus = HttpStatus.METHOD_NOT_ALLOWED)
  }

  @Test
  fun `should catch DfE maintenance time exceptions and return Failed Dependency Error`() {
    val expectedResponse = HmppsBoldLrsExceptionHandler.ErrorResponse(
      HttpStatus.FAILED_DEPENDENCY,
      "DfE API failed to Respond",
      "LRS API Dependency Failed - DfE API is under maintenance",
      "LRS API Dependency Failed - DfE API is under maintenance, please check DfE API maintenance window for more details",
      "LRS API Dependency Failed - DfE API is under maintenance",
    )
    testExceptionHandling("/test/test-dfe-api-down", expectedResponse, expectedStatus = HttpStatus.FAILED_DEPENDENCY)
  }
}
