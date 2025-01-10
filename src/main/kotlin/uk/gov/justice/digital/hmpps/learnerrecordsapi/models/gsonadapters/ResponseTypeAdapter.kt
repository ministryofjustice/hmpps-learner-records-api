package uk.gov.justice.digital.hmpps.learnerrecordsapi.models.gsonadapters

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.response.ResponseType
import java.io.IOException

class ResponseTypeAdapter : TypeAdapter<ResponseType>() {

  override fun write(out: JsonWriter, value: ResponseType) {
    out.value(value.englishName)
  }

  @Throws(IOException::class)
  override fun read(input: JsonReader): ResponseType {
    val value = input.nextString()
    return ResponseType.entries.firstOrNull { it.lrsResponseCode == value }
      ?: throw IllegalArgumentException("Unknown MatchType value: $value")
  }
}
