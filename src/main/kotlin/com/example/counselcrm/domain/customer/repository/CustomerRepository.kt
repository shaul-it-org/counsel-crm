package com.example.counselcrm.domain.customer.repository

import com.example.counselcrm.domain.customer.entity.Customer
import org.springframework.data.jpa.repository.JpaRepository

interface CustomerRepository : JpaRepository<Customer, Long>, CustomerRepositoryCustom {
    fun existsByPhoneNumber(phoneNumber: String): Boolean
    fun findByPhoneNumber(phoneNumber: String): Customer?
}
