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
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.lrsapi.response.Learner
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.request.FindLearnerByDemographicsRequest
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.response.FindLearnerByDemographicsResponse
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.response.LRSResponseType
import java.time.LocalDate

class LearnersResourceIntTest : IntegrationTestBase() {

  @Nested
  @DisplayName("POST /learners")
  inner class LearnersEndpoint {

    private val gson = GsonBuilder()
      .registerTypeAdapter(LocalDate::class.java, LocalDateAdapter().nullSafe())
      .registerTypeAdapter(LRSResponseType::class.java, ResponseTypeAdapter().nullSafe())
      .create()

    private val findLearnerByDemographicsRequest =
      FindLearnerByDemographicsRequest(
        "Some",
        "Person",
        LocalDate.parse("2024-01-01"),
        1,
        "CV49EE",
      )

    private fun actualResponse(
      request: FindLearnerByDemographicsRequest = findLearnerByDemographicsRequest,
      requestAsJson: String? = null,
      expectedStatus: Int = 200,
    ): String? {
      val executedRequest = webTestClient.post()
        .uri("/learners")
        .headers(setAuthorisation(roles = listOf("ROLE_LEARNER_RECORDS_SEARCH__RW")))
        .contentType(MediaType.APPLICATION_JSON)
        .bodyValue(requestAsJson ?: request)
        .accept(MediaType.parseMediaType("application/json"))
        .exchange()
        .expectStatus()

      return when (expectedStatus) {
        200 -> executedRequest.isOk.expectBody().returnResult().responseBody?.toString(Charsets.UTF_8)
        500 -> executedRequest.is5xxServerError.expectBody().returnResult().responseBody?.toString(Charsets.UTF_8)
        400 -> executedRequest.isBadRequest.expectBody().returnResult().responseBody?.toString(Charsets.UTF_8)
        else ->
          throw RuntimeException("Unimplemented Expected Status")
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
          "givenName": "Some",
          "familyName": "Person",
          "gender": 1,
          "dateOfBirth": "1976-08-16",
          "lastKnownPostCode": "CV49EE"
        }
      """

      val scenarios = listOf(
        "\"givenName\": \"Some\",",
        "\"familyName\": \"Person\",",
        "\"dateOfBirth\": \"1976-08-16\",",
        "\"lastKnownPostCode\": \"CV49EE\"",
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
          "gender": 1,
          "dateOfBirth": "1976-08-16",
          "lastKnownPostCode": "CV49EE"
        }
        """

      val scenarios = listOf(
        "InvalidNameInvalidNameInvalidNameInvalidNameInvalidName" to "Some",
        "InvalidNameInvalidNameInvalidNameInvalidNameInvalidName" to "Person",
        "-1" to "1",
        "\"invalid-date\"" to "1976-08-16",
        "\"INVALID\"" to "CV49EE",
      )

      for ((invalidValue, valueToReplace) in scenarios) {
        val requestWithInvalidField = validRequest.replace(valueToReplace, invalidValue)
        val response = actualResponse(requestAsJson = requestWithInvalidField, expectedStatus = 400).orEmpty()
        assertThat(response.contains("JSON parse error"))
      }
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
        givenName = "Darcie",
        middleOtherName = "Isla",
        familyName = "Tucker",
        preferredGivenName = "Darcie",
        previousFamilyName = "CAMPBELL",
        familyNameAtAge16 = "TUCKER",
        schoolAtAge16 = "Mill Hill School Foundation ",
        lastKnownAddressLine1 = "1 JOBS LANE",
        lastKnownAddressTown = "COVENTRY",
        lastKnownAddressCountyOrCity = "WEST MIDLANDS",
        lastKnownPostCode = "CV4 9EE",
        dateOfAddressCapture = "2009-04-25",
        dateOfBirth = "1976-08-16",
        placeOfBirth = "Blean ",
        gender = "2",
        emailAddress = "darcie.tucker@aol.compatibilitytest.com",
        scottishCandidateNumber = "845759406",
        abilityToShare = "1",
        learnerStatus = "1",
        verificationType = "1",
        tierLevel = "0",
      )

      val expectedResponse = gson.toJson(
        FindLearnerByDemographicsResponse(
          searchParameters = findLearnerByDemographicsRequest,
          responseType = LRSResponseType.EXACT_MATCH,
          matchedLearners = listOf(expectedExactMatchLearner),
        ),
      )

      assertThat(actualResponse()).isEqualTo(expectedResponse)
    }

