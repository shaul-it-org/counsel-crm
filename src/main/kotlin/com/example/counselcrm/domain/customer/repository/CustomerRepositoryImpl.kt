package com.example.counselcrm.domain.customer.repository

import com.example.counselcrm.domain.customer.dto.CustomerSearchCondition
import com.example.counselcrm.domain.customer.entity.Customer
import com.example.counselcrm.domain.customer.entity.CustomerGrade
import com.example.counselcrm.domain.customer.entity.QCustomer.customer
import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.support.PageableExecutionUtils
import org.springframework.stereotype.Repository

@Repository
class CustomerRepositoryImpl(
    private val queryFactory: JPAQueryFactory
) : CustomerRepositoryCustom {

    override fun search(condition: CustomerSearchCondition, pageable: Pageable): Page<Customer> {
        val content = queryFactory
            .selectFrom(customer)
            .where(
                nameContains(condition.name),
                phoneNumberContains(condition.phoneNumber),
                emailContains(condition.email),
                gradeEq(condition.grade)
            )
            .offset(pageable.offset)
            .limit(pageable.pageSize.toLong())
            .orderBy(customer.createdAt.desc())
            .fetch()

        val countQuery = queryFactory
            .select(customer.count())
            .from(customer)
            .where(
                nameContains(condition.name),
                phoneNumberContains(condition.phoneNumber),
                emailContains(condition.email),
                gradeEq(condition.grade)
            )

        return PageableExecutionUtils.getPage(content, pageable) { countQuery.fetchOne() ?: 0L }
    }

    private fun nameContains(name: String?): BooleanExpression? =
        name?.takeIf { it.isNotBlank() }?.let { customer.name.containsIgnoreCase(it) }

    private fun phoneNumberContains(phoneNumber: String?): BooleanExpression? =
        phoneNumber?.takeIf { it.isNotBlank() }?.let { customer.phoneNumber.contains(it) }

    private fun emailContains(email: String?): BooleanExpression? =
        email?.takeIf { it.isNotBlank() }?.let { customer.email.containsIgnoreCase(it) }

    private fun gradeEq(grade: CustomerGrade?): BooleanExpression? =
        grade?.let { customer.grade.eq(it) }
}
