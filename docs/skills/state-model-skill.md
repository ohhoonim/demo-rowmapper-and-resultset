# 🧩 State Model Skill (상태 모델 스킬)

본 문서는 **Matthew's Architecture**의 'Step 1: 도메인 자아 확립'과 'Step 2: 법전 정의'를 실무적으로 구현하는 핵심 패턴인 **상태 모델(State Model)**의 구현 가이드를 정의한다.

## 📌 개요 (Overview)

상태 모델은 Java 17+의 **Sealed Interface**와 **Record**를 활용하여, 도메인 엔티티의 복잡한 생애주기와 상태 전이 로직을 타입 안전(Type-safe)하고 명시적으로 관리하는 패턴이다.

---

## 🏗️ 핵심 구성 요소 (Core Components)

### 1. Context (`C`)
상태 변화의 주체가 되는 도메인 엔티티 (예: `User`, `Order`, `Product`).

### 2. Status (`S`) - `Status<S, T, C>`
상태를 정의하는 **Sealed Interface**.
- `trigger(T event)` 메서드를 통해 다음 상태를 결정하는 '뇌(Brain)' 역할을 수행한다.
- 각 개별 상태는 `record`로 구현하며, `switch` 표현식을 통해 유효한 전이만 허용한다.

### 3. TransitionEvent (`T`) - `TransitionEvent<C>`
상태 전이를 유발하는 트리거.
- `actions()` 메서드를 통해 전이 성공 후 실행될 `PostAction` 목록을 반환한다.
- 전이에 필요한 모든 컨텍스트 데이터(예: 실패 횟수, IP 등)를 필드로 포함한다.

### 4. TransitionResult (`S, C`)
전이의 최종 결과물.
- `status()`: 전이된 새로운 상태.
- `actions()`: 실행해야 할 후속 조치 목록.

### 5. PostAction (`C`)
엔티티에 부수 효과(Side Effect)를 적용하는 함수형 인터페이스.
- 상태 결정과 데이터 수정을 분리하여 응집도를 높인다.

### 6. StateTransitionPolicy (`S, T, C`)
전이 로직을 실행하는 법령(Policy).
- 서비스 계층에서 모델의 `trigger`를 안전하게 호출하도록 오케스트레이션한다.

---

## 🛠️ 구현 단계 (Implementation Steps)

### Step 1: Context 정의
상태를 가질 엔티티 클래스를 준비한다.

### Step 2: TransitionEvent 구현
```java
public sealed interface XxxTransitionEvent extends TransitionEvent<Xxx> permits EventA, EventB {
    record EventA(Data data) implements XxxTransitionEvent {
        @Override public List<PostAction<Xxx>> actions() {
            return List.of(xxx -> xxx.updateSomething(data));
        }
    }
}
```

### Step 3: Status 구현
```java
public sealed interface XxxStatus extends Status<XxxStatus, XxxTransitionEvent, Xxx> 
    permits StateA, StateB {
    
    record StateA() implements XxxStatus {
        @Override
        public TransitionResult<XxxStatus, Xxx> trigger(XxxTransitionEvent event) {
            return switch (event) {
                case EventB e -> new XxxTransitionResult(new StateB(), e.actions());
                default -> throw new DomainException("유효하지 않은 전이입니다.");
            };
        }
    }
}
```

### Step 4: Policy 및 Result 구현
프로젝트의 표준 인터페이스를 상속받아 구체 클래스를 생성한다.

---

## 🛡️ Best Practices & Rules

1. **불변성 (Immutability)**: Status와 Event는 반드시 `record`로 작성하여 데이터의 무결성을 보장한다.
2. **완전성 (Exhaustiveness)**: `switch` 표현식을 사용하여 컴파일 타임에 모든 상태/이벤트 조합이 처리되었는지 검증한다.
3. **예외 처리**: 허용되지 않는 상태 전이 시에는 반드시 `DomainException`을 발생시켜 비정상적인 흐름을 차단한다.
4. **부수 효과 분리**: 필드 수정 로직은 `Status` 내부가 아닌 `PostAction`을 통해 전달하여, '상태 결정'과 '데이터 반영'의 책임을 명확히 나눈다.
5. **봉인 (Sealing)**: `sealed`와 `permits`를 통해 도메인 상태의 확장을 명시적으로 제어한다.

---

## 🔎 참고 구현체
- `dev.ohhoonim.system.user.model.UserStatus`
- `dev.ohhoonim.system.user.model.UserTransitionEvent`
