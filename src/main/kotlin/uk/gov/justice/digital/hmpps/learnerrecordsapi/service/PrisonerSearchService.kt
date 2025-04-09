package uk.gov.justice.digital.hmpps.learnerrecordsapi.service

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.response.PrisonerSearchResponse

@Service
class PrisonerSearchService(
  @Qualifier("prisonerSearchApiWebClient") private val webClient: WebClient,
  private val objectMapper: ObjectMapper,
) {
  fun findPrisonersByPrisonId(prisonId: String, pageSize: Int): List<PrisonerSearchResponse> {
    var currentPage = 0
    var allPrisoners: MutableList<PrisonerSearchResponse> = mutableListOf()
    var totalPages: Int

    do {
      val response = webClient.get()
        .uri("/prisoner-search/prison/$prisonId?page=$currentPage&size=$pageSize")
        .header("Content-Type", "application/json")
        .retrieve()
        .bodyToMono(String::class.java)
        .block()!!

      val contentJson = objectMapper.readTree(response)["content"]
      val prisoners = contentJson.map { objectMapper.treeToValue(it, PrisonerSearchResponse::class.java) }
      allPrisoners.addAll(prisoners)

      totalPages = objectMapper.readTree(response)["totalPages"].asInt()

      currentPage++
    } while (currentPage < totalPages)

    return allPrisoners
  }
}
