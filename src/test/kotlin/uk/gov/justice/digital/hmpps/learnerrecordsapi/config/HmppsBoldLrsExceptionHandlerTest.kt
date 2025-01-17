package uk.gov.justice.digital.hmpps.learnerrecordsapi.config

import com.google.gson.GsonBuilder
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import uk.gov.justice.digital.hmpps.learnerrecordsapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.gsonadapters.LocalDateAdapter
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.gsonadapters.ResponseTypeAdapter
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.response.LRSResponseType
import java.time.LocalDate

class HmppsBoldLrsExceptionHandlerTest : IntegrationTestBase() {

  val gson = GsonBuilder()
    .registerTypeAdapter(LocalDate::class.java, LocalDateAdapter().nullSafe())
    .registerTypeAdapter(LRSResponseType::class.java, ResponseTypeAdapter().nullSafe())
    .create()

  @Test
  fun `should return validation errors when user postcode is invalid`() {
    val expectedResponse = HmppsBoldLrsExceptionHandler.ErrorResponse(
      HttpStatus.BAD_REQUEST,
      "Validation Failed",
      "Please correct the error and retry",
      "Validation(s) failed for [lastKnownPostCode]",
      "Validation(s) failed for [lastKnownPostCode] with reason(s): [must match \"^[A-Z]{1,2}[0-9R][0-9A-Z]? ?[0-9][ABDEFGHJLNPQRSTUWXYZ]{2}|BFPO ?[0-9]{1,4}|([AC-FHKNPRTV-Y]\\d{2}|D6W)? ?[0-9AC-FHKNPRTV-Y]{4}\$\"]",
    )

    val findLearnerByDemographicsRequest =
      uk.gov.justice.digital.hmpps.learnerrecordsapi.models.request.FindLearnerByDemographicsRequest(
        "Darcie",
        "Tucker",
        LocalDate.parse("2024-01-01"),
        1,
        "ABC123",
      )

    val actualResponse = webTestClient.post()
      .uri("/learners")
      .headers(setAuthorisation(roles = listOf("ROLE_TEMPLATE_KOTLIN__UI")))
      .bodyValue(findLearnerByDemographicsRequest)
      .accept(MediaType.parseMediaType("application/json"))
      .exchange()
      .expectStatus()
      .isBadRequest
      .expectBody()
      .returnResult()
      .responseBody

    val actualResponseString = actualResponse?.toString(Charsets.UTF_8)
    assertThat(actualResponseString).isEqualTo(gson.toJson(expectedResponse))
  }

  @Test
  fun `should return validation errors when user givenName is invalid`() {
    val expectedResponse = HmppsBoldLrsExceptionHandler.ErrorResponse(
      HttpStatus.BAD_REQUEST,
      "Validation Failed",
      "Please correct the error and retry",
      "Validation(s) failed for [givenName]",
      "Validation(s) failed for [givenName] with reason(s): [must match \"^[A-Za-z]{3,35}$\"]",
    )

    val findLearnerByDemographicsRequest =
      uk.gov.justice.digital.hmpps.learnerrecordsapi.models.request.FindLearnerByDemographicsRequest(
        "DarcieDarcieDarcieDarcieDarcieDarcieDarcieDarcieDarcieDarcieDarcieDarcieDarcieDarcieDarcieDarcie",
        "Tucker",
        LocalDate.parse("2024-01-01"),
        1,
        "CV49EE",
      )

    val actualResponse = webTestClient.post()
      .uri("/learners")
      .headers(setAuthorisation(roles = listOf("ROLE_TEMPLATE_KOTLIN__UI")))
      .bodyValue(findLearnerByDemographicsRequest)
      .accept(MediaType.parseMediaType("application/json"))
      .exchange()
      .expectStatus()
      .isBadRequest
      .expectBody()
      .returnResult()
      .responseBody

    val actualResponseString = actualResponse?.toString(Charsets.UTF_8)
    assertThat(actualResponseString).isEqualTo(gson.toJson(expectedResponse))
  }

  @Test
  fun `should return validation errors when user familyName is invalid`() {
    val expectedResponse = HmppsBoldLrsExceptionHandler.ErrorResponse(
      HttpStatus.BAD_REQUEST,
      "Validation Failed",
      "Please correct the error and retry",
      "Validation(s) failed for [familyName]",
      "Validation(s) failed for [familyName] with reason(s): [must match \"^[A-Za-z]{3,35}$\"]",
    )

    val findLearnerByDemographicsRequest =
      uk.gov.justice.digital.hmpps.learnerrecordsapi.models.request.FindLearnerByDemographicsRequest(
        "Darcie",
        "TuckerTuckerTuckerTuckerTuckerTuckerTuckerTuckerTuckerTuckerTuckerTuckerTuckerTuckerTuckerTuckerTuckerTucker",
        LocalDate.parse("2024-01-01"),
        1,
        "CV49EE",
      )

    val actualResponse = webTestClient.post()
      .uri("/learners")
      .headers(setAuthorisation(roles = listOf("ROLE_TEMPLATE_KOTLIN__UI")))
      .bodyValue(findLearnerByDemographicsRequest)
      .accept(MediaType.parseMediaType("application/json"))
      .exchange()
      .expectStatus()
      .isBadRequest
      .expectBody()
      .returnResult()
      .responseBody

    val actualResponseString = actualResponse?.toString(Charsets.UTF_8)
    assertThat(actualResponseString).isEqualTo(gson.toJson(expectedResponse))
  }

