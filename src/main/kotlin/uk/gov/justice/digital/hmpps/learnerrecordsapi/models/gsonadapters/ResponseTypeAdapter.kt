package uk.gov.justice.digital.hmpps.learnerrecordsapi.models.gsonadapters

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.response.LRSResponseType
import java.io.IOException

class ResponseTypeAdapter : TypeAdapter<LRSResponseType>() {

  override fun write(out: JsonWriter, value: LRSResponseType) {
    out.value(value.englishName)
  }

  @Throws(IOException::class)
  override fun read(input: JsonReader): LRSResponseType {
    val value = input.nextString()
    return LRSResponseType.entries.firstOrNull { it.lrsResponseCode == value }
      ?: throw IllegalArgumentException("Unknown MatchType value: $value")
  }
}
