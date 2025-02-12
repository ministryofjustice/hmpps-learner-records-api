package uk.gov.justice.digital.hmpps.learnerrecordsapi.config

import org.slf4j.Logger
import uk.gov.justice.digital.hmpps.learnerrecordsapi.logging.LoggerUtil
import uk.gov.justice.digital.hmpps.learnerrecordsapi.logging.LoggerUtil.log
import java.io.FileInputStream
import java.security.KeyStore
import java.security.SecureRandom
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

private const val PFX_FILE_PASSWORD = "PFX_FILE_PASSWORD"

private const val PFX_FILE_TYPE = "PKCS12"

private const val PROTOCOL = "TLS"

class SSLContextConfiguration(private val pfxFilePath: String) {

  private val logger: Logger = LoggerUtil.getLogger<SSLContextConfiguration>()

  fun createSSLContext(): SSLContext {
    val passwordEnv =
      System.getenv(PFX_FILE_PASSWORD)
        ?: throw IllegalArgumentException("Password environment variable not found.")
    val password = passwordEnv.toCharArray()

    logger.log("Loading PFX file from path: $pfxFilePath")
    val keyStore = KeyStore.getInstance(PFX_FILE_TYPE)
    keyStore.load(FileInputStream(pfxFilePath), password)

    logger.log("Initializing KeyManagerFactory")
    val keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm())
    keyManagerFactory.init(keyStore, password)

    logger.log("Initializing TrustManagerFactory")
    val trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
    trustManagerFactory.init(keyStore)

    logger.log("Initializing SSLContext")
    val sslContext = SSLContext.getInstance(PROTOCOL)
    sslContext.init(keyManagerFactory.keyManagers, null, SecureRandom())

    return sslContext
  }

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
