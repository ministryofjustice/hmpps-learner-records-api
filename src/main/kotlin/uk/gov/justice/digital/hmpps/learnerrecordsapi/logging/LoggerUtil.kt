package uk.gov.justice.digital.hmpps.learnerrecordsapi.logging

import org.slf4j.Logger
import org.slf4j.LoggerFactory

object LoggerUtil {
  inline fun <reified T> getLogger(): Logger = LoggerFactory.getLogger(T::class.java)

  private fun formatDebug(message: String, vararg args: Any?): String = "Details: $message".appendIfNotEmpty(" | Variables: ", args)

  private fun formatInfo(message: String, vararg args: Any?): String = "Event: $message".appendIfNotEmpty(" | Context: ", args)

  private fun formatWarn(message: String, vararg args: Any?): String = "Issue: $message".appendIfNotEmpty(" | Impact: ", args)

  private fun formatError(message: String, vararg args: Any?, exception: Throwable? = null): String {
    val baseMessage = "Failure: $message".appendIfNotEmpty(" | Cause: ", args)
    return if (exception != null) "$baseMessage | Exception: ${exception.message}" else baseMessage
  }

  private fun String.appendIfNotEmpty(prefix: String, args: Array<out Any?>): String {
    val filteredArgs = args.filterNotNull()
    return if (filteredArgs.isNotEmpty()) this + prefix + filteredArgs.joinToString() else this
  }

  fun Logger.debugLog(details: String, vararg args: Any?) {
    this.debug(formatDebug(details, *args))
  }

  fun Logger.log(message: String, vararg args: Any?) {
    this.info(formatInfo(message, *args))
  }

  fun Logger.warnLog(message: String, vararg args: Any?) {
    this.warn(formatWarn(message, *args))
  }

  fun Logger.errorLog(message: String, vararg args: Any?, exception: Throwable? = null) {
    this.error(formatError(message, *args, exception = exception), exception)
  }
}
