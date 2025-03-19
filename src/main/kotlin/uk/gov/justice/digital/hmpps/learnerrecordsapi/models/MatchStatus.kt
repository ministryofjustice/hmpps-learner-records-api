package uk.gov.justice.digital.hmpps.learnerrecordsapi.models

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

enum class MatchStatus(val description: String) {
  UNMATCHED("Unmatched"),
  MATCHED("Matched"),
  MATCH_NOT_POSSIBLE("Cannot be matched"),
  ;

  @JsonValue
  override fun toString(): String = description

  companion object {
    @JsonCreator
    @JvmStatic
    fun fromString(value: String): MatchStatus = entries.find { it.description.equals(value, ignoreCase = true) }
      ?: throw IllegalArgumentException("Invalid Match Status value: $value")
  }
}
