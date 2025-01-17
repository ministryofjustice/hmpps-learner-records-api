package uk.gov.justice.digital.hmpps.learnerrecordsapi.config

import com.google.gson.JsonParseException
import org.mockito.Mockito
import org.springframework.core.MethodParameter
import org.springframework.http.HttpInputMessage
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.validation.BindException
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.lrsapi.response.exceptions.LRSException
import java.io.File

// Simply to assist in testing HmppsBoldLrsExceptionHandler, the endpoints are only visible to tests.

@RestController
class TestExceptionResource {

  @PostMapping("/test/validation")
  fun triggerValidationException(): Nothing = throw MethodArgumentNotValidException(
    MethodParameter(this::class.java.getMethod("triggerValidationException"), -1),
    BindException(Any(), "testObject").apply {
      addError(FieldError("testObject", "testField", "Validation failed"))
    },
  )

  @PostMapping("/test/missing-mandatory-field")
  fun triggerMissingFieldException(): Nothing = throw HttpMessageNotReadableException(
    "JSON parse error: Missing Field",
    JsonParseException("Missing Field"),
    Mockito.mock(HttpInputMessage::class.java),
  )

  @PostMapping("/test/lrs-error")
  fun triggerLRSException(): Nothing = throw LRSException()

  @PostMapping("/test/forbidden")
  fun triggerForbiddenException(): Nothing = throw AccessDeniedException(file = File(""))

  @PostMapping("/test/generic-exception")
  fun triggerGenericException(): Nothing = throw Exception()
}
