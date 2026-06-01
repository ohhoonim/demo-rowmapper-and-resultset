# 5-Step Architecture Spec (Revised v3.3)

이 문서는 헥사고날 아키텍처와 DDD 전략적 설계를 실무적으로 결합하고, 바이브 모델링을 통해 도출된 도메인 자아를 소프트웨어 제조 공정으로 치환한 표준 지침입니다.

## Core Philosophy

Service는 도메인 모델이라는 Category와 유스케이스라는 Category를 연결하는 Functor입니다. 모델은 비즈니스 규칙의 정적 보존 처인 Brain이며, Policy는 그 규칙이 환경에 따라 발현되는 방식인 Law입니다. Service는 이들 사이의 Morphism(사상)을 조율하여 유스케이스의 맥락으로 데이터를 전이시키는 역할을 수행합니다. 따라서 Service는 로직을 소유하지 않고 오직 흐름만을 관장함으로써 요구사항의 변화에 모델을 수정하지 않고도 대응할 수 있는 유연성을 확보합니다.

## The 5-Step Standard Process

### [Step 1] 도메인 자아 확립 (Domain Model Discovery)

비즈니스의 핵심 상태와 행위를 관리하는 Aggregate Root(AR)를 정의합니다. 모든 AR은 시스템의 공통 명세인 BaseEntity를 상속받아 정체성과 생애 주기를 관리합니다.

1. **BaseEntity 기반의 자아 증명**
    - 정체성(Identity) 계승: AR은 BaseEntity<EntityId>를 상속받음으로써 기술적 식별자가 아닌 도메인 식별자를 통해 스스로를 증명합니다.
    - Auditing의 내재화: createdAt, createdBy 등의 정보는 인프라(JPA 등)에 맡기는 부가 정보가 아니라, 모델이 생성되고 수정된 비즈니스 이력으로서 모델의 필드에 명시적으로 보존됩니다.

2. **Reconstitution (복원 능력)의 이원화**
AR은 상황에 따라 두 가지 방식의 생성 전략을 소유합니다.
    - 신규 생성(Creation): protected BaseEntity(I id, String operator)를 사용하여 도메인 이벤트의 시작점을 기록합니다.
    - 데이터 복원(Reconstitution): reconstitute라는 정적 팩토리 메서드 내부에서 BaseEntity의 복원용 생성자를 호출합니다. 이는 인프라(DB)로부터 과거의 상태를 그대로 이어받아 현재의 메모리에 자아를 재현하는 공정입니다.

3. **상태 변경과 이력 기록 (Strict Encapsulation)**
    - 모델 내부의 상태가 변경될 때, 반드시 recordModification(operator)을 호출하여 "누가, 언제" 자아를 변화시켰는지 기록해야 합니다. 이는 모델의 정합성을 유지하는 최소한의 방어 기제입니다.

### [Step 2] 법전 정의 (Policy Abstraction)

비즈니스 제약 조건 중 외부 환경(보안, 계산 로직 등)에 따라 변하기 쉬운 규칙을 @FunctionalInterface 등으로 추상화합니다.

- **원칙**: 모델은 법전의 내용을 알 필요가 없습니다. 법전(Policy)이 모델에 주입될 때, 모델은 그 법에 따라 판단할 뿐입니다.

### [Step 3] 흐름 조율 (Service Orchestration)

서비스는 비즈니스 로직을 구현하지 않고, 모델과 필요한 행위(Activity)들의 명세를 정의하며 전체 공정을 조율합니다. activity는 유스케이스로 부터 도출되어야 하며, 요구사항 추적성을 위해 activity에는 Usecase ID를 기록해야한다. Usecase가 없는경우 Usecase 목록을 작성하도록 요청하여야한다.

- **원칙**: Interface Driven. 이 단계에서 필요한 Activity의 인터페이스 명세를 먼저 도출합니다. (예: 여기서 저장이 필요하니 save()가 있는 CommandActivity가 필요하겠군)
- **프로세스**: Activity 명세 정의 -> AR 복원(reconstitute) -> Policy 주입 및 비즈니스 수행 -> 결과 저장 명세 정의

### [Step 4] 도구 제작 (Activity Implementation)

Step 3에서 설계된 유스케이스 시나리오를 완성하기 위해 필요한 구체적인 도구들을 구현합니다. Activity는 유스케이스가 외부 세계에 던지는 기술적 명령의 실체이자, 인프라 기술을 비즈니스 언어로 추상화하는 완충 지대입니다.

