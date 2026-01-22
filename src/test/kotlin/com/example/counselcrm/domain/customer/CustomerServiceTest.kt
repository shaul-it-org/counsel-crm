package com.example.counselcrm.domain.customer

import com.example.counselcrm.domain.customer.dto.CustomerCreateRequest
import com.example.counselcrm.domain.customer.dto.CustomerUpdateRequest
import com.example.counselcrm.domain.customer.entity.Customer
import com.example.counselcrm.domain.customer.entity.CustomerGrade
import com.example.counselcrm.domain.customer.repository.CustomerRepository
import com.example.counselcrm.domain.customer.service.CustomerService
import com.example.counselcrm.global.exception.DuplicateResourceException
import com.example.counselcrm.global.exception.EntityNotFoundException
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

@DisplayName("CustomerService 테스트")
class CustomerServiceTest {

    private lateinit var customerRepository: CustomerRepository
    private lateinit var customerService: CustomerService

    @BeforeEach
    fun setUp() {
        customerRepository = mockk()
        customerService = CustomerService(customerRepository)
    }

    @Nested
    @DisplayName("getCustomer")
    inner class GetCustomer {
        @Test
        @DisplayName("존재하는 고객을 조회하면 CustomerResponse를 반환한다")
        fun `should return customer when exists`() {
            // given
            val customer = createCustomer(1L, "홍길동", "01012345678")
            every { customerRepository.findById(1L) } returns Optional.of(customer)

            // when
            val result = customerService.getCustomer(1L)

            // then
            assertEquals("홍길동", result.name)
            assertEquals("01012345678", result.phoneNumber)
        }

        @Test
        @DisplayName("존재하지 않는 고객을 조회하면 EntityNotFoundException을 던진다")
        fun `should throw exception when customer not found`() {
            // given
            every { customerRepository.findById(999L) } returns Optional.empty()

            // when & then
            assertThrows<EntityNotFoundException> {
                customerService.getCustomer(999L)
            }
        }
    }

    @Nested
    @DisplayName("createCustomer")
    inner class CreateCustomer {
        @Test
        @DisplayName("유효한 요청으로 고객을 생성하면 CustomerResponse를 반환한다")
        fun `should create customer with valid request`() {
            // given
            val request = CustomerCreateRequest(
                name = "홍길동",
                phoneNumber = "010-1234-5678",
                email = "hong@example.com"
            )
            val savedCustomer = createCustomer(1L, "홍길동", "01012345678", "hong@example.com")

            every { customerRepository.existsByPhoneNumber("01012345678") } returns false
            every { customerRepository.save(any()) } returns savedCustomer

            // when
            val result = customerService.createCustomer(request)

            // then
            assertEquals("홍길동", result.name)
            assertEquals("01012345678", result.phoneNumber)
            verify { customerRepository.save(any()) }
        }

        @Test
        @DisplayName("중복된 전화번호로 고객을 생성하면 DuplicateResourceException을 던진다")
        fun `should throw exception when phone number is duplicated`() {
            // given
            val request = CustomerCreateRequest(
                name = "홍길동",
                phoneNumber = "010-1234-5678"
            )
            every { customerRepository.existsByPhoneNumber("01012345678") } returns true

            // when & then
            assertThrows<DuplicateResourceException> {
                customerService.createCustomer(request)
            }
        }
    }

    @Nested
    @DisplayName("updateCustomer")
    inner class UpdateCustomer {
        @Test
        @DisplayName("고객 정보를 수정하면 수정된 CustomerResponse를 반환한다")
        fun `should update customer with valid request`() {
            // given
            val customer = createCustomer(1L, "홍길동", "01012345678")
            val request = CustomerUpdateRequest(
                name = "김철수",
                grade = CustomerGrade.VIP
            )

            every { customerRepository.findById(1L) } returns Optional.of(customer)

            // when
            val result = customerService.updateCustomer(1L, request)

            // then
            assertEquals("김철수", result.name)
            assertEquals(CustomerGrade.VIP, result.grade)
        }
    }

    private fun createCustomer(
        id: Long,
        name: String,
        phoneNumber: String,
        email: String? = null
    ): Customer {
        return Customer(
            id = id,
            name = name,
            phoneNumber = phoneNumber,
            email = email
        )
    }
}
