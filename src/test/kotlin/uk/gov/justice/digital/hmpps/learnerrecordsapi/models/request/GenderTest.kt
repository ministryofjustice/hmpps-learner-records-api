package uk.gov.justice.digital.hmpps.learnerrecordsapi.models.request

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class GenderTest {

  private val objectMapper = jacksonObjectMapper()

  @Test
  fun `valid gender should parse`() {
    objectMapper.readValue("\"MALE\"", Gender::class.java)
    objectMapper.readValue("\"FEMALE\"", Gender::class.java)
    objectMapper.readValue("\"NOT_KNOWN\"", Gender::class.java)
    objectMapper.readValue("\"NOT_SPECIFIED\"", Gender::class.java)
  }

  @Test
  fun `invalid gender should fail at runtime with an illegal argument exception`() {
    assertThrows<Exception> {
      objectMapper.readValue("\"STRAWBERRY\"", Gender::class.java)
    }

    assertThrows<Exception> {
      objectMapper.readValue("\"2\"", Gender::class.java)
    }

    assertThrows<Exception> {
      objectMapper.readValue("\"1\"", Gender::class.java)
    }
  }
}
