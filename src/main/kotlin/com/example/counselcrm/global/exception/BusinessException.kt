package com.example.counselcrm.global.exception

open class BusinessException(
    val errorCode: ErrorCode,
    override val message: String = errorCode.message
) : RuntimeException(message)

class EntityNotFoundException(errorCode: ErrorCode) : BusinessException(errorCode)

class InvalidStatusTransitionException(
    val currentStatus: String,
    val targetStatus: String
) : BusinessException(
    ErrorCode.INVALID_STATUS_TRANSITION,
    "Cannot transition from $currentStatus to $targetStatus"
)

class DuplicateResourceException(errorCode: ErrorCode, message: String) : BusinessException(errorCode, message)
