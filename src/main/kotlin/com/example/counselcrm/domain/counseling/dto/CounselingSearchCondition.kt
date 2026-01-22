package com.example.counselcrm.domain.counseling.dto

import com.example.counselcrm.domain.counseling.entity.CounselingCategory
import com.example.counselcrm.domain.counseling.entity.CounselingStatus
import java.time.LocalDate

data class CounselingSearchCondition(
    val customerId: Long? = null,
    val counselorId: Long? = null,
    val status: CounselingStatus? = null,
    val category: CounselingCategory? = null,
    val titleKeyword: String? = null,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null
)
