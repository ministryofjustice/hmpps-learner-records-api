package uk.gov.justice.digital.hmpps.learnerrecordsapi.models.request

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import tools.jackson.databind.exc.ValueInstantiationException
import tools.jackson.module.kotlin.jsonMapper
import tools.jackson.module.kotlin.kotlinModule

class GenderTest {

  private val jsonMapper = jsonMapper { addModule(kotlinModule()) }

  @Test
  fun `valid gender should parse`() {
    jsonMapper.readValue("\"MALE\"", Gender::class.java)
    jsonMapper.readValue("\"FEMALE\"", Gender::class.java)
    jsonMapper.readValue("\"NOT_KNOWN\"", Gender::class.java)
    jsonMapper.readValue("\"NOT_SPECIFIED\"", Gender::class.java)
  }

  @Test
  fun `invalid gender should fail at runtime with an illegal argument exception`() {
    assertThrows<ValueInstantiationException> {
      jsonMapper.readValue("\"STRAWBERRY\"", Gender::class.java)
    }

    assertThrows<ValueInstantiationException> {
      jsonMapper.readValue("\"2\"", Gender::class.java)
    }

    assertThrows<ValueInstantiationException> {
      jsonMapper.readValue("\"1\"", Gender::class.java)
    }
  }
}
