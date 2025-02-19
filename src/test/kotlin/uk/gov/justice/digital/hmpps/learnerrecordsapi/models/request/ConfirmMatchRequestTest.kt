package uk.gov.justice.digital.hmpps.learnerrecordsapi.models.request

import jakarta.validation.Validation
import jakarta.validation.ValidatorFactory
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class ConfirmMatchRequestTest {

  private val factory: ValidatorFactory = Validation.buildDefaultValidatorFactory()
  private val validator = factory.validator

  @Test
  fun `valid request should pass validation`() {
    val request = ConfirmMatchRequest(
      nomisId = "A1417AE",
      matchingUln = "1234567890",
    )

    assertTrue(validator.validate(request).isEmpty())
  }

  @Test
  fun `invalid uln should fail validation`() {
    val request = ConfirmMatchRequest(
      nomisId = "A1417AE",
      matchingUln = "1234567890abcdedf",
    )

    assertTrue(validator.validate(request).size == 1)
  }

  @Test
  fun `invalid nomisId should fail validation`() {
    val request = ConfirmMatchRequest(
      nomisId = "A1424357AE",
      matchingUln = "1234567890",
    )

    assertTrue(validator.validate(request).size == 1)
  }
}
