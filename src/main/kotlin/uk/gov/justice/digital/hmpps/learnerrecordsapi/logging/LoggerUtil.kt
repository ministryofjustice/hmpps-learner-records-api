package uk.gov.justice.digital.hmpps.learnerrecordsapi.logging

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.time.Instant

object LoggerUtil {
  inline fun <reified T> getLogger(): Logger = LoggerFactory.getLogger(T::class.java)

  private fun formatDebug(className: String, message: String, vararg args: Any?): String = "[DEBUG] | Timestamp: ${Instant.now()} | Class: $className | Action: Debugging | Details: $message | Variables: ${args.joinToString()}"

  private fun formatInfo(className: String, message: String, vararg args: Any?): String = "[INFO] | Timestamp: ${Instant.now()} | Class: $className | Event: $message | Context: ${args.joinToString()}"

  private fun formatWarn(className: String, message: String, vararg args: Any?): String = "[WARNING] | Timestamp: ${Instant.now()} | Class: $className | Issue: $message | Impact: ${args.joinToString()}"

  private fun formatError(className: String, message: String, vararg args: Any?, exception: Throwable? = null): String = "[ERROR] | Timestamp: ${Instant.now()} | Class: $className | Failure: $message | Cause: ${args.joinToString()} | Exception: ${exception?.message}"

  fun Logger.debugLog(message: String, vararg args: Any?) {
    this.debug(formatDebug(this.name, message, *args))
  }

  fun Logger.log(message: String, vararg args: Any?) {
    this.info(formatInfo(this.name, message, *args))
  }

  fun Logger.warnLog(message: String, vararg args: Any?) {
    this.warn(formatWarn(this.name, message, *args))
  }

  fun Logger.errorLog(message: String, vararg args: Any?, exception: Throwable? = null) {
    this.error(formatError(this.name, message, *args, exception = exception), exception)
  }
}
