package com.example.counselcrm.global.exception

import com.fasterxml.jackson.annotation.JsonInclude
import java.time.LocalDateTime

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ErrorResponse(
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val status: Int,
    val code: String,
    val message: String,
    val errors: List<FieldError>? = null
) {
    data class FieldError(
        val field: String,
        val value: String?,
        val reason: String?
    )

    companion object {
        fun of(errorCode: ErrorCode): ErrorResponse = ErrorResponse(
            status = errorCode.status.value(),
            code = errorCode.code,
            message = errorCode.message
        )

        fun of(errorCode: ErrorCode, message: String): ErrorResponse = ErrorResponse(
            status = errorCode.status.value(),
            code = errorCode.code,
            message = message
        )

        fun of(errorCode: ErrorCode, errors: List<FieldError>): ErrorResponse = ErrorResponse(
            status = errorCode.status.value(),
            code = errorCode.code,
            message = errorCode.message,
            errors = errors
        )
    }
}
