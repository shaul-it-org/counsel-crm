package com.example.counselcrm.scenario

import com.example.counselcrm.domain.customer.dto.CustomerCreateRequest
import com.example.counselcrm.domain.customer.dto.CustomerSearchCondition
import com.example.counselcrm.domain.customer.dto.CustomerUpdateRequest
import com.example.counselcrm.domain.customer.entity.Customer
import com.example.counselcrm.domain.customer.entity.CustomerGrade
import com.example.counselcrm.domain.customer.repository.CustomerRepository
import com.example.counselcrm.domain.customer.service.CustomerService
import com.example.counselcrm.global.exception.DuplicateResourceException
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
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import java.util.*

@DisplayName("고객 시나리오 테스트")
class 고객시나리오Test {

    private lateinit var customerRepository: CustomerRepository
    private lateinit var customerService: CustomerService

    @BeforeEach
    fun setUp() {
        customerRepository = mockk(relaxed = true)
        customerService = CustomerService(customerRepository)
    }

    @Nested
    @DisplayName("신규 고객 등록 시나리오")
    inner class 신규고객등록 {

        @Test
        fun `관리자가 신규 고객을 등록하면 NEW 등급으로 생성된다`() {
            // given
            val request = CustomerCreateRequest(
                name = "홍길동",
                phoneNumber = "010-1234-5678",
                email = "hong@example.com"
            )
            val customerSlot = slot<Customer>()

            every { customerRepository.existsByPhoneNumber("01012345678") } returns false
            every { customerRepository.save(capture(customerSlot)) } answers {
                customerSlot.captured.apply {
                    val idField = Customer::class.java.getDeclaredField("id")
                    idField.isAccessible = true
                    idField.set(this, 1L)
                }
            }

            // when
            val result = customerService.createCustomer(request)

            // then
            assertEquals("홍길동", result.name)
            assertEquals("01012345678", result.phoneNumber)
            assertEquals(CustomerGrade.NEW, result.grade)  // 기본값은 NEW
            verify { customerRepository.save(any()) }
        }

        @Test
        fun `관리자가 VIP 등급으로 고객을 등록할 수 있다`() {
            // given
            val request = CustomerCreateRequest(
                name = "김VIP",
                phoneNumber = "010-9999-8888",
                email = "vip@example.com",
                grade = CustomerGrade.VIP
            )
            val customerSlot = slot<Customer>()

            every { customerRepository.existsByPhoneNumber("01099998888") } returns false
            every { customerRepository.save(capture(customerSlot)) } answers {
                customerSlot.captured.apply {
                    val idField = Customer::class.java.getDeclaredField("id")
                    idField.isAccessible = true
                    idField.set(this, 2L)
                }
            }

            // when
            val result = customerService.createCustomer(request)

            // then
            assertEquals(CustomerGrade.VIP, result.grade)
        }

        @Test
        fun `이미 등록된 전화번호로 고객을 등록하면 예외가 발생한다`() {
            // given
            val request = CustomerCreateRequest(
                name = "중복고객",
                phoneNumber = "010-1234-5678"
            )

            every { customerRepository.existsByPhoneNumber("01012345678") } returns true

            // when & then
            assertThrows<DuplicateResourceException> {
                customerService.createCustomer(request)
            }
        }

        @Test
        fun `전화번호에 하이픈이 있어도 정규화되어 저장된다`() {
            // given
            val request = CustomerCreateRequest(
                name = "테스트",
                phoneNumber = "010-5555-6666"
            )
            val customerSlot = slot<Customer>()

            every { customerRepository.existsByPhoneNumber("01055556666") } returns false
            every { customerRepository.save(capture(customerSlot)) } answers {
                customerSlot.captured.apply {
                    val idField = Customer::class.java.getDeclaredField("id")
                    idField.isAccessible = true
                    idField.set(this, 3L)
                }
            }

            // when
            customerService.createCustomer(request)

            // then
            assertEquals("01055556666", customerSlot.captured.phoneNumber)
        }
    }

    @Nested
    @DisplayName("고객 정보 조회 시나리오")
    inner class 고객정보조회 {

        @Test
        fun `고객 ID로 상세 정보를 조회할 수 있다`() {
            // given
            val customer = createCustomer(1L, "홍길동", "01012345678", CustomerGrade.VIP)

            every { customerRepository.findById(1L) } returns Optional.of(customer)

            // when
            val result = customerService.getCustomer(1L)

            // then
            assertEquals(1L, result.id)
            assertEquals("홍길동", result.name)
            assertEquals(CustomerGrade.VIP, result.grade)
        }

        @Test
        fun `존재하지 않는 고객 ID로 조회하면 예외가 발생한다`() {
            // given
            every { customerRepository.findById(999L) } returns Optional.empty()

            // when & then
            assertThrows<EntityNotFoundException> {
                customerService.getCustomer(999L)
            }
        }
    }

