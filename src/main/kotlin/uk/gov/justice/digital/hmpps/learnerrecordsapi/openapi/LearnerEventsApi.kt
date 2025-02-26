package uk.gov.justice.digital.hmpps.learnerrecordsapi.openapi

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.enums.ParameterIn
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.parameters.RequestBody
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.request.LearnerEventsRequest
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.response.LearnerEventsResponse
import uk.gov.justice.hmpps.kotlin.common.ErrorResponse

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@Operation(
  summary = "Get learning events by learner ULN and filter by keyword if provided",
  description = "Get personal learning records and events by a ULN",
  parameters = [Parameter(name = "X-Username", `in` = ParameterIn.HEADER, required = true)],
  requestBody = RequestBody(
    description = "ULN and demographic details of the learner",
    required = true,
    content = [
      Content(
        mediaType = "application/json",
        schema = Schema(implementation = LearnerEventsRequest::class),
        examples = [
          ExampleObject(
            name = "Exact Match",
            value = """
              {
                "givenName": "Sean",
                "familyName": "Findlay",
                "uln": "1174112637",
                "dateOfBirth": "1980-11-01",
                "gender": "MALE",
                "keywords": []
              }
            """,
          ),
          ExampleObject(
            name = "Learner Not Verified",
            value = """
              {
                "givenName": "Test",
                "familyName": "Test",
                "uln": "1174112637",
                "dateOfBirth": "1995-05-12",
                "gender": "FEMALE"
              }
            """,
          ),
          ExampleObject(
            name = "Learner Not Sharing Data",
            value = """
              {
                "givenName": "Rebecca",
                "familyName": "Brennan-Ace",
                "uln": "1026922983",
                "dateOfBirth": "1992-03-27",
                "gender": "FEMALE"
              }
            """,
          ),
          ExampleObject(
            name = "Linked Learner",
            value = """
              {
                "givenName": "Connor",
                "familyName": "Carrol",
                "uln": "4444599390",
                "dateOfBirth": "2000-07-19",
                "gender": "MALE"
              }
            """,
          ),
        ],
      ),
    ],
  ),
  security = [SecurityRequirement(name = "learner-records-search-read-only-role")],
  responses = [
    ApiResponse(
      responseCode = "200",
      description = "The request was successful and a response was returned.",
      content = [
        Content(
          mediaType = "application/json",
          schema = Schema(implementation = LearnerEventsResponse::class),
          examples = [
            ExampleObject(
              name = "Exact Match Response",
              value = """
                {
                  "searchParameters": {
                      "givenName": "Sean",
                      "familyName": "Findlay",
                      "uln": "1174112637",
                      "dateOfBirth": "1980-11-01",
                      "gender": "MALE",
                      "keywords": ["english"]
                  },
                  "responseType": "Exact Match",
                  "foundUln": "1174112637",
                  "incomingUln": "1174112637",
                  "learnerRecord": [
                      {
                          "id": "2931",
                          "achievementProviderUkprn": "10030488",
                          "achievementProviderName": "LUTON PENTECOSTAL CHURCH",
                          "awardingOrganisationName": "UNKNOWN",
                          "qualificationType": "GCSE",
                          "subjectCode": "50079116",
                          "achievementAwardDate": "2011-10-24",
                          "credits": "0",
                          "source": "ILR",
                          "dateLoaded": "2012-05-31 16:47:04",
                          "underDataChallenge": "N",
                          "level": "",
                          "status": "F",
                          "subject": "GCSE in English Literature",
                          "grade": "9999999999",
                          "awardingOrganisationUkprn": "UNKNWN",
                          "collectionType": "W",
                          "returnNumber": "02",
                          "participationStartDate": "2011-10-02",
                          "participationEndDate": "2011-10-24"
                      }
                  ]
              }
              """,
            ),
            ExampleObject(
              name = "Linked Learner Match Response",
              value = """
              {
                "searchParameters": {
                    "givenName": "Connor",
                    "familyName": "Carroll",
                    "uln": "4444599390"
                },
                "responseType": "Linked Learner Match",
                "foundUln": "6936002314",
                "incomingUln": "4444599390",
                "learnerRecord": [
                    {
                        "id": "4284",
                        "achievementProviderUkprn": "10032743",
                        "achievementProviderName": "PRIORSLEE PRIMARY SCHOOL ACADEMY TRUST",
                        "awardingOrganisationName": "UNKNOWN",
                        "qualificationType": "NVQ/GNVQ Key Skills Unit",
                        "subjectCode": "1000323X",
                        "achievementAwardDate": "2010-09-26",
                        "credits": "0",
                        "source": "ILR",
                        "dateLoaded": "2012-05-31 16:47:04",
                        "underDataChallenge": "N",
                        "level": "",
                        "status": "F",
                        "subject": "Key Skills in Application of Number - level 1",
                        "grade": "9999999999",
                        "awardingOrganisationUkprn": "UNKNWN",
                        "collectionType": "W",
                        "returnNumber": "02",
                        "participationStartDate": "2010-09-01",
                        "participationEndDate": "2010-09-26"
                    }
                ]
            }
            """,
            ),
            ExampleObject(
              name = "Learner opted to not share data Response",
              value = """
                {
                  "searchParameters": {
                      "givenName": "John",
                      "familyName": "Smith",
                      "uln": "1026922983"
                  },
                  "responseType": "Learner opted to not share data",
                  "foundUln": "1026922983",
                  "incomingUln": "1026922983",
                  "learnerRecord": []
              }
              """,
            ),
            ExampleObject(
              name = "Learner could not be verified Response",
              value = """
                {
                  "searchParameters": {
                      "givenName": "John",
                      "familyName": "Smith",
                      "uln": "1174112637"
                  },
                  "responseType": "Learner could not be verified",
                  "foundUln": "",
                  "incomingUln": "1174112637",
                  "learnerRecord": []
              }
              """,
            ),
          ],
        ),
      ],
    ),
    ApiResponse(
      responseCode = "401",
      description = "Unauthorized to access this endpoint",
      content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
    ),
    ApiResponse(
      responseCode = "403",
      description = "Forbidden to access this endpoint",
      content = [Content(mediaType = "application/json", schema = Schema(implementation = ErrorResponse::class))],
    ),
  ],
)
annotation class LearnerEventsApi
