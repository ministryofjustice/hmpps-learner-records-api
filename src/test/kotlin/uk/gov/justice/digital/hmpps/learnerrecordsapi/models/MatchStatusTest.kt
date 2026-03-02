package uk.gov.justice.digital.hmpps.learnerrecordsapi.models

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import tools.jackson.databind.exc.ValueInstantiationException
import tools.jackson.module.kotlin.jsonMapper
import tools.jackson.module.kotlin.kotlinModule

class MatchStatusTest {

  private val jsonMapper = jsonMapper { addModule(kotlinModule()) }

  @Test
  fun `valid match status should parse`() {
    jsonMapper.readValue("\"Unmatched\"", MatchStatus::class.java)
    jsonMapper.readValue("\"Matched\"", MatchStatus::class.java)
    jsonMapper.readValue("\"Cannot be matched\"", MatchStatus::class.java)
  }

  @Test
  fun `invalid match status should fail at runtime with an illegal argument exception`() {
    assertThrows<ValueInstantiationException> {
      jsonMapper.readValue("\"STRAWBERRY\"", MatchStatus::class.java)
    }

    assertThrows<ValueInstantiationException> {
      jsonMapper.readValue("\"2\"", MatchStatus::class.java)
    }

    assertThrows<ValueInstantiationException> {
      jsonMapper.readValue("\"1\"", MatchStatus::class.java)
    }
  }
}
