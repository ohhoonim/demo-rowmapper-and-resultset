# Smart Factory Project - GEMINI Context

This project is a modern, high-standard industrial software system built with a focus on **Hexagonal Architecture**, **Domain-Driven Design (DDD)**, and **Spring Modulith**. It follows a rigorous **5-Step Architecture Specification** to ensure maintainability, testability, and clear separation of concerns.

## Project Overview

- **Purpose**: A smart factory backend system for industrial automation and management.
- **Main Technologies**:
  - **Language**: Java 25 (utilizing latest language features).
  - **Framework**: Spring Boot 4.0.5, Spring Modulith 2.0.1.
  - **Persistence**: PostgreSQL, JDBC.
  - **Caching**: Valkey / Data Valkey.
  - **Security**: Spring Security with JWT (jjwt).
  - **Testing**: JUnit 5, MockMvcTester, Testcontainers (PostgreSQL, Valkey), ArchUnit.
  - **Documentation**: Spring REST Docs with Asciidoctor.

## Core Architectural Mandates (The 5-Step Process)

All development MUST adhere to the `docs/architecture-spec.md` and `docs/domain-module-skill.md` guidelines.

1.  **[Step 1] Domain Model Discovery (AR)**: Define Aggregate Roots (AR) inheriting from `BaseEntity`. Use strict encapsulation. Logic remains in the model.
2.  **[Step 2] Policy Abstraction**: Abstract environment-dependent rules as `@FunctionalInterface`.
3.  **[Step 3] Service Orchestration**: Services act as **Functors**. They do NOT contain business logic; they orchestrate the flow between Models, Policies, and Activities.
4.  **[Step 4] Activity Implementation**: Use "Activities" as ports/adapters to bridge the domain with infrastructure (DB, external APIs).
5.  **[Step 5] Multilayered Testing**:
    - **Model Tests**: Pure unit tests for business logic.
    - **Service Tests**: Flow verification using mocks.
    - **Slice Tests**: `MockMvcTester` for endpoints, including REST Docs generation.

## Directory Structure & Package Conventions

The project is divided into major domains: `business`, `component`, and `system`.

Within each domain/module (e.g., `dev.ohhoonim.system.user`):
- `model`: Aggregate Roots, VOs, Policies, and Exceptions.
- `application`: Services (Orchestrators), DTOs, and Factories.
- `activity`: Activity Interfaces (Port definitions).
- `endpoint`: `RouterFunction` (Routers) and `Handler` classes for REST APIs.
- `infra`:
  - `activity`: Implementations of Activity Interfaces.
  - `adapter`: Infrastructure-specific adapters (Repository Impls).

## Building and Running

- **Build**: `./gradlew build`
- **Run**: `./gradlew bootRun`
- **Test**: `./gradlew test`
- **API Documentation**: Build task generates Asciidoc in `build/api-spec`.
- **Docker**: `compose.yaml` is provided for local infrastructure (PostgreSQL, Redis).

## Development Guidelines

- **Identity**: Always use `BaseEntity<IdType>` for entities to maintain consistent identity and auditing (createdAt, modifiedAt, etc.).
- **Reconstitution**: Use `static reconstitute(...)` methods in ARs to load state from the database without triggering domain events/business rules of a new creation.
- **Responses**: Use `CommonUtil.defaultResponse()` in routers to ensure a unified response format.
- **Validation**: Prefer `jakarta.validation` annotations on DTOs and internal model state checks.
- **Lombok**: Extensively used for boilerplate reduction (`@Getter`, `@RequiredArgsConstructor`, etc.).

## Key References

- `docs/skills/aggregate-root-skill.md`: AR 구성 가이드 
- `docs/skills/domain-vo-skill.md`: Standards for Domain Components (Value Objects).
- `docs/skills/architecture-spec.md`: Detailed 5-Step process and philosophy.
- `docs/skills/components-model-skill.md`: Components Model Skill (공통 도메인 컴포넌트)
- `docs/skills/factory-skill.md`: repository factory guide
- `docs/skills/domain-module-skill.md`: Package and layer responsibility guide.
- `docs/skills/functional-endpoint-skill.md`: Functional Endpoint Creation Skill guide
- `docs/skills/postgresql-convention-skill.md`: PostgreSQL 컨벤션 
- `docs/skills/service-orchestration-skill.md.md`: Service Orchestration 작성 가이드
- `docs/skills/slice-testing-skill.md`: Standards for endpoint testing and documentation.
- `docs/skills/state-model-skill.md`: State Model Skill (상태 모델 스킬)
- `src/main/java/dev/ohhoonim/component/model/unit/BaseEntity.java`: The foundation for all domain entities.
