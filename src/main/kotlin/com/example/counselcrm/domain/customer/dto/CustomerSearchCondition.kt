package com.example.counselcrm.domain.customer.dto

import com.example.counselcrm.domain.customer.entity.CustomerGrade

data class CustomerSearchCondition(
    val name: String? = null,
    val phoneNumber: String? = null,
    val email: String? = null,
    val grade: CustomerGrade? = null
)
