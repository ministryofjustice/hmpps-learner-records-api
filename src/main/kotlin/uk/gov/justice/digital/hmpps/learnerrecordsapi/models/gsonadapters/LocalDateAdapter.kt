package uk.gov.justice.digital.hmpps.learnerrecordsapi.models.gsonadapters

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class LocalDateAdapter : TypeAdapter<LocalDate>() {

  //TODO: what if value is null
  override fun write(out: JsonWriter, value: LocalDate) {
    out.value(DateTimeFormatter.ISO_LOCAL_DATE.format(value))
  }

  //TODO: what if value is null
  override fun read(input: JsonReader): LocalDate = LocalDate.parse(input.nextString())
}
