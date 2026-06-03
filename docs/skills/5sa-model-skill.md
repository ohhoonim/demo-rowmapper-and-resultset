# 5SA Model Skill (공통 도메인 컴포넌트 모델)

본 문서는 `dev.ohhoonim.component.model` 패키지에서 제공하는 공통 도메인 컴포넌트들의 역할과 사용법을 정의한다. 이 컴포넌트들은 헥사고날 아키텍처와 DDD를 기반으로 한 프로젝트의 표준 빌딩 블록이다.

---

## 1. 개요 (Overview)

`component.model` 모듈은 비즈니스 도메인을 구축하는 데 필요한 최상위 추상화와 공통 유틸리티 모델을 제공한다.
- Java 25+의 `sealed interface`와 `record`를 적극 활용하여 타입 안전성과 데이터 무결성을 보장한다.
- 모든 도메인 모듈은 이 컴포넌트들을 상속받거나 활용하여 일관된 구조를 유지한다.

---

## 2. Unit (도메인 정체성 및 이력)

도메인 모델의 가장 기초가 되는 인터페이스와 클래스들을 포함한다.

### 2.1 BaseEntity
모든 Aggregate Root(AR)의 부모 클래스이다.
- 식별성(Identity): `EntityId`를 통한 식별자 관리.
- 이력관리(Auditing): 생성/수정 일시 및 작업자를 관리한다.
- 복원 지원: 신규 생성용 생성자와 DB 복원용 생성자를 분리하여 제공한다.

### 2.2 EntityId
내부용 PK(UUID/ULID)와 외부 노출용 Public ID를 분리하여 관리하는 이중 키 전략을 지원한다.
- `JsonValue`를 통해 외부 노출 시 자동으로 Public ID로 직렬화된다.

### 2.3 MasterCode
시스템의 공통 코드(코드성 데이터)를 위한 인터페이스이다. Enum과 결합하여 타입 안전한 코드 관리를 지원한다.

---

## 3. Factory (도메인 복원 공정)

DDD의 Factory 패턴을 구현하기 위한 인터페이스를 제공한다.

### 3.1 ArFactory<A, I, C>
저장소(Infrastructure)의 기술적 데이터를 도메인 객체(AR)로 복원(Reconstitution)하는 책임을 정의한다.
- 유스케이스에 필요한 컴포넌트만 선택적으로 로딩(Selective Loading)할 수 있는 구조를 가진다.
- 상세 내용은 `domain-factory-skill.md`를 참조한다.

---

## 4. Paging (표준 페이징 모델)

목록 조회 시 일관된 페이징 처리를 위한 모델이다.

- PageRequest: 클라이언트의 페이징 요청 정보 (pageNo, pageSize, sort 등).
- Paged: 서버 측의 페이징 결과 정보 (totalCount, totalPages 등).
- PagedData<T>: 실제 데이터 목록과 페이징 정보를 결합한 래퍼 객체.
- `Paging` 인터페이스를 통해 `PageRequest`와 `Paged`가 논리적으로 연결된다.

---

## 5. Payload (통합 응답 규격)

API의 응답 포맷을 표준화하는 객체들이다.

### 5.1 Response & ResponseCode
모든 API 응답은 `Response.Success` 또는 `Response.Fail`로 캡슐화된다.
- Success: 데이터와 함께 `SUCCESS` 코드 반환.
- Fail: 에러 메시지와 함께 `ERROR` 코드 반환.

### 5.2 DefaultResponseHandler & DefaultEndpointHandler
`@RestControllerAdvice` 및 Handler Filter를 통해 서비스의 반환값을 자동으로 `Response` 규격으로 감싸준다. 개발자는 핸들러나 컨트롤러에서 순수 데이터(DTO/Entity)만 반환하면 된다.

---

## 6. State (상태 전이 모델)

복잡한 비즈니스 상태를 관리하기 위한 프레임워크를 제공한다.

- Status / TransitionEvent: 상태와 전이 이벤트를 정의한다.
- PostAction: 전이 성공 후 실행될 부수 효과를 정의한다.
- StateTransitionPolicy: 전이 로직을 오케스트레이션한다.
- 상세 내용은 `state-model-skill.md`를 참조한다.

---

## 7. 주요 설계 원칙 (Design Principles)

1.  불변성 (Immutability): 대부분의 컴포넌트는 `record`로 선언되어 생성 후 상태가 변하지 않음을 보장한다.
2.  봉인 (Sealing): `sealed interface`를 통해 무분별한 확장을 막고, `switch` 표현식 등을 통해 컴파일 타임에 모든 경우의 수를 체크한다.
3.  명시적 복원 (Explicit Reconstitution): `BaseEntity`와 `ArFactory`를 통해 데이터가 생성되는 시점과 저장소에서 읽어오는 시점을 명확히 구분한다.
4.  관심사 분리 (Separation of Concerns): 비즈니스 로직(Model), 흐름 조율(Service), 기술적 세부사항(Infra)을 명확히 분리할 수 있는 기반을 제공한다.

---

## 8. 논리적 아키텍처 구조 설정을 위한 jmolecules-stereotypes.json 설정 예

- 파일 경로: `src/main/resources/META-INF/jmolecules-stereotypes.json`

```json
{
  "stereotypes": {
    "5sa.EntityId": {
      "name": "EntityId",
      "assignments": [
        "dev.ohhoonim.component.model.unit.EntityId"
      ],
      "groups": [
        "5sa"
      ]
    },
    "5sa.AggregateRoot": {
      "name": "Aggregate Root",
      "assignments": [
        "dev.ohhoonim.component.model.unit.BaseEntity"
      ],
      "groups": [
        "5sa"
      ]
    },
    "5sa.StatePolicy": {
      "name": "State Policy",
      "assignments": [
        "dev.ohhoonim.component.model.state.StateTransitionPolicy"
      ],
      "groups": [
        "5sa"
      ]
    },
    "5sa.ArFactory": {
      "name": "AR Factory",
      "assignments": [
        "dev.ohhoonim.component.model.factory.ArFactory"
      ],
      "groups": [
        "5sa"
      ]
    },
    "5sa.Endpoint": {
      "name": "DTO",
      "assignments": [
        "dev.ohhoonim.component.model.unit.Endpoint"
      ],
      "groups": [
        "5sa"
      ]
    },
    "5sa.DTO": {
      "name": "DTO",
      "assignments": [
        "@dev.ohhoonim.component.model.payload.Dto"
      ],
      "groups": [
        "5sa"
      ]
    },
    "5sa.ValueObject": {
      "name": "ValueObject",
      "assignments": [
        "@dev.ohhoonim.component.model.unit.ValueObject"
      ],
      "groups": [
        "5sa"
      ]
    },
    "5sa.Activity": {
      "name": "Activity",
      "assignments": [
        "@dev.ohhoonim.component.model.unit.Activity"
      ],
      "groups": [
        "5sa"
      ]
    },
    "5sa.Adapter": {
      "name": "Adapter",
      "assignments": [
        "@dev.ohhoonim.component.model.unit.Adapter"
      ],
      "groups": [
        "5sa"
      ]
    },
    "5sa.Policy": {
      "name": "Adapter",
      "assignments": [
        "@dev.ohhoonim.component.model.unit.Policy"
      ],
      "groups": [
        "5sa"
      ]
    }
  },
  "groups": {
    "5sa": {
      "displayName": "5SA",
      "type": "design"
    }
    
  }
}
```