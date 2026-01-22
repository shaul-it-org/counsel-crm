package com.example.counselcrm.domain.statistics.controller

import com.example.counselcrm.domain.statistics.dto.*
import com.example.counselcrm.domain.statistics.service.StatisticsService
import com.example.counselcrm.global.common.ApiResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@Tag(name = "Statistics", description = "통계 API")
@RestController
@RequestMapping("/api/v1/statistics")
class StatisticsController(
    private val statisticsService: StatisticsService
) {
    @Operation(summary = "대시보드 통계")
    @GetMapping("/dashboard")
    fun getDashboardStatistics(): ApiResponse<DashboardStatistics> {
        return ApiResponse.success(statisticsService.getDashboardStatistics())
    }

    @Operation(summary = "상담 상태별 통계")
    @GetMapping("/status")
    fun getStatusStatistics(): ApiResponse<List<CounselingStatusStatistics>> {
        return ApiResponse.success(statisticsService.getStatusStatistics())
    }

    @Operation(summary = "상담 카테고리별 통계")
    @GetMapping("/category")
    fun getCategoryStatistics(): ApiResponse<List<CounselingCategoryStatistics>> {
        return ApiResponse.success(statisticsService.getCategoryStatistics())
    }

    @Operation(summary = "상담사 성과 통계")
    @GetMapping("/counselor-performance")
    fun getCounselorPerformance(
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) startDate: LocalDate,
        @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) endDate: LocalDate
    ): ApiResponse<List<CounselorPerformance>> {
        return ApiResponse.success(
            statisticsService.getCounselorPerformance(
                startDate.atStartOfDay(),
                endDate.plusDays(1).atStartOfDay()
            )
        )
    }
}
