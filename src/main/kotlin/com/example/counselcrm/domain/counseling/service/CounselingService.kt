package com.example.counselcrm.domain.counseling.service

import com.example.counselcrm.domain.counseling.dto.*
import com.example.counselcrm.domain.counseling.entity.Counseling
import com.example.counselcrm.domain.counseling.entity.CounselingStatus
import com.example.counselcrm.domain.counseling.repository.CounselingRepository
import com.example.counselcrm.domain.counselor.entity.CounselorStatus
import com.example.counselcrm.domain.counselor.service.CounselorService
import com.example.counselcrm.domain.customer.service.CustomerService
import com.example.counselcrm.global.exception.BusinessException
import com.example.counselcrm.global.exception.EntityNotFoundException
import com.example.counselcrm.global.exception.ErrorCode
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class CounselingService(
    private val counselingRepository: CounselingRepository,
    private val customerService: CustomerService,
    private val counselorService: CounselorService
) {
    fun getCounseling(id: Long): CounselingResponse {
        val counseling = findCounselingById(id)
        return CounselingResponse.from(counseling)
    }

    fun searchCounselings(
        condition: CounselingSearchCondition,
        pageable: Pageable
    ): Page<CounselingListResponse> {
        return counselingRepository.search(condition, pageable)
            .map { CounselingListResponse.from(it) }
    }

    fun getWaitingCounselings(): List<CounselingListResponse> {
        return counselingRepository.findByStatus(CounselingStatus.WAITING)
            .map { CounselingListResponse.from(it) }
    }

    fun getCounselingsByCustomer(customerId: Long): List<CounselingListResponse> {
        return counselingRepository.findByCustomerIdWithCustomer(customerId)
            .map { CounselingListResponse.from(it) }
    }

    fun getCounselNotes(counselingId: Long): List<CounselNoteResponse> {
        val counseling = findCounselingById(counselingId)
        return counseling.notes.map { CounselNoteResponse.from(it) }
    }

    @Transactional
    fun createCounseling(request: CounselingCreateRequest): CounselingResponse {
        val customer = customerService.findCustomerById(request.customerId)

        val counseling = Counseling(
            customer = customer,
            category = request.category,
            title = request.title,
            content = request.content
        )

        val savedCounseling = counselingRepository.save(counseling)
        return CounselingResponse.from(savedCounseling)
    }

    @Transactional
    fun assignCounseling(id: Long, request: CounselingAssignRequest): CounselingResponse {
        val counseling = findCounselingById(id)
        val counselor = counselorService.findCounselorById(request.counselorId)

        if (!counselor.isAvailableForCounseling()) {
            throw BusinessException(ErrorCode.COUNSELOR_NOT_AVAILABLE)
        }

        counseling.assign(counselor)
        counselor.changeStatus(CounselorStatus.BUSY)

        return CounselingResponse.from(counseling)
    }

    @Transactional
    fun startCounseling(id: Long): CounselingResponse {
        val counseling = findCounselingById(id)
        counseling.start()
        return CounselingResponse.from(counseling)
    }

    @Transactional
    fun completeCounseling(id: Long): CounselingResponse {
        val counseling = findCounselingById(id)
        counseling.complete()

        counseling.counselor?.changeStatus(CounselorStatus.AVAILABLE)

        return CounselingResponse.from(counseling)
    }

    @Transactional
    fun cancelCounseling(id: Long): CounselingResponse {
        val counseling = findCounselingById(id)
        counseling.cancel()

        counseling.counselor?.changeStatus(CounselorStatus.AVAILABLE)

        return CounselingResponse.from(counseling)
    }

    @Transactional
    fun holdCounseling(id: Long): CounselingResponse {
        val counseling = findCounselingById(id)
        counseling.hold()
        return CounselingResponse.from(counseling)
    }

    @Transactional
    fun addNote(counselingId: Long, request: CounselNoteCreateRequest): CounselNoteResponse {
        val counseling = findCounselingById(counselingId)
        val counselor = counselorService.findCounselorById(request.counselorId)

        val note = counseling.addNote(request.content, counselor)
        return CounselNoteResponse.from(note)
    }

    private fun findCounselingById(id: Long): Counseling {
        return counselingRepository.findById(id)
            .orElseThrow { EntityNotFoundException(ErrorCode.COUNSELING_NOT_FOUND) }
    }
}
