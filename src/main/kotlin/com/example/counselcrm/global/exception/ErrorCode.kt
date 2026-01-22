package com.example.counselcrm.global.exception

import org.springframework.http.HttpStatus

enum class ErrorCode(
    val status: HttpStatus,
    val code: String,
    val message: String
) {
    // Common
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "C001", "Invalid input value"),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "C002", "Resource not found"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C003", "Internal server error"),

    // Customer
    CUSTOMER_NOT_FOUND(HttpStatus.NOT_FOUND, "CU001", "Customer not found"),
    DUPLICATE_PHONE_NUMBER(HttpStatus.CONFLICT, "CU002", "Phone number already exists"),

    // Counselor
    COUNSELOR_NOT_FOUND(HttpStatus.NOT_FOUND, "CO001", "Counselor not found"),
    COUNSELOR_NOT_AVAILABLE(HttpStatus.BAD_REQUEST, "CO002", "Counselor is not available"),

    // Counseling
    COUNSELING_NOT_FOUND(HttpStatus.NOT_FOUND, "CS001", "Counseling not found"),
    INVALID_STATUS_TRANSITION(HttpStatus.BAD_REQUEST, "CS002", "Invalid status transition"),
    COUNSELING_ALREADY_ASSIGNED(HttpStatus.CONFLICT, "CS003", "Counseling is already assigned"),

    // Contract
    CONTRACT_NOT_FOUND(HttpStatus.NOT_FOUND, "CT001", "Contract not found"),
}
