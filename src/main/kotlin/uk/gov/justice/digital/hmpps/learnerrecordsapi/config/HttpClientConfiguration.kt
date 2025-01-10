package uk.gov.justice.digital.hmpps.learnerrecordsapi.config

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Level
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import retrofit2.Retrofit
import retrofit2.converter.jaxb.JaxbConverterFactory

@Configuration
class HttpClientConfiguration(
  @Value("\${lrs.pfx-path}") val pfxFilePath: String,
  @Value("\${lrs.base-url}") val baseUrl: String,
) {
  fun sslHttpClient(): OkHttpClient {
    log.info("Building HTTP Client With SSL")
    val sslContextProvider = SSLContextProvider(pfxFilePath)
    val sslContext = sslContextProvider.createSSLContext()

    val trustManager = sslContextProvider.getTrustManager()

    val loggingInterceptor = HttpLoggingInterceptor()
    loggingInterceptor.level = Level.BODY

    val httpClientBuilder = OkHttpClient.Builder()
      .sslSocketFactory(sslContext.socketFactory, trustManager)
      .addInterceptor(loggingInterceptor)

    return httpClientBuilder.build()
  }

  fun retrofit(): Retrofit {
    log.info("Retrofit Client")

    return Retrofit.Builder()
      .baseUrl(baseUrl)
      .client(sslHttpClient())
      .addConverterFactory(JaxbConverterFactory.create())
      .build()
  }

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }
}
