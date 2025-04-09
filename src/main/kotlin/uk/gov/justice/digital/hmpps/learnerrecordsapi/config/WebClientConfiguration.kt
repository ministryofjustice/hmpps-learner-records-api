package uk.gov.justice.digital.hmpps.learnerrecordsapi.config

import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.codec.ClientCodecConfigurer
import org.springframework.security.oauth2.client.AuthorizedClientServiceOAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction
import org.springframework.web.reactive.function.client.ExchangeStrategies
import org.springframework.web.reactive.function.client.WebClient

@Configuration
class WebClientConfiguration(
  @Value("\${hmpps-auth.url}") private val oauthApiUrl: String,
  @Value("\${prisoner.search.api.endpoint.url}") private val prisonerSearchApiBaseUrl: String,
  private val webClientBuilder: WebClient.Builder,
) {
  @Bean
  fun oauthApiHealthWebClient(): WebClient = WebClient.builder().baseUrl(oauthApiUrl).build()

  @Bean
  fun authorizedClientManager(
    clientRegistrationRepository: ClientRegistrationRepository,
    oAuth2AuthorizedClientService: OAuth2AuthorizedClientService,
  ): OAuth2AuthorizedClientManager? {
    val authorizedClientProvider = OAuth2AuthorizedClientProviderBuilder.builder().clientCredentials().build()
    val authorizedClientManager =
      AuthorizedClientServiceOAuth2AuthorizedClientManager(clientRegistrationRepository, oAuth2AuthorizedClientService)
    authorizedClientManager.setAuthorizedClientProvider(authorizedClientProvider)
    return authorizedClientManager
  }

  @Bean
  fun prisonerSearchApiWebClient(authorizedClientManager: OAuth2AuthorizedClientManager): WebClient {
    val oauth2Client = ServletOAuth2AuthorizedClientExchangeFilterFunction(authorizedClientManager)
    oauth2Client.setDefaultClientRegistrationId("prisoner-search-api")
    val exchangeStrategies =
      ExchangeStrategies.builder()
        .codecs { configurer: ClientCodecConfigurer -> configurer.defaultCodecs().maxInMemorySize(-1) }
        .build()
    return webClientBuilder
      .baseUrl(prisonerSearchApiBaseUrl)
      .apply(oauth2Client.oauth2Configuration())
      .exchangeStrategies(exchangeStrategies)
      .build()
  }

  @Bean
  fun prisonerSearchApiHealthWebClient(): WebClient = webClientBuilder.baseUrl(prisonerSearchApiBaseUrl).build()
}
