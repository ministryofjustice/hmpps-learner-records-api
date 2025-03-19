package uk.gov.justice.digital.hmpps.learnerrecordsapi.models

import com.fasterxml.jackson.databind.exc.ValueInstantiationException
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class MatchStatusTest {

  private val objectMapper = jacksonObjectMapper()

  @Test
  fun `valid match status should parse`() {
    objectMapper.readValue("\"Unmatched\"", MatchStatus::class.java)
    objectMapper.readValue("\"Matched\"", MatchStatus::class.java)
    objectMapper.readValue("\"Cannot be matched\"", MatchStatus::class.java)
  }

  @Test
  fun `invalid match status should fail at runtime with an illegal argument exception`() {
    assertThrows<ValueInstantiationException> {
      objectMapper.readValue("\"STRAWBERRY\"", MatchStatus::class.java)
    }

    assertThrows<ValueInstantiationException> {
      objectMapper.readValue("\"2\"", MatchStatus::class.java)
    }

    assertThrows<ValueInstantiationException> {
      objectMapper.readValue("\"1\"", MatchStatus::class.java)
    }
  }
}
