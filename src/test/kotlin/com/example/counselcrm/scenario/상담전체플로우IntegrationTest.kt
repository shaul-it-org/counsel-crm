package com.example.counselcrm.scenario

import com.example.counselcrm.domain.counseling.dto.CounselingAssignRequest
import com.example.counselcrm.domain.counseling.dto.CounselingCreateRequest
import com.example.counselcrm.domain.counseling.dto.CounselNoteCreateRequest
import com.example.counselcrm.domain.counseling.entity.CounselingCategory
import com.example.counselcrm.domain.counseling.entity.CounselingStatus
import com.example.counselcrm.domain.counseling.service.CounselingService
import com.example.counselcrm.domain.counselor.dto.CounselorCreateRequest
import com.example.counselcrm.domain.counselor.entity.CounselorStatus
import com.example.counselcrm.domain.counselor.entity.CounselorTeam
import com.example.counselcrm.domain.counselor.service.CounselorService
import com.example.counselcrm.domain.customer.dto.CustomerCreateRequest
import com.example.counselcrm.domain.customer.entity.CustomerGrade
import com.example.counselcrm.domain.customer.service.CustomerService
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.MethodOrderer
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Order
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestMethodOrder
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.test.context.ActiveProfiles
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("상담 전체 플로우 통합 테스트")
class 상담전체플로우IntegrationTest {

    @Autowired
    private lateinit var customerService: CustomerService

    @Autowired
    private lateinit var counselorService: CounselorService

    @Autowired
    private lateinit var counselingService: CounselingService

    @Nested
    @DisplayName("시나리오 1: VIP 고객 긴급 상담 처리")
    @TestMethodOrder(MethodOrderer.OrderAnnotation::class)
    inner class VIP고객긴급상담처리 {

        @Test
        @Order(1)
        fun `VIP_고객이_불만_상담을_요청하면_VIP팀_상담사가_처리한다`() {
            // 1. VIP 고객 등록
            val customerResponse = customerService.createCustomer(
                CustomerCreateRequest(
                    name = "김VIP",
                    phoneNumber = "010-1111-1111",
                    email = "vip@example.com",
                    grade = CustomerGrade.VIP
                )
            )
            assertEquals(CustomerGrade.VIP, customerResponse.grade)

            // 2. VIP 전담 상담사 등록
            val counselorResponse = counselorService.createCounselor(
                CounselorCreateRequest(
                    name = "박VIP담당",
                    employeeId = "VIP001",
                    team = CounselorTeam.VIP
                )
            )
            assertEquals(CounselorTeam.VIP, counselorResponse.team)
            assertEquals(CounselorStatus.AVAILABLE, counselorResponse.status)

            // 3. 불만 상담 생성
            val counselingResponse = counselingService.createCounseling(
                CounselingCreateRequest(
                    customerId = customerResponse.id,
                    category = CounselingCategory.COMPLAINT,
                    title = "서비스 품질 불만",
                    content = "제품 배송이 3일이나 지연되었습니다."
                )
            )
            assertEquals(CounselingStatus.WAITING, counselingResponse.status)
            assertEquals(CounselingCategory.COMPLAINT, counselingResponse.category)

            // 4. VIP 상담사 배정
            val assignedCounseling = counselingService.assignCounseling(
                counselingResponse.id,
                CounselingAssignRequest(counselorId = counselorResponse.id)
            )
            assertEquals(CounselingStatus.ASSIGNED, assignedCounseling.status)
            assertEquals("박VIP담당", assignedCounseling.counselor?.name)

            // 5. 상담 시작
            val startedCounseling = counselingService.startCounseling(counselingResponse.id)
            assertEquals(CounselingStatus.IN_PROGRESS, startedCounseling.status)
            assertNotNull(startedCounseling.startedAt)

            // 6. 상담 메모 추가
            val note = counselingService.addNote(
                counselingResponse.id,
                CounselNoteCreateRequest(
                    counselorId = counselorResponse.id,
                    content = "배송 지연에 대해 사과 및 보상 안내 완료. 다음 주문 시 10% 할인쿠폰 제공 약속."
                )
            )
            assertTrue(note.content.contains("보상"))

            // 7. 상담 완료
            val completedCounseling = counselingService.completeCounseling(counselingResponse.id)
            assertEquals(CounselingStatus.COMPLETED, completedCounseling.status)
            assertNotNull(completedCounseling.completedAt)
        }
    }

