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
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RestController
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.lrsapi.response.exceptions.DFEApiDownException
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.lrsapi.response.exceptions.LRSException
import java.io.File
import java.util.concurrent.TimeUnit

// Simply to assist in testing HmppsBoldLrsExceptionHandler, the endpoints are only visible to tests.

@RestController
class TestExceptionResource(
  @Autowired
  private val httpClientConfiguration: HttpClientConfiguration,
) {

  val listOfMethods = listOf("POST")

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
    mockTimeoutServer.enqueue(MockResponse().setBody("Delayed response").setBodyDelay(40, TimeUnit.SECONDS))
    mockTimeoutServer.start()

    val request = Request.Builder()
      .url(mockTimeoutServer.url("/timeout")) // Point to the MockWebServer URL
      .build()

    val response = httpClientConfiguration.sslHttpClient().newCall(request).execute()
    return response.body?.string()
  }

  @PostMapping("/test/unsupported-http-verb")
  fun triggerHttpRequestMethodNotSupportedException(): Nothing = throw HttpRequestMethodNotSupportedException("GET", listOfMethods)

  @PostMapping("/test/test-dfe-api-down")
  fun triggerDFEApiDownException(): String? {
    val mockDfeApiDownTimeServer = MockWebServer()
    val body = "<!DOCTYPE html>\n" +
      "<html>\n" +
      "    <head>\n" +
      "        <title>UnsupportedHttpVerb</title>\n" +
      "    </head>\n" +
      "    <body>\n" +
      "        <h1>The resource doesn't support specified Http Verb.</h1>\n" +
      "        <p>\n" +
      "            <ul>\n" +
      "                <li>HttpStatusCode: 405</li>\n" +
      "                <li>ErrorCode: UnsupportedHttpVerb</li>\n" +
      "                <li>RequestId : 41d0cf65-b01e-002a-75e5-817a3f000000</li>\n" +
      "                <li>TimeStamp : 2025-02-18T09:14:38.3421624Z</li>\n" +
      "            </ul>\n" +
      "        </p>\n" +
      "    </body>\n" +
      "</html>"
    mockDfeApiDownTimeServer.enqueue(MockResponse().setBody(body))
    mockDfeApiDownTimeServer.start()

    val request = Request.Builder()
      .url(mockDfeApiDownTimeServer.url("/test-dfe-api-down")) // Point to the MockWebServer URL
      .build()

    val response = httpClientConfiguration.sslHttpClient().newCall(request).execute()
    throw DFEApiDownException(response.toString())
  }
}
