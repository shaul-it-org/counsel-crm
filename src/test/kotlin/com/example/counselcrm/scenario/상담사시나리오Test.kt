package com.example.counselcrm.scenario

import com.example.counselcrm.domain.counselor.dto.CounselorCreateRequest
import com.example.counselcrm.domain.counselor.dto.CounselorStatusUpdateRequest
import com.example.counselcrm.domain.counselor.entity.Counselor
import com.example.counselcrm.domain.counselor.entity.CounselorStatus
import com.example.counselcrm.domain.counselor.entity.CounselorTeam
import com.example.counselcrm.domain.counselor.repository.CounselorRepository
import com.example.counselcrm.domain.counselor.service.CounselorService
import com.example.counselcrm.global.exception.EntityNotFoundException
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*

@DisplayName("상담사 시나리오 테스트")
class 상담사시나리오Test {

    private lateinit var counselorRepository: CounselorRepository
    private lateinit var counselorService: CounselorService

    @BeforeEach
    fun setUp() {
        counselorRepository = mockk(relaxed = true)
        counselorService = CounselorService(counselorRepository)
    }

    @Nested
    @DisplayName("상담사 등록 시나리오")
    inner class 상담사등록 {

        @Test
        fun `관리자가 일반 상담 팀에 상담사를 등록할 수 있다`() {
            // given
            val request = CounselorCreateRequest(
                name = "김상담",
                employeeId = "EMP001",
                extensionNumber = "1234",
                team = CounselorTeam.GENERAL
            )
            val counselorSlot = slot<Counselor>()

            every { counselorRepository.save(capture(counselorSlot)) } answers {
                counselorSlot.captured.apply {
                    setId(this, 1L)
                }
            }

            // when
            val result = counselorService.createCounselor(request)

            // then
            assertEquals("김상담", result.name)
            assertEquals("EMP001", result.employeeId)
            assertEquals(CounselorTeam.GENERAL, result.team)
            assertEquals(CounselorStatus.AVAILABLE, result.status)
            assertTrue(result.isActive)
        }

        @Test
        fun `관리자가 VIP 전담 팀에 상담사를 등록할 수 있다`() {
            // given
            val request = CounselorCreateRequest(
                name = "박VIP",
                employeeId = "EMP002",
                team = CounselorTeam.VIP
            )
            val counselorSlot = slot<Counselor>()

            every { counselorRepository.save(capture(counselorSlot)) } answers {
                counselorSlot.captured.apply { setId(this, 2L) }
            }

            // when
            val result = counselorService.createCounselor(request)

            // then
            assertEquals(CounselorTeam.VIP, result.team)
        }

        @Test
        fun `관리자가 불만처리 팀에 상담사를 등록할 수 있다`() {
            // given
            val request = CounselorCreateRequest(
                name = "이불만",
                employeeId = "EMP003",
                team = CounselorTeam.COMPLAINT
            )
            val counselorSlot = slot<Counselor>()

            every { counselorRepository.save(capture(counselorSlot)) } answers {
                counselorSlot.captured.apply { setId(this, 3L) }
            }

            // when
            val result = counselorService.createCounselor(request)

            // then
            assertEquals(CounselorTeam.COMPLAINT, result.team)
        }

        @Test
        fun `관리자가 기술지원 팀에 상담사를 등록할 수 있다`() {
            // given
            val request = CounselorCreateRequest(
                name = "최기술",
                employeeId = "EMP004",
                team = CounselorTeam.TECHNICAL
            )
            val counselorSlot = slot<Counselor>()

            every { counselorRepository.save(capture(counselorSlot)) } answers {
                counselorSlot.captured.apply { setId(this, 4L) }
            }

            // when
            val result = counselorService.createCounselor(request)

            // then
            assertEquals(CounselorTeam.TECHNICAL, result.team)
        }

        @Test
        fun `신규 등록된 상담사는 기본적으로 AVAILABLE 상태이다`() {
            // given
            val request = CounselorCreateRequest(
                name = "신규상담사",
                employeeId = "EMP005",
                team = CounselorTeam.GENERAL
            )
            val counselorSlot = slot<Counselor>()

            every { counselorRepository.save(capture(counselorSlot)) } answers {
                counselorSlot.captured.apply { setId(this, 5L) }
            }

            // when
            val result = counselorService.createCounselor(request)

            // then
            assertEquals(CounselorStatus.AVAILABLE, result.status)
        }
    }

