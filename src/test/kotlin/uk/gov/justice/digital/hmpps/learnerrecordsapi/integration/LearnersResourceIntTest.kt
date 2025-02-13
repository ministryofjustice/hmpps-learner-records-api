package uk.gov.justice.digital.hmpps.learnerrecordsapi.integration

import com.fasterxml.jackson.databind.ObjectMapper
import com.google.gson.GsonBuilder
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest
import uk.gov.justice.digital.hmpps.learnerrecordsapi.integration.wiremock.LRSApiExtension.Companion.lrsApiMock
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.gsonadapters.LocalDateAdapter
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.gsonadapters.ResponseTypeAdapter
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.lrsapi.response.Learner
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.request.Gender
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.request.LearnersRequest
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.response.LRSResponseType
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.response.LearnersResponse
import uk.gov.justice.hmpps.sqs.HmppsQueueService
import uk.gov.justice.hmpps.sqs.MissingQueueException
import uk.gov.justice.hmpps.sqs.audit.HmppsAuditEvent
import java.time.Instant
import java.time.LocalDate
import java.util.*

class LearnersResourceIntTest : IntegrationTestBase() {
  @Autowired
  protected lateinit var hmppsQueueService: HmppsQueueService

  @Autowired
  protected lateinit var objectMapper: ObjectMapper

  private val auditQueue by lazy {
    hmppsQueueService.findByQueueId("audit") ?: throw MissingQueueException("HmppsQueue audit not found")
  }
  protected val auditSqsClient by lazy { auditQueue.sqsClient }
  protected val auditQueueUrl by lazy { auditQueue.queueUrl }

  @Nested
  @DisplayName("POST /learners")
  inner class LearnersEndpoint {

    private val gson = GsonBuilder()
      .registerTypeAdapter(LocalDate::class.java, LocalDateAdapter().nullSafe())
      .registerTypeAdapter(LRSResponseType::class.java, ResponseTypeAdapter().nullSafe())
      .create()

    private val findLearnerByDemographicsRequest =
      LearnersRequest(
        "Some",
        "Person",
        LocalDate.parse("2024-01-01"),
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
    ): String? {
      val executedRequest = webTestClient.post()
        .uri("/learners")
        .headers(setAuthorisation(roles = listOf("ROLE_LEARNER_RECORDS_SEARCH__RO")))
        .header("X-Username", "TestUser")
        .bodyValue(request)
        .accept(MediaType.parseMediaType("application/json"))
        .exchange()
        .expectStatus()

      return when (expectedStatus) {
        200 ->
          executedRequest
            .isOk
            .expectBody()
            .returnResult()
            .responseBody?.toString(Charsets.UTF_8)

        500 ->
          executedRequest
            .is5xxServerError
            .expectBody()
            .returnResult()
            .responseBody?.toString(Charsets.UTF_8)

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
        gender = "FEMALE",
        emailAddress = "darcie.tucker@aol.compatibilitytest.com",
        scottishCandidateNumber = "845759406",
        abilityToShare = "1",
        learnerStatus = "1",
        verificationType = "1",
        tierLevel = "0",
      )

      val expectedResponse = gson.toJson(
        LearnersResponse(
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
        gender = Gender.FEMALE,
        schoolAtAge16 = "Ellern Mede School ",
        placeOfBirth = "Chard ",
        emailAddress = "anna.cheng@yahoo.compatibilitytest.co.uk",
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
          gender = "FEMALE",
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
          gender = "FEMALE",
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
        LearnersResponse(
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
        LearnersResponse(
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
        gender = "MALE",
        emailAddress = "william-connor.carroll@inbox.compatibilitytest.com",
        scottishCandidateNumber = "145589606",
        abilityToShare = "1",
        learnerStatus = "1",
        verificationType = "0",
        tierLevel = "0",
      )

      val expectedResponse = gson.toJson(
        LearnersResponse(
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
        LearnersResponse(
          searchParameters = findLearnerByDemographicsRequest,
          responseType = LRSResponseType.TOO_MANY_MATCHES,
        ),
      )

      assertThat(actualResponse()).isEqualTo(expectedResponse)
    }

    @Test
    fun `should return 400 with an appropriate error response if X-Username header is missing`() {
      lrsApiMock.stubLearnerByDemographicsExactMatch()

      val executedRequest = webTestClient.post()
        .uri("/learners")
        .headers(setAuthorisation(roles = listOf("ROLE_LEARNER_RECORDS_SEARCH__RO")))
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
        .headers(setAuthorisation(roles = listOf("ROLE_LEARNER_RECORDS_SEARCH__RO")))
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

    @Test
    fun `should emit an event that request is received for findByDemographics `() {
      lrsApiMock.stubLearnerByDemographicsExactMatch()
      webTestClient.post()
        .uri("/learners")
        .headers(setAuthorisation(roles = listOf("ROLE_LEARNER_RECORDS_SEARCH__RO")))
        .header("X-Username", "TestUser")
        .bodyValue(findLearnerByDemographicsRequest)
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

      assertThat(receivedEvent.what).isEqualTo("Read Request Received")
      assertThat(receivedEvent.subjectId).isEqualTo("From TestUser")
      assertThat(receivedEvent.subjectType).isEqualTo("Read")
      assertThat(receivedEvent.who).isEqualTo("TestUser")
      assertThat(receivedEvent.service).isEqualTo("learner-records-api")
      assertThat(receivedEvent.`when`).isBeforeOrEqualTo(Instant.now())
    }
  }
}
