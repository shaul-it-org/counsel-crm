package com.example.counselcrm.scenario

import com.example.counselcrm.domain.counseling.entity.CounselingCategory
import com.example.counselcrm.domain.counseling.entity.CounselingStatus
import com.example.counselcrm.domain.statistics.dto.CounselingCategoryStatistics
import com.example.counselcrm.domain.statistics.dto.CounselingStatusStatistics
import com.example.counselcrm.domain.statistics.dto.CounselorPerformance
import com.example.counselcrm.domain.statistics.dto.DashboardStatistics
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

/**
 * 통계 시나리오 테스트
 *
 * StatisticsService가 QueryDSL의 JPAQueryFactory를 사용하므로
 * 단위 테스트에서 모킹이 복잡합니다.
 * 따라서 통계 DTO 검증 및 비즈니스 로직 테스트에 집중합니다.
 * 실제 쿼리 동작은 통합 테스트에서 검증합니다.
 */
@DisplayName("통계 시나리오 테스트")
class 통계시나리오Test {

    @Nested
    @DisplayName("대시보드 통계 검증 시나리오")
    inner class 대시보드통계검증 {

        @Test
        fun `대시보드_통계_객체가_올바르게_생성된다`() {
            // given
            val dashboard = DashboardStatistics(
                totalCounselings = 100,
                waitingCount = 10,
                inProgressCount = 5,
                completedToday = 20,
                availableCounselors = 3
            )

            // then
            assertEquals(100L, dashboard.totalCounselings)
            assertEquals(10L, dashboard.waitingCount)
            assertEquals(5L, dashboard.inProgressCount)
            assertEquals(20L, dashboard.completedToday)
            assertEquals(3, dashboard.availableCounselors)
        }

        @Test
        fun `대기_상담이_많으면_상담사_증원이_필요함을_판단할_수_있다`() {
            // given: 대기 50건, 가용 상담사 2명
            val dashboard = DashboardStatistics(
                totalCounselings = 200,
                waitingCount = 50,
                inProgressCount = 10,
                completedToday = 30,
                availableCounselors = 2
            )

            // when: 상담사당 대기 건수 계산
            val waitingPerCounselor = dashboard.waitingCount.toDouble() / dashboard.availableCounselors

            // then: 상담사 1인당 25건 대기 - 증원 필요
            assertEquals(25.0, waitingPerCounselor)
            assertTrue(waitingPerCounselor > 10, "상담사 증원 필요")
        }

        @Test
        fun `가용_상담사가_없으면_긴급_상황임을_판단할_수_있다`() {
            // given
            val dashboard = DashboardStatistics(
                totalCounselings = 100,
                waitingCount = 30,
                inProgressCount = 5,
                completedToday = 10,
                availableCounselors = 0
            )

            // then
            assertEquals(0, dashboard.availableCounselors)
            assertTrue(dashboard.waitingCount > 0 && dashboard.availableCounselors == 0,
                "대기 중인 상담이 있으나 가용 상담사 없음 - 긴급 상황")
        }
    }

    @Nested
    @DisplayName("상태별 통계 검증 시나리오")
    inner class 상태별통계검증 {

        @Test
        fun `상담_상태별_통계를_분석할_수_있다`() {
            // given
            val statusStats = listOf(
                CounselingStatusStatistics(CounselingStatus.WAITING, 10),
                CounselingStatusStatistics(CounselingStatus.IN_PROGRESS, 5),
                CounselingStatusStatistics(CounselingStatus.COMPLETED, 80),
                CounselingStatusStatistics(CounselingStatus.CANCELLED, 5)
            )

            // when: 완료율 계산
            val total = statusStats.sumOf { it.count }
            val completed = statusStats.find { it.status == CounselingStatus.COMPLETED }?.count ?: 0
            val completionRate = completed.toDouble() / total * 100

            // then
            assertEquals(100L, total)
            assertEquals(80L, completed)
            assertEquals(80.0, completionRate)
        }

        @Test
        fun `취소율이_높으면_서비스_개선이_필요함을_판단할_수_있다`() {
            // given
            val statusStats = listOf(
                CounselingStatusStatistics(CounselingStatus.WAITING, 10),
                CounselingStatusStatistics(CounselingStatus.COMPLETED, 70),
                CounselingStatusStatistics(CounselingStatus.CANCELLED, 20)  // 20% 취소
            )

            // when
            val total = statusStats.sumOf { it.count }
            val cancelled = statusStats.find { it.status == CounselingStatus.CANCELLED }?.count ?: 0
            val cancellationRate = cancelled.toDouble() / total * 100

            // then
            assertEquals(20.0, cancellationRate)
            assertTrue(cancellationRate > 10, "취소율 20% - 서비스 품질 점검 필요")
        }

        @Test
        fun `진행_중인_상담_비율로_상담사_업무량을_파악할_수_있다`() {
            // given
            val statusStats = listOf(
                CounselingStatusStatistics(CounselingStatus.WAITING, 5),
                CounselingStatusStatistics(CounselingStatus.IN_PROGRESS, 15),
                CounselingStatusStatistics(CounselingStatus.COMPLETED, 80)
            )

            // when
            val inProgress = statusStats.find { it.status == CounselingStatus.IN_PROGRESS }?.count ?: 0
            val waiting = statusStats.find { it.status == CounselingStatus.WAITING }?.count ?: 0

            // then: 진행 중 15건, 대기 5건 = 적절한 업무량
            assertEquals(15L, inProgress)
            assertEquals(5L, waiting)
            assertTrue(inProgress > waiting, "상담 진행이 원활함")
        }
    }