    @Nested
    @DisplayName("상담사 조회 시나리오")
    inner class 상담사조회 {

        @Test
        fun `상담사 ID로 상세 정보를 조회할 수 있다`() {
            // given
            val counselor = createCounselor(1L, "김상담", CounselorStatus.AVAILABLE, CounselorTeam.GENERAL)

            every { counselorRepository.findById(1L) } returns Optional.of(counselor)

            // when
            val result = counselorService.getCounselor(1L)

            // then
            assertEquals(1L, result.id)
            assertEquals("김상담", result.name)
        }

        @Test
        fun `존재하지 않는 상담사 ID로 조회하면 예외가 발생한다`() {
            // given
            every { counselorRepository.findById(999L) } returns Optional.empty()

            // when & then
            assertThrows<EntityNotFoundException> {
                counselorService.getCounselor(999L)
            }
        }

        @Test
        fun `모든 활성 상담사를 조회할 수 있다`() {
            // given
            val activeCounselors = listOf(
                createCounselor(1L, "김상담", CounselorStatus.AVAILABLE),
                createCounselor(2L, "이상담", CounselorStatus.BUSY),
                createCounselor(3L, "박상담", CounselorStatus.BREAK)
            )

            every { counselorRepository.findByIsActiveTrue() } returns activeCounselors

            // when
            val result = counselorService.getAllActiveCounselors()

            // then
            assertEquals(3, result.size)
        }

        @Test
        fun `상담 가능한 상담사만 조회할 수 있다`() {
            // given
            val availableCounselors = listOf(
                createCounselor(1L, "김상담", CounselorStatus.AVAILABLE),
                createCounselor(2L, "이상담", CounselorStatus.AVAILABLE)
            )

            every {
                counselorRepository.findByStatusAndIsActiveTrue(CounselorStatus.AVAILABLE)
            } returns availableCounselors

            // when
            val result = counselorService.getAvailableCounselors()

            // then
            assertEquals(2, result.size)
            assertTrue(result.all { it.status == CounselorStatus.AVAILABLE })
        }

        @Test
        fun `특정 팀의 상담사만 조회할 수 있다`() {
            // given
            val vipCounselors = listOf(
                createCounselor(1L, "VIP상담사1", CounselorStatus.AVAILABLE, CounselorTeam.VIP),
                createCounselor(2L, "VIP상담사2", CounselorStatus.BUSY, CounselorTeam.VIP)
            )

            every {
                counselorRepository.findByTeamAndIsActiveTrue(CounselorTeam.VIP)
            } returns vipCounselors

            // when
            val result = counselorService.getCounselorsByTeam(CounselorTeam.VIP)

            // then
            assertEquals(2, result.size)
            assertTrue(result.all { it.team == CounselorTeam.VIP })
        }
    }

    @Nested
    @DisplayName("상담사 상태 변경 시나리오")
    inner class 상담사상태변경 {

        @Test
        fun `상담사가 휴식 상태로 변경할 수 있다`() {
            // given
            val counselor = createCounselor(1L, "김상담", CounselorStatus.AVAILABLE)
            val request = CounselorStatusUpdateRequest(status = CounselorStatus.BREAK)

            every { counselorRepository.findById(1L) } returns Optional.of(counselor)

            // when
            val result = counselorService.updateStatus(1L, request)

            // then
            assertEquals(CounselorStatus.BREAK, result.status)
        }

        @Test
        fun `휴식 중인 상담사가 상담 가능 상태로 복귀할 수 있다`() {
            // given
            val counselor = createCounselor(1L, "김상담", CounselorStatus.BREAK)
            val request = CounselorStatusUpdateRequest(status = CounselorStatus.AVAILABLE)

            every { counselorRepository.findById(1L) } returns Optional.of(counselor)

            // when
            val result = counselorService.updateStatus(1L, request)

            // then
            assertEquals(CounselorStatus.AVAILABLE, result.status)
        }

        @Test
        fun `상담사가 오프라인 상태로 변경할 수 있다`() {
            // given
            val counselor = createCounselor(1L, "김상담", CounselorStatus.AVAILABLE)
            val request = CounselorStatusUpdateRequest(status = CounselorStatus.OFFLINE)

            every { counselorRepository.findById(1L) } returns Optional.of(counselor)

            // when
            val result = counselorService.updateStatus(1L, request)

            // then
            assertEquals(CounselorStatus.OFFLINE, result.status)
        }
    }

