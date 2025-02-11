package uk.gov.justice.digital.hmpps.learnerrecordsapi.config

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Level
import org.slf4j.Logger
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import retrofit2.Retrofit
import retrofit2.converter.jaxb.JaxbConverterFactory
import uk.gov.justice.digital.hmpps.learnerrecordsapi.interfaces.LRSApiInterface
import uk.gov.justice.digital.hmpps.learnerrecordsapi.logging.LoggerUtil
import uk.gov.justice.digital.hmpps.learnerrecordsapi.logging.LoggerUtil.log
import java.util.concurrent.TimeUnit

@Configuration
class HttpClientConfiguration {

  @Value("\${lrs.pfx-path}")
  lateinit var pfxFilePath: String

  @Value("\${lrs.base-url}")
  lateinit var baseUrl: String

  @Autowired
  lateinit var appConfig: AppConfig

  private val logger: Logger = LoggerUtil.getLogger<HttpClientConfiguration>()

  @Bean
  fun lrsClient(): LRSApiInterface = Retrofit.Builder()
    .baseUrl(baseUrl)
    .client(sslHttpClient())
    .addConverterFactory(JaxbConverterFactory.create())
    .build().create(LRSApiInterface::class.java)

  fun sslHttpClient(): OkHttpClient {
    logger.log("Building HTTP client with SSL")
    val loggingInterceptor = HttpLoggingInterceptor()
    loggingInterceptor.level = Level.BODY

    val sslContextConfiguration = SSLContextConfiguration(pfxFilePath)
    val sslContext = sslContextConfiguration.createSSLContext()
    val trustManager = sslContextConfiguration.getTrustManager()

    val httpClientBuilder = OkHttpClient.Builder()
      .connectTimeout(appConfig.lrsConnectTimeout(), TimeUnit.SECONDS)
      .writeTimeout(appConfig.lrsWriteTimeout(), TimeUnit.SECONDS)
      .readTimeout(appConfig.lrsReadTimeout(), TimeUnit.SECONDS)
      .sslSocketFactory(sslContext.socketFactory, trustManager)
      .addInterceptor(loggingInterceptor)

    logger.log("HTTP client with SSL built successfully!")
    return httpClientBuilder.build()
  }
}
