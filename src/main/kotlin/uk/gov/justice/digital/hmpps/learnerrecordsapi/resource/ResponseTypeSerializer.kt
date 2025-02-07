package uk.gov.justice.digital.hmpps.learnerrecordsapi.resource

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.core.JsonParser
import com.fasterxml.jackson.databind.DeserializationContext
import com.fasterxml.jackson.databind.JsonDeserializer
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializerProvider
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.response.LRSResponseType
import java.io.IOException

class ResponseTypeSerializer : JsonSerializer<LRSResponseType>() {
  @Throws(IOException::class)
  override fun serialize(value: LRSResponseType?, gen: JsonGenerator, serializers: SerializerProvider) {
    gen.writeString(value.toString())
  }
}