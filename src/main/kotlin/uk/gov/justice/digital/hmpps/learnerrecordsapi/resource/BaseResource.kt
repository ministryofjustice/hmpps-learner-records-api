package uk.gov.justice.digital.hmpps.learnerrecordsapi.resource

import com.google.gson.GsonBuilder
import uk.gov.justice.digital.hmpps.learnerrecordsapi.logging.LoggerUtil

open class BaseResource {
  val gson = GsonBuilder().create() // TODO: Remove the base resource.
  val log = LoggerUtil(javaClass, gson)
}
