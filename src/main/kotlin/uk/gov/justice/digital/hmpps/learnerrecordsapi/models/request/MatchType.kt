package uk.gov.justice.digital.hmpps.learnerrecordsapi.models.request

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

enum class MatchType(val description: String) {
  NO_MATCH_RETURNED_FROM_LRS("No Match"),
  EXACT_MATCH("Exact Match"),
  POSSIBLE_MATCH("Possible Match"),
  LINKED_LEARNER_MATCH("Linked Learner Match"),
  TOO_MANY_RESULTS("Too Many Matches"),
  ;

  @JsonValue
  override fun toString(): String = description

  companion object {
    @JsonCreator
    @JvmStatic
    fun fromString(value: String): MatchType = entries.find { it.description.equals(value, ignoreCase = true) }
      ?: throw IllegalArgumentException("Invalid Match Type value: $value")
  }
}
