package uk.gov.justice.digital.hmpps.learnerrecordsapi.models.request

import jakarta.validation.Validation
import jakarta.validation.ValidatorFactory
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import uk.gov.justice.digital.hmpps.learnerrecordsapi.utils.toISOFormat
import java.time.LocalDate

class LearnerEventsRequestTest {

  private val factory: ValidatorFactory = Validation.buildDefaultValidatorFactory()
  private val validator = factory.validator

  @Test
  fun `valid request should pass validation`() {
    val request = LearnerEventsRequest(
      givenName = "Firstname",
      familyName = "Lastname",
      uln = "1234567890",
      dateOfBirth = "1990-01-01",
      gender = Gender.MALE,
    )

    assertTrue(validator.validate(request).isEmpty())
  }

  @Test
  fun `invalid givenNames and familyNames should fail validation`() {
    val requestWithSymbolsInNames = LearnerEventsRequest(
      givenName = "Firstname!",
      familyName = "L@stname",
      uln = "1234567890",
      dateOfBirth = "1990-01-01",
      gender = Gender.MALE,
    )

    val requestWithTooShortNames = LearnerEventsRequest(
      givenName = "Fi",
      familyName = "La",
      uln = "1234567890",
      dateOfBirth = "1990-01-01",
      gender = Gender.MALE,
    )

    val requestWithTooLongNames = LearnerEventsRequest(
      givenName = "F" + "i".repeat(35),
      familyName = "L" + "a".repeat(35),
      uln = "1234567890",
      dateOfBirth = "1990-01-01",
      gender = Gender.MALE,
    )

    assertTrue(validator.validate(requestWithSymbolsInNames).size == 2)
    assertTrue(validator.validate(requestWithTooShortNames).size == 2)
    assertTrue(validator.validate(requestWithTooLongNames).size == 2)
  }

  @Test
  fun `invalid uln should fail validation`() {
    val requestWithLettersInUln = LearnerEventsRequest(
      givenName = "Firstname",
      familyName = "Lastname",
      uln = "I2E4S67B90",
      dateOfBirth = "1990-01-01",
      gender = Gender.MALE,
    )

    val requestWithTooLongUln = LearnerEventsRequest(
      givenName = "Firstname",
      familyName = "Lastname",
      uln = "1".repeat(50),
      dateOfBirth = "1990-01-01",
      gender = Gender.MALE,
    )

    val requestWithEmptyUln = LearnerEventsRequest(
      givenName = "Firstname",
      familyName = "Lastname",
      uln = "",
      dateOfBirth = "2000-01-01",
      gender = Gender.MALE,
    )

    assertTrue(validator.validate(requestWithLettersInUln).size == 1)
    assertTrue(validator.validate(requestWithTooLongUln).size == 1)
    assertTrue(validator.validate(requestWithEmptyUln).size == 1)
  }

  @Test
  fun `dateOfBirth in the future should fail validation`() {
    val requestWithDateInFuture = LearnerEventsRequest(
      givenName = "Firstname",
      familyName = "Lastname",
      uln = "1234567890",
      dateOfBirth = LocalDate.now().plusDays(1).toISOFormat(),
      gender = Gender.MALE,
    )

    assertTrue(validator.validate(requestWithDateInFuture).size == 1)
  }

  @Test
  fun `invalid gender should fail at runtime with an illegal argument exception`() {
    assertThrows<IllegalArgumentException> {
      LearnerEventsRequest(
        givenName = "Firstname",
        familyName = "Lastname",
        uln = "1234567890",
        dateOfBirth = "1990-01-01",
        gender = Gender.valueOf("ENGLAND"),
      )
    }
    assertThrows<IllegalArgumentException> {
      LearnerEventsRequest(
        givenName = "Firstname",
        familyName = "Lastname",
        uln = "1234567890",
        dateOfBirth = "1990-01-01",
        gender = Gender.valueOf("1"),
      )
    }
  }
}
