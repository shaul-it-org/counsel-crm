package com.example.counselcrm.domain.counselor.entity

import com.example.counselcrm.global.common.BaseEntity
import jakarta.persistence.*

@Entity
@Table(name = "counselors")
class Counselor(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false, length = 50)
    var name: String,

    @Column(nullable = false, unique = true, length = 50)
    var employeeId: String,

    @Column(length = 20)
    var extensionNumber: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: CounselorStatus = CounselorStatus.AVAILABLE,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var team: CounselorTeam = CounselorTeam.GENERAL,

    @Column(nullable = false)
    var isActive: Boolean = true
) : BaseEntity() {

    fun changeStatus(newStatus: CounselorStatus) {
        this.status = newStatus
    }

    fun activate() {
        this.isActive = true
    }

    fun deactivate() {
        this.isActive = false
        this.status = CounselorStatus.OFFLINE
    }

    fun isAvailableForCounseling(): Boolean =
        isActive && status == CounselorStatus.AVAILABLE
}

enum class CounselorStatus {
    AVAILABLE,   // 상담 가능
    BUSY,        // 상담 중
    BREAK,       // 휴식 중
    OFFLINE      // 오프라인
}

enum class CounselorTeam {
    GENERAL,     // 일반 상담
    VIP,         // VIP 전담
    COMPLAINT,   // 불만 처리
    TECHNICAL    // 기술 지원
}
