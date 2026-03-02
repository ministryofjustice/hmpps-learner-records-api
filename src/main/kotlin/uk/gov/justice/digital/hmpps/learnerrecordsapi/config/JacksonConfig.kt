package uk.gov.justice.digital.hmpps.learnerrecordsapi.config

import com.fasterxml.jackson.annotation.JsonInclude
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import tools.jackson.databind.DeserializationFeature
import tools.jackson.databind.json.JsonMapper

@Configuration
class JacksonConfig {
  @Bean
  fun jsonMapper(): JsonMapper = JsonMapper.builder()
    .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true)
    .changeDefaultPropertyInclusion { it.withValueInclusion(JsonInclude.Include.NON_NULL) }
    .changeDefaultPropertyInclusion { it.withContentInclusion(JsonInclude.Include.NON_NULL) }
    .build()
}
