package uk.gov.justice.digital.hmpps.learnerrecordsapi.config

import org.springframework.context.annotation.Configuration

@Configuration
class AppConfig {
  fun ukprn(): String = System.getenv("UK_PRN") ?: throw IllegalArgumentException("UK_PRN environment variable not found.")
  fun password(): String = System.getenv("ORG_PASSWORD") ?: throw IllegalArgumentException("ORG_PASSWORD environment variable not found.")
  fun vendorId(): String = System.getenv("VENDOR_ID") ?: throw IllegalArgumentException("VENDOR_ID environment variable not found.")
}
