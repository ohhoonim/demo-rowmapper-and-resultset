# Domain Factory 작성 가이드

## 1. 개요
Domain Factory는 도메인 애그리거트(AR)를 저장소로부터 복원(Reconstitution)하는 책임을 집니다. 일반적인 팩토리 패턴이 객체 생성에 집중한다면, DDD의 Factory는 저장소(Infrastructure)의 기술적 데이터를 도메인 모델의 순수한 자아로 변환하는 '복원 공정'의 핵심 도구입니다. 특히 유스케이스별로 필요한 데이터 컴포넌트만 선택적으로 조회(Selective Loading)하여 성능을 최적화하고 모델의 정합성을 유지하는 것을 목적으로 합니다.

## 2. 주요 개념 및 구성 요소

### 1) ArFactory Interface
모든 도메인 팩토리는 `ArFactory<A, I, C>`를 상속받아 정의합니다.
- `A`: Aggregate Root 타입
- `I`: Aggregate Root Id 타입
- `C`: Component Marker Interface (선택적 로딩의 단위)

### 2) Component (Value Objects)
애그리거트의 상태를 구성하는 필드들을 논리적인 단위로 묶은 Java Record입니다.
- **Marker Interface**: `sealed interface` 등을 사용하여 해당 애그리거트에 속한 컴포넌트임을 명시합니다.
- **Selective Loading**: 유스케이스에 따라 필요한 컴포넌트만 DB에서 조회하여 메모리 사용량과 쿼리 효율을 높입니다.

## 3. 구현 표준 공정

### Step 1: Factory Port 정의 (Activity Layer)
유스케이스별로 자주 사용되는 컴포넌트 조합을 `default method`로 정의합니다.
```java
public interface PostArFactory extends ArFactory<Post, PostId, PostComponent> {
    // 유스케이스별 필요한 컴포넌트 정의
    default List<Class<? extends PostComponent>> forOwner() {
        return List.of(PostMeta.class); // 작성자 전용 화면에는 메타 정보 포함
    }
}
```

### Step 2: Factory Adapter 구현 (Infrastructure Layer)
기술적 데이터를 도메인 모델로 매핑하는 상세 로직을 구현합니다.

1.  **resolveRequiredColumns**: 
    - 리플렉션을 사용하여 Record의 필드명을 DB 컬럼명(Snake Case)으로 자동 변환합니다.
    - 기본 컬럼(ID, 공통 속성)에 요청된 컴포넌트의 컬럼들을 병합합니다.
2.  **reconstitute**:
    - `Map<String, Object>` 형태의 로우 데이터를 받아 컴포넌트 객체를 생성합니다.
    - 최종적으로 AR의 정적 팩토리 메서드(`AR.reconstitute`)를 호출하여 객체를 복원합니다.

### Step 3: Repository와의 협업 (Adapter Layer)
저장소 어댑터는 Factory를 통해 필요한 컬럼을 도출하고 쿼리를 실행합니다.
```java
public List<Map<String, Object>> findAll(List<Class<? extends PostComponent>> components) {
    List<String> columns = postArFactory.resolveRequiredColumns(components);
    String sql = "SELECT " + String.join(", ", columns) + " FROM tb_post";
    return jdbcClient.sql(sql).query().list();
}
```

## 4. Domain Factory의 이점
- **기술 격리**: DB 테이블 구조나 SQL 컬럼 변경이 도메인 모델(AR)에 직접적인 영향을 주지 않도록 완충 작용을 합니다.
- **성능 최적화**: `SELECT *`를 지양하고, 실제 비즈니스 로직 수행에 필요한 데이터만 정교하게 로드합니다.
- **유연성**: 동일한 데이터 소스로부터 유스케이스의 맥락(Context)에 따라 서로 다른 형태의 컴포넌트 조합을 가진 모델을 복원할 수 있습니다.

## 5. Factory Adapter 구현 예시

```java

@Component
public class PostFactoryAdapter implements PostArFactory {

    @Override
    public Post reconsitute(PostId id, List<Class<? extends PostComponent>> requiredVos,
            Map<String, Object> data) {

        Map<String, ? extends PostComponent> vos = requiredVos.stream().map(registry::get)
                .filter(Objects::nonNull).map(func -> func.apply(data))
                .collect(Collectors.toMap(
                    vo -> vo.getClass().getSimpleName(),
                    vo -> vo,
                    (existing, replacement) -> existing
                ));

        return Post.reconsitute(id, 
                PostStatus.valueOf(data.get("status").toString()),
                data.get("title").toString(), 
                data.get("contents").toString(),
                (PostMeta) vos.get("PostMeta"),
                Timestamp.valueOf(data.get("created_at").toString()).toInstant(),
                data.get("created_by").toString(),
                Timestamp.valueOf(data.get("modified_at").toString()).toInstant(),
                data.get("modified_by").toString());
    }

    private Map<Class<?>, Function<Map<String, Object>, PostComponent>> registry =
            Map.of(PostMeta.class, d -> new PostMeta(String.valueOf(d.get("tags")),
                    String.valueOf(d.get("permanent_link"))));

    @Override
    public List<String> resolveRequiredColumns(List<Class<? extends PostComponent>> columnTypes) {
        List<String> columns = new ArrayList<>();
        columns.addAll(List.of("post_id", "status", "title", "contents", "created_at", "created_by", "modified_at",
                "modified_by"));

        columnTypes.stream().forEach(type -> {
            var fields = Arrays.asList(type.getRecordComponents()).stream()
                    .map(RecordComponent::getName)
                    .map(s -> PropertyNamingStrategies.SNAKE_CASE.nameForField(null, null, s))
                    .toList();
            columns.addAll(fields);
        });

        return Collections.unmodifiableList(columns);
    }
}
```