# Counsel CRM

상담사 업무 효율화를 위한 CRM API 서버

## 프로젝트 개요

렌탈/구독 서비스 기업의 상담 업무를 지원하는 백엔드 API 시스템입니다.

### 주요 기능
- **고객 관리**: 고객 정보 CRUD, 검색
- **상담 관리**: 상담 생성, 상담사 배정, 상태 관리, 메모 기록
- **상담사 관리**: 상담사 정보 관리, 상태 변경
- **계약 조회**: 고객별 계약 정보 조회
- **통계**: 대시보드 통계, 상담사 성과 통계

## 기술 스택

| Category | Technology |
|----------|------------|
| Language | Kotlin 1.9 |
| Framework | Spring Boot 3.2 |
| ORM | JPA/Hibernate + QueryDSL |
| Database | PostgreSQL 16 |
| API Doc | SpringDoc OpenAPI (Swagger) |
| Test | JUnit 5, MockK, Testcontainers |
| Build | Gradle (Kotlin DSL) |
| Container | Docker |

## 프로젝트 구조

```
src/main/kotlin/com/example/counselcrm/
├── CounselCrmApplication.kt
├── domain/
│   ├── customer/          # 고객 도메인
│   │   ├── entity/
│   │   ├── repository/
│   │   ├── service/
│   │   ├── controller/
│   │   └── dto/
│   ├── counseling/        # 상담 도메인
│   ├── counselor/         # 상담사 도메인
│   ├── contract/          # 계약 도메인
│   └── statistics/        # 통계 도메인
├── global/
│   ├── config/           # 설정 클래스
│   ├── exception/        # 예외 처리
│   └── common/           # 공통 클래스
└── infra/                # 인프라 관련
```

## 시작하기

### 사전 요구사항
- JDK 21
- Docker & Docker Compose
- PostgreSQL 16 (또는 Docker 사용)

### 로컬 실행

#### 1. Docker Compose로 실행 (권장)
```bash
docker-compose up -d
```
애플리케이션: http://localhost:8080
Swagger UI: http://localhost:8080/swagger-ui.html

#### 2. 로컬에서 직접 실행
```bash
# PostgreSQL 실행 (Docker)
docker run -d --name counsel-crm-db \
  -e POSTGRES_DB=counsel_crm \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 \
  postgres:16-alpine

# 애플리케이션 실행
./gradlew bootRun
```

### 테스트 실행
```bash
./gradlew test
```

## API 엔드포인트

### Customer API
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | /api/v1/customers | 고객 검색 |
| GET | /api/v1/customers/{id} | 고객 단건 조회 |
| POST | /api/v1/customers | 고객 등록 |
| PUT | /api/v1/customers/{id} | 고객 정보 수정 |
| DELETE | /api/v1/customers/{id} | 고객 삭제 |

### Counseling API
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | /api/v1/counselings | 상담 검색 |
| GET | /api/v1/counselings/{id} | 상담 단건 조회 |
| GET | /api/v1/counselings/waiting | 대기 중인 상담 목록 |
| POST | /api/v1/counselings | 상담 생성 |
| POST | /api/v1/counselings/{id}/assign | 상담사 배정 |
| POST | /api/v1/counselings/{id}/start | 상담 시작 |
| POST | /api/v1/counselings/{id}/complete | 상담 완료 |
| POST | /api/v1/counselings/{id}/cancel | 상담 취소 |
| POST | /api/v1/counselings/{id}/notes | 메모 추가 |

### Counselor API
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | /api/v1/counselors | 활성 상담사 전체 조회 |
| GET | /api/v1/counselors/available | 상담 가능한 상담사 조회 |
| POST | /api/v1/counselors | 상담사 등록 |
| PATCH | /api/v1/counselors/{id}/status | 상담사 상태 변경 |

### Statistics API
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | /api/v1/statistics/dashboard | 대시보드 통계 |
| GET | /api/v1/statistics/status | 상태별 통계 |
| GET | /api/v1/statistics/category | 카테고리별 통계 |
| GET | /api/v1/statistics/counselor-performance | 상담사 성과 |

## 도메인 모델

### 상담 상태 머신
```
WAITING → ASSIGNED → IN_PROGRESS → COMPLETED
    ↓         ↓           ↓
 CANCELLED  CANCELLED   CANCELLED
                         ↓
                      ON_HOLD
```

### 주요 엔티티
- **Customer**: 고객 정보 (이름, 전화번호, 등급 등)
- **Counselor**: 상담사 정보 (이름, 상태, 팀 등)
- **Counseling**: 상담 정보 (상태, 카테고리, 상담 이력 등)
- **Contract**: 계약 정보 (상품, 기간, 요금 등)

## 기술적 특징

### 1. 도메인 중심 설계
- 비즈니스 로직을 엔티티에 캡슐화
- 상태 전이 검증 로직 구현

### 2. QueryDSL 동적 쿼리
- 복잡한 검색 조건 처리
- 타입 안전한 쿼리 작성

### 3. 글로벌 예외 처리
- 일관된 에러 응답 형식
- 비즈니스 예외와 기술 예외 분리

### 4. 테스트 전략
- 단위 테스트: MockK 활용
- 통합 테스트: @SpringBootTest + H2

## 라이선스

MIT License
