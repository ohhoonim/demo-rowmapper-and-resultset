## PostgreSQL 컨벤션

### 1. 식별자 및 명명 규칙 (Naming)

- 소문자 및 스네이크 케이스: 모든 식별자(테이블, 컬럼, 인덱스 등)는 소문자와 언더스코어(`_`)만 사용합니다.
    - Bad: `UserTable`, `"UserName"`
    - Good: `users`, `user_name`
- 복수형 테이블명: 엔티티의 집합임을 나타내기 위해 복수형 사용을 권장합니다.
    - Example: `users`, `orders`, `products`
- 명확한 제약 조건 이름: 시스템 자동 생성 이름 대신 의미 있는 이름을 부여합니다.
    - PK: `pk_테이블명` (예: `pk_users`)
    - UK: `uk_테이블명_컬럼명` (예: `uk_users_email`)
    - FK: `fk_테이블명_참조테이블명` (예: `fk_orders_users`)

### 2. 이중 Key(Dual Key) 전략

성능과 보안을 동시에 잡기 위해 내부 관리용 키와 외부 노출용 키를 분리합니다.

- Internal PK (Primary Key):
    - 타입: `uuid`
    - 알고리즘: ULID 또는 UUID v7 (시간 기반 정렬 가능)
    - 이점: B-Tree 인덱스 최적화로 삽입 성능 극대화 및 내부 Join 속도 향상.
- External ID (Unique Key):
    - 타입: `uuid`
    - 알고리즘: UUID v4 (완전 랜덤)
    - 이점: API 및 브라우저 노출 시 생성 시간 유추 방지 및 비즈니스 지표 보호.

### 3. 데이터 타입 권장사항

- 문자열: `varchar(n)` 보다는 `text`를 기본으로 사용합니다. (성능 차이 없음, 유연성 높음)
- 날짜 및 시간: 기본으로 `timestamptz(6)`를 사용합니다.
    - 타임존 정보를 포함하여 글로벌 대응이 가능하게 하고, 마이크로초(6자) 정밀도를 유지합니다.
    - Entity의 java 타입이 `LocalDateTime`인 경우, `timpestamp`로 지정하고, `time_zone` 컬럼을 추가합니다. 
- 식별자 참조: 외래키(FK)나 Audit 컬럼(`created_by` 등)은 반드시 참조하는 PK와 동일한 타입(`uuid`)으로 선언합니다.

### 4. 제약 조건(Constraints) 및 스키마 구조

- 테이블 레벨 PK 선언: 컬럼 정의 시 `primary key`를 붙이기보다, 하단에 `CONSTRAINT` 구문을 사용하여 이름을 명시합니다.
    - Reason: 복합키 확장성 확보 및 에러 로그 가독성 향상.
- 인덱스 관리:
    - PostgreSQL은 FK에 인덱스를 자동으로 만들지 않으므로, Join이 빈번한 FK 컬럼에는 수동으로 인덱스를 생성합니다.
    - 자주 조회되는 조건(예: `email`, `external_id`)에는 `UNIQUE` 또는 `INDEX`를 명시합니다.
- Audit 컬럼: 모든 테이블에 공통적으로 적용하여 추적 가능성을 확보합니다.
    - `created_at`, `created_by`, `modified_at`, `modified_by`

## 5. 주석

- Table DDL 작성시 반드시 comment 가 포함되어야 한다.

---

### 적용 스크립트 예시

```sql
create table if not exists users (
    -- 1. 식별자 (Internal & External)
    user_id uuid not null,
    external_id uuid not null,
    
    -- 2. 상태 및 기본 정보
    status varchar(30),
    username text not null,
    employee_no text,
    email text not null,
    department_id uuid,
    position text,
    job_role text,
    
    -- 3. 계정 자격 증명
    password text not null,
    last_login_at timestamptz(6),
    failed_login_attempt smallint default 0,
    auth_source varchar(20),
    
    -- 4. Auditing
    created_at timestamptz(6) not null default now(),
    created_by uuid not null,
    modified_at timestamptz(6) not null default now(),
    modified_by uuid not null,

    -- 제약 조건 명명 (콤마 추가 및 중복 정리)
    constraint pk_users primary key (user_id),
    constraint uk_users_external_id unique (external_id),
    constraint uk_users_email unique (email),
    constraint uk_users_username unique (username) -- Login ID 중복 방지 추가
);

-- 인덱스 관련 참고:
-- UNIQUE 제약 조건(uk_users_external_id, uk_users_email)은 
-- PostgreSQL에서 자동으로 해당 컬럼에 인덱스를 생성합니다.
-- 따라서 별도의 CREATE INDEX 문은 자원 낭비이므로 작성하지 않아도 됩니다.

-- (선택) 조회가 빈번한 Auditing 컬럼에 대한 인덱스
create index idx_users_created_at on users(created_at);
```

---