package com.example.counselcrm.scenario

import com.example.counselcrm.domain.counseling.dto.CounselingAssignRequest
import com.example.counselcrm.domain.counseling.dto.CounselingCreateRequest
import com.example.counselcrm.domain.counseling.dto.CounselNoteCreateRequest
import com.example.counselcrm.domain.counseling.entity.Counseling
import com.example.counselcrm.domain.counseling.entity.CounselingCategory
import com.example.counselcrm.domain.counseling.entity.CounselingStatus
import com.example.counselcrm.domain.counseling.repository.CounselingRepository
import com.example.counselcrm.domain.counseling.service.CounselingService
import com.example.counselcrm.domain.counselor.entity.Counselor
import com.example.counselcrm.domain.counselor.entity.CounselorStatus
import com.example.counselcrm.domain.counselor.entity.CounselorTeam
import com.example.counselcrm.domain.counselor.service.CounselorService
import com.example.counselcrm.domain.customer.entity.Customer
import com.example.counselcrm.domain.customer.entity.CustomerGrade
import com.example.counselcrm.domain.customer.service.CustomerService
import com.example.counselcrm.global.exception.BusinessException
import com.example.counselcrm.global.exception.InvalidStatusTransitionException
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

@DisplayName("상담 시나리오 테스트")
class 상담시나리오Test {

    private lateinit var counselingRepository: CounselingRepository
    private lateinit var customerService: CustomerService
    private lateinit var counselorService: CounselorService
    private lateinit var counselingService: CounselingService

    @BeforeEach
    fun setUp() {
        counselingRepository = mockk(relaxed = true)
        customerService = mockk()
        counselorService = mockk()
        counselingService = CounselingService(counselingRepository, customerService, counselorService)
    }

    @Nested
    @DisplayName("상담 요청 생성 시나리오")
    inner class 상담요청생성 {

        @Test
        fun `고객이 상품 문의 상담을 요청하면 대기 상태로 생성된다`() {
            // given
            val customer = createCustomer(1L, "홍길동")
            val request = CounselingCreateRequest(
                customerId = 1L,
                category = CounselingCategory.PRODUCT_INQUIRY,
                title = "렌탈 상품 문의드립니다",
                content = "정수기 렌탈 가격이 궁금합니다"
            )
            val counselingSlot = slot<Counseling>()

            every { customerService.findCustomerById(1L) } returns customer
            every { counselingRepository.save(capture(counselingSlot)) } answers {
                counselingSlot.captured.apply {
                    setId(this, 1L)
                }
            }

            // when
            val result = counselingService.createCounseling(request)

            // then
            assertEquals(CounselingStatus.WAITING, result.status)
            assertEquals(CounselingCategory.PRODUCT_INQUIRY, result.category)
            assertEquals("렌탈 상품 문의드립니다", result.title)
            assertNull(result.counselor)
        }

        @Test
        fun `불만 접수 카테고리로 상담을 요청할 수 있다`() {
            // given
            val customer = createCustomer(1L, "김철수")
            val request = CounselingCreateRequest(
                customerId = 1L,
                category = CounselingCategory.COMPLAINT,
                title = "서비스 불만 접수",
                content = "배송이 너무 늦습니다"
            )
            val counselingSlot = slot<Counseling>()

            every { customerService.findCustomerById(1L) } returns customer
            every { counselingRepository.save(capture(counselingSlot)) } answers {
                counselingSlot.captured.apply { setId(this, 2L) }
            }

            // when
            val result = counselingService.createCounseling(request)

            // then
            assertEquals(CounselingCategory.COMPLAINT, result.category)
        }

        @Test
        fun `해지 문의 상담을 요청할 수 있다`() {
            // given
            val customer = createCustomer(1L, "이영희")
            val request = CounselingCreateRequest(
                customerId = 1L,
                category = CounselingCategory.CANCELLATION,
                title = "계약 해지 문의"
            )
            val counselingSlot = slot<Counseling>()

            every { customerService.findCustomerById(1L) } returns customer
            every { counselingRepository.save(capture(counselingSlot)) } answers {
                counselingSlot.captured.apply { setId(this, 3L) }
            }

            // when
            val result = counselingService.createCounseling(request)

            // then
            assertEquals(CounselingCategory.CANCELLATION, result.category)
        }
    }