  @Test
  fun `should return validation errors when user gender is invalid`() {
    val expectedResponse = HmppsBoldLrsExceptionHandler.ErrorResponse(
      HttpStatus.BAD_REQUEST,
      "Validation Failed",
      "Please correct the error and retry",
      "Validation(s) failed for [gender]",
      "Validation(s) failed for [gender] with reason(s): [must be less than or equal to 2]",
    )

    val findLearnerByDemographicsRequest =
      uk.gov.justice.digital.hmpps.learnerrecordsapi.models.request.FindLearnerByDemographicsRequest(
        "Darcie",
        "Tucker",
        LocalDate.parse("2024-01-01"),
        4,
        "CV49EE",
      )

    val actualResponse = webTestClient.post()
      .uri("/learners")
      .headers(setAuthorisation(roles = listOf("ROLE_TEMPLATE_KOTLIN__UI")))
      .bodyValue(findLearnerByDemographicsRequest)
      .accept(MediaType.parseMediaType("application/json"))
      .exchange()
      .expectStatus()
      .isBadRequest
      .expectBody()
      .returnResult()
      .responseBody

    val actualResponseString = actualResponse?.toString(Charsets.UTF_8)
    assertThat(actualResponseString).isEqualTo(gson.toJson(expectedResponse))
  }

  @Test
  fun `should return No Resource errors when an unknown resource is called`() {
    val expectedResponse = HmppsBoldLrsExceptionHandler.ErrorResponse(
      HttpStatus.NOT_FOUND,
      "No Resource Found",
      "No resource found failure: No static resource someotherEndpoint.",
      "Requested Resource not found on the server",
      moreInfo = "Requested Resource not found on the server",
    )

    val findLearnerByDemographicsRequest =
      uk.gov.justice.digital.hmpps.learnerrecordsapi.models.request.FindLearnerByDemographicsRequest(
        "Darcie",
        "Tucker",
        LocalDate.parse("2024-01-01"),
        2,
        "CV49EE",
      )

    val actualResponse = webTestClient.post()
      .uri("/someotherEndpoint")
      .headers(setAuthorisation(roles = listOf("ROLE_TEMPLATE_KOTLIN__UI")))
      .bodyValue(findLearnerByDemographicsRequest)
      .accept(MediaType.parseMediaType("application/json"))
      .exchange()
      .expectStatus()
      .isNotFound
      .expectBody()
      .returnResult()
      .responseBody

    val actualResponseString = actualResponse?.toString(Charsets.UTF_8)
    assertThat(actualResponseString).isEqualTo(gson.toJson(expectedResponse))
  }

  @Test
  fun `should return bad request when a mandatory input is not provided`() {
    val expectedResponse = HmppsBoldLrsExceptionHandler.ErrorResponse(
      HttpStatus.BAD_REQUEST,
      "Unreadable HTTP message",
      "Unreadable HTTP message",
      "JSON parse error: Instantiation of " +
        "[simple type, class uk.gov.justice.digital.hmpps.learnerrecordsapi.models.request.FindLearnerByDemographicsRequest] " +
        "value failed for JSON property givenName due to missing (therefore NULL) value " +
        "for creator parameter givenName which is a non-nullable type",
      "Unreadable HTTP message",
    )

    val requestJsonWithoutGivenName = """
      {
        "lastName": "Tucker",
        "dateOfBirth": "2024-01-01",
        "gender": 1,
        "postcode": "CV49EE"
      }
    """

    val actualResponse = webTestClient.post()
      .uri("/learners")
      .headers(setAuthorisation(roles = listOf("ROLE_TEMPLATE_KOTLIN__UI")))
      .contentType(MediaType.APPLICATION_JSON)
      .bodyValue(requestJsonWithoutGivenName)
      .accept(MediaType.parseMediaType("application/json"))
      .exchange()
      .expectStatus()
      .isBadRequest
      .expectBody()
      .returnResult()
      .responseBody

    val actualResponseString = actualResponse?.toString(Charsets.UTF_8)
    assertThat(actualResponseString).isEqualTo(gson.toJson(expectedResponse))
  }
}