    @Nested
    @DisplayName("시나리오 2: 신규 고객 상품 문의 → 계약")
    inner class 신규고객상품문의계약 {

        @Test
        fun `신규_고객이_상품_문의_후_계약_상담으로_이어진다`() {
            // 1. 신규 고객 등록
            val customer = customerService.createCustomer(
                CustomerCreateRequest(
                    name = "이신규",
                    phoneNumber = "010-2222-2222",
                    email = "new@example.com",
                    grade = CustomerGrade.NEW
                )
            )
            assertEquals(CustomerGrade.NEW, customer.grade)

            // 2. 일반 상담사 등록
            val counselor = counselorService.createCounselor(
                CounselorCreateRequest(
                    name = "김일반",
                    employeeId = "GEN001",
                    team = CounselorTeam.GENERAL
                )
            )

            // 3. 상품 문의 상담 생성
            val inquiryCounseling = counselingService.createCounseling(
                CounselingCreateRequest(
                    customerId = customer.id,
                    category = CounselingCategory.PRODUCT_INQUIRY,
                    title = "정수기 렌탈 문의",
                    content = "정수기 렌탈 가격과 조건이 궁금합니다."
                )
            )

            // 4. 상담 진행
            counselingService.assignCounseling(
                inquiryCounseling.id,
                CounselingAssignRequest(counselorId = counselor.id)
            )
            counselingService.startCounseling(inquiryCounseling.id)
            counselingService.addNote(
                inquiryCounseling.id,
                CounselNoteCreateRequest(counselor.id, "정수기 모델별 가격 안내 완료. 계약 의향 있음.")
            )
            val completedInquiry = counselingService.completeCounseling(inquiryCounseling.id)
            assertEquals(CounselingStatus.COMPLETED, completedInquiry.status)

            // 5. 계약 관련 후속 상담 생성
            val contractCounseling = counselingService.createCounseling(
                CounselingCreateRequest(
                    customerId = customer.id,
                    category = CounselingCategory.CONTRACT,
                    title = "정수기 렌탈 계약 진행",
                    content = "정수기 모델 A 렌탈 계약 희망"
                )
            )
            assertEquals(CounselingCategory.CONTRACT, contractCounseling.category)

            // 6. 고객 상담 이력 확인
            val customerHistory = counselingService.getCounselingsByCustomer(customer.id)
            assertEquals(2, customerHistory.size)
            assertTrue(customerHistory.any { it.category == CounselingCategory.PRODUCT_INQUIRY })
            assertTrue(customerHistory.any { it.category == CounselingCategory.CONTRACT })
        }
    }

    @Nested
    @DisplayName("시나리오 3: 상담 보류 후 재개")
    inner class 상담보류후재개 {

        @Test
        fun `고객_확인이_필요한_상담을_보류했다가_재개한다`() {
            // 준비
            val customer = customerService.createCustomer(
                CustomerCreateRequest(name = "박보류", phoneNumber = "010-3333-3333")
            )
            val counselor = counselorService.createCounselor(
                CounselorCreateRequest(name = "이상담", employeeId = "GEN002", team = CounselorTeam.GENERAL)
            )

            // 상담 생성 및 시작
            val counseling = counselingService.createCounseling(
                CounselingCreateRequest(
                    customerId = customer.id,
                    category = CounselingCategory.PAYMENT,
                    title = "결제 관련 문의"
                )
            )
            counselingService.assignCounseling(counseling.id, CounselingAssignRequest(counselor.id))
            counselingService.startCounseling(counseling.id)

            // 메모 추가 - 확인 필요
            counselingService.addNote(
                counseling.id,
                CounselNoteCreateRequest(counselor.id, "고객에게 결제 내역 확인 요청. 콜백 대기.")
            )

            // 보류 처리
            val heldCounseling = counselingService.holdCounseling(counseling.id)
            assertEquals(CounselingStatus.ON_HOLD, heldCounseling.status)

            // 재개 (고객 확인 완료 후)
            val resumedCounseling = counselingService.startCounseling(counseling.id)
            assertEquals(CounselingStatus.IN_PROGRESS, resumedCounseling.status)

            // 완료 메모 추가
            counselingService.addNote(
                counseling.id,
                CounselNoteCreateRequest(counselor.id, "고객 확인 완료. 이중 결제 확인되어 환불 처리 안내.")
            )

            // 상담 완료
            val completedCounseling = counselingService.completeCounseling(counseling.id)
            assertEquals(CounselingStatus.COMPLETED, completedCounseling.status)

            // 메모 이력 확인
            val notes = counselingService.getCounselNotes(counseling.id)
            assertEquals(2, notes.size)
        }
    }