    @Nested
    @DisplayName("상담사 배정 시나리오")
    inner class 상담사배정 {

        @Test
        fun `대기중인 상담에 가능한 상담사를 배정하면 배정 상태가 된다`() {
            // given
            val customer = createCustomer(1L, "홍길동")
            val counselor = createCounselor(1L, "김상담", CounselorStatus.AVAILABLE)
            val counseling = createCounseling(1L, customer, CounselingStatus.WAITING)
            val request = CounselingAssignRequest(counselorId = 1L)

            every { counselingRepository.findById(1L) } returns Optional.of(counseling)
            every { counselorService.findCounselorById(1L) } returns counselor

            // when
            val result = counselingService.assignCounseling(1L, request)

            // then
            assertEquals(CounselingStatus.ASSIGNED, result.status)
            assertNotNull(result.counselor)
            assertEquals("김상담", result.counselor?.name)
        }

        @Test
        fun `상담사 배정 시 상담사 상태가 BUSY로 변경된다`() {
            // given
            val customer = createCustomer(1L, "홍길동")
            val counselor = createCounselor(1L, "김상담", CounselorStatus.AVAILABLE)
            val counseling = createCounseling(1L, customer, CounselingStatus.WAITING)
            val request = CounselingAssignRequest(counselorId = 1L)

            every { counselingRepository.findById(1L) } returns Optional.of(counseling)
            every { counselorService.findCounselorById(1L) } returns counselor

            // when
            counselingService.assignCounseling(1L, request)

            // then
            assertEquals(CounselorStatus.BUSY, counselor.status)
        }

        @Test
        fun `이미 상담 중인 상담사에게 배정하면 예외가 발생한다`() {
            // given
            val customer = createCustomer(1L, "홍길동")
            val busyCounselor = createCounselor(1L, "바쁜상담사", CounselorStatus.BUSY)
            val counseling = createCounseling(1L, customer, CounselingStatus.WAITING)
            val request = CounselingAssignRequest(counselorId = 1L)

            every { counselingRepository.findById(1L) } returns Optional.of(counseling)
            every { counselorService.findCounselorById(1L) } returns busyCounselor

            // when & then
            assertThrows<BusinessException> {
                counselingService.assignCounseling(1L, request)
            }
        }

        @Test
        fun `휴식 중인 상담사에게 배정하면 예외가 발생한다`() {
            // given
            val customer = createCustomer(1L, "홍길동")
            val breakCounselor = createCounselor(1L, "휴식상담사", CounselorStatus.BREAK)
            val counseling = createCounseling(1L, customer, CounselingStatus.WAITING)
            val request = CounselingAssignRequest(counselorId = 1L)

            every { counselingRepository.findById(1L) } returns Optional.of(counseling)
            every { counselorService.findCounselorById(1L) } returns breakCounselor

            // when & then
            assertThrows<BusinessException> {
                counselingService.assignCounseling(1L, request)
            }
        }

        @Test
        fun `비활성화된 상담사에게 배정하면 예외가 발생한다`() {
            // given
            val customer = createCustomer(1L, "홍길동")
            val inactiveCounselor = createCounselor(1L, "비활성상담사", CounselorStatus.AVAILABLE, isActive = false)
            val counseling = createCounseling(1L, customer, CounselingStatus.WAITING)
            val request = CounselingAssignRequest(counselorId = 1L)

            every { counselingRepository.findById(1L) } returns Optional.of(counseling)
            every { counselorService.findCounselorById(1L) } returns inactiveCounselor

            // when & then
            assertThrows<BusinessException> {
                counselingService.assignCounseling(1L, request)
            }
        }
    }

    @Nested
    @DisplayName("상담 진행 시나리오")
    inner class 상담진행 {

        @Test
        fun `배정된 상담을 시작하면 진행 중 상태가 된다`() {
            // given
            val customer = createCustomer(1L, "홍길동")
            val counselor = createCounselor(1L, "김상담", CounselorStatus.BUSY)
            val counseling = createCounseling(1L, customer, CounselingStatus.ASSIGNED, counselor)

            every { counselingRepository.findById(1L) } returns Optional.of(counseling)

            // when
            val result = counselingService.startCounseling(1L)

            // then
            assertEquals(CounselingStatus.IN_PROGRESS, result.status)
            assertNotNull(result.startedAt)
        }

        @Test
        fun `대기 상태의 상담을 바로 시작하면 예외가 발생한다`() {
            // given
            val customer = createCustomer(1L, "홍길동")
            val counseling = createCounseling(1L, customer, CounselingStatus.WAITING)

            every { counselingRepository.findById(1L) } returns Optional.of(counseling)

            // when & then
            assertThrows<InvalidStatusTransitionException> {
                counselingService.startCounseling(1L)
            }
        }
    }

