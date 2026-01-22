package com.example.counselcrm.domain.customer.dto

import com.example.counselcrm.domain.customer.entity.Customer
import com.example.counselcrm.domain.customer.entity.CustomerGrade
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import java.time.LocalDateTime

data class CustomerCreateRequest(
    @field:NotBlank(message = "Name is required")
    val name: String,

    @field:NotBlank(message = "Phone number is required")
    @field:Pattern(regexp = "^01[0-9]-?[0-9]{3,4}-?[0-9]{4}$", message = "Invalid phone number format")
    val phoneNumber: String,

    @field:Email(message = "Invalid email format")
    val email: String? = null,

    val address: String? = null,
    val memo: String? = null,
    val grade: CustomerGrade = CustomerGrade.NEW
)

data class CustomerUpdateRequest(
    val name: String? = null,

    @field:Pattern(regexp = "^01[0-9]-?[0-9]{3,4}-?[0-9]{4}$", message = "Invalid phone number format")
    val phoneNumber: String? = null,

    @field:Email(message = "Invalid email format")
    val email: String? = null,

    val address: String? = null,
    val memo: String? = null,
    val grade: CustomerGrade? = null
)

data class CustomerResponse(
    val id: Long,
    val name: String,
    val phoneNumber: String,
    val email: String?,
    val address: String?,
    val memo: String?,
    val grade: CustomerGrade,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) {
    companion object {
        fun from(customer: Customer) = CustomerResponse(
            id = customer.id,
            name = customer.name,
            phoneNumber = customer.phoneNumber,
            email = customer.email,
            address = customer.address,
            memo = customer.memo,
            grade = customer.grade,
            createdAt = customer.createdAt,
            updatedAt = customer.updatedAt
        )
    }
}