    @Nested
    @DisplayName("카테고리별 통계 검증 시나리오")
    inner class 카테고리별통계검증 {

        @Test
        fun `상담_카테고리별_분포를_분석할_수_있다`() {
            // given
            val categoryStats = listOf(
                CounselingCategoryStatistics(CounselingCategory.PRODUCT_INQUIRY, 30),
                CounselingCategoryStatistics(CounselingCategory.COMPLAINT, 25),
                CounselingCategoryStatistics(CounselingCategory.CONTRACT, 20),
                CounselingCategoryStatistics(CounselingCategory.PAYMENT, 15),
                CounselingCategoryStatistics(CounselingCategory.CANCELLATION, 10)
            )

            // when
            val topCategory = categoryStats.maxByOrNull { it.count }
            val total = categoryStats.sumOf { it.count }

            // then
            assertEquals(CounselingCategory.PRODUCT_INQUIRY, topCategory?.category)
            assertEquals(100L, total)
        }

        @Test
        fun `불만_상담_비율로_서비스_품질을_판단할_수_있다`() {
            // given
            val categoryStats = listOf(
                CounselingCategoryStatistics(CounselingCategory.PRODUCT_INQUIRY, 50),
                CounselingCategoryStatistics(CounselingCategory.COMPLAINT, 30),  // 30%
                CounselingCategoryStatistics(CounselingCategory.OTHER, 20)
            )

            // when
            val total = categoryStats.sumOf { it.count }
            val complaintCount = categoryStats.find { it.category == CounselingCategory.COMPLAINT }?.count ?: 0
            val complaintRate = complaintCount.toDouble() / total * 100

            // then
            assertEquals(30.0, complaintRate)
            assertTrue(complaintRate > 20, "불만 상담 30% - 서비스 품질 개선 시급")
        }

        @Test
        fun `해지_문의_증가는_고객_이탈_위험_신호이다`() {
            // given: 해지 문의가 많은 상황
            val categoryStats = listOf(
                CounselingCategoryStatistics(CounselingCategory.PRODUCT_INQUIRY, 20),
                CounselingCategoryStatistics(CounselingCategory.CANCELLATION, 35),  // 35%
                CounselingCategoryStatistics(CounselingCategory.OTHER, 45)
            )

            // when
            val total = categoryStats.sumOf { it.count }
            val cancellationCount = categoryStats.find { it.category == CounselingCategory.CANCELLATION }?.count ?: 0
            val cancellationRate = cancellationCount.toDouble() / total * 100

            // then
            assertEquals(35.0, cancellationRate)
            assertTrue(cancellationRate > 15, "해지 문의 35% - 고객 이탈 위험")
        }

        @Test
        fun `기술지원_상담이_많으면_제품_품질_점검이_필요하다`() {
            // given
            val categoryStats = listOf(
                CounselingCategoryStatistics(CounselingCategory.PRODUCT_INQUIRY, 20),
                CounselingCategoryStatistics(CounselingCategory.TECHNICAL_SUPPORT, 40),  // 40%
                CounselingCategoryStatistics(CounselingCategory.OTHER, 40)
            )

            // when
            val total = categoryStats.sumOf { it.count }
            val techCount = categoryStats.find { it.category == CounselingCategory.TECHNICAL_SUPPORT }?.count ?: 0
            val techRate = techCount.toDouble() / total * 100

            // then
            assertEquals(40.0, techRate)
            assertTrue(techRate > 25, "기술지원 40% - 제품 품질 점검 필요")
        }
    }

