package com.example.counselcrm.domain.counseling.repository

import com.example.counselcrm.domain.counseling.dto.CounselingSearchCondition
import com.example.counselcrm.domain.counseling.entity.Counseling
import com.example.counselcrm.domain.counseling.entity.CounselingCategory
import com.example.counselcrm.domain.counseling.entity.CounselingStatus
import com.example.counselcrm.domain.counseling.entity.QCounseling.counseling
import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.support.PageableExecutionUtils
import org.springframework.stereotype.Repository
import java.time.LocalDate

@Repository
class CounselingRepositoryImpl(
    private val queryFactory: JPAQueryFactory
) : CounselingRepositoryCustom {

    override fun search(condition: CounselingSearchCondition, pageable: Pageable): Page<Counseling> {
        val content = queryFactory
            .selectFrom(counseling)
            .leftJoin(counseling.customer).fetchJoin()
            .leftJoin(counseling.counselor).fetchJoin()
            .where(
                customerIdEq(condition.customerId),
                counselorIdEq(condition.counselorId),
                statusEq(condition.status),
                categoryEq(condition.category),
                titleContains(condition.titleKeyword),
                createdAtBetween(condition.startDate, condition.endDate)
            )
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .orderBy(counseling.createdAt.desc())
            .fetch()

        val countQuery = queryFactory
            .select(counseling.count())
            .from(counseling)
            .where(
                customerIdEq(condition.customerId),
                counselorIdEq(condition.counselorId),
                statusEq(condition.status),
                categoryEq(condition.category),
                titleContains(condition.titleKeyword),
                createdAtBetween(condition.startDate, condition.endDate)
            )

        return PageableExecutionUtils.getPage(content, pageable) { countQuery.fetchOne() ?: 0L }
    }

    private fun customerIdEq(customerId: Long?): BooleanExpression? =
        customerId?.let { counseling.customer.id.eq(it) }

    private fun counselorIdEq(counselorId: Long?): BooleanExpression? =
        counselorId?.let { counseling.counselor.id.eq(it) }

    private fun statusEq(status: CounselingStatus?): BooleanExpression? =
        status?.let { counseling.status.eq(it) }

    private fun categoryEq(category: CounselingCategory?): BooleanExpression? =
        category?.let { counseling.category.eq(it) }

    private fun titleContains(keyword: String?): BooleanExpression? =
        keyword?.takeIf { it.isNotBlank() }?.let { counseling.title.containsIgnoreCase(it) }

    private fun createdAtBetween(startDate: LocalDate?, endDate: LocalDate?): BooleanExpression? {
        return when {
            startDate != null && endDate != null -> counseling.createdAt.between(
                startDate.atStartOfDay(),
                endDate.plusDays(1).atStartOfDay()
            )
            startDate != null -> counseling.createdAt.goe(startDate.atStartOfDay())
            endDate != null -> counseling.createdAt.lt(endDate.plusDays(1).atStartOfDay())
            else -> null
        }
    }
}
