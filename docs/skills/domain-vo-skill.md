# Domain Component (VO) 작성 가이드

## 1. 개요
Domain Component(이하 VO, Value Object)는 애그리거트 루트(AR)의 속성들을 논리적으로 그룹화한 불변 객체입니다. 단순히 데이터를 담는 그릇을 넘어, 해당 데이터와 관련된 도메인 로직을 스스로 내포하여 AR의 복잡도를 낮추는 역할을 합니다.

## 2. 핵심 설계 원칙

1.  불변성 (Immutability): 생성 후 상태가 변하지 않아야 합니다. Java `record` 사용을 원칙으로 합니다.
2.  논리적 응집 (Cohesion): 서로 밀접하게 연관된 속성들을 하나의 단위로 묶습니다 (예: `username`, `email` -> `UserProfile`).
3.  자가 로직 소유 (Logic Possession): 해당 데이터에 대한 검증이나 판단 로직은 VO 내부에서 처리합니다 (예: IP 대역 체크, 날짜 만료 여부).
4.  자가 유효성 검증: 생성자에서 필수 값 누락이나 형식 오류를 검증하여, 항상 유효한 상태의 객체만 존재하도록 보장합니다.

## 3. 구현 패턴: Sealed Interface Grouping

하나의 AR에 속한 다양한 컴포넌트들을 명시적으로 관리하고 가독성을 높이기 위해 `sealed interface` 패턴을 사용합니다.

```java
public sealed interface MyArComponent permits ComponentA, ComponentB, StatusEnum {
    // 내부 record 또는 enum으로 구현
}
```

### 이점
- 가독성: AR이 어떤 데이터 그룹으로 구성되어 있는지 한눈에 파악 가능합니다.
- 타입 안전성: 특정 AR 전용 컴포넌트임을 컴파일 타임에 보장합니다.
- Factory 연동: `ArFactory`에서 선택적 로딩(Selective Loading) 시 마커 인터페이스로 활용됩니다.

## 4. 세부 구현 지침

### 4.1 Java Record 활용
- 모든 VO는 `record`로 선언하여 `equals`, `hashCode`, `toString` 등을 자동으로 처리합니다.
- 컬렉션 필드가 있는 경우, 생성자에서 `List.copyOf()` 등을 사용하여 외부로부터의 수정을 차단(Defensive Copy)합니다.

### 4.2 도메인 로직 구현
- VO는 단순한 Getter의 모음이 아닙니다. 비즈니스 질문에 답하는 메서드를 포함해야 합니다.

```java
public record AllowedIpRange(String cidr) implements UserComponent {
    public boolean isSatisfiedBy(String clientIp) {
        // IP 대역 검증 로직...
        return isInRange(baseAddress, targetAddress, prefixLength);
    }
}
```

### 4.3 팩토리 및 DB 매핑
- VO의 필드명은 `ArFactory`에서 리플렉션을 통해 DB 컬럼명(snake_case)으로 자동 매핑됩니다.
- 필드명을 정의할 때 DB 스키마 컨벤션을 고려하십시오.

## 5. 예시 (UserComponent.java 패턴)

```java
public sealed interface UserComponent permits UserProfile, LoginInfo, UserAuthorization {
    
    // 기본 인적 정보
    public record UserProfile(
            String username,
            String employeeNo,
            String email,
            String departmentId
    ) implements UserComponent {}

    // 권한 그룹
    public record UserAuthorization(
            List<String> assignedRoles,
            boolean isHighPrivilege
    ) implements UserComponent {
        public UserAuthorization {
            // 방어적 복사
            assignedRoles = List.copyOf(Objects.requireNonNullElse(assignedRoles, List.of()));
        }
    }
}
```

## 6. 관련 가이드
- `docs/aggregate-root-skill.md`: AR에서 VO를 사용하는 방법.
- `docs/domain-factory-skill.md`: VO를 DB로부터 복원(Reconstitute)하는 방법.
