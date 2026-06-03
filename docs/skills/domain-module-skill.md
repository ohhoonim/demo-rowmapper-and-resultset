
# Domain Module 구성 가이드 (Spring Modulith & 5-Step Architecture 지향)

## Domain 계층(package)

### application

모듈의 외부에 도메인의 '실행 의도'를 드러내는 최상위 진입점이며, 단일 책임을 수행하는 Functor이자 모듈 간 협업을 조율하는 Service Orchestration 영역입니다. 복잡한 세부 비즈니스 로직을 직접 처리(Facade)하지 않고, 외부 요청을 받아 하나의 큰 유스케이스 관점으로 전체 프로세스를 개시하고 흐름을 제어하는 실행 단위 역할을 합니다. Spring Modulith의 @NamedInterface로 외부 모듈에 노출됩니다.

```java
// application/package-info.java
@org.springframework.modulith.NamedInterface("app")
package dev.ohhoonim.business.cart.application;
```

### activity
유스케이스가 요구하는 고수준 비즈니스 명령 및 기술 핵심의 명세(Inbound Port)입니다. 애플리케이션의 실행 흐름은 이 인터페이스에만 의존하여 구체적인 기술 실체로부터 격리됩니다.

- 필수 가이드: 요구사항 추적성을 위해 반드시 클래스/인터페이스 상단에 Usecase ID를 주석으로 명시해야 합니다.

### model
비즈니스의 심장(Domain Model / Brain)입니다. 인프라나 실행 환경에 대한 지식이 전혀 없으며, BaseEntity를 통해 도메인의 식별성과 이력(Auditing)을 관리합니다. 
- 기술 제약: Aggregate Root는 BaseEntity 상속을 위해 일반 class로 구현하며, Value Object(VO) 및 Id는 record로 구현합니다.
- 코드 컨벤션: Record 활용 및 불변성 보장을 위해 도메인 모델 내부에서의 Lombok 어노테이션 사용을 전면 금지합니다.
외부 변경으로부터의 보호를 위해 내부 컬렉션은 Defensive Copy 형태로 반환하며, 정책(Policy)은 메서드 파라미터로 명시적으로 주입받아 처리합니다.

### activity.out
Activity 구현체가 비즈니스 행위를 완료하기 위해 필요한 하위 기술 명세(Out Port)입니다. 외부 시스템이나 영속성 환경과의 접점을 도메인 언어로 정의합니다.

### infra.activity
`activity` 계층의 명세를 구체적으로 구현하는 곳으로, 단일 유스케이스 내부의 세부 절차를 통제하는 Flow Orchestration 영역입니다. 캐싱, 재시도, 다중 포트 조합 등 유스케이스가 실제로 작동하기 위한 기술적 실행 시나리오와 흐름을 촘촘히 조율합니다. Activity 구현 클래스는 suffix로 'Activity' 대신 'Actions'를 사용합니다.

### infra.adapter
`activity.out`의 포트를 구현하는 어댑터 영역입니다. 특정 인프라 기술(JDBC, Spring Data, 외부 API 등)을 사용하여 외부 세계의 기술적 데이터(Table/JSON)를 도메인 모델(AR)로 복원(Reconstitute)하는 책임을 집니다.

### endpoint 
외부의 HTTP/gRPC 등의 요청을 받아 application 계층이 이해할 수 있는 포맷으로 변환하여 전달하는 단순 입구입니다. MockMvcTester 기반의 슬라이스 테스트와 Spring Rest Docs 문서화가 강제되는 영역입니다.

---

## Domain Layer convention

5-Step Architecture(Functor 기반) 및 내부 컴포넌트 간의 커뮤니케이션을 위한 convention을 다음과 같이 정의한다.

- Model: model (Aggregate Root, VO 등 / 별도 접미사 없음)
- Service Orchestration (Functor / UseCase): application (Suffix: ~Service , ~CommandHandler 또는 ~QueryHandler)
- Domain Flow Specification: activity (Interface / Suffix: ~Activity)
- Flow Orchestration (Domain Flow Implementation): infra.activity (Implementation / Suffix: ~ActivityImpl 또는 ~Actions)
- Out Port: activity.out (Interface / Suffix: ~Port 또는 ~Repository)
- Adapter: infra.adapter (Implementation / Suffix: ~Adapter 또는 ~RepositoryImpl)
- Controller: endpoint (Suffix: ~Controller , ~Endpoint)


## Package 구성 요약표

| 계층(package)  | 주요 컴포넌트                                                                    | 5-Step 관점의 역할                         |
|----------------|----------------------------------------------------------------------------------|--------------------------------------------|
| model          | AR(extends BaseEntity), VO, Policy interface                                     | 도메인 모델 (Core)                         |
| activity       | Activity Interface (Usecase ID 기재 필수)                                        | 비즈니스 명세 (Port)                       |
| activity.out   | Infra Port (Double Porting), Factory interface, Repository interface, DTO(Mapper)| 외부 인프라 명세 (Port)                    |
| application    | Functor (Command/Query Handler), DTO(Response), Event listener                   | 실행 의도 정의 (Service Orchestration / Functor) |
| infra.activity | Activity Implementation (Actions)                                                | 비즈니스 세부 제어 및 구현 (Flow Orchestration)  |
| endpoint       | REST Controller, OpenAPI, Functional Endpoint, DTO(Request)                      | 외부 진입점 (Inbound Adapter)              |
| infra.adapter  | Adapter (Port Implementation), Repository implementation, Factory Implementation | 인프라 기술 명세 구현 (Outbound Adapter)   |