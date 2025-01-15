package uk.gov.justice.digital.hmpps.learnerrecordsapi.logging

import com.google.gson.Gson
import org.slf4j.LoggerFactory

class LoggerUtil(clazz: Class<Any>, private val gson: Gson? = null) {
  private val log = LoggerFactory.getLogger(clazz)

  fun info(message: String, vararg args: Any?) {
    log.info(message, *args)
  }

  fun debug(message: String, vararg args: Any?) {
    log.debug(message, *args)
  }

  fun warn(message: String, vararg args: Any?) {
    log.warn(message, *args)
  }

  fun error(message: String, throwable: Throwable? = null, vararg args: Any?) {
    if (throwable != null) {
      log.error(message, throwable, *args)
    } else {
      log.error(message, *args)
    }
  }

  fun inboundRequest(clientId: String = "example_clientId", requestModelObject: Any) {
    val gsonInstance = gson ?: throw RuntimeException("Gson instance is null")
    val body = gsonInstance.toJson(requestModelObject)
    log.info(
      "Request received with client id {} and body {}",
      clientId,
      body,
    )
  }
}
