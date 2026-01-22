package com.example.counselcrm.domain.counseling.repository

import com.example.counselcrm.domain.counseling.entity.Counseling
import com.example.counselcrm.domain.counseling.entity.CounselingStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param

interface CounselingRepository : JpaRepository<Counseling, Long>, CounselingRepositoryCustom {
    fun findByStatus(status: CounselingStatus): List<Counseling>

    @Query("SELECT c FROM Counseling c WHERE c.counselor.id = :counselorId AND c.status = :status")
    fun findByCounselorIdAndStatus(
        @Param("counselorId") counselorId: Long,
        @Param("status") status: CounselingStatus
    ): List<Counseling>

    @Query("SELECT c FROM Counseling c JOIN FETCH c.customer WHERE c.customer.id = :customerId ORDER BY c.createdAt DESC")
    fun findByCustomerIdWithCustomer(@Param("customerId") customerId: Long): List<Counseling>
}
