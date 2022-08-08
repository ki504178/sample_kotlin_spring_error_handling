package com.error.handling.spring.controller.error

import com.error.handling.spring.controller.error.model.CommonErrorTypeEnum
import com.error.handling.spring.controller.error.model.ErrorResponse
import com.error.handling.spring.exception.HandleableException
import org.apache.catalina.connector.ClientAbortException
import org.springframework.boot.autoconfigure.web.servlet.error.AbstractErrorController
import org.springframework.boot.web.servlet.error.ErrorAttributes
import org.springframework.context.MessageSource
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.validation.BindException
import org.springframework.web.bind.MissingServletRequestParameterException
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.context.request.ServletWebRequest
import javax.servlet.RequestDispatcher
import javax.servlet.http.HttpServletRequest
import javax.validation.ConstraintViolationException

/**
 * すべてのエラーを補足する共通エラーハンドラ
 */
@RestController
@RequestMapping("\${server.error.path:\${error.path:/error}}")
class ErrorRestController(
    private val errorAttributes: ErrorAttributes,
    private val messageSource: MessageSource
) : AbstractErrorController(errorAttributes) {
    companion object {
        private const val INVALID_ERROR = "InvalidError"
    }

    // バリデーションエラーハンドラ
    private val formErrorHandler = FormErrorHandler()
    private val parameterErrorHandler = ParameterErrorHandler()

    /** 404レスポンス */
    private val notFound = ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(
            ErrorResponse(
                errorCause = "ResourceNotFound",
                message = "Not Found"
            )
        )

    @RequestMapping
    fun handleError(request: HttpServletRequest): ResponseEntity<*>? {
        val error = errorAttributes.getError(ServletWebRequest(request)) ?: return notFound
        if (error.cause is ClientAbortException) return null

        val status = getStatus(request)
        val body = when (error) {
            is BindException -> {
                ErrorResponse(
                    errorCause = INVALID_ERROR,
                    invalidErrors = formErrorHandler.handleFormException(error, messageSource)
                )
            }

            is ConstraintViolationException -> {
                ErrorResponse(
                    errorCause = INVALID_ERROR,
                    invalidErrors = mutableListOf(parameterErrorHandler.handleParameterException(error))
                )
            }

            is MissingServletRequestParameterException -> {
                ErrorResponse(
                    errorCause = INVALID_ERROR,
                    invalidErrors = mutableListOf(parameterErrorHandler.handleParameterException(error))
                )
            }

            is HandleableException -> {
                ErrorResponse(
                    errorCause = "HandleableError",
                    message = error.message
                )
            }

            else -> {
                val reqMessage = request.getAttribute(RequestDispatcher.ERROR_MESSAGE)
                ErrorResponse(
                    errorCause = "SystemException",
                    message = if (reqMessage is String && reqMessage.isNotBlank()) reqMessage else error.message
                )
            }
        }

        return ResponseEntity.status(status).body(body)
    }
}
