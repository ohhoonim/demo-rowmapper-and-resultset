# 스마트 팩토리 프로젝트 (Smart Factory Project)

이 프로젝트는 헥사고날 아키텍처(Hexagonal Architecture), 도메인 주도 설계(DDD), 그리고 Spring Modulith에 초점을 맞추어 구축된 높은 표준의 현대적인 산업용 소프트웨어 시스템입니다. 유지보수성, 테스트 가능성, 명확한 관심사 분리를 보장하기 위해 엄격한 '5-Step 아키텍처 명세'를 따릅니다.

## 프로젝트 개요 (Project Overview)

- 목적: 산업 자동화 및 관리를 위한 스마트 팩토리 백엔드 시스템.
- 주요 기술 스택:
  - 언어: Java 25 (최신 언어 기능 활용).
  - 프레임워크: Spring Boot 4.0.5, Spring Modulith 2.0.1.
  - 영속성(Persistence): PostgreSQL, JDBC.
  - 캐싱(Caching): Valkey / Data Valkey.
  - 보안(Security): JWT를 포함한 Spring Security (jjwt).
  - 테스트(Testing): JUnit 5, MockMvcTester, Testcontainers (PostgreSQL, Valkey), ArchUnit.
  - 문서화(Documentation): Asciidoctor를 결합한 Spring REST Docs.

## 핵심 아키텍처 의무 조항 (The 5-Step Process)

모든 개발 작업은 반드시 `docs/skills/architecture-spec.md` 및 `docs/skills/domain-module-skill.md` 가이드라인을 준수해야 합니다.

- [Step 1] 도메인 자아 확립 (Domain Model Discovery): BaseEntity를 상속받는 애그리거트 루트(AR)를 정의합니다. 엄격한 캡슐화를 적용하며, 비즈니스 로직은 도메인 모델 내에만 존재합니다.
- [Step 2] 법전 정의 (Policy Abstraction): 외부 환경에 의존적인 규칙들을 @FunctionalInterface 등을 통해 추상화합니다.
- [Step 3] 흐름 조율 (Service Orchestration): 서비스는 함자(Functor)로서 동작합니다. 서비스는 비즈니스 로직을 포함하지 않으며, 오직 모델(Models), 정책(Policies), 행위(Activities) 간의 흐름만을 조율합니다.
- [Step 4] 도구 제작 (Activity Implementation): 도메인과 인프라(DB, 외부 API)를 연결하는 포트/어댑터로서 "Activity"를 구현합니다.
- [Step 5] 반복 공정 검증 (Multilayered Testing):
  - 모델 테스트: 비즈니스 로직의 순수성을 검증하는 순수 단위 테스트.
  - 서비스 테스트: 모의 객체(Mock)를 사용한 흐름 검증.
  - 슬라이스 테스트: 엔드포인트 검증을 위한 MockMvcTester 활용 및 REST Docs 문서 자동 생성.

## 디렉토리 구조 및 패키지 컨벤션 (Directory Structure & Package Conventions)

- 프로젝트는 대규모 도메인 영역인 `business`, `component`, `system`으로 나뉩니다.
  - `business`: 개별 비즈니스 모듈들의 최상위 패키지.
  - `component`: business 모듈을 구현시 사용할 공통 모듈 규약. `docs/skills/5sa-model-skill.md`를 참고하십시오.
  - `system`: 애플리케이션에서 공통으로 제공하는 기능 요구사항(사용자관리, 첨부파일, DataGrid 등)
- 각 도메인/모듈의 내부 구조는 `docs/skills/domain-module-skill.md` 가이드라인을 참고하십시오. 

## 빌드 및 실행 (Building and Running)

- 빌드: `./gradlew build`
- 개발환경 실행: `./gradlew bootRun`
- 테스트: `./gradlew test`. 상세내용은 `docs/skills/slice-testing-skill.md`를 참고하십시오
- API 문서: 빌드 태스크 수행 시 `build/api-spec` 경로에 Asciidoc 결과물이 생성됩니다. 상세내용은 `docs/skills/rest-docs-skill.md`를 참고하십시오
- Docker: 로컬 인프라 환경(PostgreSQL 등) 구동을 위한 `compose.yaml` 파일이 제공됩니다.

## 개발 가이드라인 (Development Guidelines)

- 정체성(Identity): 일관된 식별자 관리와 이력 관리(createdAt, modifiedAt 등)를 위해 모든 엔티티는 반드시 `BaseEntity<IdType>`를 상속받아야 합니다.
- 복원(Reconstitution): 데이터베이스에서 상태를 조회할 때, 새로운 엔티티 생성 시 발생하는 도메인 이벤트나 비즈니스 규칙이 중복 트리거되지 않도록 AR 내부의 `static reconstitute(...)` 메서드를 사용해야 합니다. 상세내용은 `docs/skills/factory-skill.md`를 참고하십시오.
- 응답 포맷: 일관된 통신 계약 구조를 보장하기 위해 라우터에서는 항상 `DefaultResponseHandler`를 통한 통합 응답 규격(`Response.Success` / `Response.Fail`) 사용하여 응답을 반환해야 합니다. 상세내용은 `docs/skills/5sa-model-skill.md`를 참고하십시오.
- 검증(Validation): 도메인 모델 내부에서는 자체적인 상태 체크 로직을 구현합니다. 상세내용은 `docs/skills/state-model-skill.md`를 참고하십시오.
- Lombok: 보일러플레이트 코드를 줄이기 위해 Lombok을 사용하지 않습니다. IDE의 소스 generate 기능을 권장합니다. 

## Key References

- `docs/skills/5sa-model-skill.md`: 5SA Model Skill (공통 도메인 컴포넌트)
- `docs/skills/aggregate-root-skill.md`: Aggregate Root 작성 가이드
- `docs/skills/architecture-spec.md`: 5-Step Architecture Spec(Philosophy & Process)
- `docs/skills/dependency-skill.md`: Dependency 가이드(Framework & Build tool)
- `docs/skills/domain-factory-skill.md`: Domain Factory 작성 가이드
- `docs/skills/domain-module-skill.md`: Domain Module 구성 가이드(Package, Layer)
- `docs/skills/domain-vo-skill.md`: Domain Component (VO) 작성 가이드
- `docs/skills/functional-endpoint-skill.md`: Functional Endpoint Creation Skill
- `docs/skills/postgresql-convention-skill.md`: PostgreSQL 컨벤션 
- `docs/skills/rest-docs-skill.md`: Spring REST Docs & Asciidoctor 작성 가이드
- `docs/skills/service-orchestration-skill.md`: Service Orchestration 작성 가이드
- `docs/skills/slice-testing-skill.md`: Standards for endpoint testing and documentation.
- `docs/skills/state-model-skill.md`: State Model Skill (상태 모델 스킬)
- `docs/skills/agentic-engineering-skill.md`: Agentic Engineering 가이드 
- `src/main/java/dev/ohhoonim/component/model/unit/BaseEntity.java`: The foundation for all domain entities.
