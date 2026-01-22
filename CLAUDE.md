# Counsel CRM

## Project Overview
상담사 업무 효율화를 위한 CRM API 서버. 렌탈/구독 서비스 기업의 상담 업무를 지원.

## Tech Stack
- Language: Kotlin 1.9
- Framework: Spring Boot 3.2
- ORM: JPA/Hibernate + QueryDSL
- Database: PostgreSQL 16
- Test: JUnit 5, MockK

## Directory Structure
- `domain/`: 도메인별 패키지 (customer, counseling, counselor, contract, statistics)
- `global/`: 공통 설정, 예외 처리
- `infra/`: 인프라 관련 코드

## Development Guidelines
- 도메인 중심 설계 적용
- 비즈니스 로직은 엔티티/서비스에 캡슐화
- QueryDSL로 복잡한 검색 쿼리 처리
- 모든 API는 ApiResponse로 래핑

## Commands
- Build: `./gradlew build`
- Test: `./gradlew test`
- Run: `./gradlew bootRun`
- Docker: `docker-compose up -d`

## API Documentation
Swagger UI: http://localhost:8080/swagger-ui.html
