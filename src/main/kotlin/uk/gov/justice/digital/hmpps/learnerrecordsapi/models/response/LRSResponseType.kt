package uk.gov.justice.digital.hmpps.learnerrecordsapi.models.response

import com.fasterxml.jackson.annotation.JsonValue

enum class LRSResponseType(val englishName: String, val lrsResponseCode: String) {
  NO_MATCH("No Match", "WSRC0001"),
  TOO_MANY_MATCHES("Too Many Matches", "WSRC0002"),
  POSSIBLE_MATCH("Possible Match", "WSRC0003"),
  EXACT_MATCH("Exact Match", "WSRC0004"),
  LINKED_LEARNER("Linked Learner Match", "WSRC0022"),
  NOT_SHARED("Learner opted to not share data", "WSEC0206"),
  NOT_VERIFIED("Learner could not be verified", "WSEC0208"),
  UNKNOWN_RESPONSE_TYPE("", ""),
  ;

  companion object {
    fun fromLrsResponseCode(lrsResponseCode: String): LRSResponseType = entries.firstOrNull { it.lrsResponseCode == lrsResponseCode } ?: UNKNOWN_RESPONSE_TYPE
  }

  @JsonValue
  fun whichToUseForJson(): String = englishName
}
