package uk.gov.justice.digital.hmpps.learnerrecordsapi.config


import com.google.gson.JsonParseException
import org.springframework.core.MethodParameter
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpInputMessage
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.validation.BindException
import org.springframework.validation.FieldError
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import java.io.ByteArrayInputStream

@RestController
class TestExceptionResource {

  @PostMapping("/test/validation")
  fun triggerValidationException(): String {
    val methodParameter = MethodParameter(
      this::class.java.getMethod("triggerValidationException"),
      -1
    )

    val bindException = BindException(Any(), "testObject")
    bindException.addError(FieldError("testObject", "testField", "Validation failed"))

    throw MethodArgumentNotValidException(methodParameter, bindException)
  }

  @PostMapping("/test/missing-mandatory-field")
  fun triggerMissingFieldException(): String {
    val mockHttpInputMessage = object : HttpInputMessage {
      override fun getHeaders(): HttpHeaders = HttpHeaders.EMPTY
      override fun getBody() = ByteArrayInputStream("{}".toByteArray())
    }
    val exception = JsonParseException("JSON parse error: Missing Field")
    throw HttpMessageNotReadableException(exception.message.orEmpty(), exception, mockHttpInputMessage)
  }

}
