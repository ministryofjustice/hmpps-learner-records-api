# hmpps-learner-records-api
The hmpps-learner-records-api retrieves Unique Learner Number (ULN) and Personal Learning Record (PLR) for matching
individuals from the Learning Records Service (LRS) data held by the Education and Skills Funding Agency (EFSA) at Department
for Education (DfE).

This repository has been generated from https://github.com/ministryofjustice/hmpps-template-kotlin.

---

## Hosting 

This service is available at:
* Local: `http://localhost:8080/`
* Dev: `https://learner-records-api-dev.hmpps.service.justice.gov.uk/`
* UAT - `https://learner-records-api-uat.hmpps.service.justice.gov.uk/`
* Preprod: tbc
* Prod: tbc

---

## Endpoints

The service provides 2 endpoints to consumers.
* `/learners` - Search for a learner's ULN via their demographic data
* `/learner-events` - Request a learner's learning record via their ULN

### `POST:/learners`
This endpoint is to search for learners by their demographic information.
The search may yield varied results depending on the accuracy of the demographic information and the DfE data available:

* No match
* Too many matches
* Possible matches 
* Exact match
* Linked Learner Found

Assuming a successful search, the response should contain a ULN for each learner found. This ULN may be used on the`/learner-events` endpoint to retrieve their respective PLRs.

Example request body:
```json
{
  "givenName": "Darcie",
  "familyName": "Tucker",
  "dateOfBirth": "1976-08-16",
  "gender": "2",
  "lastKnownPostCode": "CV49EE"
}
```

Example response body:
```json
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
```

Response codes:
* 200 - Success
* 400 - Bad Request, malformed inputs
* 401 - Unauthorised
* 403 - Forbidden

### `POST:/learner-events`
This endpoint is used to request a learner's learning events (or Personal Learning Record [PLR]) by their Unique Learner Number (ULN).

Generally when using a valid ULN there should be no issues with this request, but there are a few possible responses.
* Exact Match
* Linked Learner Match
* Learner opted to not share data
* Learner could not be verified

Example request body:
```json
{
  "givenName": "Sean",
  "familyName": "Findlay",
  "uln": "1174112637",
  "dateOfBirth": "1980-11-01",
  "gender": 1
}
```

Example response body:
```json
{
  "searchParameters": {
    "givenName": "Sean",
    "familyName": "Findlay",
    "uln": "1174112637",
    "dateOfBirth": "1980-11-01",
    "gender": 1
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
```

Response codes:
* 200 - Success
* 400 - Bad Request, malformed inputs
* 401 - Unauthorised
* 403 - Forbidden
---
## API Documentation

OpenAPI documentation is available here - http://localhost:8080/swagger-ui/index.html

---

## Authentication

The hmpps-learner-records-api requires bearer authorization.

The tokens for authenticating with this service need to be requested from hmpps-auth via a `basic` authentication, citing your service's client id and client secret.

You should ensure your service has the appropriate roles present to access this service.

Example header
```
"Authorization": "bearer <token>"
```

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

### LRS Connection Certificate

In order to make a connection to the LRS Development environment (achieved when using the `development` profile) you will require the relevant certificate.

Reach out to the development team if you don't have this. Once downloaded, add the `WebServiceClientCert.pfx` to the project root directory.

Uncomment these two lines in the Dockerfile when running locally:
```
COPY WebServiceClientCert.pfx /app/WebServiceClientCert.pfx
RUN ls -la /app/WebServiceClientCert.pfx
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