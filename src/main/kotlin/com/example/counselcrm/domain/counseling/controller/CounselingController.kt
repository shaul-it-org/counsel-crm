package com.example.counselcrm.domain.counseling.controller

import com.example.counselcrm.domain.counseling.dto.*
import com.example.counselcrm.domain.counseling.entity.CounselingCategory
import com.example.counselcrm.domain.counseling.entity.CounselingStatus
import com.example.counselcrm.domain.counseling.service.CounselingService
import com.example.counselcrm.global.common.ApiResponse
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@Tag(name = "Counseling", description = "상담 관리 API")
@RestController
@RequestMapping("/api/v1/counselings")
class CounselingController(
    private val counselingService: CounselingService
) {
    @Operation(summary = "상담 단건 조회")
    @GetMapping("/{id}")
    fun getCounseling(@PathVariable id: Long): ApiResponse<CounselingResponse> {
        return ApiResponse.success(counselingService.getCounseling(id))
    }

    @Operation(summary = "상담 검색")
    @GetMapping
    fun searchCounselings(
        @RequestParam(required = false) customerId: Long?,
        @RequestParam(required = false) counselorId: Long?,
        @RequestParam(required = false) status: CounselingStatus?,
        @RequestParam(required = false) category: CounselingCategory?,
        @RequestParam(required = false) titleKeyword: String?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) startDate: LocalDate?,
        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) endDate: LocalDate?,
        @PageableDefault(size = 20) pageable: Pageable
    ): ApiResponse<Page<CounselingListResponse>> {
        val condition = CounselingSearchCondition(
            customerId = customerId,
            counselorId = counselorId,
            status = status,
            category = category,
            titleKeyword = titleKeyword,
            startDate = startDate,
            endDate = endDate
        )
        return ApiResponse.success(counselingService.searchCounselings(condition, pageable))
    }

    @Operation(summary = "대기 중인 상담 목록")
    @GetMapping("/waiting")
    fun getWaitingCounselings(): ApiResponse<List<CounselingListResponse>> {
        return ApiResponse.success(counselingService.getWaitingCounselings())
    }

    @Operation(summary = "고객별 상담 이력")
    @GetMapping("/customer/{customerId}")
    fun getCounselingsByCustomer(@PathVariable customerId: Long): ApiResponse<List<CounselingListResponse>> {
        return ApiResponse.success(counselingService.getCounselingsByCustomer(customerId))
    }

    @Operation(summary = "상담 생성")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    fun createCounseling(
        @Valid @RequestBody request: CounselingCreateRequest
    ): ApiResponse<CounselingResponse> {
        return ApiResponse.success(counselingService.createCounseling(request))
    }

    @Operation(summary = "상담사 배정")
    @PostMapping("/{id}/assign")
    fun assignCounseling(
        @PathVariable id: Long,
        @Valid @RequestBody request: CounselingAssignRequest
    ): ApiResponse<CounselingResponse> {
        return ApiResponse.success(counselingService.assignCounseling(id, request))
    }

    @Operation(summary = "상담 시작")
    @PostMapping("/{id}/start")
    fun startCounseling(@PathVariable id: Long): ApiResponse<CounselingResponse> {
        return ApiResponse.success(counselingService.startCounseling(id))
    }

    @Operation(summary = "상담 완료")
    @PostMapping("/{id}/complete")
    fun completeCounseling(@PathVariable id: Long): ApiResponse<CounselingResponse> {
        return ApiResponse.success(counselingService.completeCounseling(id))
    }

    @Operation(summary = "상담 취소")
    @PostMapping("/{id}/cancel")
    fun cancelCounseling(@PathVariable id: Long): ApiResponse<CounselingResponse> {
        return ApiResponse.success(counselingService.cancelCounseling(id))
    }

    @Operation(summary = "상담 보류")
    @PostMapping("/{id}/hold")
    fun holdCounseling(@PathVariable id: Long): ApiResponse<CounselingResponse> {
        return ApiResponse.success(counselingService.holdCounseling(id))
    }

    @Operation(summary = "상담 메모 목록")
    @GetMapping("/{id}/notes")
    fun getCounselNotes(@PathVariable id: Long): ApiResponse<List<CounselNoteResponse>> {
        return ApiResponse.success(counselingService.getCounselNotes(id))
    }

    @Operation(summary = "상담 메모 추가")
    @PostMapping("/{id}/notes")
    @ResponseStatus(HttpStatus.CREATED)
    fun addNote(
        @PathVariable id: Long,
        @Valid @RequestBody request: CounselNoteCreateRequest
    ): ApiResponse<CounselNoteResponse> {
        return ApiResponse.success(counselingService.addNote(id, request))
    }
}
