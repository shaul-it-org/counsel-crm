package com.example.counselcrm.domain.counselor.repository

import com.example.counselcrm.domain.counselor.entity.Counselor
import com.example.counselcrm.domain.counselor.entity.CounselorStatus
import com.example.counselcrm.domain.counselor.entity.CounselorTeam
import org.springframework.data.jpa.repository.JpaRepository

interface CounselorRepository : JpaRepository<Counselor, Long> {
    fun findByEmployeeId(employeeId: String): Counselor?
    fun findByStatusAndIsActiveTrue(status: CounselorStatus): List<Counselor>
    fun findByTeamAndIsActiveTrue(team: CounselorTeam): List<Counselor>
    fun findByIsActiveTrue(): List<Counselor>
}
