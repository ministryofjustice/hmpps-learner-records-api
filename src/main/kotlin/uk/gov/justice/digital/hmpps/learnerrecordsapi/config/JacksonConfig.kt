package uk.gov.justice.digital.hmpps.learnerrecordsapi.config

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder

@Configuration
class JacksonConfig {
  @Bean
  fun objectMapper(): ObjectMapper {
    val objectMapper = Jackson2ObjectMapperBuilder.json().build<ObjectMapper>()
    objectMapper.registerModule(JavaTimeModule())
    objectMapper.setSerializationInclusion(com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL)
    objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, true)
    return objectMapper
  }
}
