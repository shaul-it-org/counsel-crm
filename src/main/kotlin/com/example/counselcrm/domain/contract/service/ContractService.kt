package com.example.counselcrm.domain.contract.service

import com.example.counselcrm.domain.contract.dto.ContractResponse
import com.example.counselcrm.domain.contract.dto.ContractSummaryResponse
import com.example.counselcrm.domain.contract.entity.ContractStatus
import com.example.counselcrm.domain.contract.repository.ContractRepository
import com.example.counselcrm.global.exception.EntityNotFoundException
import com.example.counselcrm.global.exception.ErrorCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class ContractService(
    private val contractRepository: ContractRepository
) {
    fun getContract(id: Long): ContractResponse {
        val contract = contractRepository.findById(id)
            .orElseThrow { EntityNotFoundException(ErrorCode.CONTRACT_NOT_FOUND) }
        return ContractResponse.from(contract)
    }

    fun getContractByNumber(contractNumber: String): ContractResponse {
        val contract = contractRepository.findByContractNumber(contractNumber)
            ?: throw EntityNotFoundException(ErrorCode.CONTRACT_NOT_FOUND)
        return ContractResponse.from(contract)
    }

    fun getContractsByCustomer(customerId: Long): List<ContractSummaryResponse> {
        return contractRepository.findByCustomerId(customerId)
            .map { ContractSummaryResponse.from(it) }
    }

    fun getActiveContractsByCustomer(customerId: Long): List<ContractSummaryResponse> {
        return contractRepository.findByCustomerIdAndStatus(customerId, ContractStatus.ACTIVE)
            .map { ContractSummaryResponse.from(it) }
    }
}