    @Nested
    @DisplayName("상담 완료 시나리오")
    inner class 상담완료 {

        @Test
        fun `진행 중인 상담을 완료하면 완료 상태가 된다`() {
            // given
            val customer = createCustomer(1L, "홍길동")
            val counselor = createCounselor(1L, "김상담", CounselorStatus.BUSY)
            val counseling = createCounseling(1L, customer, CounselingStatus.IN_PROGRESS, counselor)

            every { counselingRepository.findById(1L) } returns Optional.of(counseling)

            // when
            val result = counselingService.completeCounseling(1L)

            // then
            assertEquals(CounselingStatus.COMPLETED, result.status)
            assertNotNull(result.completedAt)
        }

        @Test
        fun `상담 완료 시 상담사 상태가 AVAILABLE로 변경된다`() {
            // given
            val customer = createCustomer(1L, "홍길동")
            val counselor = createCounselor(1L, "김상담", CounselorStatus.BUSY)
            val counseling = createCounseling(1L, customer, CounselingStatus.IN_PROGRESS, counselor)

            every { counselingRepository.findById(1L) } returns Optional.of(counseling)

            // when
            counselingService.completeCounseling(1L)

            // then
            assertEquals(CounselorStatus.AVAILABLE, counselor.status)
        }

        @Test
        fun `이미 완료된 상담을 다시 완료하면 예외가 발생한다`() {
            // given
            val customer = createCustomer(1L, "홍길동")
            val counseling = createCounseling(1L, customer, CounselingStatus.COMPLETED)

            every { counselingRepository.findById(1L) } returns Optional.of(counseling)

            // when & then
            assertThrows<InvalidStatusTransitionException> {
                counselingService.completeCounseling(1L)
            }
        }
    }

    @Nested
    @DisplayName("상담 보류 및 재개 시나리오")
    inner class 상담보류및재개 {

        @Test
        fun `진행 중인 상담을 보류할 수 있다`() {
            // given
            val customer = createCustomer(1L, "홍길동")
            val counselor = createCounselor(1L, "김상담", CounselorStatus.BUSY)
            val counseling = createCounseling(1L, customer, CounselingStatus.IN_PROGRESS, counselor)

            every { counselingRepository.findById(1L) } returns Optional.of(counseling)

            // when
            val result = counselingService.holdCounseling(1L)

            // then
            assertEquals(CounselingStatus.ON_HOLD, result.status)
        }

        @Test
        fun `보류된 상담을 다시 시작할 수 있다`() {
            // given
            val customer = createCustomer(1L, "홍길동")
            val counselor = createCounselor(1L, "김상담", CounselorStatus.BUSY)
            val counseling = createCounseling(1L, customer, CounselingStatus.ON_HOLD, counselor)

            every { counselingRepository.findById(1L) } returns Optional.of(counseling)

            // when
            val result = counselingService.startCounseling(1L)

            // then
            assertEquals(CounselingStatus.IN_PROGRESS, result.status)
        }

        @Test
        fun `보류된 상담을 바로 완료할 수 있다`() {
            // given
            val customer = createCustomer(1L, "홍길동")
            val counselor = createCounselor(1L, "김상담", CounselorStatus.BUSY)
            val counseling = createCounseling(1L, customer, CounselingStatus.ON_HOLD, counselor)

            every { counselingRepository.findById(1L) } returns Optional.of(counseling)

            // when
            val result = counselingService.completeCounseling(1L)

            // then
            assertEquals(CounselingStatus.COMPLETED, result.status)
        }
    }

