package uk.gov.justice.digital.hmpps.learnerrecordsapi.config

import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException
import org.apache.commons.lang3.StringUtils
import org.slf4j.Logger
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.security.authorization.AuthorizationDeniedException
import org.springframework.validation.BindingResult
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.MissingRequestHeaderException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.context.request.WebRequest
import org.springframework.web.servlet.resource.NoResourceFoundException
import uk.gov.justice.digital.hmpps.learnerrecordsapi.logging.LoggerUtil
import uk.gov.justice.digital.hmpps.learnerrecordsapi.logging.LoggerUtil.errorLog
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.lrsapi.response.exceptions.DFEApiDownException
import uk.gov.justice.digital.hmpps.learnerrecordsapi.models.lrsapi.response.exceptions.LRSException
import java.net.SocketTimeoutException

@RestControllerAdvice
class HmppsBoldLrsExceptionHandler {

  private val logger: Logger = LoggerUtil.getLogger<HmppsBoldLrsExceptionHandler>()
  val unExpectedError = "Unexpected error"
  val unReadableHttpMessage = "Unreadable HTTP message"
  val forbiddenAccessDenied = "Forbidden - Access Denied"
  val forbiddenAuthorizationDenied = "Forbidden - Authorization Denied"
  val dFEApiFailedToRespond = "DfE API failed to Respond"
  val dfeApiDependencyFailed = "LRS API Dependency Failed - DfE API is under maintenance"

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
  ): ResponseEntity<ErrorResponse> {
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
    logger.errorLog("Validation(s) failed for $erroredFields", ex)
    return ResponseEntity(errorResponse, HttpStatus.BAD_REQUEST)
  }

  @ExceptionHandler(NoResourceFoundException::class)
  fun handleNoResourceFoundException(
    ex: NoResourceFoundException,
    request: WebRequest,
  ): ResponseEntity<ErrorResponse> {
    val errorResponse = ErrorResponse(
      status = HttpStatus.NOT_FOUND,
      errorCode = "No Resource Found",
      userMessage = "No resource found failure: ${ex.message}",
      developerMessage = "Requested Resource not found on the server",
      moreInfo = "Requested Resource not found on the server",
    )
    logger.errorLog("Requested Resource was not found", ex)
    return ResponseEntity(errorResponse, HttpStatus.NOT_FOUND)
  }

  @ExceptionHandler(AccessDeniedException::class)
  fun handleAccessDeniedException(
    ex: AccessDeniedException,
    request: WebRequest,
  ): ResponseEntity<ErrorResponse> {
    val errorResponse = ErrorResponse(
      status = HttpStatus.FORBIDDEN,
      errorCode = forbiddenAccessDenied,
      userMessage = "Forbidden: ${ex.message}",
      developerMessage = forbiddenAccessDenied,
      moreInfo = forbiddenAccessDenied,
    )
    logger.errorLog("Forbidden (403) returned", ex)
    return ResponseEntity(errorResponse, HttpStatus.FORBIDDEN)
  }

  @ExceptionHandler(AuthorizationDeniedException::class)
  fun handleAuthorizationDeniedException(
    ex: AuthorizationDeniedException,
    request: WebRequest,
  ): ResponseEntity<ErrorResponse> {
    val errorResponse = ErrorResponse(
      status = HttpStatus.FORBIDDEN,
      errorCode = forbiddenAuthorizationDenied,
      userMessage = "Forbidden: ${ex.message}",
      developerMessage = forbiddenAuthorizationDenied,
      moreInfo = forbiddenAuthorizationDenied,
    )
    logger.errorLog("Forbidden (403) returned", ex)
    return ResponseEntity(errorResponse, HttpStatus.FORBIDDEN)
  }

  @ExceptionHandler(HttpMessageNotReadableException::class)
  fun handleUnreadableHttpMessage(
    ex: HttpMessageNotReadableException,
    request: WebRequest,
  ): ResponseEntity<ErrorResponse> {
    if (ex.mostSpecificCause.javaClass.simpleName == "UnrecognizedPropertyException") {
      return handleUnrecognizedPropertyException(ex, request)
    }
    val errorResponse = ErrorResponse(
      status = HttpStatus.BAD_REQUEST,
      errorCode = unReadableHttpMessage,
      userMessage = unReadableHttpMessage,
      developerMessage = "${ex.message}",
      moreInfo = unReadableHttpMessage,
    )
    logger.errorLog(unExpectedError, ex)
    return ResponseEntity(errorResponse, HttpStatus.BAD_REQUEST)
  }

  @ExceptionHandler(MissingRequestHeaderException::class)
  fun handleMissingRequestHeaderException(
    ex: Exception,
    request: WebRequest,
  ): ResponseEntity<ErrorResponse> {
    val errorMessage =
      if (request.getHeader("X-Username") == null) "Missing X-Username Header" else "Missing Request Header"

    val errorResponse = ErrorResponse(
      status = HttpStatus.BAD_REQUEST,
      errorCode = errorMessage,
      userMessage = "$errorMessage: ${ex.message}",
      developerMessage = "$errorMessage: ${ex.message}",
      moreInfo = errorMessage,
    )
    logger.errorLog(unExpectedError, ex)
    return ResponseEntity(errorResponse, HttpStatus.BAD_REQUEST)
  }

  @ExceptionHandler(UnrecognizedPropertyException::class)
  fun handleUnrecognizedPropertyException(
    ex: Exception,
    request: WebRequest,
  ): ResponseEntity<ErrorResponse> {
    val errorResponse = ErrorResponse(
      status = HttpStatus.BAD_REQUEST,
      errorCode = "Unreadable HTTP message",
      userMessage = "Unrecognized field in request",
      developerMessage = "Unrecognized field: ${ex.message}",
      moreInfo = "Unrecognized field",
    )
    logger.errorLog(unExpectedError, ex)
    return ResponseEntity(errorResponse, HttpStatus.BAD_REQUEST)
  }

  @ExceptionHandler(Exception::class)
  fun handleException(
    ex: Exception,
    request: WebRequest,
  ): ResponseEntity<ErrorResponse> {
    val errorResponse = ErrorResponse(
      status = HttpStatus.INTERNAL_SERVER_ERROR,
      errorCode = unExpectedError,
      userMessage = "$unExpectedError: ${ex.message}",
      developerMessage = "$unExpectedError: ${ex.message}",
      moreInfo = unExpectedError,
    )
    logger.errorLog(unExpectedError, ex)
    return ResponseEntity(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR)
  }

  @ExceptionHandler(LRSException::class)
  fun handleLRSException(
    ex: LRSException,
    request: WebRequest,
  ): ResponseEntity<ErrorResponse> {
    val errorResponse = ErrorResponse(
      status = HttpStatus.INTERNAL_SERVER_ERROR,
      errorCode = "LRS Error",
      userMessage = "${ex.message}",
      developerMessage = "${ex.message}",
      moreInfo = "LRS Error",
    )
    logger.errorLog(ex.message.orEmpty())
    return ResponseEntity(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR)
  }

  @ExceptionHandler(SocketTimeoutException::class)
  fun handleSocketTimeoutException(
    ex: SocketTimeoutException,
    request: WebRequest,
  ): ResponseEntity<ErrorResponse> {
    val errorResponse = ErrorResponse(
      status = HttpStatus.REQUEST_TIMEOUT,
      errorCode = "Request Timeout",
      userMessage = "A request to an upstream service timed out.",
      developerMessage = "${ex.message}",
      moreInfo = "A request timed out while waiting for a response from an upstream service.",
    )
    logger.errorLog("Socket Timeout Error", ex)
    return ResponseEntity(errorResponse, HttpStatus.REQUEST_TIMEOUT)
  }

  @ExceptionHandler(HttpRequestMethodNotSupportedException::class)
  fun handleHttpRequestMethodNotSupportedException(
    ex: HttpRequestMethodNotSupportedException,
    request: WebRequest,
  ): ResponseEntity<ErrorResponse> {
    val errorResponse = ErrorResponse(
      status = HttpStatus.METHOD_NOT_ALLOWED,
      errorCode = "Method (${ex.method}) not allowed",
      userMessage = "HTTP Method (${ex.method}) is not allowed",
      developerMessage = "HTTP Method (${ex.method}) is not allowed, use only (${ex.supportedMethods?.get(0)}) method",
      moreInfo = "HTTP Method (${ex.method}) is not allowed, use only this (${ex.supportedMethods?.get(0)}) method",
    )
    logger.errorLog("HTTP Verb Not Supported", ex)
    return ResponseEntity(errorResponse, HttpStatus.METHOD_NOT_ALLOWED)
  }

  @ExceptionHandler(DFEApiDownException::class)
  fun handleDFEApiDownException(
    ex: DFEApiDownException,
    request: WebRequest,
  ): ResponseEntity<ErrorResponse> {
    val errorResponse = ErrorResponse(
      status = HttpStatus.FAILED_DEPENDENCY,
      errorCode = dFEApiFailedToRespond,
      userMessage = dfeApiDependencyFailed,
      developerMessage = "LRS API Dependency Failed - DfE API is under maintenance, please check DfE API maintenance window for more details",
      moreInfo = "LRS API Dependency Failed - DfE API is under maintenance",
    )
    logger.errorLog("LRS API Dependency Failed - DfE API is under maintenance")
    return ResponseEntity(errorResponse, HttpStatus.FAILED_DEPENDENCY)
  }
}
