package com.example.counselcrm.domain.customer.repository

import com.example.counselcrm.domain.customer.dto.CustomerSearchCondition
import com.example.counselcrm.domain.customer.entity.Customer
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface CustomerRepositoryCustom {
    fun search(condition: CustomerSearchCondition, pageable: Pageable): Page<Customer>
}