    @Nested
    @DisplayName("상담사 활성화/비활성화 시나리오")
    inner class 상담사활성화비활성화 {

        @Test
        fun `관리자가 상담사를 비활성화하면 오프라인 상태가 된다`() {
            // given
            val counselor = createCounselor(1L, "김상담", CounselorStatus.AVAILABLE)

            every { counselorRepository.findById(1L) } returns Optional.of(counselor)

            // when
            val result = counselorService.deactivate(1L)

            // then
            assertFalse(result.isActive)
            assertEquals(CounselorStatus.OFFLINE, result.status)
        }

        @Test
        fun `관리자가 비활성화된 상담사를 다시 활성화할 수 있다`() {
            // given
            val counselor = createCounselor(1L, "김상담", CounselorStatus.OFFLINE, isActive = false)

            every { counselorRepository.findById(1L) } returns Optional.of(counselor)

            // when
            val result = counselorService.activate(1L)

            // then
            assertTrue(result.isActive)
        }

        @Test
        fun `비활성화된 상담사는 상담 배정이 불가능하다`() {
            // given
            val inactiveCounselor = createCounselor(1L, "비활성상담사", CounselorStatus.AVAILABLE, isActive = false)

            // when
            val canCounsel = inactiveCounselor.isAvailableForCounseling()

            // then
            assertFalse(canCounsel)
        }

        @Test
        fun `BUSY 상태의 상담사는 상담 배정이 불가능하다`() {
            // given
            val busyCounselor = createCounselor(1L, "바쁜상담사", CounselorStatus.BUSY)

            // when
            val canCounsel = busyCounselor.isAvailableForCounseling()

            // then
            assertFalse(canCounsel)
        }

        @Test
        fun `AVAILABLE 상태이고 활성화된 상담사만 상담 배정이 가능하다`() {
            // given
            val availableCounselor = createCounselor(1L, "가능상담사", CounselorStatus.AVAILABLE)

            // when
            val canCounsel = availableCounselor.isAvailableForCounseling()

            // then
            assertTrue(canCounsel)
        }
    }

    @Nested
    @DisplayName("상담사 업무 플로우 시나리오")
    inner class 상담사업무플로우 {

        @Test
        fun `상담사의 하루 업무 플로우_출근에서_퇴근까지`() {
            // given
            val counselor = createCounselor(1L, "김상담", CounselorStatus.OFFLINE)

            every { counselorRepository.findById(1L) } returns Optional.of(counselor)

            // 1. 출근 - AVAILABLE로 변경
            var result = counselorService.updateStatus(1L, CounselorStatusUpdateRequest(CounselorStatus.AVAILABLE))
            assertEquals(CounselorStatus.AVAILABLE, result.status)
            assertTrue(counselor.isAvailableForCounseling())

            // 2. 상담 시작 - BUSY로 변경 (상담 배정 시 자동)
            result = counselorService.updateStatus(1L, CounselorStatusUpdateRequest(CounselorStatus.BUSY))
            assertEquals(CounselorStatus.BUSY, result.status)
            assertFalse(counselor.isAvailableForCounseling())

            // 3. 상담 완료 - AVAILABLE로 변경
            result = counselorService.updateStatus(1L, CounselorStatusUpdateRequest(CounselorStatus.AVAILABLE))
            assertEquals(CounselorStatus.AVAILABLE, result.status)

            // 4. 점심 휴식 - BREAK로 변경
            result = counselorService.updateStatus(1L, CounselorStatusUpdateRequest(CounselorStatus.BREAK))
            assertEquals(CounselorStatus.BREAK, result.status)
            assertFalse(counselor.isAvailableForCounseling())

            // 5. 휴식 종료 - AVAILABLE로 복귀
            result = counselorService.updateStatus(1L, CounselorStatusUpdateRequest(CounselorStatus.AVAILABLE))
            assertEquals(CounselorStatus.AVAILABLE, result.status)

            // 6. 퇴근 - OFFLINE으로 변경
            result = counselorService.updateStatus(1L, CounselorStatusUpdateRequest(CounselorStatus.OFFLINE))
            assertEquals(CounselorStatus.OFFLINE, result.status)
        }
    }

    // Helper functions
    private fun createCounselor(
        id: Long,
        name: String,
        status: CounselorStatus,
        team: CounselorTeam = CounselorTeam.GENERAL,
        isActive: Boolean = true
    ): Counselor {
        return Counselor(
            id = id,
            name = name,
            employeeId = "EMP${id.toString().padStart(3, '0')}",
            status = status,
            team = team,
            isActive = isActive
        )
    }

    private fun setId(counselor: Counselor, id: Long) {
        val idField = Counselor::class.java.getDeclaredField("id")
        idField.isAccessible = true
        idField.set(counselor, id)
    }
}
