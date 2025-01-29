package uk.gov.justice.digital.hmpps.learnerrecordsapi.openapi

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.ExampleObject
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.parameters.RequestBody
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.request.LearnersRequest
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.response.LearnersResponse
import uk.gov.justice.hmpps.kotlin.common.ErrorResponse
import java.nio.file.Files
import java.nio.file.Paths
import kotlin.text.Charsets.UTF_8

fun readResourceFile(fileName: String): String {
  val resourcePath = Paths.get("src/test/resources", fileName)
  return Files.readString(resourcePath, UTF_8)
}

@Target(AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@Operation(
  summary = "Find learners by demographic information",
  description = "Find possible or exact matches for learners by demographic information",
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
                "givenName": "Darcie",
                "familyName": "Tucker",
                "dateOfBirth": "1976-08-16",
                "gender": "2",
                "lastKnownPostCode": "CV49EE"
              }
            """,
          ),
          ExampleObject(
            name = "Possible Match Example",
            value = """
              {
                "givenName": "Anna",
                "familyName": "Cheng",
                "dateOfBirth": "1995-06-28",
                "gender": "2",
                "lastKnownPostCode": "ZZ12ZZ"
              }
            """,
          ),
          ExampleObject(
            name = "Linked Learner Example",
            value = """
              {
                "givenName": "Connor",
                "familyName": "Carroll",
                "dateOfBirth": "1985-03-27",
                "gender": "1",
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
                "gender": "2",
                "lastKnownPostCode": "CV49EE"
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
      description = "Successful response, response type may vary - e.g Possible Match.",
      content = [
        Content(
          mediaType = "application/json",
          schema = Schema(implementation = LearnersResponse::class),
          examples = [
            ExampleObject(
              name = "Exact Match Response",
              value = """
                {
                  "searchParameters": {
                      "givenName": "Darcie",
                      "familyName": "Tucker",
                      "dateOfBirth": "1976-08-16",
                      "gender": 2,
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
                          "givenName": "Darcie",
                          "middleOtherName": "Isla",
                          "familyName": "Tucker",
                          "preferredGivenName": "Darcie",
                          "previousFamilyName": "CAMPBELL",
                          "familyNameAtAge16": "TUCKER",
                          "schoolAtAge16": "Mill Hill School Foundation ",
                          "lastKnownAddressLine1": "1 JOBS LANE",
                          "lastKnownAddressTown": "COVENTRY",
                          "lastKnownAddressCountyOrCity": "WEST MIDLANDS",
                          "lastKnownPostCode": "CV4 9EE",
                          "dateOfAddressCapture": "2009-04-25",
                          "dateOfBirth": "1976-08-16",
                          "placeOfBirth": "Blean ",
                          "gender": "2",
                          "emailAddress": "darcie.tucker@aol.compatibilitytest.com",
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
              name = "Possible Match Response",
              value = """
                {
                    "searchParameters": {
                        "givenName": "Anna",
                        "familyName": "Cheng",
                        "dateOfBirth": "1995-06-28",
                        "gender": 2,
                        "lastKnownPostcode": "ZZ12ZZ"
                    },
                    "responseType": "Possible Match",
                    "mismatchedFields": {
                        "lastKnownPostCode": [
                            "NE26 3ND",
                            "SO40 4JX"
                        ]
                    },
                    "matchedLearners": [
                        {
                            "createdDate": "2012-05-25",
                            "lastUpdatedDate": "2012-05-25",
                            "uln": "1964986809",
                            "versionNumber": "1",
                            "title": "Miss",
                            "givenName": "Anna",
                            "middleOtherName": "Joanna",
                            "familyName": "Cheng",
                            "preferredGivenName": "Anna",
                            "familyNameAtAge16": "CHENG",
                            "schoolAtAge16": "Ellern Mede School ",
                            "lastKnownAddressLine1": "1 ILFRACOMBE GARDENS",
                            "lastKnownAddressTown": "WHITLEY BAY",
                            "lastKnownAddressCountyOrCity": "TYNE AND WEAR",
                            "lastKnownPostCode": "NE26 3ND",
                            "dateOfAddressCapture": "2010-09-07",
                            "dateOfBirth": "1995-06-28",
                            "placeOfBirth": "Chard ",
                            "gender": "2",
                            "emailAddress": "anna.cheng@yahoo.compatibilitytest.co.uk",
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
                            "givenName": "Anna",
                            "middleOtherName": "Joanna",
                            "familyName": "Cheng",
                            "preferredGivenName": "Anna",
                            "familyNameAtAge16": "CHENG",
                            "schoolAtAge16": "Ellern Mede School ",
                            "lastKnownAddressLine1": "14 LARKSPUR DRIVE",
                            "lastKnownAddressLine2": "MARCHWOOD",
                            "lastKnownAddressTown": "SOUTHAMPTON",
                            "lastKnownAddressCountyOrCity": "HAMPSHIRE",
                            "lastKnownPostCode": "SO40 4JX",
                            "dateOfAddressCapture": "2010-09-07",
                            "dateOfBirth": "1995-06-28",
                            "placeOfBirth": "Chard ",
                            "gender": "2",
                            "emailAddress": "anna.cheng@yahoo.compatibilitytest.co.uk",
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
              name = "Linked Learner Found Response",
              value = """
                {
                    "searchParameters": {
                        "givenName": "Connor",
                        "familyName": "Carroll",
                        "dateOfBirth": "1985-03-27",
                        "gender": 1,
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
                            "givenName": "William-Connor",
                            "middleOtherName": "Alistair",
                            "familyName": "Carroll",
                            "preferredGivenName": "Connor",
                            "familyNameAtAge16": "CARROLL",
                            "schoolAtAge16": "Oasis Academy Bristol ",
                            "lastKnownAddressLine1": "28 TOLLOHILL SQUARE",
                            "lastKnownAddressTown": "ABERDEEN",
                            "lastKnownAddressCountyOrCity": "ABERDEENSHIRE",
                            "lastKnownPostCode": "AB12 5EQ",
                            "dateOfAddressCapture": "2008-07-13",
                            "dateOfBirth": "1985-03-27",
                            "placeOfBirth": "Whittlesey ",
                            "gender": "1",
                            "emailAddress": "william-connor.carroll@inbox.compatibilitytest.com",
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
              name = "No Match Response",
              value = """
                {
                    "searchParameters": {
                        "givenName": "Someone",
                        "familyName": "Unknown",
                        "dateOfBirth": "1976-08-16",
                        "gender": 2,
                        "lastKnownPostcode": "CV49EE"
                    },
                    "responseType": "No Match"
                }
              """,
            ),
            ExampleObject(
              name = "Too Many Matches Response",
              value = """
                {
                    "searchParameters": {
                        "givenName": "Someone",
                        "familyName": "Common",
                        "dateOfBirth": "1976-08-16",
                        "gender": 2,
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
