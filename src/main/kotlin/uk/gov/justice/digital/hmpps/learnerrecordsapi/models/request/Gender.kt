package uk.gov.justice.digital.hmpps.learnerrecordsapi.models.request

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

enum class Gender(val description: String, val code: Int) {
  MALE("MALE", 1),
  FEMALE("FEMALE", 2),
  NOT_KNOWN("NOT_KNOWN", 0),
  NOT_SPECIFIED("NOT_SPECIFIED", 9),
  ;

  @JsonValue
  override fun toString(): String = description

  companion object {
    @JsonCreator
    @JvmStatic
    fun fromString(value: String): Gender = entries.find { it.description.equals(value, ignoreCase = true) }
      ?: throw IllegalArgumentException("Invalid gender value: $value")
  }
}
