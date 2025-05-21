# hmpps-learner-records-api
The hmpps-learner-records-api retrieves Unique Learner Number (ULN) and Personal Learning Record (PLR) for matching
individuals from the Learning Records Service (LRS) data held by the Education and Skills Funding Agency (EFSA) at Department
for Education (DfE), and stores match data in a database for downstream use.

This repository has been generated from https://github.com/ministryofjustice/hmpps-template-kotlin.

### Team
This application is in development by the hmpps-lrs-devs team under the Education Skills & Work Team.

### API external dependencies
This API is dependent on data from the Department for Education [Learning Records Service API](https://www.gov.uk/government/publications/lrs-maintenance-schedule/lrs-maintenance-schedule-for-2024).

### API consumers
The following are the known consumers of this API. Any changes to this API - especially breaking or potentially breaking
changes should consider the use case of these consumers.

* `hmpps-match-learner-record-ui` - Match a learners record UI

---

## Configuring the project

### JDK
To develop and build the application locally you will need JDK 21 installed and configured.

### Ktlint formatting
Ktlint is used to format the source code and a task runs in the GitHub build action to check the formatting.

You should run the following commands to make sure that the source code is formatted locally before it breaks the GitHub build action.

#### Apply ktlint formatting rules to Intellij
`./gradlew ktlintApplyToIdea`

Or to apply to all Intellij projects:

`./gradlew ktlintApplyToIdeaGlobally`

#### Run ktlint formatter on git commit
`./gradlew addKtlintFormatGitPreCommitHook`

## Hosting 

This service is available at:
* Local: http://localhost:8080/
* Dev: https://learner-records-api-dev.hmpps.service.justice.gov.uk/
* Preprod: https://learner-records-api-preprod.hmpps.service.justice.gov.uk/
* Prod: https://learner-records-api.hmpps.service.justice.gov.uk/

### Health
The application has a health endpoint found at `/health` which indicates if the app is running and is healthy.
The application has a ping endpoint found at `health/ping` which indicates that the app is responding to requests.

---

## Headers
### Username

The hmpps-learner-records-api requires consumer services to identify their users when connecting to our service.

Where possible when making a request you should identify the user (not the service) in the custom header `X-Username`.

The value should be a unique identifier for the user, either a name or an email address.

Example header:
```
"X-Username": "john.doe@justice.gov.uk"
```

### Authentication

The hmpps-learner-records-api requires bearer authorization.

The tokens for authenticating with this service need to be requested from hmpps-auth via a `basic` authentication, citing your service's client id and client secret. You should ensure your service has the appropriate roles present to access this service.

`curl -X POST "https://<hmpps-auth-url>/auth/oauth/token?grant_type=client_credentials" \ -H 'Content-Type: application/json' -H "Authorization: Basic <base64 encoded clientid:clientsecret>"`

Example header:
```
"Authorization": "bearer <token>"
```

---

## Endpoints

The service provides the following endpoints to consumers and requires the role **ROLE_LEARNER_RECORDS_SEARCH__RO**:
* `GET /match/{nomisId}` - Search for a learner's ULN via their NOMIS ID
* `GET /match/{nomisId}/learner-events` - Search for a learner's personal learning record (PLR) via their Nomis ID

The following endpoints are used by the [hmpps-match-learner-record-ui](https://github.com/ministryofjustice/hmpps-match-learner-record-ui) only and uses the role **ROLE_LEARNER_RECORDS__LEARNER_RECORDS_MATCH_UI**:
* `POST /learners` - Search for a learner's ULN via their demographic data
* `POST /learner-events` - Request a learner's personal learning record (PLR) via their ULN
* `POST /match/:nomisId` - Confirm a match between a learner's NOMIS ID and ULN
* `POST /match/:nomisId/no-match` - Confirm a no match for a learner's NOMIS ID
* `POST /match/:nomisId/unmatch` - Confirm an un-match for a learner's NOMIS ID

### `POST:/learners`
This endpoint searches for a learner's ULN by their demographic information.

The search may yield varied results depending on the accuracy of the demographic information and the DfE data available:
* No match
* Too many matches (more than 10 matches)
* Possible matches (between 1-10 matches)
* Exact match
* Linked Learner Found

Assuming a successful search, the response should contain a ULN for each learner found. This ULN, Given Name and Family Name may be used on the`/learner-events` endpoint to retrieve their respective PLRs.

<details>
<summary>Example request body:</summary>
<br>
<pre>
{
  "givenName": "Sample",
  "familyName": "Testname",
  "dateOfBirth": "1976-08-16",
  "gender": "FEMALE",
  "lastKnownPostCode": "CV49EE", 
  // below are optional fields
  "previousFamilyName": "OLDTESTNAME",
  "schoolAtAge16": "Test Strategy School Foundation ",
  "placeOfBirth": "Blean ",
  "emailAddress": "sample.testname@aol.compatibilitytest.com"
}
</pre>
</details>

<details>
<summary>Example response body:</summary>
<br>
<pre>
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
            "middleOtherName": "Isla",
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
</pre>
</details>

Response codes:
* 200 - Success
* 400 - Bad Request, malformed inputs
* 401 - Unauthorised
* 403 - Forbidden

### `POST:/learner-events`
This endpoint request a learner's learning events/personal learning record (PLR) by their Unique Learner Number (ULN), Given Name and Family Name.

Generally when using a valid ULN, Given Name and Family Name there should be no issues with this request, but there are a few possible responses:
* Exact Match
* Linked Learner Match
* Learner opted to not share data
* Learner could not be verified

<details>
<summary>Example request body:</summary>
<br>
<pre>
{
  "givenName": "Sean",
  "familyName": "Findlay",
  "uln": "1174112637",
  // below are optional fields
  "dateOfBirth": "1980-11-01",
  "gender": "MALE"
}
</pre>
</details>

<details>
<summary>Example response body:</summary>
<br>
<pre>
{
  "searchParameters": {
    "givenName": "Sean",
    "familyName": "Findlay",
    "uln": "1174112637",
    "dateOfBirth": "1980-11-01",
    "gender": "MALE"
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
</pre>
</details>

Response codes:
* 200 - Success
* 400 - Bad Request, malformed inputs
* 401 - Unauthorised
* 403 - Forbidden

### `POST:/match/:nomisId`
This endpoint is to confirm a match between a learner's NOMIS ID and ULN.
The givenName and familyName should be as per the LRS data for a match.
The match will be saved as a `MatchEntity` in the database.

<details>
<summary>Example request body for a match:</summary>
<br>
<pre>
{
  "matchingUln": "1234567890",
  "givenName": "John",
  "familyName": "Smith",
  "matchType": "Possible match",
  "countOfReturnedUlns": "2"
}
</pre>
</details>

Response codes:
* 201 - Created
* 400 - Bad Request, malformed ULN or json body
* 401 - Unauthorised
* 403 - Forbidden
* 409 - Conflict, ULN is already matched
* 500 - Likely that the database is unreachable

### `POST:/match/:nomisId/no-match`
This endpoint is to confirm a no match for a learner's NOMIS ID.
This will be saved as a `MatchEntity` in the database.

<details>
<summary>Example request body for a no match:</summary>
<br>
<pre>
{
  "matchType": "No match returned from LRS",
  "countOfReturnedUlns": "0"
}
</pre>
</details>

Response codes:
* 201 - Created
* 400 - Bad Request, malformed json body
* 401 - Unauthorised
* 403 - Forbidden
* 500 - Likely that the database is unreachable

### `POST:/match/:nomisId/unmatch`
This endpoint is to confirm an un-match for a learner's NOMIS ID.
This will be saved as a `MatchEntity` in the database.

There is no request body for this end-point.

Response codes:
* 201 - Created
* 400 - Bad Request, malformed json body
* 401 - Unauthorised
* 403 - Forbidden
* 500 - Likely that the database is unreachable

### `GET:/match/:nomisId`
This endpoint is to search the database for a match given a NOMIS ID.

The response will be OK (200) if the NOMIS ID exists and NOT_FOUND (404) if it does not exist in the database. 

In the response body, the `status` will have one of the following values as explained below.
* `Found` = A match has been found for `nomisId` in the database and a ULN is in `matchedUln`
* `NoMatch` = A match has been found for `nomisId` in the database but no ULN has been matched in `matchedUln`
* `NotFound` = No match has been found for `nomisId` in the database and only the status is returned in the response body

<details>
<summary>Example response body:</summary>
<br>
<pre>
{
  "matchedUln": "1234567890",
  "givenName": "Charlie",
  "familyName": "Brown",
  "status": "Found"
}
</pre>
</details>

<details>
<summary>Example response body for NotFound:</summary>
<br>
<pre>
{
  "status": "NotFound"
}
</pre>
</details>

Response codes:
* 200 - Success
* 400 - Bad Request, malformed inputs
* 401 - Unauthorised
* 403 - Forbidden
* 404 - Not Found

### `GET:/match/:nomisId/learner-events`
This endpoint searches the database for a match given a NOMIS ID and then requests for a learner's personal learning record (PLR) using ULN, Given Name and Family Name. 

The possible responses are:
* Exact Match
* Linked Learner Match
* Learner opted to not share data
* Learner could not be verified
* Match not found exception - a match has not been found for `nomisId` in the database
* Match not possible exception - a match has been found for `nomisId` in the database but no ULN, Given Name, and Family Name has been matched for requesting the learner events

<details>
<summary>Example response body:</summary>
<br>
<pre>
{
  "searchParameters": {
    "givenName": "Sean",
    "familyName": "Findlay",
    "uln": "1174112637"
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
</pre>
</details>

Response codes:
* 200 - Success
* 400 - Bad Request
* 401 - Unauthorised
* 403 - Forbidden
* 404 - Not Found

---

## Database

The service uses a postgres database alongside flyaway migrations to create and populate the database. Any changes made to the [MatchEntity](https://github.com/ministryofjustice/hmpps-learner-records-api/blob/main/src/main/kotlin/uk/gov/justice/digital/hmpps/learnerrecordsapi/models/db/MatchEntity.kt) need to be applied to the database too using a flyway migration script (see the SQL files [here](https://github.com/ministryofjustice/hmpps-learner-records-api/tree/main/src/main/resources/db/migration)).

---

## API Documentation

OpenAPI documentation is available at:
* Local: http://localhost:8080/swagger-ui/index.html
* Dev: https://learner-records-api-dev.hmpps.service.justice.gov.uk/swagger-ui/index.html
* Preprod: https://learner-records-api-preprod.hmpps.service.justice.gov.uk/swagger-ui/index.html 
* Prod: https://learner-records-api.hmpps.service.justice.gov.uk/swagger-ui/index.html

---

## Running the application

The hmpps-learner-records-api is generally run via docker so ensure you have docker or docker desktop installed and running.

### Profiles

When run on local developer machines there are two profiles available to run with that include varying levels of supporting services.

#### `local`

This will set up all services required to access the API as local instances and will ensure connections between those within the docker container.
There should be no external connections made.

Services run:
1. HMPPS-Auth (for authentication)
2. Wiremock API (for LRS)
3. hmpps-learner-records-api (this service)

#### `development`

Running in the development profile will only spin up the hmpps-learner-records-api and make connections to the dev environment hosted versions of hmpps-auth and the DfE LRS environment.

Services run:
1. hmpps-learner-records-api (this service)

### Environment Variables

Secrets are stored in the .env file for Local `.env.local` and Development `.env.development`.
Ask a member of the Development team for the values for these fields place it in the root project directory.
```
UK_PRN=
ORG_PASSWORD=
VENDOR_ID=
PFX_FILE_PASSWORD=
SPRING_PROFILES_ACTIVE=
```

### LRS SSL Client Certificate

In order to make a connection to the LRS Development environment (achieved when using the `development` profile) you will require the relevant certificate.

Reach out to the development team if you don't have this. Once downloaded, add the `WebServiceClientCert.pfx` to the project root directory.

Uncomment this line in the Dockerfile when running locally:
```
COPY WebServiceClientCert.pfx /app/WebServiceClientCert.pfx
```

### Starting the service

Use the docker compose file to start the services including your chosen profile.

Run:
```bash
docker-compose down
docker-compose --profile=<local/development> --env-file .env.<dev/local> up --build
```

Local:
```bash
docker-compose --profile=local --env-file .env.local up --build
```
Development:
```bash
docker-compose --profile=development --env-file .env.development up --build
```

---

### Running in IntelliJ

To run the service in IntelliJ, add the following line to your `hosts` file
(located in `/etc/` on a Mac) and restart your machine.

```
127.0.0.1       localstack-sqs
```

Make sure all containers are running except `hmpps-learner-records-api`.
Add the following lines to `.env.local`.

```
lrs.base-url=http://localhost:8080
lrs.pfx-path=WebServiceClientCert.pfx
```

Finally modify the run configuration for the main class so that
it uses `.env.local` for environment variables.

---

## Running tests:

Ensure no docker services are running as there may be port collisions.

### Testing in a terminal:

Open a terminal either in IntelliJ or in a separate window, ensuring you are in the repo directory.

Either ensure that you have set the environment variables above or if you would prefer not to set them, you can prefix the command with the following: 
```bash
ORG_PASSWORD={pass};PFX_FILE_PASSWORD={pass};SPRING_PROFILES_ACTIVE=local;UK_PRN={pass};VENDOR_ID={pass}
```

Run the following command:

```bash
./gradlew test
```

### Testing using IntelliJ:

If you encounter issues, make sure gradle is set up properly in IntelliJ for this project.

First, right-click the `test` package and select `run ‘Tests in ‘hmpps-temp…’` This will run tests, and you will notice they will fail.

Next, in the top right corner of IntelliJ, to the left of the green play button, click the dropdown and then select `Edit Configurations`.

Select the ‘hmpps-learner-records-api.test’ configuration, ensure that `Run` is populated with `:test`, the gradle project is `hmpps-learner-records-api` and the environment variables, mentioned above, are also set here. Click `Apply` then `OK`.

Again, right click the `test` package and select `run ‘Tests in ‘hmpps-temp…’ - They should now be running in IntelliJ.

Other steps may be required to enable debugging within IntelliJ.