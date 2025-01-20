# hmpps-learner-records-api
The hmpps-learner-records-api retrieves Unique Learner Number (ULN) and Personal Learning Record (PLR) for matching
individuals from the Learning Records Service (LRS) data held by the Education and Skills Funding Agency (EFSA) at Department
for Education (DfE).

This repository has been generated from https://github.com/ministryofjustice/hmpps-template-kotlin.

## Endpoints

This service runs on:
* Local: `http://localhost:8080`
* Dev: `https://learner-records-api-dev.hmpps.service.justice.gov.uk/`

### `POST:/learners`
This endpoint is to search for learners by their demographic information.
The search may yield varied results, such as an exact match or possible matches.
The response contains a ULN for each learner found, which may be used on another endpoint to retrieve their respective PLNs.

#### How it works:

The controller `LearnersResource` accepts a request with a `json` body taking the form of our model `FindLearnerByDemographicRequest`.

Example `json` for use with wiremock (local)

```json
{
  "givenName": "Test_Possible_Match_Two_Learners",
  "familyName": "some_name",
  "dateOfBirth": "2022-02-02",
  "gender": "1",
  "lastKnownPostCode": "1234"
}
```

Example `json` for use with Dev API

```json
{
  "givenName": "Darcie",
  "familyName": "Tucker",
  "dateOfBirth": "1976-08-16",
  "gender": "2",
  "lastKnownPostCode": "CV49EE"
}
```

The model asserts correct inputs for the request body using validation annotations and correct datatypes. In the event that inputs are malformed, error handlers will catch this and return a `400 Bad Request`.

In the case that the request is accepted, the controller will then call a service object `LRSService` to handle interfacing the LRS API.

The service `LRSService` has a method called `findLearner` which accepts a single argument of type `FindLearnerByDemographicsRequest`.

The service has an instance of `retrofit` which provides a way to interface with the LRS API.

`retrofit` along with `JAXBConverter`, the models under the `models.lrsapi` package, and the interface `LRSApiServiceInterface` handles all the heavy lifting when it comes to parsing `XML` responses from the LRS API.

When `findLearner` is called, retrofit is used to make a call to the LRS API. The service returns the response as a model `FindLearnerResponse`.

The controller parses this into `json` and responds with that to the user.

### `POST:/plr`

The `/plr` endpoint is used to request a Learner's learning events by their Unique Learner Number (ULN).

Generally when using a valid ULN there should be no issues with this request, but there are a few possible responses.
* Exact Match
* Linked Learner Match
* Learner opted to not share data
* Learner could not be verified

Example JSON Body
```json
{
  "givenName": "Connor",
  "familyName": "Carroll",
  "uln": "4444599390"
}
```

Example JSON Response
```json
{
  "responseCode": "WSRC0004",
  "foundUln": "6936002314",
  "incomingUln": "4444599390",
  "learnerRecord": [
    {
      "id": "1234",
      "achievementProviderUkprn": "11111112",
      "achievementProviderName": "PRIMARY SCHOOL",
      "awardingOrganisationName": "UNKNOWN",
      "qualificationType": "NVQ/GNVQ Key Skills Unit",
      "subjectCode": "1000123A",
      "achievementAwardDate": "2010-01-01",
      "credits": "0",
      "source": "ILR",
      "dateLoaded": "2012-05-31 16:47:04",
      "underDataChallenge": "N",
      "level": "",
      "status": "F",
      "subject": "Key Skills",
      "grade": "9999999999",
      "awardingOrganisationUkprn": "UNKNWN",
      "collectionType": "W",
      "returnNumber": "02",
      "participationStartDate": "2010-09-01",
      "participationEndDate": "2010-09-26"
    }
  ]
}
```

## API Documentation

API documentation is available here - http://localhost:8080/swagger-ui/index.html

## Running the application while developing

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

Details for acquiring this certificate can be found [here](https://github.com/moj-analytical-services/dmet-bold/wiki/RR-Pilot-%E2%80%90-LRS-Microservice).

Once downloaded, add the `WebServiceClientCert.pfx` to the root project directory.


### Starting the service
Use the docker compose file to start the services - HMPPS-Auth, Wiremock for mocking LRS API Response and this microservice.

Run:
```bash
docker-compose down
docker-compose --profile=<local/development> --env-file .env.<dev/local> up --build
```

As mentioned before there are two profiles.

`local` will run:
1. HMPPS-Auth - Use the guidance [here](https://github.com/moj-analytical-services/dmet-bold/wiki/RR-Pilot-%E2%80%90-LRS-API-%E2%80%90-HMPPS-Auth#make-oauth-request)
to get the access_token from the local instance of `hmpps-auth`.
2. Wiremock API
3. This Service

Or `development` will run:
1. This Service

_Instead of the wiremock API, this profile will attempt connection to the LRS Dev environment. This profile will also
require you to connect to the `hmpps-auth` Dev environment._

Follow the guidance [here](https://github.com/moj-analytical-services/dmet-bold/wiki/RR-Pilot-%E2%80%90-LRS-API-%E2%80%90-HMPPS-Auth#client-credentials)
to get the access_token from the `hmpps-auth` Dev environment.

E.g.
```bash
docker-compose --profile=local --env-file .env.local up --build
```
or
```bash
docker-compose --profile=development --env-file .env.development up --build
```

## Running tests:

Ensure no docker services are running as there may be port collisions.

## Testing in a terminal:

Open a terminal either in IntelliJ or in a separate window, ensuring you are in the repo directory.

Either ensure that you have set the environment variables above or if you would prefer not to set them, you can prefix the command with the following: 
```bash
ORG_PASSWORD={pass};PFX_FILE_PASSWORD={pass};SPRING_PROFILES_ACTIVE=local;UK_PRN={pass};VENDOR_ID={pass}
```

Run the following command:

```bash
./gradlew test
```

## Testing using IntelliJ:

If you encounter issues, make sure gradle is set up properly in IntelliJ for this project.

First, right-click the `test` package and select `run ‘Tests in ‘hmpps-temp…’` This will run tests, and you will notice they will fail.

Next, in the top right corner of IntelliJ, to the left of the green play button, click the dropdown and then select `Edit Configurations`.

Select the ‘hmpps-learner-records-api.test’ configuration, ensure that `Run` is populated with `:test`, the gradle project is `kotlin-template-experimental-lrs` and the environment variables, mentioned above, are also set here. Click `Apply` then `OK`.

Again, right click the `test` package and select `run ‘Tests in ‘hmpps-temp…’ - They should now be running in IntelliJ.

Other steps may be required to enable debugging within IntelliJ.