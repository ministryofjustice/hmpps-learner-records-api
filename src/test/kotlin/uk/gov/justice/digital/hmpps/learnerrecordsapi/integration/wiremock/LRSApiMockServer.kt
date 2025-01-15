package uk.gov.justice.digital.hmpps.learnerrecordsapi.integration.wiremock

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.anyUrl
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.urlPathMatching
import org.junit.jupiter.api.extension.AfterAllCallback
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.BeforeEachCallback
import org.junit.jupiter.api.extension.ExtensionContext
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

class LRSApiMockServer : WireMockServer(8082) {

  private val basePathLearnerByDemographics = "/LearnerService.svc"
  private val basePathLearningEvents = "/LearnerServiceR9.svc"

  init {
    this.start()
  }

  private fun readTemplateToString(fileName: String): String {
    val inputStream = javaClass.classLoader.getResourceAsStream("$fileName.xml")
      ?: throw IllegalArgumentException("File not found in resources: $fileName")
    return InputStreamReader(inputStream, StandardCharsets.UTF_8).readText()
  }

  fun stubLearningEventsExactMatchFull() {
    stubFor(
      post(urlPathMatching(basePathLearningEvents))
        .willReturn(
          aResponse()
            .withHeader("Content-Type", "text/xml")
            .withTransformers("response-template")
            .withBody(
              readTemplateToString("get_learning_events_exact_match_full"),
            )
            .withStatus(200),
        ),
    )
  }

  fun stubLearningEventsLinkedMatchFull() {
    stubFor(
      post(urlPathMatching(basePathLearningEvents))
        .willReturn(
          aResponse()
            .withHeader("Content-Type", "text/xml")
            .withTransformers("response-template")
            .withBody(
              readTemplateToString("get_learning_events_linked_match_full"),
            )
            .withStatus(200),
        ),
    )
  }

  fun stubLearningEventsNotShared() {
    stubFor(
      post(urlPathMatching(basePathLearningEvents))
        .willReturn(
          aResponse()
            .withHeader("Content-Type", "text/xml")
            .withTransformers("response-template")
            .withBody(
              readTemplateToString("get_learning_events_not_shared"),
            )
            .withStatus(200),
        ),
    )
  }
  fun stubLearningEventsNotVerified() {
    stubFor(
      post(urlPathMatching(basePathLearningEvents))
        .willReturn(
          aResponse()
            .withHeader("Content-Type", "text/xml")
            .withTransformers("response-template")
            .withBody(
              readTemplateToString("get_learning_events_not_verified"),
            )
            .withStatus(200),
        ),
    )
  }

  fun stubLearnerByDemographicsExactMatch() {
    stubFor(
      post(urlPathMatching(basePathLearnerByDemographics))
        .willReturn(
          aResponse()
            .withHeader("Content-Type", "text/xml")
            .withTransformers("response-template")
            .withBody(
              readTemplateToString("find_by_demographic_exact_match_ful"),
            )
            .withStatus(200),
        ),
    )
  }

  fun stubLearnerByDemographicsPossibleMatchTwoLearners() {
    stubFor(
      post(urlPathMatching(basePathLearnerByDemographics))
        .willReturn(
          aResponse()
            .withHeader("Content-Type", "text/xml")
            .withTransformers("response-template")
            .withBody(
              readTemplateToString("find_by_demographic_possible_match_two_learners_ful"),
            )
            .withStatus(200),
        ),
    )
  }

  fun stubLearnerByDemographicsNoMatch() {
    stubFor(
      post(urlPathMatching(basePathLearnerByDemographics))
        .willReturn(
          aResponse()
            .withHeader("Content-Type", "text/xml")
            .withTransformers("response-template")
            .withBody(
              readTemplateToString("find_by_demographic_no_match_ful"),
            )
            .withStatus(200),
        ),
    )
  }

  fun stubLearnerByDemographicsLinkedLearner() {
    stubFor(
      post(urlPathMatching(basePathLearnerByDemographics))
        .willReturn(
          aResponse()
            .withHeader("Content-Type", "text/xml")
            .withTransformers("response-template")
            .withBody(
              readTemplateToString("find_by_demographic_linked_learner_ful"),
            )
            .withStatus(200),
        ),
    )
  }

  fun stubLearnerByDemographicsTooManyMatches() {
    stubFor(
      post(urlPathMatching(basePathLearnerByDemographics))
        .willReturn(
          aResponse()
            .withHeader("Content-Type", "text/xml")
            .withTransformers("response-template")
            .withBody(
              readTemplateToString("find_by_demographic_too_many_matches_ful"),
            )
            .withStatus(200),
        ),
    )
  }

  fun stubLearnerByDemographicsPostServerErrorNoDetail() {
    stubFor(
      post(anyUrl())
        .willReturn(
          aResponse()
            .withStatus(500),
        ),
    )
  }

  fun stubLearnerByDemographicsPostServerError() {
    stubFor(
      post(anyUrl())
        .willReturn(
          aResponse()
            .withStatus(500).withBody(readTemplateToString("error_ful")),
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
