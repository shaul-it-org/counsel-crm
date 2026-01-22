package com.example.counselcrm.domain.counseling.entity

import com.example.counselcrm.domain.counselor.entity.Counselor
import com.example.counselcrm.domain.customer.entity.Customer
import com.example.counselcrm.global.common.BaseEntity
import com.example.counselcrm.global.exception.InvalidStatusTransitionException
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "counselings")
class Counseling(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "customer_id", nullable = false)
    val customer: Customer,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "counselor_id")
    var counselor: Counselor? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: CounselingStatus = CounselingStatus.WAITING,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    var category: CounselingCategory,

    @Column(nullable = false, length = 200)
    var title: String,

    @Column(columnDefinition = "TEXT")
    var content: String? = null,

    @Column
    var assignedAt: LocalDateTime? = null,

    @Column
    var startedAt: LocalDateTime? = null,

    @Column
    var completedAt: LocalDateTime? = null,

    @OneToMany(mappedBy = "counseling", cascade = [CascadeType.ALL], orphanRemoval = true)
    val notes: MutableList<CounselNote> = mutableListOf()
) : BaseEntity() {

    fun assign(counselor: Counselor) {
        validateStatusTransition(CounselingStatus.ASSIGNED)
        this.counselor = counselor
        this.status = CounselingStatus.ASSIGNED
        this.assignedAt = LocalDateTime.now()
    }

    fun start() {
        validateStatusTransition(CounselingStatus.IN_PROGRESS)
        this.status = CounselingStatus.IN_PROGRESS
        this.startedAt = LocalDateTime.now()
    }

    fun complete() {
        validateStatusTransition(CounselingStatus.COMPLETED)
        this.status = CounselingStatus.COMPLETED
        this.completedAt = LocalDateTime.now()
    }

    fun cancel() {
        validateStatusTransition(CounselingStatus.CANCELLED)
        this.status = CounselingStatus.CANCELLED
    }

    fun hold() {
        validateStatusTransition(CounselingStatus.ON_HOLD)
        this.status = CounselingStatus.ON_HOLD
    }

    fun addNote(content: String, counselor: Counselor): CounselNote {
        val note = CounselNote(
            counseling = this,
            counselor = counselor,
            content = content
        )
        notes.add(note)
        return note
    }

    private fun validateStatusTransition(targetStatus: CounselingStatus) {
        if (!status.canTransitionTo(targetStatus)) {
            throw InvalidStatusTransitionException(status.name, targetStatus.name)
        }
    }
}

enum class CounselingStatus {
    WAITING,      // 대기
    ASSIGNED,     // 배정됨
    IN_PROGRESS,  // 진행 중
    ON_HOLD,      // 보류
    COMPLETED,    // 완료
    CANCELLED;    // 취소

    fun canTransitionTo(target: CounselingStatus): Boolean = when (this) {
        WAITING -> target in listOf(ASSIGNED, CANCELLED)
        ASSIGNED -> target in listOf(IN_PROGRESS, WAITING, CANCELLED)
        IN_PROGRESS -> target in listOf(COMPLETED, ON_HOLD, CANCELLED)
        ON_HOLD -> target in listOf(IN_PROGRESS, COMPLETED, CANCELLED)
        COMPLETED, CANCELLED -> false
    }
}

enum class CounselingCategory {
    PRODUCT_INQUIRY,    // 상품 문의
    CONTRACT,           // 계약 관련
    PAYMENT,            // 결제 관련
    DELIVERY,           // 배송 관련
    COMPLAINT,          // 불만 접수
    TECHNICAL_SUPPORT,  // 기술 지원
    CANCELLATION,       // 해지 문의
    OTHER               // 기타
}
