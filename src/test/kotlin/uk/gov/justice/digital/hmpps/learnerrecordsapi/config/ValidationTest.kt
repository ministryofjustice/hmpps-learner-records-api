package uk.gov.justice.digital.hmpps.learnerrecordsapi.config

import com.google.gson.GsonBuilder
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import uk.gov.justice.digital.hmpps.learnerrecordsapi.integration.IntegrationTestBase
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.gsonadapters.LocalDateAdapter
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.gsonadapters.ResponseTypeAdapter
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.request.Gender
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.response.LRSResponseType
import java.time.LocalDate

// Tests that HmppsBoldLrsExceptionHandler works as expected when actually calling our endpoints.

class ValidationTest : IntegrationTestBase() {

  val gson = GsonBuilder()
    .registerTypeAdapter(LocalDate::class.java, LocalDateAdapter().nullSafe())
    .registerTypeAdapter(LRSResponseType::class.java, ResponseTypeAdapter().nullSafe())
    .disableHtmlEscaping()
    .create()

  @Test
  fun `learners endpoint should return validation errors when user postcode is invalid`() {
    val expectedResponse = HmppsBoldLrsExceptionHandler.ErrorResponse(
      HttpStatus.BAD_REQUEST,
      "Validation Failed",
      "Please correct the error and retry",
      "Validation(s) failed for [lastKnownPostCode]",
      "Validation(s) failed for [lastKnownPostCode] with reason(s): [must match \"^[A-Z]{1,2}[0-9R][0-9A-Z]? ?[0-9][ABDEFGHJLNPQRSTUWXYZ]{2}|BFPO ?[0-9]{1,4}|([AC-FHKNPRTV-Y]\\d{2}|D6W)? ?[0-9AC-FHKNPRTV-Y]{4}\$\"]",
    )

    val findLearnerByDemographicsRequest =
      uk.gov.justice.digital.hmpps.learnerrecordsapi.models.request.LearnersRequest(
        "Darcie",
        "Tucker",
        LocalDate.parse("2024-01-01"),
        Gender.MALE,
        "ABC123",
        "Test",
        "Test High School",
        "Some place",
        "test_email@test.com",
      )

    val actualResponse = webTestClient.post()
      .uri("/learners")
      .headers(setAuthorisation(roles = listOf("ROLE_LEARNER_RECORDS_SEARCH__RO")))
      .header("X-Username", "TestUser")
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
  fun `learners endpoint should return validation errors when user givenName is invalid`() {
    val expectedResponse = HmppsBoldLrsExceptionHandler.ErrorResponse(
      HttpStatus.BAD_REQUEST,
      "Validation Failed",
      "Please correct the error and retry",
      "Validation(s) failed for [givenName]",
      "Validation(s) failed for [givenName] with reason(s): [must match \"^[A-Za-z' ,.-]{3,35}$\"]",
    )

    val findLearnerByDemographicsRequest =
      uk.gov.justice.digital.hmpps.learnerrecordsapi.models.request.LearnersRequest(
        "DarcieDarcieDarcieDarcieDarcieDarcieDarcieDarcieDarcieDarcieDarcieDarcieDarcieDarcieDarcieDarcie",
        "Tucker",
        LocalDate.parse("2024-01-01"),
        Gender.MALE,
        "CV49EE",
        "Test",
        "Test High School",
        "Some place",
        "test_email@test.com",
      )

    val actualResponse = webTestClient.post()
      .uri("/learners")
      .headers(setAuthorisation(roles = listOf("ROLE_LEARNER_RECORDS_SEARCH__RO")))
      .header("X-Username", "TestUser")
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
  fun `learner events endpoint should return validation errors when user givenName is invalid`() {
    val expectedResponse = HmppsBoldLrsExceptionHandler.ErrorResponse(
      HttpStatus.BAD_REQUEST,
      "Validation Failed",
      "Please correct the error and retry",
      "Validation(s) failed for [givenName]",
      "Validation(s) failed for [givenName] with reason(s): [must match \"^[A-Za-z' ,.-]{3,35}$\"]",
    )

    val learnerEventsRequest =
      uk.gov.justice.digital.hmpps.learnerrecordsapi.models.request.LearnerEventsRequest(
        "DarcieDarcieDarcieDarcieDarcieDarcieDarcieDarcieDarcieDarcieDarcieDarcieDarcieDarcieDarcieDarcie",
        "Tucker",
        "1234567890",
        LocalDate.parse("2024-01-01"),
        Gender.MALE,
      )

    val actualResponse = webTestClient.post()
      .uri("/learner-events")
      .headers(setAuthorisation(roles = listOf("ROLE_LEARNER_RECORDS_SEARCH__RO")))
      .header("X-Username", "TestUser")
      .bodyValue(learnerEventsRequest)
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
  fun `learner endpoint should return validation errors when user familyName is invalid`() {
    val expectedResponse = HmppsBoldLrsExceptionHandler.ErrorResponse(
      HttpStatus.BAD_REQUEST,
      "Validation Failed",
      "Please correct the error and retry",
      "Validation(s) failed for [familyName]",
      "Validation(s) failed for [familyName] with reason(s): [must match \"^[A-Za-z' ,.-]{3,35}\$\"]",
    )
    val findLearnerByDemographicsRequest =
      uk.gov.justice.digital.hmpps.learnerrecordsapi.models.request.LearnersRequest(
        "Darcie",
        "TuckerTuckerTuckerTuckerTuckerTuckerTuckerTuckerTuckerTuckerTuckerTuckerTuckerTuckerTuckerTuckerTuckerTucker",
        LocalDate.parse("2024-01-01"),
        Gender.MALE,
        "CV49EE",
        "Test",
        "Test High School",
        "Some place",
        "test_email@test.com",
      )

    val actualResponse = webTestClient.post()
      .uri("/learners")
      .headers(setAuthorisation(roles = listOf("ROLE_LEARNER_RECORDS_SEARCH__RO")))
      .header("X-Username", "TestUser")
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
  fun `learner endpoint should return validation errors when user previousFamilyName is invalid`() {
    val expectedResponse = HmppsBoldLrsExceptionHandler.ErrorResponse(
      HttpStatus.BAD_REQUEST,
      "Validation Failed",
      "Please correct the error and retry",
      "Validation(s) failed for [previousFamilyName]",
      "Validation(s) failed for [previousFamilyName] with reason(s): [must match \"^[A-Za-z' ,.-]{3,35}\$\"]",
    )
    val findLearnerByDemographicsRequest =
      uk.gov.justice.digital.hmpps.learnerrecordsapi.models.request.LearnersRequest(
        "Darcie",
        "Tucker",
        LocalDate.parse("2024-01-01"),
        Gender.MALE,
        "CV49EE",
        "TestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTestTest",
        "Test High School",
        "Some place",
        "test_email@test.com",
      )

    val actualResponse = webTestClient.post()
      .uri("/learners")
      .headers(setAuthorisation(roles = listOf("ROLE_LEARNER_RECORDS_SEARCH__RO")))
      .header("X-Username", "TestUser")
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
  fun `learner endpoint should return validation errors when user emailAddress is invalid`() {
    val expectedResponse = HmppsBoldLrsExceptionHandler.ErrorResponse(
      HttpStatus.BAD_REQUEST,
      "Validation Failed",
      "Please correct the error and retry",
      "Validation(s) failed for [emailAddress]",
      "Validation(s) failed for [emailAddress] with reason(s): [must match \"^[A-Za-z0-9._'%+-]{1,64}@(?:(?=[A-Za-z0-9-]{1,63}\\.)[A-Za-z0-9]+(?:-[A-Za-z0-9]+)*\\.){1,8}[A-Za-z]{2,63}\$\"]",
    )
    val findLearnerByDemographicsRequest =
      uk.gov.justice.digital.hmpps.learnerrecordsapi.models.request.LearnersRequest(
        "Darcie",
        "Tucker",
        LocalDate.parse("2024-01-01"),
        Gender.MALE,
        "CV49EE",
        "Test",
        "Test High School",
        "Some place",
        "test_email@@test.com",
      )

    val actualResponse = webTestClient.post()
      .uri("/learners")
      .headers(setAuthorisation(roles = listOf("ROLE_LEARNER_RECORDS_SEARCH__RO")))
      .header("X-Username", "TestUser")
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
  fun `learner events endpoint should return validation errors when user familyName is invalid`() {
    val expectedResponse = HmppsBoldLrsExceptionHandler.ErrorResponse(
      HttpStatus.BAD_REQUEST,
      "Validation Failed",
      "Please correct the error and retry",
      "Validation(s) failed for [familyName]",
      "Validation(s) failed for [familyName] with reason(s): [must match \"^[A-Za-z' ,.-]{3,35}\$\"]",
    )
    val learnerEventsRequest =
      uk.gov.justice.digital.hmpps.learnerrecordsapi.models.request.LearnerEventsRequest(
        "Darcie",
        "TuckerTuckerTuckerTuckerTuckerTuckerTuckerTuckerTuckerTuckerTuckerTuckerTuckerTuckerTuckerTuckerTuckerTucker",
        "1234567890",
        LocalDate.parse("2024-01-01"),
        Gender.MALE,
      )

    val actualResponse = webTestClient.post()
      .uri("/learner-events")
      .headers(setAuthorisation(roles = listOf("ROLE_LEARNER_RECORDS_SEARCH__RO")))
      .header("X-Username", "TestUser")
      .bodyValue(learnerEventsRequest)
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
  fun `learner events endpoint should return validation errors when user uln is invalid`() {
    val expectedResponse = HmppsBoldLrsExceptionHandler.ErrorResponse(
      HttpStatus.BAD_REQUEST,
      "Validation Failed",
      "Please correct the error and retry",
      "Validation(s) failed for [uln]",
      "Validation(s) failed for [uln] with reason(s): [must match \"^[0-9]{1,10}\$\"]",
    )
    val learnerEventsRequest =
      uk.gov.justice.digital.hmpps.learnerrecordsapi.models.request.LearnerEventsRequest(
        "Darcie",
        "Tucker",
        "12345678901234567890",
        LocalDate.parse("2024-01-01"),
        Gender.MALE,
      )

    val actualResponse = webTestClient.post()
      .uri("/learner-events")
      .headers(setAuthorisation(roles = listOf("ROLE_LEARNER_RECORDS_SEARCH__RO")))
      .header("X-Username", "TestUser")
      .bodyValue(learnerEventsRequest)
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
  fun `learner endpoint should return validation errors when user gender is invalid`() {
    val expectedResponse = HmppsBoldLrsExceptionHandler.ErrorResponse(
      HttpStatus.BAD_REQUEST,
      "Unreadable HTTP message",
      "Unreadable HTTP message",
      "JSON parse error: Cannot deserialize value of type `uk.gov.justice.digital.hmpps.learnerrecordsapi.models.request.Gender` from String \"TESTINGENUM\": not one of the values accepted for Enum class: [NOT_SPECIFIED, MALE, NOT_KNOWN, FEMALE]",
      "Unreadable HTTP message",
    )

    val findLearnerByDemographicsRequest =
      """{
        "givenName":"Darcie", 
        "familyName": "Tucker",
        "dateOfBirth": "2024-01-01",
        "gender": "TESTINGENUM",
        "postcode": "CV49EE"
        }"""
    val actualResponse = webTestClient.post()
      .uri("/learners")
      .headers(setAuthorisation(roles = listOf("ROLE_LEARNER_RECORDS_SEARCH__RO")))
      .header("X-Username", "TestUser")
      .contentType(MediaType.APPLICATION_JSON)
      .bodyValue(findLearnerByDemographicsRequest)
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
  fun `learner events endpoint should return validation errors when user gender is invalid`() {
    val expectedResponse = HmppsBoldLrsExceptionHandler.ErrorResponse(
      HttpStatus.BAD_REQUEST,
      "Unreadable HTTP message",
      "Unreadable HTTP message",
      "JSON parse error: Cannot deserialize value of type `uk.gov.justice.digital.hmpps.learnerrecordsapi.models.request.Gender` from String \"TESTINGENUM\": not one of the values accepted for Enum class: [NOT_SPECIFIED, MALE, NOT_KNOWN, FEMALE]",
      "Unreadable HTTP message",
    )

    val findLearnerByDemographicsRequest =
      """{
        "givenName":"Darcie", 
        "familyName": "Tucker",
        "dateOfBirth": "2024-01-01",
        "gender": "TESTINGENUM",
        "uln": "1234567890"
        }"""
    val actualResponse = webTestClient.post()
      .uri("/learner-events")
      .headers(setAuthorisation(roles = listOf("ROLE_LEARNER_RECORDS_SEARCH__RO")))
      .header("X-Username", "TestUser")
      .contentType(MediaType.APPLICATION_JSON)
      .bodyValue(findLearnerByDemographicsRequest)
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
  fun `learner endpoint should return bad request when a mandatory input is not provided`() {
    val expectedResponse = HmppsBoldLrsExceptionHandler.ErrorResponse(
      HttpStatus.BAD_REQUEST,
      "Unreadable HTTP message",
      "Unreadable HTTP message",
      "JSON parse error: Instantiation of " +
        "[simple type, class uk.gov.justice.digital.hmpps.learnerrecordsapi.models.request.LearnersRequest] " +
        "value failed for JSON property givenName due to missing (therefore NULL) value " +
        "for creator parameter givenName which is a non-nullable type",
      "Unreadable HTTP message",
    )

    val requestJsonWithoutGivenName = """
      {
        "lastName": "Tucker",
        "dateOfBirth": "2024-01-01",
        "gender": "FEMALE",
        "postcode": "CV49EE"
      }
    """

    val actualResponse = webTestClient.post()
      .uri("/learners")
      .headers(setAuthorisation(roles = listOf("ROLE_LEARNER_RECORDS_SEARCH__RO")))
      .header("X-Username", "TestUser")
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

  @Test
  fun `learner events endpoint should return bad request when a mandatory input is not provided`() {
    val expectedResponse = HmppsBoldLrsExceptionHandler.ErrorResponse(
      HttpStatus.BAD_REQUEST,
      "Unreadable HTTP message",
      "Unreadable HTTP message",
      "JSON parse error: Instantiation of " +
        "[simple type, class uk.gov.justice.digital.hmpps.learnerrecordsapi.models.request.LearnerEventsRequest] " +
        "value failed for JSON property givenName due to missing (therefore NULL) value " +
        "for creator parameter givenName which is a non-nullable type",
      "Unreadable HTTP message",
    )

    val requestJsonWithoutGivenName = """
      {
        "lastName": "Tucker",
        "dateOfBirth": "2024-01-01",
        "uln": "1234567890",
        "gender": "MALE"
      }
    """

    val actualResponse = webTestClient.post()
      .uri("/learner-events")
      .headers(setAuthorisation(roles = listOf("ROLE_LEARNER_RECORDS_SEARCH__RO")))
      .header("X-Username", "TestUser")
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
