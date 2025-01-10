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

class LRSApiMockServer : WireMockServer(8082) {

  private val basePath = "/LearnerService.svc"

  init {
    this.start()
  }

  private fun readTemplateToString(fileName: String): String {
    val inputStream = javaClass.classLoader.getResourceAsStream("$fileName.xml")
      ?: throw IllegalArgumentException("File not found in resources: $fileName")
    return InputStreamReader(inputStream, StandardCharsets.UTF_8).readText()
  }

  fun stubExactMatch() {
    stubFor(
      post(urlPathMatching(basePath))
        .willReturn(
          aResponse()
            .withHeader("Content-Type", "text/xml")
            .withTransformers("response-template")
            .withBody(
              readTemplateToString("exact_match_ful"),
            )
            .withStatus(200),
        ),
    )
  }

  fun stubPossibleMatchTwoLearners() {
    stubFor(
      post(urlPathMatching(basePath))
        .willReturn(
          aResponse()
            .withHeader("Content-Type", "text/xml")
            .withTransformers("response-template")
            .withBody(
              readTemplateToString("possible_match_two_learners_ful"),
            )
            .withStatus(200),
        ),
    )
  }

  fun stubNoMatch() {
    stubFor(
      post(urlPathMatching(basePath))
        .willReturn(
          aResponse()
            .withHeader("Content-Type", "text/xml")
            .withTransformers("response-template")
            .withBody(
              readTemplateToString("no_match_ful"),
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
            .withStatus(400),
        ),
    )
  }

  fun stubPostServerError() {
    stubFor(
      post(urlPathMatching(basePath))
        .willReturn(
          aResponse()
            .withStatus(500),
        ),
    )
  }
}

class LRSApiExtension :
  BeforeAllCallback,
  AfterAllCallback,
  BeforeEachCallback {
  companion object {
    @JvmField
    val lrsApiMock = LRSApiMockServer()
  }

  override fun beforeAll(context: ExtensionContext): Unit = lrsApiMock.start()
  override fun beforeEach(context: ExtensionContext): Unit = lrsApiMock.resetAll()
  override fun afterAll(context: ExtensionContext): Unit = lrsApiMock.stop()
}
