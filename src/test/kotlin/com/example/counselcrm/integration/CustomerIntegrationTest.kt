package com.example.counselcrm.integration

import com.example.counselcrm.domain.customer.dto.CustomerCreateRequest
import com.example.counselcrm.domain.customer.entity.CustomerGrade
import com.example.counselcrm.domain.customer.repository.CustomerRepository
import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DisplayName("Customer API 통합 테스트")
class CustomerIntegrationTest {

    @Autowired
    private lateinit var mockMvc: MockMvc

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Autowired
    private lateinit var customerRepository: CustomerRepository

    @BeforeEach
    fun setUp() {
        customerRepository.deleteAll()
    }

    @Test
    @DisplayName("POST /api/v1/customers - 고객 등록 성공")
    fun `should create customer successfully`() {
        // given
        val request = CustomerCreateRequest(
            name = "홍길동",
            phoneNumber = "010-1234-5678",
            email = "hong@example.com",
            grade = CustomerGrade.NEW
        )

        // when & then
        mockMvc.perform(
            post("/api/v1/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.name").value("홍길동"))
            .andExpect(jsonPath("$.data.phoneNumber").value("01012345678"))
            .andExpect(jsonPath("$.data.email").value("hong@example.com"))
            .andExpect(jsonPath("$.data.grade").value("NEW"))
    }

    @Test
    @DisplayName("POST /api/v1/customers - 유효하지 않은 전화번호로 등록 실패")
    fun `should fail to create customer with invalid phone number`() {
        // given
        val request = CustomerCreateRequest(
            name = "홍길동",
            phoneNumber = "invalid-phone"
        )

        // when & then
        mockMvc.perform(
            post("/api/v1/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isBadRequest)
            .andExpect(jsonPath("$.code").value("C001"))
    }

    @Test
    @DisplayName("POST /api/v1/customers - 중복 전화번호로 등록 실패")
    fun `should fail to create customer with duplicate phone number`() {
        // given
        val request = CustomerCreateRequest(
            name = "홍길동",
            phoneNumber = "010-1234-5678"
        )

        // 첫 번째 등록
        mockMvc.perform(
            post("/api/v1/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andExpect(status().isCreated)

        // when & then - 두 번째 등록 (중복)
        mockMvc.perform(
            post("/api/v1/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request.copy(name = "김철수")))
        )
            .andExpect(status().isConflict)
            .andExpect(jsonPath("$.code").value("CU002"))
    }

    @Test
    @DisplayName("GET /api/v1/customers/{id} - 고객 단건 조회 성공")
    fun `should get customer by id`() {
        // given
        val createRequest = CustomerCreateRequest(
            name = "홍길동",
            phoneNumber = "010-1234-5678"
        )

        val createResult = mockMvc.perform(
            post("/api/v1/customers")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(createRequest))
        )
            .andReturn()

        val createdCustomer = objectMapper.readTree(createResult.response.contentAsString)
        val customerId = createdCustomer.get("data").get("id").asLong()

        // when & then
        mockMvc.perform(get("/api/v1/customers/$customerId"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.success").value(true))
            .andExpect(jsonPath("$.data.name").value("홍길동"))
    }

    @Test
    @DisplayName("GET /api/v1/customers/{id} - 존재하지 않는 고객 조회 실패")
    fun `should return 404 for non-existent customer`() {
        // when & then
        mockMvc.perform(get("/api/v1/customers/99999"))
            .andExpect(status().isNotFound)
            .andExpect(jsonPath("$.code").value("CU001"))
    }

    @Test
    @DisplayName("GET /api/v1/customers - 고객 검색")
    fun `should search customers`() {
        // given
        val requests = listOf(
            CustomerCreateRequest(name = "홍길동", phoneNumber = "010-1111-1111", grade = CustomerGrade.VIP),
            CustomerCreateRequest(name = "김철수", phoneNumber = "010-2222-2222", grade = CustomerGrade.NORMAL),
            CustomerCreateRequest(name = "홍길순", phoneNumber = "010-3333-3333", grade = CustomerGrade.VIP)
        )

        requests.forEach { request ->
            mockMvc.perform(
                post("/api/v1/customers")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
        }

        // when & then - 이름으로 검색
        mockMvc.perform(get("/api/v1/customers").param("name", "홍"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data.content.length()").value(2))

        // when & then - 등급으로 검색
        mockMvc.perform(get("/api/v1/customers").param("grade", "VIP"))
            .andExpect(status().isOk)
            .andExpect(jsonPath("$.data.content.length()").value(2))
    }
}
