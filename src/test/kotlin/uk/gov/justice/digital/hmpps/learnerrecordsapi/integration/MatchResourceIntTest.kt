package uk.gov.justice.digital.hmpps.learnerrecordsapi.integration

import com.fasterxml.jackson.databind.ObjectMapper
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.mockito.Mockito.any
import org.mockito.Mockito.verify
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.request.ConfirmMatchRequest
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.response.MatchResponse
import uk.gov.justice.digital.hmpps.learnerrecordsapi.service.MatchService

class MatchResourceIntTest : IntegrationTestBase() {

  @Autowired
  lateinit var objectMapper: ObjectMapper

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
        .isOk
        .expectBody()
        .returnResult()
        .responseBody,
      MatchResponse::class.java,
    )

    val expectedSavedMatchEntity = confirmMatchRequest.asMatchEntity().copy(id = 1)
    val expectedResponse = MatchResponse("Match confirmed successfully", expectedSavedMatchEntity)

    assertThat(actualResponse).isEqualTo(expectedResponse)
  }
}
