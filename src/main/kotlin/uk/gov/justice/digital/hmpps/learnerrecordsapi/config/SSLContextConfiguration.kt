package uk.gov.justice.digital.hmpps.learnerrecordsapi.config

import org.slf4j.LoggerFactory
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

class SSLContextProvider(private val pfxFilePath: String) {

  fun createSSLContext(): SSLContext {

    val logger = LoggerFactory.getLogger(SSLContextProvider::class.java)

    val passwordEnv =
      System.getenv(PFX_FILE_PASSWORD) ?: throw IllegalArgumentException("Password environment variable not found.")
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