package com.example.counselcrm.domain.statistics.dto

import com.example.counselcrm.domain.counseling.entity.CounselingCategory
import com.example.counselcrm.domain.counseling.entity.CounselingStatus

data class DashboardStatistics(
    val totalCounselings: Long,
    val waitingCount: Long,
    val inProgressCount: Long,
    val completedToday: Long,
    val availableCounselors: Int
)

data class CounselingStatusStatistics(
    val status: CounselingStatus,
    val count: Long
)

data class CounselingCategoryStatistics(
    val category: CounselingCategory,
    val count: Long
)

data class CounselorPerformance(
    val counselorId: Long,
    val counselorName: String,
    val completedCount: Long,
    val averageHandlingTimeMinutes: Double?
)
