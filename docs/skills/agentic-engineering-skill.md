# Agentic Engineering 가이드

## 5-Step Architecture 진입점 기반 단계별 참고 Skill 목록

 `architecture-spec.md`에서 정의한 5-Step 원칙 및 계층 정의를 매핑하여, Agentic Engineering 코드 생성 공정에서 단계별로 참고할 실무 Skill 목록을 작성했습니다.

| 단계 | 참고 문서 파일명 | 참고 문서 제목 | 비고 / 매핑 포인트 |
| --- | --- | --- | --- |
| 공통 인프라 명세 | `domain-module-skill.md` | Domain Module 구성 가이드 (Spring Modulith & 5-Step Architecture 지향) | 전체 5-Step 공정의 패키지/접미사(Suffix) 컨벤션 수립 |
| 공통 인프라 명세 | `5sa-model-skill.md` | 5SA Model Skill (공통 도메인 컴포넌트 모델) | `BaseEntity`, `EntityId`, 페이징 및 응답 규격의 상위 공통 모델 정의 |
| [Step 1] 도메인 자아 확립 | `aggregate-root-skill.md` | Aggregate Root 작성 가이드 | BaseEntity 상속, 신규 생성용/DB 복원용 생성자 분리 및 Reconstitute(복원) 구현 |
| [Step 1] 도메인 자아 확립 | `domain-vo-skill.md` | Domain Component (VO) 작성 가이드 | 불변 객체(Java record) 설계, 자가 유효성 검증 및 Sealed Interface Grouping 패턴 |
| [Step 1] 도메인 자아 확립 | `state-model-skill.md` | State Model Skill (상태 모델 스킬) | 복잡한 생애주기를 가진 도메인의 Sealed Interface 기반 상태 전이(Status, Event) 설계 |
| [Step 2] 법전 정의 | `state-model-skill.md` | State Model Skill (상태 모델 스킬) | 비즈니스 제어 법령에 해당하는 `StateTransitionPolicy` 추상화 정의 파트 참고 |
| [Step 3] 흐름 조율 | `service-orchestration-skill.md` | Service Orchestration 작성 가이드 | 로직을 소유하지 않는 Functor(함자)로서의 거시적 흐름 조율, 트랜잭션 및 유스케이스 ID 명시 규칙 |
| [Step 4] 도구 제작 | `domain-factory-skill.md` | Domain Factory 작성 가이드 | 저장소 데이터로부터 도메인을 복원하는 `ArFactory` 포트(`activity.out`) 및 어댑터(`infra.adapter`) 구현 |
| [Step 4] 도구 제작 | `postgresql-convention-skill.md` | PostgreSQL 컨벤션 | `infra.adapter` 구현 시 DB 테이블 명명 규칙, 이중 Key(ULID PK / UUID v4 External ID) 전략 반영 |
| [Step 5] 반복 공정 검증 | `slice-testing-skill.md` | Slice Testing Skill (Spring Boot 4+, Java 25) | Pure Java 기반 Model 테스트, Mockito 기반 Application 테스트, Testcontainers 기반 Adapter 테스트 작성 표준 |
| [추가 외부 인터페이스] | `functional-endpoint-skill.md` | Functional Endpoint Creation Skill | @Controller 대신 Router와 Handler를 사용하는 Functional Endpoint 패턴 구현 |
| [추가 외부 인터페이스] | `rest-docs-skill.md` | Spring REST Docs & Asciidoctor 작성 가이드 | 테스트 통과 기반의 신뢰성 있는 API 명세서(.adoc) 생성 및 빌드 연동 규칙 |