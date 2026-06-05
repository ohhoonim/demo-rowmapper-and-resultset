# Aggregate Root 작성 가이드

## 1. 개요
Aggregate Root(이하 AR)는 도메인 모델의 진입점이자 데이터 정합성을 보장하는 경계입니다. `dev.ohhoonim.component.model.unit` 패키지의 기반 클래스들을 사용하여 식별성(Identity)과 이력 관리(Auditing) 기능을 일관되게 구현합니다.

## 2. 식별자 (EntityId) 정의
모든 AR은 고유한 식별자 타입을 가져야 합니다. 도메인이 '외부ID'를 제공해야하는 경우는 '외부 ID를 제공하는 경우의 구현 예'를 참고한다.
- 형식: Java `record` 사용 권장.
- 인터페이스: `EntityId`를 구현.
- 표준 패턴:
    - 생성자에서 유효성 검증.
    - `Creator` 인터페이스 구현을 통한 생성/변환 로직 제공.

### 외부 ID를 제공하는 경우의 구현 예

```java
public record UserId(UUID internalId, UUID externalId) implements EntityId {
    public UserId {
        if (externalId == null) {
            throw new UserException("외부 식별자가 없습니다");
        }
    }
    public static Creator<UUID, UserId> Creator = new Creator<>() {
        @Override
        public UserId from(UUID internalId, UUID externalId) {
            if (internalId == null) throw new UserException("내부 식별자가 누락되었습니다");
            return new UserId(internalId, externalId);
        }
        @Override
        public UserId fromPublic(String publicId) {
            // 조회 목적이므로 internalId는 비워둠
            return new UserId(null, UUID.fromString(publicId));
        }
        @Override
        public UserId generate() {
            return new UserId(UlidCreator.getMonotonicUlid().toUuid(), UUID.randomUUID());
        }
    };
    @Override
    public UUID getRawValue() {
        if (internalId == null) {
            throw new UserException("내부 식별자가 확인되지 않은 ID입니다. Resolve가 필요합니다.");
        }
        return internalId;
    }
    @Override
    public String getPublicValue() {
        return externalId.toString();
    }
}
```

## 3. Aggregate Root 구조 및 구현

### 3.1 기본 상속 및 필드
- BaseEntity를 상속받습니다. (주의: Java Record는 클래스 상속이 불가능하므로, Aggregate Root 본체는 반드시 일반 'class'로 선언해야 합니다.)
- 관련 속성들은 내부 컴포넌트(record) 단위로 묶어서 관리합니다.

### 3.2 생성자 설계
1. 신규 생성용 (Protected): 비즈니스 로직에 의해 처음 만들어질 때 사용합니다. `super(id, operator)`를 호출하여 생성/수정 정보를 자동 설정합니다.
2. DB 복원용 (Private): 인프라 어댑터에서 기존 데이터를 불러올 때 사용합니다. 모든 Auditing 정보를 매개변수로 받아 부모 생성자에 전달합니다.

### 3.3 복원 메서드 (Reconstitute)
인프라 계층(`infra.adapter`)에서 도메인 객체를 다시 조립할 때 사용하는 정적 메서드입니다.

```java
public static MyAR reconstitute(MyId id, MyComponent component, MyStatus status,
        Instant createdAt, String createdBy, Instant modifiedAt, String modifiedBy) {
    return new MyAR(id, component, status, createdAt, createdBy, modifiedAt, modifiedBy);
}
```

## 4. 상태 관리 및 비즈니스 규칙
- 상태 패턴: `UserStatus` 예시와 같이 `sealed interface`와 `record`를 사용한 상태 패턴 적용을 권장합니다.
- 수정 기록: AR 내부에서 상태를 변경하는 비즈니스 메서드가 실행될 때, 마지막에 반드시 `recordModification(operator)`을 호출하여 수정 이력을 갱신합니다.
- 캡슐화: 외부에서 직접 필드를 수정하지 못하도록 `Setter` 사용을 지양하고 의도가 담긴 메서드를 제공합니다.

## 5. 컴포넌트 (Value Objects)
- 데이터 그룹은 `record`를 사용하여 불변성을 유지합니다.
- `sealed interface [Name]Component` 패턴을 사용하여 한 AR에 속한 다양한 컴포넌트들을 한 곳에서 명시적으로 관리할 수 있습니다.
- Value Object가 Aggregate Root에서 List 형태를 가지는 경우(Collection VO) `sealed interface [Name]Component` 패턴을 사용하지 않고 별개의 `record`로 작성한다.
- 상세한 작성 방법은 `docs/domain-vo-skill.md`를 참조하십시오.