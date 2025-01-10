package uk.gov.justice.digital.hmpps.learnerrecordsapi.resource

import com.google.gson.GsonBuilder
import uk.gov.justice.digital.hmpps.learnerrecordsapi.logging.LoggerUtil
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.gsonadapters.LocalDateAdapter
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.gsonadapters.ResponseTypeAdapter
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.response.ResponseType
import java.time.LocalDate

open class BaseResource {
  val gson = GsonBuilder()
    .registerTypeAdapter(LocalDate::class.java, LocalDateAdapter().nullSafe())
    .registerTypeAdapter(ResponseType::class.java, ResponseTypeAdapter().nullSafe())
    .create()
  val log = LoggerUtil(javaClass, gson)
}