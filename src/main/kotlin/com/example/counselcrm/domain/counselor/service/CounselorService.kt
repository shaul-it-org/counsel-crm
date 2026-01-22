package com.example.counselcrm.domain.counselor.service

import com.example.counselcrm.domain.counselor.dto.*
import com.example.counselcrm.domain.counselor.entity.Counselor
import com.example.counselcrm.domain.counselor.entity.CounselorStatus
import com.example.counselcrm.domain.counselor.entity.CounselorTeam
import com.example.counselcrm.domain.counselor.repository.CounselorRepository
import com.example.counselcrm.global.exception.EntityNotFoundException
import com.example.counselcrm.global.exception.ErrorCode
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class CounselorService(
    private val counselorRepository: CounselorRepository
) {
    fun getCounselor(id: Long): CounselorResponse {
        val counselor = findCounselorById(id)
        return CounselorResponse.from(counselor)
    }

    fun getAllActiveCounselors(): List<CounselorResponse> {
        return counselorRepository.findByIsActiveTrue()
            .map { CounselorResponse.from(it) }
    }

    fun getAvailableCounselors(): List<CounselorSummaryResponse> {
        return counselorRepository.findByStatusAndIsActiveTrue(CounselorStatus.AVAILABLE)
            .map { CounselorSummaryResponse.from(it) }
    }

    fun getCounselorsByTeam(team: CounselorTeam): List<CounselorResponse> {
        return counselorRepository.findByTeamAndIsActiveTrue(team)
            .map { CounselorResponse.from(it) }
    }

    @Transactional
    fun createCounselor(request: CounselorCreateRequest): CounselorResponse {
        val counselor = Counselor(
            name = request.name,
            employeeId = request.employeeId,
            extensionNumber = request.extensionNumber,
            team = request.team
        )

        val savedCounselor = counselorRepository.save(counselor)
        return CounselorResponse.from(savedCounselor)
    }

    @Transactional
    fun updateStatus(id: Long, request: CounselorStatusUpdateRequest): CounselorResponse {
        val counselor = findCounselorById(id)
        counselor.changeStatus(request.status)
        return CounselorResponse.from(counselor)
    }

    @Transactional
    fun deactivate(id: Long): CounselorResponse {
        val counselor = findCounselorById(id)
        counselor.deactivate()
        return CounselorResponse.from(counselor)
    }

    @Transactional
    fun activate(id: Long): CounselorResponse {
        val counselor = findCounselorById(id)
        counselor.activate()
        return CounselorResponse.from(counselor)
    }

    internal fun findCounselorById(id: Long): Counselor {
        return counselorRepository.findById(id)
            .orElseThrow { EntityNotFoundException(ErrorCode.COUNSELOR_NOT_FOUND) }
    }
}
