package uk.gov.justice.digital.hmpps.learnerrecordsapi.config

import com.google.gson.JsonParseException
import okhttp3.Request
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.MockWebServer
import org.mockito.Mockito
import org.springframework.beans.factory.annotation.Autowired
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
import java.util.concurrent.TimeUnit

// Simply to assist in testing HmppsBoldLrsExceptionHandler, the endpoints are only visible to tests.

@RestController
class TestExceptionResource(
  @Autowired
  private val httpClientConfiguration: HttpClientConfiguration,
) {

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

  @PostMapping("/test/okhttp-timeout")
  fun triggerOkhttpTimeout(): String? {
    val mockTimeoutServer = MockWebServer()
    mockTimeoutServer.enqueue(MockResponse().setBody("Delayed response").setBodyDelay(15, TimeUnit.SECONDS))
    mockTimeoutServer.start()

    val request = Request.Builder()
      .url(mockTimeoutServer.url("/timeout")) // Point to the MockWebServer URL
      .build()

    val response = httpClientConfiguration.buildSSLHttpClient().newCall(request).execute()
    return response.body?.string()
  }
}
