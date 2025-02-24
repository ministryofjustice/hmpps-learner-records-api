package uk.gov.justice.digital.hmpps.learnerrecordsapi.models.request

import jakarta.validation.Validation
import jakarta.validation.ValidatorFactory
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ConfirmMatchRequestTest {

  private val factory: ValidatorFactory = Validation.buildDefaultValidatorFactory()
  private val validator = factory.validator

  @Test
  fun `valid uln should pass validation`() {
    val request = ConfirmMatchRequest(
      matchingUln = "1234567890",
      givenName = "John",
      familyName = "Smith",
      dateOfBirth = "1990-01-01",
      gender = "MALE",
    )

    assertTrue(validator.validate(request).isEmpty())
  }

  @Test
  fun `invalid uln should fail validation`() {
    val request = ConfirmMatchRequest(
      matchingUln = "1234567890abcdedf",
      givenName = "John",
      familyName = "Smith",
      dateOfBirth = "1990-01-01",
      gender = "MALE",
    )

    assertTrue(validator.validate(request).size == 1)
  }
}
