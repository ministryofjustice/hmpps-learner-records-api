package uk.gov.justice.digital.hmpps.learnerrecordsapi.models.request

import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonValue

enum class MatchType(val description: String) {
  NO_MATCH_RETURNED_FROM_LRS("No match returned from LRS"),
  NO_MATCH_SELECTED("No match selected"),
  EXACT_MATCH("Exact match"),
  POSSIBLE_MATCH("Possible match"),
  LINKED_LEARNER_MATCH("linked learner match"),
  TOO_MANY_RESULTS("Too many results"),
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
