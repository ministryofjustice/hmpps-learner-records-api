package uk.gov.justice.digital.hmpps.learnerrecordsapi.models.response

enum class ResponseType(val englishName: String, val lrsResponseCode: String) {
  EXACT_MATCH("Exact Match", "WSRC0004"),
  POSSIBLE_MATCH("Possible Match", "WSRC0003"),
  NO_MATCH("No Match", "WSRC0001"),
  UNKNOWN_RESPONSE_TYPE("", "");

  companion object {
    fun fromLrsResponseCode(lrsResponseCode: String): ResponseType {
      return entries.firstOrNull { it.lrsResponseCode == lrsResponseCode } ?: UNKNOWN_RESPONSE_TYPE
    }
  }

}