    @Test
    fun `should return OK and the correct response with appropriate mismatched fields when LRS returns a possible match with two learners`() {
      lrsApiMock.stubLearnerByDemographicsPossibleMatchTwoLearners()

      val requestWithTwoMismatches = findLearnerByDemographicsRequest.copy(
        givenName = "Anna",
        familyName = "Cheng",
        lastKnownPostCode = "NE26 3ND",
        dateOfBirth = LocalDate.parse("1995-06-27"),
        gender = 2,
      )

      val expectedPossibleMatchLearners = mutableListOf(
        Learner(
          createdDate = "2012-05-25",
          lastUpdatedDate = "2012-05-25",
          uln = "1964986809",
          title = "Miss",
          givenName = "Anna",
          middleOtherName = "Joanna",
          familyName = "Cheng",
          preferredGivenName = "Anna",
          familyNameAtAge16 = "CHENG",
          schoolAtAge16 = "Ellern Mede School ",
          lastKnownAddressLine1 = "1 ILFRACOMBE GARDENS",
          lastKnownAddressTown = "WHITLEY BAY",
          lastKnownAddressCountyOrCity = "TYNE AND WEAR",
          lastKnownPostCode = "NE26 3ND",
          dateOfAddressCapture = "2010-09-07",
          dateOfBirth = "1995-06-28",
          placeOfBirth = "Chard ",
          gender = "2",
          emailAddress = "anna.cheng@yahoo.compatibilitytest.co.uk",
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
          givenName = "Anna",
          middleOtherName = "Joanna",
          familyName = "Cheng",
          preferredGivenName = "Anna",
          familyNameAtAge16 = "CHENG",
          schoolAtAge16 = "Ellern Mede School ",
          lastKnownAddressLine1 = "14 LARKSPUR DRIVE",
          lastKnownAddressLine2 = "MARCHWOOD",
          lastKnownAddressTown = "SOUTHAMPTON",
          lastKnownAddressCountyOrCity = "HAMPSHIRE",
          lastKnownPostCode = "SO40 4JX",
          dateOfAddressCapture = "2010-09-07",
          dateOfBirth = "1995-06-28",
          placeOfBirth = "Chard ",
          gender = "2",
          emailAddress = "anna.cheng@yahoo.compatibilitytest.co.uk",
          scottishCandidateNumber = "820208781",
          verificationType = "5",
          tierLevel = "2",
          abilityToShare = "1",
          learnerStatus = "1",
          versionNumber = "1",
        ),
      )

      val expectedResponse = gson.toJson(
        FindLearnerByDemographicsResponse(
          searchParameters = requestWithTwoMismatches,
          responseType = LRSResponseType.POSSIBLE_MATCH,
          mismatchedFields = mutableMapOf(
            ("dateOfBirth" to mutableListOf("1995-06-28", "1995-06-28")),
            ("lastKnownPostCode" to mutableListOf("SO40 4JX")),
          ),
          matchedLearners = expectedPossibleMatchLearners,
        ),
      )

      assertThat(actualResponse(requestWithTwoMismatches)).isEqualTo(expectedResponse)
    }

    @Test
    fun `should return OK and the correct response when LRS returns a no match response`() {
      lrsApiMock.stubLearnerByDemographicsNoMatch()

      val expectedResponse = gson.toJson(
        FindLearnerByDemographicsResponse(
          searchParameters = findLearnerByDemographicsRequest,
          responseType = LRSResponseType.NO_MATCH,
        ),
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
        givenName = "William-Connor",
        middleOtherName = "Alistair",
        familyName = "Carroll",
        preferredGivenName = "Connor",
        familyNameAtAge16 = "CARROLL",
        schoolAtAge16 = "Oasis Academy Bristol ",
        lastKnownAddressLine1 = "28 TOLLOHILL SQUARE",
        lastKnownAddressTown = "ABERDEEN",
        lastKnownAddressCountyOrCity = "ABERDEENSHIRE",
        lastKnownPostCode = "AB12 5EQ",
        dateOfAddressCapture = "2008-07-13",
        dateOfBirth = "1985-03-27",
        placeOfBirth = "Whittlesey ",
        gender = "1",
        emailAddress = "william-connor.carroll@inbox.compatibilitytest.com",
        scottishCandidateNumber = "145589606",
        abilityToShare = "1",
        learnerStatus = "1",
        verificationType = "0",
        tierLevel = "0",
      )

      val expectedResponse = gson.toJson(
        FindLearnerByDemographicsResponse(
          searchParameters = findLearnerByDemographicsRequest,
          responseType = LRSResponseType.LINKED_LEARNER,
          matchedLearners = mutableListOf(expectedLinkedLearner),
        ),
      )

      assertThat(actualResponse()).isEqualTo(expectedResponse)
    }

    @Test
    fun `should return OK and the correct response when LRS returns a too many matches response`() {
      lrsApiMock.stubLearnerByDemographicsTooManyMatches()

      val expectedResponse = gson.toJson(
        FindLearnerByDemographicsResponse(
          searchParameters = findLearnerByDemographicsRequest,
          responseType = LRSResponseType.TOO_MANY_MATCHES,
        ),
      )

      assertThat(actualResponse()).isEqualTo(expectedResponse)
    }
  }
}
