package com.example.counselcrm.domain.contract.entity

import com.example.counselcrm.domain.customer.entity.Customer
import com.example.counselcrm.global.common.BaseEntity
import jakarta.persistence.*
import java.math.BigDecimal
import java.time.LocalDate

@Entity
@Table(name = "contracts")
class Contract(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false, unique = true, length = 30)
    val contractNumber: String,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    val customer: Customer,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: ContractStatus = ContractStatus.ACTIVE,

    @Column(nullable = false, length = 100)
    val productName: String,

    @Column(nullable = false, precision = 10, scale = 2)
    val monthlyFee: BigDecimal,

    @Column(nullable = false)
    val contractPeriodMonths: Int,

    @Column(nullable = false)
    val startDate: LocalDate,

    @Column(nullable = false)
    val endDate: LocalDate,

    @Column(length = 500)
    var memo: String? = null
) : BaseEntity() {

    fun isExpired(): Boolean = LocalDate.now().isAfter(endDate)

    fun terminate() {
        this.status = ContractStatus.TERMINATED
    }

    fun suspend() {
        this.status = ContractStatus.SUSPENDED
    }
}

enum class ContractStatus {
    ACTIVE,      // 활성
    SUSPENDED,   // 일시정지
    TERMINATED,  // 해지
    EXPIRED      // 만료
}