    @Nested
    @DisplayName("상담 취소 시나리오")
    inner class 상담취소 {

        @Test
        fun `대기 중인 상담을 취소할 수 있다`() {
            // given
            val customer = createCustomer(1L, "홍길동")
            val counseling = createCounseling(1L, customer, CounselingStatus.WAITING)

            every { counselingRepository.findById(1L) } returns Optional.of(counseling)

            // when
            val result = counselingService.cancelCounseling(1L)

            // then
            assertEquals(CounselingStatus.CANCELLED, result.status)
        }

        @Test
        fun `배정된 상담을 취소하면 상담사 상태가 AVAILABLE로 변경된다`() {
            // given
            val customer = createCustomer(1L, "홍길동")
            val counselor = createCounselor(1L, "김상담", CounselorStatus.BUSY)
            val counseling = createCounseling(1L, customer, CounselingStatus.ASSIGNED, counselor)

            every { counselingRepository.findById(1L) } returns Optional.of(counseling)

            // when
            counselingService.cancelCounseling(1L)

            // then
            assertEquals(CounselorStatus.AVAILABLE, counselor.status)
        }

        @Test
        fun `진행 중인 상담을 취소할 수 있다`() {
            // given
            val customer = createCustomer(1L, "홍길동")
            val counselor = createCounselor(1L, "김상담", CounselorStatus.BUSY)
            val counseling = createCounseling(1L, customer, CounselingStatus.IN_PROGRESS, counselor)

            every { counselingRepository.findById(1L) } returns Optional.of(counseling)

            // when
            val result = counselingService.cancelCounseling(1L)

            // then
            assertEquals(CounselingStatus.CANCELLED, result.status)
            assertEquals(CounselorStatus.AVAILABLE, counselor.status)
        }

        @Test
        fun `이미 완료된 상담은 취소할 수 없다`() {
            // given
            val customer = createCustomer(1L, "홍길동")
            val counseling = createCounseling(1L, customer, CounselingStatus.COMPLETED)

            every { counselingRepository.findById(1L) } returns Optional.of(counseling)

            // when & then
            assertThrows<InvalidStatusTransitionException> {
                counselingService.cancelCounseling(1L)
            }
        }

        @Test
        fun `이미 취소된 상담은 다시 취소할 수 없다`() {
            // given
            val customer = createCustomer(1L, "홍길동")
            val counseling = createCounseling(1L, customer, CounselingStatus.CANCELLED)

            every { counselingRepository.findById(1L) } returns Optional.of(counseling)

            // when & then
            assertThrows<InvalidStatusTransitionException> {
                counselingService.cancelCounseling(1L)
            }
        }
    }

    @Nested
    @DisplayName("상담 메모 시나리오")
    inner class 상담메모 {

        @Test
        fun `상담사가 상담에 메모를 추가할 수 있다`() {
            // given
            val customer = createCustomer(1L, "홍길동")
            val counselor = createCounselor(1L, "김상담", CounselorStatus.BUSY)
            val counseling = createCounseling(1L, customer, CounselingStatus.IN_PROGRESS, counselor)
            val request = CounselNoteCreateRequest(
                counselorId = 1L,
                content = "고객이 정수기 렌탈 가격 문의함. 월 29,900원 안내 완료"
            )

            every { counselingRepository.findById(1L) } returns Optional.of(counseling)
            every { counselorService.findCounselorById(1L) } returns counselor

            // when
            val result = counselingService.addNote(1L, request)

            // then
            assertEquals("고객이 정수기 렌탈 가격 문의함. 월 29,900원 안내 완료", result.content)
            assertEquals("김상담", result.counselorName)
        }

        @Test
        fun `하나의 상담에 여러 메모를 추가할 수 있다`() {
            // given
            val customer = createCustomer(1L, "홍길동")
            val counselor = createCounselor(1L, "김상담", CounselorStatus.BUSY)
            val counseling = createCounseling(1L, customer, CounselingStatus.IN_PROGRESS, counselor)

            every { counselingRepository.findById(1L) } returns Optional.of(counseling)
            every { counselorService.findCounselorById(1L) } returns counselor

            // when
            counselingService.addNote(1L, CounselNoteCreateRequest(1L, "첫 번째 메모"))
            counselingService.addNote(1L, CounselNoteCreateRequest(1L, "두 번째 메모"))
            val notes = counselingService.getCounselNotes(1L)

            // then
            assertEquals(2, notes.size)
        }
    }

