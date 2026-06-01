# Service Orchestration 작성 가이드

## 1. 철학 (Core Philosophy)
서비스는 비즈니스 로직의 소유자가 아니라, 도메인 모델이라는 **Category**와 유스케이스라는 **Category**를 연결하는 **함자(Functor)**입니다. 서비스는 오직 흐름(Morphism)만을 관장하여 요구사항 변화에 모델을 수정하지 않고도 유연하게 대응하는 것을 목적으로 합니다.

## 2. 서비스의 4대 책임 (Responsibilities)

### 2.1 흐름 조율 (Orchestration)
유스케이스를 완료하기 위해 필요한 `Activity`, `Port`, `Policy`, `Event`들의 호출 순서를 결정합니다. 

### 2.2 환경 제공 (Policy Injection)
도메인 모델이 판단을 내릴 때 필요한 외부 환경 규칙(`Policy`)을 모델의 메서드 파라미터로 주입합니다. 모델은 주입된 법(Law)에 따라 스스로 판단합니다.

### 2.3 트랜잭션 경계 (Transaction Management)
하나의 유스케이스가 "전부 성공하거나 전부 실패"해야 하는 논리적 작업 단위를 설정합니다 (`@Transactional`).

### 2.4 컨텍스트 전이 (Context Transition)
도메인의 변화를 시스템 전체의 맥락으로 전이시킵니다. 
- 예: SecurityContext에 식별자 저장, 감사 로그 이벤트 발행 등.

## 3. 예시 구조 (AuthenticationService 기반)

```java
@Transactional
public SignedToken signIn(SignUser loginTryUser) {
    // 1. [Activity] 기술적 검증 및 모델 복원
    User user = identityChallenge.signIn(loginTryUser.name(), loginTryUser.password());

    // 2. [Model + Policy] 도메인 로직 수행 (상태 전이)
    user.userStateTransition(new LoginSuccess(), policy);

    // 3. [Port] 변경 사항 영속화
    userPersistencePort.save(user);

    // 4. [Side Effects] 보안 컨텍스트 설정 및 이벤트 발행
    updateSecurityContext(user.getUserId());
    publisher.publishEvent(new AuditLogEvent(...));

    return generateToken(user);
}
```