    @Nested
    @DisplayName("고객 검색 시나리오")
    inner class 고객검색 {

        @Test
        fun `이름으로 고객을 검색할 수 있다`() {
            // given
            val condition = CustomerSearchCondition(name = "홍")
            val pageable = PageRequest.of(0, 10)
            val customers = listOf(
                createCustomer(1L, "홍길동", "01011111111"),
                createCustomer(2L, "홍길순", "01022222222")
            )

            every { customerRepository.search(condition, pageable) } returns PageImpl(customers)

            // when
            val result = customerService.searchCustomers(condition, pageable)

            // then
            assertEquals(2, result.totalElements)
            assertTrue(result.content.all { it.name.contains("홍") })
        }

        @Test
        fun `전화번호로 고객을 검색할 수 있다`() {
            // given
            val condition = CustomerSearchCondition(phoneNumber = "0101234")
            val pageable = PageRequest.of(0, 10)
            val customers = listOf(createCustomer(1L, "홍길동", "01012345678"))

            every { customerRepository.search(condition, pageable) } returns PageImpl(customers)

            // when
            val result = customerService.searchCustomers(condition, pageable)

            // then
            assertEquals(1, result.totalElements)
        }

        @Test
        fun `등급별로 고객을 필터링할 수 있다`() {
            // given
            val condition = CustomerSearchCondition(grade = CustomerGrade.VIP)
            val pageable = PageRequest.of(0, 10)
            val vipCustomers = listOf(
                createCustomer(1L, "VIP고객1", "01011111111", CustomerGrade.VIP),
                createCustomer(2L, "VIP고객2", "01022222222", CustomerGrade.VIP)
            )

            every { customerRepository.search(condition, pageable) } returns PageImpl(vipCustomers)

            // when
            val result = customerService.searchCustomers(condition, pageable)

            // then
            assertTrue(result.content.all { it.grade == CustomerGrade.VIP })
        }
    }

    @Nested
    @DisplayName("고객 정보 수정 시나리오")
    inner class 고객정보수정 {

        @Test
        fun `관리자가 고객 이름을 수정할 수 있다`() {
            // given
            val customer = createCustomer(1L, "홍길동", "01012345678")
            val request = CustomerUpdateRequest(name = "홍길순")

            every { customerRepository.findById(1L) } returns Optional.of(customer)

            // when
            val result = customerService.updateCustomer(1L, request)

            // then
            assertEquals("홍길순", result.name)
        }

        @Test
        fun `관리자가 고객 등급을 VIP로 승급시킬 수 있다`() {
            // given
            val customer = createCustomer(1L, "일반고객", "01012345678", CustomerGrade.NORMAL)
            val request = CustomerUpdateRequest(grade = CustomerGrade.VIP)

            every { customerRepository.findById(1L) } returns Optional.of(customer)

            // when
            val result = customerService.updateCustomer(1L, request)

            // then
            assertEquals(CustomerGrade.VIP, result.grade)
        }

        @Test
        fun `다른 고객이 이미 사용 중인 전화번호로 변경하면 예외가 발생한다`() {
            // given
            val customer = createCustomer(1L, "홍길동", "01012345678")
            val request = CustomerUpdateRequest(phoneNumber = "010-9999-9999")

            every { customerRepository.findById(1L) } returns Optional.of(customer)
            every { customerRepository.existsByPhoneNumber("01099999999") } returns true

            // when & then
            assertThrows<DuplicateResourceException> {
                customerService.updateCustomer(1L, request)
            }
        }

        @Test
        fun `동일한 전화번호로 수정 요청하면 중복 검증을 하지 않는다`() {
            // given
            val customer = createCustomer(1L, "홍길동", "01012345678")
            val request = CustomerUpdateRequest(
                name = "홍길순",
                phoneNumber = "010-1234-5678"  // 기존과 동일 (하이픈 포함)
            )

            every { customerRepository.findById(1L) } returns Optional.of(customer)

            // when
            val result = customerService.updateCustomer(1L, request)

            // then
            assertEquals("홍길순", result.name)
            verify(exactly = 0) { customerRepository.existsByPhoneNumber(any()) }
        }
    }

    @Nested
    @DisplayName("고객 삭제 시나리오")
    inner class 고객삭제 {

        @Test
        fun `관리자가 고객을 삭제할 수 있다`() {
            // given
            val customer = createCustomer(1L, "홍길동", "01012345678")

            every { customerRepository.findById(1L) } returns Optional.of(customer)
            every { customerRepository.delete(customer) } returns Unit

            // when
            customerService.deleteCustomer(1L)

            // then
            verify { customerRepository.delete(customer) }
        }

        @Test
        fun `존재하지 않는 고객을 삭제하면 예외가 발생한다`() {
            // given
            every { customerRepository.findById(999L) } returns Optional.empty()

            // when & then
            assertThrows<EntityNotFoundException> {
                customerService.deleteCustomer(999L)
            }
        }
    }

    private fun createCustomer(
        id: Long,
        name: String,
        phoneNumber: String,
        grade: CustomerGrade = CustomerGrade.NORMAL
    ): Customer {
        return Customer(
            id = id,
            name = name,
            phoneNumber = phoneNumber,
            grade = grade
        )
    }
}
