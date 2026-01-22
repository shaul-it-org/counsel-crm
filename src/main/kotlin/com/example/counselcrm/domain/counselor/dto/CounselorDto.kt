package com.example.counselcrm.domain.counselor.dto

import com.example.counselcrm.domain.counselor.entity.Counselor
import com.example.counselcrm.domain.counselor.entity.CounselorStatus
import com.example.counselcrm.domain.counselor.entity.CounselorTeam
import jakarta.validation.constraints.NotBlank
import java.time.LocalDateTime

data class CounselorCreateRequest(
    @field:NotBlank(message = "Name is required")
    val name: String,

    @field:NotBlank(message = "Employee ID is required")
    val employeeId: String,

    val extensionNumber: String? = null,
    val team: CounselorTeam = CounselorTeam.GENERAL
)

data class CounselorStatusUpdateRequest(
    val status: CounselorStatus
)

data class CounselorResponse(
    val id: Long,
    val name: String,
    val employeeId: String,
    val extensionNumber: String?,
    val status: CounselorStatus,
    val team: CounselorTeam,
    val isActive: Boolean,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) {
    companion object {
        fun from(counselor: Counselor) = CounselorResponse(
            id = counselor.id,
            name = counselor.name,
            employeeId = counselor.employeeId,
            extensionNumber = counselor.extensionNumber,
            status = counselor.status,
            team = counselor.team,
            isActive = counselor.isActive,
            createdAt = counselor.createdAt,
            updatedAt = counselor.updatedAt
        )
    }
}

data class CounselorSummaryResponse(
    val id: Long,
    val name: String,
    val status: CounselorStatus,
    val team: CounselorTeam
) {
    companion object {
        fun from(counselor: Counselor) = CounselorSummaryResponse(
            id = counselor.id,
            name = counselor.name,
            status = counselor.status,
            team = counselor.team
        )
    }
}
