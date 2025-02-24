package uk.gov.justice.digital.hmpps.learnerrecordsapi.models.request

import jakarta.validation.Validation
import jakarta.validation.ValidatorFactory
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import uk.gov.justice.digital.hmpps.learnerrecordsapi.utils.toISOFormat
import java.time.LocalDate

class LearnersRequestTest {

  private val factory: ValidatorFactory = Validation.buildDefaultValidatorFactory()
  private val validator = factory.validator

  @Test
  fun `valid request should pass validation`() {
    val request = LearnersRequest(
      givenName = "Firstname",
      familyName = "Lastname",
      dateOfBirth = "1990-01-01",
      gender = Gender.valueOf("MALE"),
      lastKnownPostCode = "NE2 2AS",
      emailAddress = "test@example.com",
    )

    assertTrue(validator.validate(request).isEmpty())
  }

  @Test
  fun `invalid givenNames, familyNames, and previousFamilyNames should fail validation`() {
    val requestWithSymbolsInNames = LearnersRequest(
      givenName = "Firstname!",
      familyName = "L@stname",
      previousFamilyName = "Previ0us",
      dateOfBirth = "1990-01-01",
      gender = Gender.MALE,
      lastKnownPostCode = "NE2 2AS",
    )

    val requestWithTooShortNames = LearnersRequest(
      givenName = "Fi",
      familyName = "La",
      previousFamilyName = "Pr",
      dateOfBirth = "1990-01-01",
      gender = Gender.MALE,
      lastKnownPostCode = "NE2 2AS",
    )

    val requestWithTooLongNames = LearnersRequest(
      givenName = "F" + "i".repeat(35),
      familyName = "L" + "i".repeat(35),
      previousFamilyName = "P" + "r".repeat(35),
      dateOfBirth = "1990-01-01",
      gender = Gender.MALE,
      lastKnownPostCode = "NE2 2AS",
    )

    assertTrue(validator.validate(requestWithSymbolsInNames).size == 3)
    assertTrue(validator.validate(requestWithTooShortNames).size == 3)
    assertTrue(validator.validate(requestWithTooLongNames).size == 3)
  }

  @Test
  fun `invalid lastKnownPostCode should fail validation`() {
    val requestWithInvalidPostCode = LearnersRequest(
      givenName = "Firstname",
      familyName = "Lastname",
      dateOfBirth = "1990-01-01",
      gender = Gender.MALE,
      lastKnownPostCode = "INVALID123",
    )

    assertTrue(validator.validate(requestWithInvalidPostCode).size == 1)
  }

  @Test
  fun `invalid email should fail validation`() {
    val requestWithInvalidEmail = LearnersRequest(
      givenName = "Firstname",
      familyName = "Lastname",
      dateOfBirth = "1990-01-01",
      gender = Gender.MALE,
      lastKnownPostCode = "NE2 2AS",
      emailAddress = "invalid-email",
    )

    assertTrue(validator.validate(requestWithInvalidEmail).size == 1)
  }

  @Test
  fun `dateOfBirth in the future should fail validation`() {
    val requestWithDateInFuture = LearnersRequest(
      givenName = "Firstname",
      familyName = "Lastname",
      dateOfBirth = LocalDate.now().plusDays(1).toISOFormat(),
      gender = Gender.MALE,
      lastKnownPostCode = "NE2 2AS",
    )
    assertTrue(validator.validate(requestWithDateInFuture).size == 1)
  }

  @Test
  fun `too long school should fail validation`() {
    val requestWithLongSchool = LearnersRequest(
      givenName = "Firstname",
      familyName = "Lastname",
      dateOfBirth = "1990-01-01",
      gender = Gender.MALE,
      lastKnownPostCode = "NE2 2AS",
      emailAddress = "test@example.com",
      schoolAtAge16 = "A" + "b".repeat(300),
    )

    assertTrue(validator.validate(requestWithLongSchool).size == 1)
  }

  @Test
  fun `too long place of birth should fail validation`() {
    val requestWithLongSchool = LearnersRequest(
      givenName = "Firstname",
      familyName = "Lastname",
      dateOfBirth = "1990-01-01",
      gender = Gender.MALE,
      lastKnownPostCode = "NE2 2AS",
      emailAddress = "test@example.com",
      placeOfBirth = "A" + "b".repeat(40),
    )
    assertTrue(validator.validate(requestWithLongSchool).size == 1)
  }
}
