package com.example.counselcrm.domain.counseling.repository

import com.example.counselcrm.domain.counseling.dto.CounselingSearchCondition
import com.example.counselcrm.domain.counseling.entity.Counseling
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface CounselingRepositoryCustom {
    fun search(condition: CounselingSearchCondition, pageable: Pageable): Page<Counseling>
}
