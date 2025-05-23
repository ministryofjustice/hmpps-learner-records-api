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
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.lrsapi.response.Learner
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.request.Gender
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.request.LearnersRequest
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.response.LRSResponseType
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.response.LearnersResponse

class LearnersResourceIntTest : IntegrationTestBase() {

  @Autowired
  protected lateinit var objectMapper: ObjectMapper

  @Nested
  @DisplayName("POST /learners")
  inner class LearnersEndpoint {

    private val findLearnerByDemographicsRequest =
      LearnersRequest(
        "Some",
        "Person",
        "2024-01-01",
        Gender.MALE,
        "CV49EE",
        "Test",
        "Test High School",
        "Some place",
        "test_email@test.com",
      )

    private fun actualResponse(
      request: LearnersRequest = findLearnerByDemographicsRequest,
      expectedStatus: Int = 200,
    ): Any? {
      val executedRequest = webTestClient.post()
        .uri("/learners")
        .headers(setAuthorisation(roles = listOf(ROLE_LEARNERS_UI)))
        .header("X-Username", "TestUser")
        .bodyValue(request)
        .accept(MediaType.parseMediaType("application/json"))
        .exchange()
        .expectStatus()

      return when (expectedStatus) {
        200 ->
          objectMapper.readValue(
            executedRequest
              .isOk
              .expectBody()
              .returnResult()
              .responseBody?.toString(Charsets.UTF_8),
            LearnersResponse::class.java,
          )

        500 ->
          objectMapper.readValue(
            executedRequest
              .is5xxServerError
              .expectBody()
              .returnResult()
              .responseBody?.toString(Charsets.UTF_8),
            HmppsBoldLrsExceptionHandler.ErrorResponse::class.java,
          )

        else ->
          throw RuntimeException("Unimplemented Expected Status")
      }
    }

    @Test
    fun `should return 500 with an appropriate error response if LRS returns an InternalServerError`() {
      lrsApiMock.stubPostServerError()
      assertThat(actualResponse(expectedStatus = 500).toString()).contains("LRS returned an error: MIAPAPIException")
    }

    @Test
    fun `should return OK and the correct response when LRS returns an exact match`() {
      lrsApiMock.stubLearnerByDemographicsExactMatch()

      val expectedExactMatchLearner = Learner(
        createdDate = "2012-05-25",
        lastUpdatedDate = "2012-05-25",
        uln = "1026893096",
        versionNumber = "1",
        title = "Mrs",
        givenName = "Sample",
        middleOtherName = "Tester",
        familyName = "Testname",
        preferredGivenName = "Sample",
        previousFamilyName = "OLDTESTNAME",
        familyNameAtAge16 = "TESTNAME",
        schoolAtAge16 = "Test Strategy School Foundation ",
        lastKnownAddressLine1 = "1 JOBS LANE",
        lastKnownAddressTown = "COVENTRY",
        lastKnownAddressCountyOrCity = "WEST MIDLANDS",
        lastKnownPostCode = "CV4 9EE",
        dateOfAddressCapture = "2009-04-25",
        dateOfBirth = "1976-08-16",
        placeOfBirth = "Blean ",
        gender = "FEMALE",
        emailAddress = "sample.testname@aol.compatibilitytest.com",
        scottishCandidateNumber = "845759406",
        abilityToShare = "1",
        learnerStatus = "1",
        verificationType = "1",
        tierLevel = "0",
      )

      val expectedResponse = LearnersResponse(
        searchParameters = findLearnerByDemographicsRequest,
        responseType = LRSResponseType.EXACT_MATCH,
        matchedLearners = listOf(expectedExactMatchLearner),
      )

      assertThat(actualResponse()).isEqualTo(expectedResponse)
    }

    @Test
    fun `should return OK and the correct response with appropriate mismatched fields when LRS returns a possible match with two learners`() {
      lrsApiMock.stubLearnerByDemographicsPossibleMatchTwoLearners()

      val requestWithTwoMismatches = findLearnerByDemographicsRequest.copy(
        givenName = "Sample",
        familyName = "Test",
        lastKnownPostCode = "NE00 1ND",
        dateOfBirth = "1995-06-27",
        gender = Gender.FEMALE,
        schoolAtAge16 = "Testing Testers School ",
        placeOfBirth = "Chard ",
        emailAddress = "sample.test@yahoo.compatibilitytest.co.uk",
      )

      val expectedPossibleMatchLearners = mutableListOf(
        Learner(
          createdDate = "2012-05-25",
          lastUpdatedDate = "2012-05-25",
          uln = "1964986809",
          title = "Miss",
          givenName = "Sample",
          middleOtherName = "Testing",
          familyName = "Test",
          preferredGivenName = "Sample",
          familyNameAtAge16 = "TEST",
          schoolAtAge16 = "Testing Testers School ",
          lastKnownAddressLine1 = "1 TEST GARDENS",
          lastKnownAddressTown = "TESTERS BAY",
          lastKnownAddressCountyOrCity = "TYNE AND WEAR",
          lastKnownPostCode = "NE00 1ND",
          dateOfAddressCapture = "2010-09-07",
          dateOfBirth = "1995-06-28",
          placeOfBirth = "Chard ",
          gender = "FEMALE",
          emailAddress = "sample.test@yahoo.compatibilitytest.co.uk",
          scottishCandidateNumber = "820208781",
          verificationType = "5",
          tierLevel = "2",
          abilityToShare = "1",
          learnerStatus = "1",
          versionNumber = "1",
        ),
        Learner(
          createdDate = "2012-05-25",
          lastUpdatedDate = "2012-05-25",
          uln = "8383558804",
          title = "Miss",
          givenName = "Sample",
          middleOtherName = "Testing",
          familyName = "Test",
          preferredGivenName = "Sample",
          familyNameAtAge16 = "TEST",
          schoolAtAge16 = "Testing Testers School ",
          lastKnownAddressLine1 = "14 TESTER DRIVE",
          lastKnownAddressLine2 = "MARCHWOOD",
          lastKnownAddressTown = "SOUTHAMPTON",
          lastKnownAddressCountyOrCity = "HAMPSHIRE",
          lastKnownPostCode = "SO00 1JX",
          dateOfAddressCapture = "2010-09-07",
          dateOfBirth = "1995-06-28",
          placeOfBirth = "Chard ",
          gender = "FEMALE",
          emailAddress = "sample.test@yahoo.compatibilitytest.co.uk",
          scottishCandidateNumber = "820208781",
          verificationType = "5",
          tierLevel = "2",
          abilityToShare = "1",
          learnerStatus = "1",
          versionNumber = "1",
        ),
      )

      val expectedResponse = LearnersResponse(
        searchParameters = requestWithTwoMismatches,
        responseType = LRSResponseType.POSSIBLE_MATCH,
        mismatchedFields = mutableMapOf(
          ("dateOfBirth" to mutableListOf("1995-06-28", "1995-06-28")),
          ("lastKnownPostCode" to mutableListOf("SO00 1JX")),
        ),
        matchedLearners = expectedPossibleMatchLearners,
      )

      assertThat(actualResponse(requestWithTwoMismatches)).isEqualTo(expectedResponse)
    }

    @Test
    fun `should return OK and the correct response when LRS returns a no match response`() {
      lrsApiMock.stubLearnerByDemographicsNoMatch()

      val expectedResponse = LearnersResponse(
        searchParameters = findLearnerByDemographicsRequest,
        responseType = LRSResponseType.NO_MATCH,
      )

      assertThat(actualResponse()).isEqualTo(expectedResponse)
    }

    @Test
    fun `should return OK and the correct response when LRS returns a linked learner response`() {
      lrsApiMock.stubLearnerByDemographicsLinkedLearner()

      val expectedLinkedLearner = Learner(
        createdDate = "2012-05-25",
        lastUpdatedDate = "2012-05-25",
        uln = "6936002314",
        versionNumber = "1",
        masterSubstituted = "Y",
        title = "Mr",
        givenName = "Testing-Sample",
        middleOtherName = "AllTesting",
        familyName = "Test",
        preferredGivenName = "Sample",
        familyNameAtAge16 = "Testsurname",
        schoolAtAge16 = "Test Academy Bristol ",
        lastKnownAddressLine1 = "28 TOLLOHILL SQUARE",
        lastKnownAddressTown = "ABERDEEN",
        lastKnownAddressCountyOrCity = "ABERDEENSHIRE",
        lastKnownPostCode = "AB12 3EQ",
        dateOfAddressCapture = "2008-07-13",
        dateOfBirth = "1985-03-27",
        placeOfBirth = "Whittlesey ",
        gender = "MALE",
        emailAddress = "testing-sample.test@inbox.compatibilitytest.com",
        scottishCandidateNumber = "145589606",
        abilityToShare = "1",
        learnerStatus = "1",
        verificationType = "0",
        tierLevel = "0",
      )

      val expectedResponse = LearnersResponse(
        searchParameters = findLearnerByDemographicsRequest,
        responseType = LRSResponseType.LINKED_LEARNER,
        matchedLearners = mutableListOf(expectedLinkedLearner),
      )

      assertThat(actualResponse()).isEqualTo(expectedResponse)
    }

    @Test
    fun `should return OK and the correct response when LRS returns a too many matches response`() {
      lrsApiMock.stubLearnerByDemographicsTooManyMatches()

      val expectedResponse = LearnersResponse(
        searchParameters = findLearnerByDemographicsRequest,
        responseType = LRSResponseType.TOO_MANY_MATCHES,
      )

      assertThat(actualResponse()).isEqualTo(expectedResponse)
    }

    @Test
    fun `should return 400 with an appropriate error response if X-Username header is missing`() {
      lrsApiMock.stubLearnerByDemographicsExactMatch()

      val executedRequest = webTestClient.post()
        .uri("/learners")
        .headers(setAuthorisation(roles = listOf(ROLE_LEARNERS_UI)))
        .bodyValue(findLearnerByDemographicsRequest)
        .accept(MediaType.parseMediaType("application/json"))
        .exchange()
        .expectStatus()
        .is4xxClientError
        .expectBody()
        .returnResult()
        .responseBody

      val actualResponseString = executedRequest?.toString(Charsets.UTF_8)
      assertThat(actualResponseString).contains("Missing X-Username Header")
    }

    @Test
    fun `should return 400 with an appropriate error response if additional unknown parameters are passed`() {
      lrsApiMock.stubLearnerByDemographicsExactMatch()
      val extendedRequestBody = mutableMapOf<String, String>()
      extendedRequestBody["givenName"] = "Some"
      extendedRequestBody["familyName"] = "Person"
      extendedRequestBody["dateOfBirth"] = "2024-01-01"
      extendedRequestBody["gender"] = "MALE"
      extendedRequestBody["lastKnownPostCode"] = "CV49EE"
      extendedRequestBody["previousFamilyName"] = "Test"
      extendedRequestBody["schoolAtAge16"] = "Test High School"
      extendedRequestBody["placeOfBirth"] = "Some place"
      extendedRequestBody["emailAddress"] = "test_email@test.com"
      extendedRequestBody["unknownValue"] = "1234"

      val executedRequest = webTestClient.post()
        .uri("/learners")
        .headers(setAuthorisation(roles = listOf(ROLE_LEARNERS_UI)))
        .header("X-Username", "TestUser")
        .bodyValue(extendedRequestBody)
        .accept(MediaType.parseMediaType("application/json"))
        .exchange()
        .expectStatus()
        .is4xxClientError
        .expectBody()
        .returnResult()
        .responseBody

      val actualResponseString = executedRequest?.toString(Charsets.UTF_8)
      assertThat(actualResponseString).contains("Unrecognized field \\\"unknownValue\\\"")
    }
  }
}
