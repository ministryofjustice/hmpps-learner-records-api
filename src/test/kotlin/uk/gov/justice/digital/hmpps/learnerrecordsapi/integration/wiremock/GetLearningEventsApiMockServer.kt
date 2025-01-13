package uk.gov.justice.digital.hmpps.learnerrecordsapi.integration.wiremock

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching
import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

class GetLearningEventsApiMockServer : WireMockServer(8082) {

  private val basePath = "/LearnerServiceR9.svc"

  init {
    this.start()
  }

  private fun readTemplateToString(fileName: String): String {
    val inputStream = javaClass.classLoader.getResourceAsStream("$fileName.xml")
      ?: throw IllegalArgumentException("File not found in resources: $fileName")
    return InputStreamReader(inputStream, StandardCharsets.UTF_8).readText()
  }

  fun stubExactMatchFull() {
    stubFor(
      post(urlPathMatching(basePath))
        .willReturn(
          aResponse()
            .withHeader("Content-Type", "text/xml")
            .withTransformers("response-template")
            .withBody(
              readTemplateToString("get_learning_events_exact_match_full")
            )
            .withStatus(200),
        ),
    )
  }

  fun stubLinkedMatchFull() {
    stubFor(
      post(urlPathMatching(basePath))
        .willReturn(
          aResponse()
            .withHeader("Content-Type", "text/xml")
            .withTransformers("response-template")
            .withBody(
              readTemplateToString("get_learning_events_linked_match_full")
            )
            .withStatus(200),
        ),
    )
  }

  fun stubNotShared() {
    stubFor(
      post(urlPathMatching(basePath))
        .willReturn(
          aResponse()
            .withHeader("Content-Type", "text/xml")
            .withTransformers("response-template")
            .withBody(
              readTemplateToString("get_learning_events_not_shared")
            )
            .withStatus(200),
        ),
    )
  }
  fun stubNotVerified() {
    stubFor(
      post(urlPathMatching(basePath))
        .willReturn(
          aResponse()
            .withHeader("Content-Type", "text/xml")
            .withTransformers("response-template")
            .withBody(
              readTemplateToString("get_learning_events_not_verified")
            )
            .withStatus(200),
        ),
    )
  }

  fun stubPostBadRequest() {
    stubFor(
      post(urlPathMatching(basePath))
        .willReturn(
          aResponse()
            .withStatus(400)
        )
    )
  }

  fun stubPostServerError() {
    stubFor(
      post(urlPathMatching(basePath))
        .willReturn(
          aResponse()
            .withStatus(500)
        )
    )
  }

}

class GetLearningEventsApiExtension : BeforeAllCallback, AfterAllCallback, BeforeEachCallback {
  companion object {
    @JvmField
    val getLearningEventsApiMock = GetLearningEventsApiMockServer()
  }

  override fun beforeAll(context: ExtensionContext): Unit = getLearningEventsApiMock.start()
  override fun beforeEach(context: ExtensionContext): Unit = getLearningEventsApiMock.resetAll()
  override fun afterAll(context: ExtensionContext): Unit = getLearningEventsApiMock.stop()
}
