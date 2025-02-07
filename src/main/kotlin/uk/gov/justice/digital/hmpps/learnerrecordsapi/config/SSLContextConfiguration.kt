package uk.gov.justice.digital.hmpps.learnerrecordsapi.config

import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.io.FileInputStream
import java.security.KeyStore
import java.security.SecureRandom
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

//TODO: move to .yml , then to the constructor
private const val PFX_FILE_PASSWORD = "PFX_FILE_PASSWORD"

private const val PFX_FILE_TYPE = "PKCS12"

private const val PROTOCOL = "TLS"

@Configuration
class SSLContextConfiguration(
  @Value("\${ssl.pfx.file-path}") private val pfxFilePath: String,
  @Value("\${ssl.pfx.password}") private val pfxPassword: String
) {


  //TODO: use @Bean - can be injected later
  @Bean
  fun createSSLContext(): SSLContext {
    val logger = LoggerFactory.getLogger(SSLContextConfiguration::class.java)

    //TODO: use @Value
    val passwordEnv =
      System.getenv(PFX_FILE_PASSWORD)
        ?: throw IllegalArgumentException("Password environment variable not found.")
    val password = passwordEnv.toCharArray()

    logger.info("Loading PFX file from path: $pfxFilePath")
    val keyStore = KeyStore.getInstance(PFX_FILE_TYPE)
    keyStore.load(FileInputStream(pfxFilePath), password)

    logger.info("Initializing KeyManagerFactory")
    val keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm())
    keyManagerFactory.init(keyStore, password)

    logger.info("Initializing TrustManagerFactory")
    val trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
    trustManagerFactory.init(keyStore)

    logger.info("Initializing SSLContext")
    val sslContext = SSLContext.getInstance(PROTOCOL)
    sslContext.init(keyManagerFactory.keyManagers, null, SecureRandom())
    /*
    val sslContext = SSLContext.getInstance(PROTOCOL).apply{
      init(keyManagerFactory.keyManagers, null, SecureRandom())
    }
    */

    return sslContext
  }

  //TODO: add @Bean
  @Bean
  fun getTrustManager(): X509TrustManager {
    val trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
    trustManagerFactory.init(
      KeyStore.getInstance(PFX_FILE_TYPE).apply {
        load(FileInputStream(pfxFilePath), System.getenv(PFX_FILE_PASSWORD).toCharArray())
      },
    )
    return trustManagerFactory.trustManagers[0] as X509TrustManager
  }
}
