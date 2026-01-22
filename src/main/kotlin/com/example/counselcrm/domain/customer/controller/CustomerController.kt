package com.example.counselcrm.domain.customer.controller

import com.example.counselcrm.domain.customer.dto.*
import com.example.counselcrm.domain.customer.entity.CustomerGrade
import com.example.counselcrm.domain.customer.service.CustomerService
import com.example.counselcrm.global.common.ApiResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@Tag(name = "Customer", description = "고객 관리 API")
@RestController
@RequestMapping("/api/v1/customers")
class CustomerController(
    private val customerService: CustomerService
) {
    @Operation(summary = "고객 단건 조회")
    @GetMapping("/{id}")
    fun getCustomer(@PathVariable id: Long): ApiResponse<CustomerResponse> {
        return ApiResponse.success(customerService.getCustomer(id))
    }

    @Operation(summary = "고객 검색")
    @GetMapping
    fun searchCustomers(
        @RequestParam(required = false) name: String?,
        @RequestParam(required = false) phoneNumber: String?,
        @RequestParam(required = false) email: String?,
        @RequestParam(required = false) grade: CustomerGrade?,
        @PageableDefault(size = 20) pageable: Pageable
    ): ApiResponse<Page<CustomerResponse>> {
        val condition = CustomerSearchCondition(
            name = name,
            phoneNumber = phoneNumber,
            email = email,
            grade = grade
        )
        return ApiResponse.success(customerService.searchCustomers(condition, pageable))
    }

    @Operation(summary = "고객 등록")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createCustomer(
        @Valid @RequestBody request: CustomerCreateRequest
    ): ApiResponse<CustomerResponse> {
        return ApiResponse.success(customerService.createCustomer(request), "Customer created successfully")
    }

    @Operation(summary = "고객 정보 수정")
    @PutMapping("/{id}")
    fun updateCustomer(
        @PathVariable id: Long,
        @Valid @RequestBody request: CustomerUpdateRequest
    ): ApiResponse<CustomerResponse> {
        return ApiResponse.success(customerService.updateCustomer(id, request))
    }

    @Operation(summary = "고객 삭제")
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    fun deleteCustomer(@PathVariable id: Long) {
        customerService.deleteCustomer(id)
    }
}
