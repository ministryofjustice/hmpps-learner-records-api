package uk.gov.justice.digital.hmpps.learnerrecordsapi.config

import jakarta.validation.constraints.NotEmpty
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import org.springframework.validation.annotation.Validated

@Configuration
@ConfigurationProperties(prefix = "lrs")
@Validated
class LRSConfiguration {
  @NotEmpty lateinit var baseUrl: String

  @NotEmpty lateinit var pfxPath: String

  @NotEmpty lateinit var ukprn: String

  @NotEmpty lateinit var orgPassword: String

  @NotEmpty lateinit var vendorId: String

  lateinit var connectTimeout: String
  lateinit var writeTimeout: String
  lateinit var readTimeout: String
}
