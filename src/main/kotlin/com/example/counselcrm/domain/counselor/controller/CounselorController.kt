package com.example.counselcrm.domain.counselor.controller

import com.example.counselcrm.domain.counselor.dto.*
import com.example.counselcrm.domain.counselor.entity.CounselorTeam
import com.example.counselcrm.domain.counselor.service.CounselorService
import com.example.counselcrm.global.common.ApiResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@Tag(name = "Counselor", description = "상담사 관리 API")
@RestController
@RequestMapping("/api/v1/counselors")
class CounselorController(
    private val counselorService: CounselorService
) {
    @Operation(summary = "상담사 단건 조회")
    @GetMapping("/{id}")
    fun getCounselor(@PathVariable id: Long): ApiResponse<CounselorResponse> {
        return ApiResponse.success(counselorService.getCounselor(id))
    }

    @Operation(summary = "활성 상담사 전체 조회")
    @GetMapping
    fun getAllActiveCounselors(): ApiResponse<List<CounselorResponse>> {
        return ApiResponse.success(counselorService.getAllActiveCounselors())
    }

    @Operation(summary = "상담 가능한 상담사 조회")
    @GetMapping("/available")
    fun getAvailableCounselors(): ApiResponse<List<CounselorSummaryResponse>> {
        return ApiResponse.success(counselorService.getAvailableCounselors())
    }

    @Operation(summary = "팀별 상담사 조회")
    @GetMapping("/team/{team}")
    fun getCounselorsByTeam(@PathVariable team: CounselorTeam): ApiResponse<List<CounselorResponse>> {
        return ApiResponse.success(counselorService.getCounselorsByTeam(team))
    }

    @Operation(summary = "상담사 등록")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createCounselor(
        @Valid @RequestBody request: CounselorCreateRequest
    ): ApiResponse<CounselorResponse> {
        return ApiResponse.success(counselorService.createCounselor(request))
    }

    @Operation(summary = "상담사 상태 변경")
    @PatchMapping("/{id}/status")
    fun updateStatus(
        @PathVariable id: Long,
        @RequestBody request: CounselorStatusUpdateRequest
    ): ApiResponse<CounselorResponse> {
        return ApiResponse.success(counselorService.updateStatus(id, request))
    }

    @Operation(summary = "상담사 비활성화")
    @PatchMapping("/{id}/deactivate")
    fun deactivate(@PathVariable id: Long): ApiResponse<CounselorResponse> {
        return ApiResponse.success(counselorService.deactivate(id))
    }

    @Operation(summary = "상담사 활성화")
    @PatchMapping("/{id}/activate")
    fun activate(@PathVariable id: Long): ApiResponse<CounselorResponse> {
        return ApiResponse.success(counselorService.activate(id))
    }
}