    @Nested
    @DisplayName("시나리오 4: 고객 요청 상담 취소")
    inner class 고객요청상담취소 {

        @Test
        fun `대기_중인_상담을_고객_요청으로_취소한다`() {
            // 준비
            val customer = customerService.createCustomer(
                CustomerCreateRequest(name = "최취소", phoneNumber = "010-4444-4444")
            )

            // 상담 생성
            val counseling = counselingService.createCounseling(
                CounselingCreateRequest(
                    customerId = customer.id,
                    category = CounselingCategory.CANCELLATION,
                    title = "해지 문의"
                )
            )
            assertEquals(CounselingStatus.WAITING, counseling.status)

            // 고객 요청으로 취소 (마음이 바뀜)
            val cancelledCounseling = counselingService.cancelCounseling(counseling.id)
            assertEquals(CounselingStatus.CANCELLED, cancelledCounseling.status)
        }

        @Test
        fun `배정된_상담을_취소하면_상담사가_다시_가용상태가_된다`() {
            // 준비
            val customer = customerService.createCustomer(
                CustomerCreateRequest(name = "정취소", phoneNumber = "010-5555-5555")
            )
            val counselor = counselorService.createCounselor(
                CounselorCreateRequest(name = "한상담", employeeId = "GEN003", team = CounselorTeam.GENERAL)
            )

            // 상담 생성 및 배정
            val counseling = counselingService.createCounseling(
                CounselingCreateRequest(
                    customerId = customer.id,
                    category = CounselingCategory.OTHER,
                    title = "기타 문의"
                )
            )
            counselingService.assignCounseling(counseling.id, CounselingAssignRequest(counselor.id))

            // 배정 후 상담사 상태 확인 (BUSY)
            val busyCounselor = counselorService.getCounselor(counselor.id)
            assertEquals(CounselorStatus.BUSY, busyCounselor.status)

            // 상담 취소
            counselingService.cancelCounseling(counseling.id)

            // 상담사 상태 확인 (AVAILABLE로 복귀)
            val availableCounselor = counselorService.getCounselor(counselor.id)
            assertEquals(CounselorStatus.AVAILABLE, availableCounselor.status)
        }
    }

    @Nested
    @DisplayName("시나리오 5: 기술지원 상담")
    inner class 기술지원상담 {

        @Test
        fun `기술_문제_상담은_기술지원팀이_처리한다`() {
            // 고객 등록
            val customer = customerService.createCustomer(
                CustomerCreateRequest(
                    name = "오기술",
                    phoneNumber = "010-6666-6666",
                    grade = CustomerGrade.PREMIUM
                )
            )

            // 기술지원팀 상담사 등록
            val techCounselor = counselorService.createCounselor(
                CounselorCreateRequest(
                    name = "강기술",
                    employeeId = "TECH001",
                    team = CounselorTeam.TECHNICAL
                )
            )
            assertEquals(CounselorTeam.TECHNICAL, techCounselor.team)

            // 기술 지원 상담 생성
            val techCounseling = counselingService.createCounseling(
                CounselingCreateRequest(
                    customerId = customer.id,
                    category = CounselingCategory.TECHNICAL_SUPPORT,
                    title = "정수기 온수 기능 오류",
                    content = "정수기 온수가 나오지 않습니다. A/S 요청합니다."
                )
            )
            assertEquals(CounselingCategory.TECHNICAL_SUPPORT, techCounseling.category)

            // 기술팀 상담사 배정 및 처리
            counselingService.assignCounseling(techCounseling.id, CounselingAssignRequest(techCounselor.id))
            counselingService.startCounseling(techCounseling.id)

            // 기술 점검 메모
            counselingService.addNote(
                techCounseling.id,
                CounselNoteCreateRequest(techCounselor.id, "원격 점검 결과 온수 히터 불량 판단. A/S 기사 방문 예약.")
            )

            // 완료
            val completed = counselingService.completeCounseling(techCounseling.id)
            assertEquals(CounselingStatus.COMPLETED, completed.status)
        }
    }

    @Nested
    @DisplayName("시나리오 6: 상담사 상태 관리")
    inner class 상담사상태관리 {

        @Test
        fun `상담사가_휴식_후_복귀하여_상담을_처리한다`() {
            // 상담사 등록
            val counselor = counselorService.createCounselor(
                CounselorCreateRequest(
                    name = "윤상담",
                    employeeId = "GEN004",
                    team = CounselorTeam.GENERAL
                )
            )
            assertEquals(CounselorStatus.AVAILABLE, counselor.status)

            // 휴식 상태로 변경
            val onBreak = counselorService.updateStatus(
                counselor.id,
                com.example.counselcrm.domain.counselor.dto.CounselorStatusUpdateRequest(CounselorStatus.BREAK)
            )
            assertEquals(CounselorStatus.BREAK, onBreak.status)

            // 가용 상담사 목록에서 제외되는지 확인
            val availableCounselors = counselorService.getAvailableCounselors()
            assertTrue(availableCounselors.none { it.id == counselor.id })

            // 휴식 종료, 상담 가능 상태로 복귀
            val backToWork = counselorService.updateStatus(
                counselor.id,
                com.example.counselcrm.domain.counselor.dto.CounselorStatusUpdateRequest(CounselorStatus.AVAILABLE)
            )
            assertEquals(CounselorStatus.AVAILABLE, backToWork.status)

            // 가용 상담사 목록에 포함되는지 확인
            val updatedAvailable = counselorService.getAvailableCounselors()
            assertTrue(updatedAvailable.any { it.id == counselor.id })
        }
    }
}
