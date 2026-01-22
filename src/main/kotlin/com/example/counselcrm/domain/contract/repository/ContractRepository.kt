package com.example.counselcrm.domain.contract.repository

import com.example.counselcrm.domain.contract.entity.Contract
import com.example.counselcrm.domain.contract.entity.ContractStatus
import org.springframework.data.jpa.repository.JpaRepository

interface ContractRepository : JpaRepository<Contract, Long> {
    fun findByContractNumber(contractNumber: String): Contract?
    fun findByCustomerId(customerId: Long): List<Contract>
    fun findByCustomerIdAndStatus(customerId: Long, status: ContractStatus): List<Contract>
}