    @Nested
    @DisplayName("상담 조회 시나리오")
    inner class 상담조회 {

        @Test
        fun `대기 중인 상담 목록을 조회할 수 있다`() {
            // given
            val customer1 = createCustomer(1L, "홍길동")
            val customer2 = createCustomer(2L, "김철수")
            val waitingCounselings = listOf(
                createCounseling(1L, customer1, CounselingStatus.WAITING),
                createCounseling(2L, customer2, CounselingStatus.WAITING)
            )

            every { counselingRepository.findByStatus(CounselingStatus.WAITING) } returns waitingCounselings

            // when
            val result = counselingService.getWaitingCounselings()

            // then
            assertEquals(2, result.size)
            assertTrue(result.all { it.status == CounselingStatus.WAITING })
        }

        @Test
        fun `특정 고객의 상담 이력을 조회할 수 있다`() {
            // given
            val customer = createCustomer(1L, "홍길동")
            val counselings = listOf(
                createCounseling(1L, customer, CounselingStatus.COMPLETED),
                createCounseling(2L, customer, CounselingStatus.IN_PROGRESS),
                createCounseling(3L, customer, CounselingStatus.WAITING)
            )

            every { counselingRepository.findByCustomerIdWithCustomer(1L) } returns counselings

            // when
            val result = counselingService.getCounselingsByCustomer(1L)

            // then
            assertEquals(3, result.size)
        }
    }

    @Nested
    @DisplayName("전체 상담 플로우 시나리오")
    inner class 전체상담플로우 {

        @Test
        fun `상담의 전체 라이프사이클_대기에서_완료까지`() {
            // given
            val customer = createCustomer(1L, "홍길동")
            val counselor = createCounselor(1L, "김상담", CounselorStatus.AVAILABLE)

            // 상담 생성
            val counseling = Counseling(
                id = 0,
                customer = customer,
                category = CounselingCategory.PRODUCT_INQUIRY,
                title = "상품 문의"
            )
            setId(counseling, 1L)

            every { customerService.findCustomerById(1L) } returns customer
            every { counselingRepository.save(any()) } returns counseling
            every { counselingRepository.findById(1L) } returns Optional.of(counseling)
            every { counselorService.findCounselorById(1L) } returns counselor

            // when & then: 1. 생성 (WAITING)
            val created = counselingService.createCounseling(
                CounselingCreateRequest(1L, CounselingCategory.PRODUCT_INQUIRY, "상품 문의")
            )
            assertEquals(CounselingStatus.WAITING, created.status)

            // 2. 배정 (ASSIGNED)
            val assigned = counselingService.assignCounseling(1L, CounselingAssignRequest(1L))
            assertEquals(CounselingStatus.ASSIGNED, assigned.status)
            assertEquals(CounselorStatus.BUSY, counselor.status)

            // 3. 시작 (IN_PROGRESS)
            val started = counselingService.startCounseling(1L)
            assertEquals(CounselingStatus.IN_PROGRESS, started.status)

            // 4. 완료 (COMPLETED)
            val completed = counselingService.completeCounseling(1L)
            assertEquals(CounselingStatus.COMPLETED, completed.status)
            assertEquals(CounselorStatus.AVAILABLE, counselor.status)
        }
    }

    // Helper functions
    private fun createCustomer(id: Long, name: String): Customer {
        return Customer(
            id = id,
            name = name,
            phoneNumber = "010${id.toString().padStart(8, '0')}",
            grade = CustomerGrade.NORMAL
        )
    }

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

    private fun createCounseling(
        id: Long,
        customer: Customer,
        status: CounselingStatus,
        counselor: Counselor? = null
    ): Counseling {
        val counseling = Counseling(
            id = 0,
            customer = customer,
            category = CounselingCategory.PRODUCT_INQUIRY,
            title = "테스트 상담"
        )
        setId(counseling, id)
        setStatus(counseling, status)
        if (counselor != null) {
            counseling.counselor = counselor
        }
        return counseling
    }

    private fun setId(counseling: Counseling, id: Long) {
        val idField = Counseling::class.java.getDeclaredField("id")
        idField.isAccessible = true
        idField.set(counseling, id)
    }

    private fun setStatus(counseling: Counseling, status: CounselingStatus) {
        val statusField = Counseling::class.java.getDeclaredField("status")
        statusField.isAccessible = true
        statusField.set(counseling, status)
    }
}
