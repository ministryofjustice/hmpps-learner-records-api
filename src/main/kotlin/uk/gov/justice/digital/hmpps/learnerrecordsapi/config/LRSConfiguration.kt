package uk.gov.justice.digital.hmpps.learnerrecordsapi.config

import jakarta.annotation.PostConstruct
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

@Configuration
@ConfigurationProperties(prefix = "lrs")
class LRSConfiguration {
  lateinit var baseUrl: String
  lateinit var pfxPath: String
  lateinit var ukprn: String
  lateinit var orgPassword: String
  lateinit var vendorId: String
  lateinit var connectTimeout: String
  lateinit var writeTimeout: String
  lateinit var readTimeout: String

  @PostConstruct
  fun init() {
    fun message(envKey : String) = "$envKey environment variable not set or not picked up by spring via app yaml."
    require(::ukprn.isInitialized) { message("UK_PRN") }
    require(::orgPassword.isInitialized) { message("ORG_PASSWORD") }
    require(::vendorId.isInitialized) { message("VENDOR_ID") }
    require(::baseUrl.isInitialized) { "LRS base url not set." }
    require(::pfxPath.isInitialized) { "LRS PFX certificate file path not set." }
  }
}

