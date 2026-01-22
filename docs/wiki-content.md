# counsel-crm 위키 페이지 내용

> wiki.shaul.kr 에 등록할 내용 (MCP 인증 문제로 수동 등록 필요)

---

## 프로젝트 페이지에 추가할 내용

```wikitext
=== counsel-crm (신규) ===
* '''설명''': 상담 CRM API 서버 - 상담사 업무 효율화를 위한 백엔드 시스템
* '''기술 스택''': Kotlin, Spring Boot 3.2, JPA/Hibernate, QueryDSL, PostgreSQL
* '''로컬 경로''': <code>~/workspace/it-org/counsel-crm</code>
* '''생성일''': 2026-01-22
* '''목적''': 아정당 백엔드 개발자 포지션 포트폴리오
* '''주요 기능''':
** 고객 관리 (CRUD, 검색)
** 상담 관리 (생성, 배정, 상태 전이, 메모)
** 상담사 관리 (등록, 상태 변경)
** 계약 조회
** 통계 API (대시보드, 상담사 성과)
```

---

## 별도 페이지 (프로젝트/counsel-crm) 내용

```wikitext
= counsel-crm =

'''상담 CRM API 서버''' - 상담사 업무 효율화를 위한 백엔드 시스템

== 프로젝트 정보 ==

{| class="wikitable"
! 항목 !! 내용
|-
| '''프로젝트명''' || counsel-crm
|-
| '''목적''' || 아정당 백엔드 개발자 포지션 포트폴리오
|-
| '''생성일''' || 2026-01-22
|-
| '''저장소''' || <code>~/workspace/it-org/counsel-crm</code>
|-
| '''상태''' || 초기 생성 완료
|}

== 기술 스택 ==

{| class="wikitable"
! Category !! Technology
|-
| Language || Kotlin 1.9
|-
| Framework || Spring Boot 3.2
|-
| ORM || JPA/Hibernate + QueryDSL
|-
| Database || PostgreSQL 16
|-
| API Doc || SpringDoc OpenAPI (Swagger)
|-
| Test || JUnit 5, MockK, Testcontainers
|-
| Build || Gradle (Kotlin DSL)
|-
| Container || Docker
|}

== 도메인 구조 ==

{| class="wikitable"
! 도메인 !! 설명 !! 주요 기능
|-
| '''Customer''' || 고객 관리 || CRUD, 검색 (QueryDSL 동적 쿼리)
|-
| '''Counseling''' || 상담 관리 || 생성, 배정, 상태 전이, 메모
|-
| '''Counselor''' || 상담사 관리 || 등록, 상태 변경, 업무 현황
|-
| '''Contract''' || 계약 조회 || 고객별 계약 정보 조회
|-
| '''Statistics''' || 통계 || 대시보드, 상담사 성과
|}

== 상담 상태 머신 ==

<pre>
WAITING → ASSIGNED → IN_PROGRESS → COMPLETED
    ↓         ↓           ↓
 CANCELLED  CANCELLED   CANCELLED
                         ↓
                      ON_HOLD
</pre>

== 작업 이력 ==

{| class="wikitable"
! 날짜 !! 작업 내용 !! 작업자
|-
| 2026-01-22 || 프로젝트 초기 생성 (Claude Code /new-project) || Claude Code
|}

[[분류:프로젝트]]
[[분류:Backend]]
[[분류:Kotlin]]
[[분류:Spring Boot]]
```
