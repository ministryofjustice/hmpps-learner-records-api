package uk.gov.justice.digital.hmpps.learnerrecordsapi.config

import org.apache.commons.lang3.StringUtils
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.BindingResult
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest
import uk.gov.justice.digital.hmpps.learnerrecordsapi.logging.LoggerUtil

@RestControllerAdvice
class HmppsBoldLrsExceptionHandler {

  private val log: LoggerUtil = LoggerUtil(javaClass)

  data class ErrorResponse(
    val status: HttpStatus,
    val errorCode: String,
    val userMessage: String? = StringUtils.EMPTY,
    val developerMessage: String,
    val moreInfo: String? = StringUtils.EMPTY,
  )

  @ExceptionHandler(MethodArgumentNotValidException::class)
  fun handleMethodArgumentNotValidException(
    ex: MethodArgumentNotValidException,
    request: WebRequest,
  ): ResponseEntity<Any> {
    val bindingResult: BindingResult = ex.bindingResult
    val errors = bindingResult.allErrors.map { it.defaultMessage }
    val erroredFields = bindingResult.fieldErrors.map { it.field }

    val errorResponse = ErrorResponse(
      status = HttpStatus.BAD_REQUEST,
      errorCode = "Validation Failed",
      userMessage = "Please correct the error and retry",
      developerMessage = "Validation(s) failed for $erroredFields",
      moreInfo = "Validation(s) failed for $erroredFields with reason(s): $errors",
    )
    log.error("Validation(s) failed for $erroredFields with reason(s): $errors", ex)
    return ResponseEntity(errorResponse, HttpStatus.BAD_REQUEST)
  }

  @ExceptionHandler(IllegalStateException::class)
  fun handleNoResourceFoundException(
    ex: IllegalStateException,
    request: WebRequest,
  ): ResponseEntity<Any> {
    val errorResponse = ErrorResponse(
      status = HttpStatus.NOT_FOUND,
      errorCode = "No Resource Found",
      userMessage = "No resource found failure: ${ex.message}",
      developerMessage = "Requested Resource Not found on the server",
      moreInfo = "Requested Resource Not found on the server",
    )
    log.error("Requested Resource was not Found {}", ex)
    return ResponseEntity(errorResponse, HttpStatus.NOT_FOUND)
  }

  @ExceptionHandler(AccessDeniedException::class)
  fun handleAccessDeniedException(
    ex: AccessDeniedException,
    request: WebRequest,
  ): ResponseEntity<Any> {
    val errorResponse = ErrorResponse(
      status = HttpStatus.FORBIDDEN,
      errorCode = "Forbidden - Access Denied",
      userMessage = "Forbidden: ${ex.message}",
      developerMessage = "Forbidden - Access Denied",
      moreInfo = "Forbidden - Access Denied",
    )
    log.error("Forbidden (403) returned: {}", ex)
    return ResponseEntity(errorResponse, HttpStatus.FORBIDDEN)
  }

  @ExceptionHandler(Exception::class)
  fun handleException(
    ex: Exception,
    request: WebRequest,
  ): ResponseEntity<Any> {
    val errorResponse = ErrorResponse(
      status = HttpStatus.INTERNAL_SERVER_ERROR,
      errorCode = "Unexpected error",
      userMessage = "Unexpected error: ${ex.message}",
      developerMessage = "Unexpected error: ${ex.message}",
      moreInfo = "Unexpected error",
    )
    log.error("Unexpected Error: {}", ex)
    return ResponseEntity(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR)
  }
}
