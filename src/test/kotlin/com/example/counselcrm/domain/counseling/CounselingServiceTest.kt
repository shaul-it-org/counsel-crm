package com.example.counselcrm.domain.counseling

import com.example.counselcrm.domain.counseling.dto.CounselingAssignRequest
import com.example.counselcrm.domain.counseling.dto.CounselingCreateRequest
import com.example.counselcrm.domain.counseling.entity.Counseling
import com.example.counselcrm.domain.counseling.entity.CounselingCategory
import com.example.counselcrm.domain.counseling.entity.CounselingStatus
import com.example.counselcrm.domain.counseling.repository.CounselingRepository
import com.example.counselcrm.domain.counseling.service.CounselingService
import com.example.counselcrm.domain.counselor.entity.Counselor
import com.example.counselcrm.domain.counselor.entity.CounselorStatus
import com.example.counselcrm.domain.counselor.service.CounselorService
import com.example.counselcrm.domain.customer.entity.Customer
import com.example.counselcrm.domain.customer.service.CustomerService
import com.example.counselcrm.global.exception.BusinessException
import com.example.counselcrm.global.exception.InvalidStatusTransitionException
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.*

@DisplayName("CounselingService 테스트")
class CounselingServiceTest {

    private lateinit var counselingRepository: CounselingRepository
    private lateinit var customerService: CustomerService
    private lateinit var counselorService: CounselorService
    private lateinit var counselingService: CounselingService

    @BeforeEach
    fun setUp() {
        counselingRepository = mockk()
        customerService = mockk()
        counselorService = mockk()
        counselingService = CounselingService(counselingRepository, customerService, counselorService)
    }

    @Nested
    @DisplayName("createCounseling")
    inner class CreateCounseling {
        @Test
        @DisplayName("유효한 요청으로 상담을 생성하면 CounselingResponse를 반환한다")
        fun `should create counseling with valid request`() {
            // given
            val customer = createCustomer(1L)
            val request = CounselingCreateRequest(
                customerId = 1L,
                category = CounselingCategory.PRODUCT_INQUIRY,
                title = "상품 문의"
            )
            val savedCounseling = createCounseling(1L, customer)

            every { customerService.findCustomerById(1L) } returns customer
            every { counselingRepository.save(any()) } returns savedCounseling

            // when
            val result = counselingService.createCounseling(request)

            // then
            assertEquals(CounselingStatus.WAITING, result.status)
            assertEquals("상품 문의", result.title)
            verify { counselingRepository.save(any()) }
        }
    }

    @Nested
    @DisplayName("assignCounseling")
    inner class AssignCounseling {
        @Test
        @DisplayName("대기 중인 상담에 상담사를 배정하면 상태가 ASSIGNED로 변경된다")
        fun `should assign counselor to waiting counseling`() {
            // given
            val customer = createCustomer(1L)
            val counselor = createCounselor(1L, CounselorStatus.AVAILABLE)
            val counseling = createCounseling(1L, customer, CounselingStatus.WAITING)
            val request = CounselingAssignRequest(counselorId = 1L)

            every { counselingRepository.findById(1L) } returns Optional.of(counseling)
            every { counselorService.findCounselorById(1L) } returns counselor

            // when
            val result = counselingService.assignCounseling(1L, request)

            // then
            assertEquals(CounselingStatus.ASSIGNED, result.status)
            assertNotNull(result.counselor)
            assertEquals(CounselorStatus.BUSY, counselor.status)
        }

        @Test
        @DisplayName("상담 불가능한 상담사를 배정하면 BusinessException을 던진다")
        fun `should throw exception when counselor is not available`() {
            // given
            val customer = createCustomer(1L)
            val counselor = createCounselor(1L, CounselorStatus.BUSY)
            val counseling = createCounseling(1L, customer, CounselingStatus.WAITING)
            val request = CounselingAssignRequest(counselorId = 1L)

            every { counselingRepository.findById(1L) } returns Optional.of(counseling)
            every { counselorService.findCounselorById(1L) } returns counselor

            // when & then
            assertThrows<BusinessException> {
                counselingService.assignCounseling(1L, request)
            }
        }
    }

    @Nested
    @DisplayName("상담 상태 전이")
    inner class StatusTransition {
        @Test
        @DisplayName("배정된 상담을 시작하면 상태가 IN_PROGRESS로 변경된다")
        fun `should start assigned counseling`() {
            // given
            val customer = createCustomer(1L)
            val counselor = createCounselor(1L, CounselorStatus.BUSY)
            val counseling = createCounseling(1L, customer, CounselingStatus.ASSIGNED, counselor)

            every { counselingRepository.findById(1L) } returns Optional.of(counseling)

            // when
            val result = counselingService.startCounseling(1L)

            // then
            assertEquals(CounselingStatus.IN_PROGRESS, result.status)
        }

        @Test
        @DisplayName("진행 중인 상담을 완료하면 상태가 COMPLETED로 변경되고 상담사는 AVAILABLE이 된다")
        fun `should complete in-progress counseling`() {
            // given
            val customer = createCustomer(1L)
            val counselor = createCounselor(1L, CounselorStatus.BUSY)
            val counseling = createCounseling(1L, customer, CounselingStatus.IN_PROGRESS, counselor)

            every { counselingRepository.findById(1L) } returns Optional.of(counseling)

            // when
            val result = counselingService.completeCounseling(1L)

            // then
            assertEquals(CounselingStatus.COMPLETED, result.status)
            assertEquals(CounselorStatus.AVAILABLE, counselor.status)
        }

        @Test
        @DisplayName("완료된 상담을 시작하려고 하면 InvalidStatusTransitionException을 던진다")
        fun `should throw exception when trying to start completed counseling`() {
            // given
            val customer = createCustomer(1L)
            val counseling = createCounseling(1L, customer, CounselingStatus.COMPLETED)

            every { counselingRepository.findById(1L) } returns Optional.of(counseling)

            // when & then
            assertThrows<InvalidStatusTransitionException> {
                counselingService.startCounseling(1L)
            }
        }
    }

    private fun createCustomer(id: Long): Customer {
        return Customer(
            id = id,
            name = "테스트 고객",
            phoneNumber = "01012345678"
        )
    }

    private fun createCounselor(id: Long, status: CounselorStatus): Counselor {
        return Counselor(
            id = id,
            name = "테스트 상담사",
            employeeId = "EMP001",
            status = status
        )
    }

    private fun createCounseling(
        id: Long,
        customer: Customer,
        status: CounselingStatus = CounselingStatus.WAITING,
        counselor: Counselor? = null
    ): Counseling {
        val counseling = Counseling(
            id = id,
            customer = customer,
            category = CounselingCategory.PRODUCT_INQUIRY,
            title = "상품 문의"
        )
        // 상태 전이를 위해 리플렉션 사용 또는 테스트용 팩토리 메서드 필요
        // 여기서는 간단히 처리
        if (counselor != null) {
            counseling.counselor = counselor
        }
        // status 설정을 위한 리플렉션
        val statusField = Counseling::class.java.getDeclaredField("status")
        statusField.isAccessible = true
        statusField.set(counseling, status)
        return counseling
    }
}
