package com.example.counselcrm.domain.customer.service

import com.example.counselcrm.domain.customer.dto.*
import com.example.counselcrm.domain.customer.entity.Customer
import com.example.counselcrm.domain.customer.repository.CustomerRepository
import com.example.counselcrm.global.exception.DuplicateResourceException
import com.example.counselcrm.global.exception.EntityNotFoundException
import com.example.counselcrm.global.exception.ErrorCode
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class CustomerService(
    private val customerRepository: CustomerRepository
) {
    fun getCustomer(id: Long): CustomerResponse {
        val customer = findCustomerById(id)
        return CustomerResponse.from(customer)
    }

    fun searchCustomers(condition: CustomerSearchCondition, pageable: Pageable): Page<CustomerResponse> {
        return customerRepository.search(condition, pageable)
            .map { CustomerResponse.from(it) }
    }

    @Transactional
    fun createCustomer(request: CustomerCreateRequest): CustomerResponse {
        validatePhoneNumberNotDuplicate(request.phoneNumber)

        val customer = Customer(
            name = request.name,
            phoneNumber = normalizePhoneNumber(request.phoneNumber),
            email = request.email,
            address = request.address,
            memo = request.memo,
            grade = request.grade
        )

        val savedCustomer = customerRepository.save(customer)
        return CustomerResponse.from(savedCustomer)
    }

    @Transactional
    fun updateCustomer(id: Long, request: CustomerUpdateRequest): CustomerResponse {
        val customer = findCustomerById(id)

        request.phoneNumber?.let { newPhoneNumber ->
            val normalized = normalizePhoneNumber(newPhoneNumber)
            if (customer.phoneNumber != normalized) {
                validatePhoneNumberNotDuplicate(normalized)
            }
            customer.update(phoneNumber = normalized)
        }

        customer.update(
            name = request.name,
            email = request.email,
            address = request.address,
            memo = request.memo,
            grade = request.grade
        )

        return CustomerResponse.from(customer)
    }

    @Transactional
    fun deleteCustomer(id: Long) {
        val customer = findCustomerById(id)
        customerRepository.delete(customer)
    }

    internal fun findCustomerById(id: Long): Customer {
        return customerRepository.findById(id)
            .orElseThrow { EntityNotFoundException(ErrorCode.CUSTOMER_NOT_FOUND) }
    }

    private fun validatePhoneNumberNotDuplicate(phoneNumber: String) {
        if (customerRepository.existsByPhoneNumber(normalizePhoneNumber(phoneNumber))) {
            throw DuplicateResourceException(
                ErrorCode.DUPLICATE_PHONE_NUMBER,
                "Phone number $phoneNumber already exists"
            )
        }
    }

    private fun normalizePhoneNumber(phoneNumber: String): String {
        return phoneNumber.replace("-", "")
    }
}
