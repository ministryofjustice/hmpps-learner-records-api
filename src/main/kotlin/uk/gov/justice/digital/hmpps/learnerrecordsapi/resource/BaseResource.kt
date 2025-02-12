package uk.gov.justice.digital.hmpps.learnerrecordsapi.resource

import com.google.gson.GsonBuilder

open class BaseResource {
  val gson = GsonBuilder().create() // TODO: Remove the base resource.
}
