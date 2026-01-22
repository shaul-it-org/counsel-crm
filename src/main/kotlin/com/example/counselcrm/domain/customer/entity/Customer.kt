package com.example.counselcrm.domain.customer.entity

import com.example.counselcrm.global.common.BaseEntity
import jakarta.persistence.*

@Entity
@Table(name = "customers")
class Customer(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false, length = 50)
    var name: String,

    @Column(nullable = false, unique = true, length = 20)
    var phoneNumber: String,

    @Column(length = 100)
    var email: String? = null,

    @Column(length = 500)
    var address: String? = null,

    @Column(length = 1000)
    var memo: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var grade: CustomerGrade = CustomerGrade.NORMAL
) : BaseEntity() {

    fun update(
        name: String? = null,
        phoneNumber: String? = null,
        email: String? = null,
        address: String? = null,
        memo: String? = null,
        grade: CustomerGrade? = null
    ) {
        name?.let { this.name = it }
        phoneNumber?.let { this.phoneNumber = it }
        email?.let { this.email = it }
        address?.let { this.address = it }
        memo?.let { this.memo = it }
        grade?.let { this.grade = it }
    }
}

enum class CustomerGrade {
    VIP,
    PREMIUM,
    NORMAL,
    NEW
}
