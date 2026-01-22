package com.example.counselcrm.domain.contract.controller

import com.example.counselcrm.domain.contract.dto.ContractResponse
import com.example.counselcrm.domain.contract.dto.ContractSummaryResponse
import com.example.counselcrm.domain.contract.service.ContractService
import com.example.counselcrm.global.common.ApiResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.web.bind.annotation.*

@Tag(name = "Contract", description = "계약 조회 API")
@RestController
@RequestMapping("/api/v1/contracts")
class ContractController(
    private val contractService: ContractService
) {
    @Operation(summary = "계약 단건 조회")
    @GetMapping("/{id}")
    fun getContract(@PathVariable id: Long): ApiResponse<ContractResponse> {
        return ApiResponse.success(contractService.getContract(id))
    }

    @Operation(summary = "계약번호로 조회")
    @GetMapping("/number/{contractNumber}")
    fun getContractByNumber(@PathVariable contractNumber: String): ApiResponse<ContractResponse> {
        return ApiResponse.success(contractService.getContractByNumber(contractNumber))
    }

    @Operation(summary = "고객별 계약 목록")
    @GetMapping("/customer/{customerId}")
    fun getContractsByCustomer(@PathVariable customerId: Long): ApiResponse<List<ContractSummaryResponse>> {
        return ApiResponse.success(contractService.getContractsByCustomer(customerId))
    }

    @Operation(summary = "고객별 활성 계약 목록")
    @GetMapping("/customer/{customerId}/active")
    fun getActiveContractsByCustomer(@PathVariable customerId: Long): ApiResponse<List<ContractSummaryResponse>> {
        return ApiResponse.success(contractService.getActiveContractsByCustomer(customerId))
    }
}