- **Use Case Driven 원칙**: Activity 명세(Interface)는 유스케이스가 비즈니스 흐름을 이어가기 위해 필요한 행위의 목적으로부터 도출됩니다. Activity interface는 유스케이스를 추척하는 유스케이스코드를 주석으로 달아야 하며, 없는경우 개발자에게 요청하여야 합니다.
- **Implementation Purity & Double Porting 원칙**
    - Activity interface: Service가 무엇을 하고 싶은지 정의합니다.
    - Activity 구현체: 어떤 순서로 기술적 도구를 조합할지 정의합니다.
    - Infra Port: 실제 기술적 행위 자체를 정의합니다.
- **Infrastructure Selection & Layered Adapter Design 원칙**
    - 기술 선택의 독립성: 요구 성능과 특성에 따라 JdbcClient, Valkey, Kafka 등 최적의 인프라를 자유롭게 선택합니다.
    - Mapping Responsibility: 어댑터는 기술적 데이터(Table/JSON)와 도메인 모델(AR) 사이의 차이를 메우는 유일한 장소입니다. reconstitute 공정을 정교하게 구현합니다.
    - 물리적 분리: 인터페이스(Port)는 Step 3 패키지에, 구현체(Activity/Adapter)는 Step 4 인프라 패키지에 두어 의존성을 격리합니다.

### [Step 5] 반복 공정 검증 (Multilayered Testing)

- **Model Test**: 정책을 Stub/Mock으로 주입하여 비즈니스 로직의 순수성을 ms 단위로 검증합니다.
- **Service Test**: 각 컴포넌트 간의 호출 흐름(Orchestration)만 검증합니다. 로직 테스트를 지양하여 테스트 무거움을 방지합니다.

## Implementation Guidelines (Deep Dive)

### 1. Unified Reconstitution

모든 AR은 reconstitute 메서드를 통해 복원됩니다. 이는 생성과 조회를 기술적으로 분리하지 않고 도메인 관점에서 하나로 통합하는 핵심 장치입니다. 도메인 모델은 이 지점을 통해 기술적 환경에 구애받지 않고 스스로의 자아를 증명합니다.

```java
public static AttachFile reconstitute(AttachFileId id, List<FileItem> items, ...) {
    return new AttachFile(id, items, ...);
}
```

### 2. The Functorial Service Principle

Service는 비즈니스 로직의 소유자가 아니라, 카테고리 간의 전이를 담당하는 Functor(함자)입니다.

- **철학**: 도메인 모델(Category A)이 가진 순수한 비즈니스 규칙을 보전하면서, 이를 유스케이스(Category B)라는 실행 문맥으로 사상(Mapping)합니다.
- **역할 (Orchestration)**: 서비스는 모델 내부에서 처리할 수 없는 흐름을 조율(Morphism)할 뿐이며, 스스로 뇌가 되지 않습니다. 유스케이스의 변화는 곧 Functor의 사상 규칙이 변하는 것이므로, 모델을 수정하지 않고도 서비스의 조율 로직(Step 3)을 변경함으로써 요구사항에 유연하게 대응합니다.

### 3. Explicit Policy Injection

AR의 메서드가 정책을 필요로 할 경우, 항상 파라미터로 정책 인터페이스를 명시적으로 전달받습니다. 서비스(Functor)가 외부 환경의 법전(Policy)을 모델의 입에 넣어주는 형태이며, 모델은 주입된 법에 따라 스스로 판단합니다.

### 4. Defensive Copy in Model

내부 컬렉션을 노출할 때는 반드시 Collections.unmodifiableList() 등을 사용하여 외부에서 내부 상태를 오염시키는 것을 원천 차단합니다. 이는 모델의 불변성과 정합성을 유지하기 위한 최소한의 방어 기제입니다.

### 5. 공통 응답 및 예외 관리 표준화

모든 API 응답은 ResponseBodyAdvice를 구현한 DefaultResponseHandler를 통해 Response.Success 또는 Response.Fail 포맷으로 자동 캡슐화되어 반환되며, 이를 통해 컨트롤러의 비즈니스 결과물과 전역 예외 처리기(@ExceptionHandler)에서 포착된 에러 메시지를 일관된 통신 계약 구조로 표준화합니다.

### 6. MockMvcTester 기반의 슬라이스 테스트 및 문서화 표준
컨트롤러의 슬라이스 테스트는 최신 MockMvcTester를 사용하여 AssertJ 스타일의 직관적인 검증을 수행해야 하며, 이때 Spring Rest Docs를 결합하여 실제 테스트를 통과한 케이스에 대해서만 API 명세서가 자동으로 생성되도록 강제합니다.

