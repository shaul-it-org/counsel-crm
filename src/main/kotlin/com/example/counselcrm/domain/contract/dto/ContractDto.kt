package com.example.counselcrm.domain.contract.dto

import com.example.counselcrm.domain.contract.entity.Contract
import com.example.counselcrm.domain.contract.entity.ContractStatus
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime

data class ContractResponse(
    val id: Long,
    val contractNumber: String,
    val customerId: Long,
    val customerName: String,
    val status: ContractStatus,
    val productName: String,
    val monthlyFee: BigDecimal,
    val contractPeriodMonths: Int,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val memo: String?,
    val createdAt: LocalDateTime
) {
    companion object {
        fun from(contract: Contract) = ContractResponse(
            id = contract.id,
            contractNumber = contract.contractNumber,
            customerId = contract.customer.id,
            customerName = contract.customer.name,
            status = contract.status,
            productName = contract.productName,
            monthlyFee = contract.monthlyFee,
            contractPeriodMonths = contract.contractPeriodMonths,
            startDate = contract.startDate,
            endDate = contract.endDate,
            memo = contract.memo,
            createdAt = contract.createdAt
        )
    }
}

data class ContractSummaryResponse(
    val id: Long,
    val contractNumber: String,
    val productName: String,
    val status: ContractStatus,
    val monthlyFee: BigDecimal
) {
    companion object {
        fun from(contract: Contract) = ContractSummaryResponse(
            id = contract.id,
            contractNumber = contract.contractNumber,
            productName = contract.productName,
            status = contract.status,
            monthlyFee = contract.monthlyFee
        )
    }
}
