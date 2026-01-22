package com.example.counselcrm.global.common

import com.fasterxml.jackson.annotation.JsonInclude

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val message: String? = null
) {
    companion object {
        fun <T> success(data: T): ApiResponse<T> = ApiResponse(
            success = true,
            data = data
        )

        fun <T> success(data: T, message: String): ApiResponse<T> = ApiResponse(
            success = true,
            data = data,
            message = message
        )

        fun success(): ApiResponse<Unit> = ApiResponse(
            success = true
        )

        fun success(message: String): ApiResponse<Unit> = ApiResponse(
            success = true,
            message = message
        )
    }
}
