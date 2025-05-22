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
import uk.gov.justice.digital.hmpps.learnerrecordsapi.config.Roles.ROLE_LEARNERS_UI
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.request.LearnersRequest
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.response.LearnersResponse
import uk.gov.justice.hmpps.kotlin.common.ErrorResponse

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@Operation(
  summary = "Find learners by demographic information",
  description = "Find learner and ULN by demographic information",
  parameters = [Parameter(name = "X-Username", `in` = ParameterIn.HEADER, required = true)],
  requestBody = RequestBody(
    description = "Demographic details of the learner",
    required = true,
    content = [
      Content(
        mediaType = "application/json",
        schema = Schema(implementation = LearnersRequest::class),
        examples = [
          ExampleObject(
            name = "Exact Match Example",
            value = """
              {
                "givenName": "Sample",
                "familyName": "Testname",
                "dateOfBirth": "1976-08-16",
                "gender": "FEMALE",
                "lastKnownPostCode": "CV49EE"
              }
            """,
          ),
          ExampleObject(
            name = "Possible Match Example",
            value = """
              {
                "givenName": "Sample",
                "familyName": "Tester",
                "dateOfBirth": "1995-06-28",
                "gender": "FEMALE",
                "lastKnownPostCode": "ZZ12ZZ"
              }
            """,
          ),
          ExampleObject(
            name = "Linked Learner Example",
            value = """
              {
                "givenName": "Sample",
                "familyName": "Test",
                "dateOfBirth": "1985-03-27",
                "gender": "MALE",
                "lastKnownPostCode": "AB125EQ"
              }
            """,
          ),
          ExampleObject(
            name = "No Match Example",
            value = """
              {
                "givenName": "Random",
                "familyName": "Name",
                "dateOfBirth": "2000-01-01",
                "gender": "FEMALE",
                "lastKnownPostCode": "CV49EE"
              }
            """,
          ),
        ],
      ),
    ],
  ),
  security = [SecurityRequirement(name = ROLE_LEARNERS_UI)],
  responses = [
    ApiResponse(
      responseCode = "200",
      description = "The request was successful and a response was returned.",
      content = [
        Content(
          mediaType = "application/json",
          schema = Schema(implementation = LearnersResponse::class),
          examples = [
            ExampleObject(
              name = "Exact match Response",
              value = """
                {
                  "searchParameters": {
                      "givenName": "Sample",
                      "familyName": "Testname",
                      "dateOfBirth": "1976-08-16",
                      "gender": "FEMALE",
                      "lastKnownPostcode": "CV49EE"
                  },
                  "responseType": "Exact Match",
                  "matchedLearners": [
                      {
                          "createdDate": "2012-05-25",
                          "lastUpdatedDate": "2012-05-25",
                          "uln": "1026893096",
                          "versionNumber": "1",
                          "title": "Mrs",
                          "givenName": "Sample",
                          "middleOtherName": "Tester",
                          "familyName": "Testname",
                          "preferredGivenName": "Sample",
                          "previousFamilyName": "OLDTESTNAME",
                          "familyNameAtAge16": "TESTNAME",
                          "schoolAtAge16": "Test Strategy School Foundation ",
                          "lastKnownAddressLine1": "1 JOBS LANE",
                          "lastKnownAddressTown": "COVENTRY",
                          "lastKnownAddressCountyOrCity": "WEST MIDLANDS",
                          "lastKnownPostCode": "CV4 9EE",
                          "dateOfAddressCapture": "2009-04-25",
                          "dateOfBirth": "1976-08-16",
                          "placeOfBirth": "Blean ",
                          "gender": "FEMALE",
                          "emailAddress": "sample.testname@aol.compatibilitytest.com",
                          "scottishCandidateNumber": "845759406",
                          "abilityToShare": "1",
                          "learnerStatus": "1",
                          "verificationType": "1",
                          "tierLevel": "0"
                      }
                  ]
              }
            """,
            ),
            ExampleObject(
              name = "Possible match Response",
              value = """
                {
                    "searchParameters": {
                        "givenName": "Sample",
                        "familyName": "Tester",
                        "dateOfBirth": "1995-06-28",
                        "gender": "FEMALE",
                        "lastKnownPostcode": "ZZ12ZZ"
                    },
                    "responseType": "Possible Match",
                    "mismatchedFields": {
                        "lastKnownPostCode": [
                            "NE00 1ND",
                            "SO00 1JX"
                        ]
                    },
                    "matchedLearners": [
                        {
                            "createdDate": "2012-05-25",
                            "lastUpdatedDate": "2012-05-25",
                            "uln": "1964986809",
                            "versionNumber": "1",
                            "title": "Miss",
                            "givenName": "Sample",
                            "middleOtherName": "Example",
                            "familyName": "Tester",
                            "preferredGivenName": "Sample",
                            "familyNameAtAge16": "TESTER",
                            "schoolAtAge16": "Testing Testers School ",
                            "lastKnownAddressLine1": "1 TEST GARDENS",
                            "lastKnownAddressTown": "TESTERS BAY",
                            "lastKnownAddressCountyOrCity": "TYNE AND WEAR",
                            "lastKnownPostCode": "NE00 1ND",
                            "dateOfAddressCapture": "2010-09-07",
                            "dateOfBirth": "1995-06-28",
                            "placeOfBirth": "Chard ",
                            "gender": "FEMALE",
                            "emailAddress": "sample.tester@yahoo.compatibilitytest.co.uk",
                            "scottishCandidateNumber": "820208781",
                            "abilityToShare": "1",
                            "learnerStatus": "1",
                            "verificationType": "5",
                            "tierLevel": "2"
                        },
                        {
                            "createdDate": "2012-05-25",
                            "lastUpdatedDate": "2012-05-25",
                            "uln": "8383558804",
                            "versionNumber": "1",
                            "title": "Miss",
                            "givenName": "Sample",
                            "middleOtherName": "Example",
                            "familyName": "Tester",
                            "preferredGivenName": "Sample",
                            "familyNameAtAge16": "TESTER",
                            "schoolAtAge16": "Testing Testers School ",
                            "lastKnownAddressLine1": "14 TESTER DRIVE",
                            "lastKnownAddressLine2": "MARCHWOOD",
                            "lastKnownAddressTown": "SOUTHAMPTON",
                            "lastKnownAddressCountyOrCity": "HAMPSHIRE",
                            "lastKnownPostCode": "SO00 1JX",
                            "dateOfAddressCapture": "2010-09-07",
                            "dateOfBirth": "1995-06-28",
                            "placeOfBirth": "Chard ",
                            "gender": "FEMALE",
                            "emailAddress": "sample.tester@yahoo.compatibilitytest.co.uk",
                            "scottishCandidateNumber": "820208781",
                            "abilityToShare": "1",
                            "learnerStatus": "1",
                            "verificationType": "5",
                            "tierLevel": "2"
                        }
                    ]
                }
              """,
            ),
            ExampleObject(
              name = "Linked learner found Response",
              value = """
                {
                    "searchParameters": {
                        "givenName": "Sample",
                        "familyName": "Test",
                        "dateOfBirth": "1985-03-27",
                        "gender": "MALE",
                        "lastKnownPostcode": "AB125EQ"
                    },
                    "responseType": "Linked Learner Found",
                    "matchedLearners": [
                        {
                            "createdDate": "2012-05-25",
                            "lastUpdatedDate": "2012-05-25",
                            "uln": "6936002314",
                            "versionNumber": "1",
                            "masterSubstituted": "Y",
                            "title": "Mr",
                            "givenName": "Testing-Sample",
                            "middleOtherName": "AllTesting",
                            "familyName": "Test",
                            "preferredGivenName": "Sample",
                            "familyNameAtAge16": "TESTSURNAME",
                            "schoolAtAge16": "Test Academy Bristol ",
                            "lastKnownAddressLine1": "28 TOLLOHILL SQUARE",
                            "lastKnownAddressTown": "ABERDEEN",
                            "lastKnownAddressCountyOrCity": "ABERDEENSHIRE",
                            "lastKnownPostCode": "AB12 3EQ",
                            "dateOfAddressCapture": "2008-07-13",
                            "dateOfBirth": "1985-03-27",
                            "placeOfBirth": "Whittlesey ",
                            "gender": "MALE",
                            "emailAddress": "testing-sample.test@inbox.compatibilitytest.com",
                            "scottishCandidateNumber": "145589606",
                            "abilityToShare": "1",
                            "learnerStatus": "1",
                            "verificationType": "0",
                            "tierLevel": "0"
                        }
                    ]
                }
              """,
            ),
            ExampleObject(
              name = "No match Response",
              value = """
                {
                    "searchParameters": {
                        "givenName": "Someone",
                        "familyName": "Unknown",
                        "dateOfBirth": "1976-08-16",
                        "gender": "FEMALE",
                        "lastKnownPostcode": "CV49EE"
                    },
                    "responseType": "No Match"
                }
              """,
            ),
            ExampleObject(
              name = "Too many matches Response",
              value = """
                {
                    "searchParameters": {
                        "givenName": "Someone",
                        "familyName": "Common",
                        "dateOfBirth": "1976-08-16",
                        "gender": "FEMALE",
                        "lastKnownPostcode": "CV49EE"
                    },
                    "responseType": "Too Many Matches"
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
annotation class FindByDemographicApi
