package uk.gov.justice.digital.hmpps.learnerrecordsapi.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.gsonadapters.LocalDateAdapter
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.gsonadapters.ResponseTypeAdapter
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.response.LRSResponseType
import uk.gov.justice.digital.hmpps.learnerrecordsapi.resource.ResponseTypeSerializer
import java.time.LocalDate

@Configuration
class JacksonConfig {

  @Bean
  fun objectMapper(): ObjectMapper {
    val module = SimpleModule()
    //module.addSerializer(LocalDate::class.java, ResponseDateSerializer())
    module.addSerializer(LRSResponseType::class.java, ResponseTypeSerializer())

    return ObjectMapper().registerModule(module)
  }
}
