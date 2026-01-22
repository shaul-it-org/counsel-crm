package com.example.counselcrm.domain.statistics.service

import com.example.counselcrm.domain.counseling.entity.CounselingStatus
import com.example.counselcrm.domain.counseling.entity.QCounseling.counseling
import com.example.counselcrm.domain.counselor.entity.CounselorStatus
import com.example.counselcrm.domain.counselor.repository.CounselorRepository
import com.example.counselcrm.domain.statistics.dto.*
import com.querydsl.core.types.Projections
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate
import java.time.LocalDateTime

@Service
@Transactional(readOnly = true)
class StatisticsService(
    private val queryFactory: JPAQueryFactory,
    private val counselorRepository: CounselorRepository
) {
    fun getDashboardStatistics(): DashboardStatistics {
        val totalCounselings = queryFactory
            .select(counseling.count())
            .from(counseling)
            .fetchOne() ?: 0L

        val waitingCount = queryFactory
            .select(counseling.count())
            .from(counseling)
            .where(counseling.status.eq(CounselingStatus.WAITING))
            .fetchOne() ?: 0L

        val inProgressCount = queryFactory
            .select(counseling.count())
            .from(counseling)
            .where(counseling.status.eq(CounselingStatus.IN_PROGRESS))
            .fetchOne() ?: 0L

        val todayStart = LocalDate.now().atStartOfDay()
        val completedToday = queryFactory
            .select(counseling.count())
            .from(counseling)
            .where(
                counseling.status.eq(CounselingStatus.COMPLETED),
                counseling.completedAt.goe(todayStart)
            )
            .fetchOne() ?: 0L

        val availableCounselors = counselorRepository
            .findByStatusAndIsActiveTrue(CounselorStatus.AVAILABLE)
            .size

        return DashboardStatistics(
            totalCounselings = totalCounselings,
            waitingCount = waitingCount,
            inProgressCount = inProgressCount,
            completedToday = completedToday,
            availableCounselors = availableCounselors
        )
    }

    fun getStatusStatistics(): List<CounselingStatusStatistics> {
        return queryFactory
            .select(
                Projections.constructor(
                    CounselingStatusStatistics::class.java,
                    counseling.status,
                    counseling.count()
                )
            )
            .from(counseling)
            .groupBy(counseling.status)
            .fetch()
    }

    fun getCategoryStatistics(): List<CounselingCategoryStatistics> {
        return queryFactory
            .select(
                Projections.constructor(
                    CounselingCategoryStatistics::class.java,
                    counseling.category,
                    counseling.count()
                )
            )
            .from(counseling)
            .groupBy(counseling.category)
            .fetch()
    }

    fun getCounselorPerformance(startDate: LocalDateTime, endDate: LocalDateTime): List<CounselorPerformance> {
        return queryFactory
            .select(
                Projections.constructor(
                    CounselorPerformance::class.java,
                    counseling.counselor.id,
                    counseling.counselor.name,
                    counseling.count(),
                    counseling.id.count().castToNum(Double::class.java) // placeholder for avg time
                )
            )
            .from(counseling)
            .where(
                counseling.status.eq(CounselingStatus.COMPLETED),
                counseling.completedAt.between(startDate, endDate),
                counseling.counselor.isNotNull
            )
            .groupBy(counseling.counselor.id, counseling.counselor.name)
            .orderBy(counseling.count().desc())
            .fetch()
    }
}
