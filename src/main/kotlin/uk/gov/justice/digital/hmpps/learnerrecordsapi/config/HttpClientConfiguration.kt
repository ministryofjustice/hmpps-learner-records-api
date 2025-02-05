package uk.gov.justice.digital.hmpps.learnerrecordsapi.config

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import okhttp3.logging.HttpLoggingInterceptor.Level
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Configuration
import retrofit2.Retrofit
import retrofit2.converter.jaxb.JaxbConverterFactory
import uk.gov.justice.digital.hmpps.learnerrecordsapi.interfaces.LRSApiInterface
import java.util.concurrent.TimeUnit

@Configuration
class HttpClientConfiguration(
  @Value("\${lrs.pfx-path}") val pfxFilePath: String,
  @Value("\${lrs.base-url}") val baseUrl: String,
  @Autowired
  private val appConfig: AppConfig,
) {

  final val sslHttpClient: OkHttpClient = try {
    log.info("Building HTTP client with SSL")
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

    log.info("HTTP client with SSL built successfully!")
    httpClientBuilder.build()
  } catch (e: Exception) {
    log.info("${e.message} Falling back to HTTP client without SSL")
    OkHttpClient.Builder()
      .addInterceptor(HttpLoggingInterceptor().apply { level = Level.BODY })
      .build()
  }

  final val lrsClient: LRSApiInterface = Retrofit.Builder()
      .baseUrl(baseUrl)
      .client(sslHttpClient)
      .addConverterFactory(JaxbConverterFactory.create())
      .build().create(LRSApiInterface::class.java)

  companion object {
    private val log = LoggerFactory.getLogger(this::class.java)
  }
}
