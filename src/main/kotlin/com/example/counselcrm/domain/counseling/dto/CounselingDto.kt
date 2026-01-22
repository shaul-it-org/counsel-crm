package com.example.counselcrm.domain.counseling.dto

import com.example.counselcrm.domain.counseling.entity.Counseling
import com.example.counselcrm.domain.counseling.entity.CounselingCategory
import com.example.counselcrm.domain.counseling.entity.CounselingStatus
import com.example.counselcrm.domain.counselor.dto.CounselorSummaryResponse
import com.example.counselcrm.domain.customer.dto.CustomerResponse
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.time.LocalDateTime

data class CounselingCreateRequest(
    @field:NotNull(message = "Customer ID is required")
    val customerId: Long,

    @field:NotNull(message = "Category is required")
    val category: CounselingCategory,

    @field:NotBlank(message = "Title is required")
    val title: String,

    val content: String? = null
)

data class CounselingAssignRequest(
    @field:NotNull(message = "Counselor ID is required")
    val counselorId: Long
)

data class CounselNoteCreateRequest(
    @field:NotNull(message = "Counselor ID is required")
    val counselorId: Long,

    @field:NotBlank(message = "Content is required")
    val content: String
)

data class CounselingResponse(
    val id: Long,
    val customer: CustomerResponse,
    val counselor: CounselorSummaryResponse?,
    val status: CounselingStatus,
    val category: CounselingCategory,
    val title: String,
    val content: String?,
    val assignedAt: LocalDateTime?,
    val startedAt: LocalDateTime?,
    val completedAt: LocalDateTime?,
    val createdAt: LocalDateTime,
    val updatedAt: LocalDateTime
) {
    companion object {
        fun from(counseling: Counseling) = CounselingResponse(
            id = counseling.id,
            customer = CustomerResponse.from(counseling.customer),
            counselor = counseling.counselor?.let { CounselorSummaryResponse.from(it) },
            status = counseling.status,
            category = counseling.category,
            title = counseling.title,
            content = counseling.content,
            assignedAt = counseling.assignedAt,
            startedAt = counseling.startedAt,
            completedAt = counseling.completedAt,
            createdAt = counseling.createdAt,
            updatedAt = counseling.updatedAt
        )
    }
}

data class CounselingListResponse(
    val id: Long,
    val customerName: String,
    val customerPhone: String,
    val counselorName: String?,
    val status: CounselingStatus,
    val category: CounselingCategory,
    val title: String,
    val createdAt: LocalDateTime
) {
    companion object {
        fun from(counseling: Counseling) = CounselingListResponse(
            id = counseling.id,
            customerName = counseling.customer.name,
            customerPhone = counseling.customer.phoneNumber,
            counselorName = counseling.counselor?.name,
            status = counseling.status,
            category = counseling.category,
            title = counseling.title,
            createdAt = counseling.createdAt
        )
    }
}

data class CounselNoteResponse(
    val id: Long,
    val counselorName: String,
    val content: String,
    val createdAt: LocalDateTime
) {
    companion object {
        fun from(note: com.example.counselcrm.domain.counseling.entity.CounselNote) = CounselNoteResponse(
            id = note.id,
            counselorName = note.counselor.name,
            content = note.content,
            createdAt = note.createdAt
        )
    }
}