    @Nested
    @DisplayName("상담사 성과 검증 시나리오")
    inner class 상담사성과검증 {

        @Test
        fun `상담사별_성과를_비교할_수_있다`() {
            // given
            val performances = listOf(
                CounselorPerformance(1L, "김상담", 100, 15.5),
                CounselorPerformance(2L, "이상담", 80, 18.2),
                CounselorPerformance(3L, "박상담", 60, 12.0)
            )

            // when
            val topPerformer = performances.maxByOrNull { it.completedCount }
            val totalCompleted = performances.sumOf { it.completedCount }

            // then
            assertEquals("김상담", topPerformer?.counselorName)
            assertEquals(100L, topPerformer?.completedCount)
            assertEquals(240L, totalCompleted)
        }

        @Test
        fun `상담사_평균_처리_건수를_계산할_수_있다`() {
            // given
            val performances = listOf(
                CounselorPerformance(1L, "김상담", 90, 15.0),
                CounselorPerformance(2L, "이상담", 90, 15.0),
                CounselorPerformance(3L, "박상담", 90, 15.0)
            )

            // when
            val avgCompleted = performances.map { it.completedCount }.average()

            // then
            assertEquals(90.0, avgCompleted)
        }

        @Test
        fun `성과_편차가_크면_업무_재분배가_필요하다`() {
            // given: 성과 편차가 큰 상황
            val performances = listOf(
                CounselorPerformance(1L, "김상담", 150, 10.0),  // 많음
                CounselorPerformance(2L, "이상담", 50, 20.0),   // 적음
                CounselorPerformance(3L, "박상담", 50, 20.0)    // 적음
            )

            // when
            val max = performances.maxOf { it.completedCount }
            val min = performances.minOf { it.completedCount }
            val variance = max - min

            // then
            assertEquals(100L, variance)
            assertTrue(variance > 50, "성과 편차 100건 - 업무 재분배 필요")
        }

        @Test
        fun `평균_처리_시간으로_상담_효율성을_판단할_수_있다`() {
            // given
            val performances = listOf(
                CounselorPerformance(1L, "빠른상담사", 100, 10.0),  // 효율적
                CounselorPerformance(2L, "느린상담사", 50, 30.0)    // 비효율적
            )

            // when
            val efficient = performances.filter { it.averageHandlingTimeMinutes!! < 15 }
            val needsTraining = performances.filter { it.averageHandlingTimeMinutes!! > 20 }

            // then
            assertEquals(1, efficient.size)
            assertEquals("빠른상담사", efficient[0].counselorName)
            assertEquals(1, needsTraining.size)
            assertEquals("느린상담사", needsTraining[0].counselorName)
        }
    }

    @Nested
    @DisplayName("통계 기반 의사결정 시나리오")
    inner class 통계기반의사결정 {

        @Test
        fun `대시보드_데이터로_현재_상황을_종합_판단할_수_있다`() {
            // given: 점심시간 직후 상황
            val dashboard = DashboardStatistics(
                totalCounselings = 500,
                waitingCount = 45,      // 대기 많음
                inProgressCount = 3,    // 진행 적음
                completedToday = 80,
                availableCounselors = 1  // 가용 상담사 1명
            )

            // when: 상황 분석
            val isEmergency = dashboard.availableCounselors < 3 && dashboard.waitingCount > 30
            val waitingPerAvailable = if (dashboard.availableCounselors > 0) {
                dashboard.waitingCount.toDouble() / dashboard.availableCounselors
            } else Double.MAX_VALUE

            // then
            assertTrue(isEmergency, "긴급 상황: 가용 상담사 부족")
            assertEquals(45.0, waitingPerAvailable)
        }

        @Test
        fun `카테고리_통계로_팀_구성을_최적화할_수_있다`() {
            // given: 현재 카테고리별 상담 분포
            val categoryStats = listOf(
                CounselingCategoryStatistics(CounselingCategory.PRODUCT_INQUIRY, 20),
                CounselingCategoryStatistics(CounselingCategory.COMPLAINT, 40),       // 많음
                CounselingCategoryStatistics(CounselingCategory.TECHNICAL_SUPPORT, 30),// 많음
                CounselingCategoryStatistics(CounselingCategory.OTHER, 10)
            )

            // when: 팀 증원 필요 영역 분석
            val total = categoryStats.sumOf { it.count }
            val needsMoreStaff = categoryStats
                .filter { it.count.toDouble() / total > 0.25 }  // 25% 이상
                .map { it.category }

            // then
            assertTrue(needsMoreStaff.contains(CounselingCategory.COMPLAINT))
            assertTrue(needsMoreStaff.contains(CounselingCategory.TECHNICAL_SUPPORT))
            assertEquals(2, needsMoreStaff.size)
        }

        @Test
        fun `성과_데이터로_인센티브_대상을_선정할_수_있다`() {
            // given
            val performances = listOf(
                CounselorPerformance(1L, "우수상담사", 120, 12.0),
                CounselorPerformance(2L, "보통상담사", 80, 15.0),
                CounselorPerformance(3L, "신입상담사", 40, 25.0)
            )

            // when: 상위 성과자 선정 (완료 건수 100건 이상, 처리시간 15분 이하)
            val topPerformers = performances.filter {
                it.completedCount >= 100 && it.averageHandlingTimeMinutes!! <= 15
            }

            // then
            assertEquals(1, topPerformers.size)
            assertEquals("우수상담사", topPerformers[0].counselorName)
        }
    }
}
