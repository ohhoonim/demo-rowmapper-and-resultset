# Domain Factory 작성 가이드

## 1. 개요
Domain Factory는 도메인 애그리거트(AR)를 저장소로부터 복원(Reconstitution)하는 책임을 집니다. 일반적인 팩토리 패턴이 객체 생성에 집중한다면, DDD의 Factory는 저장소(Infrastructure)의 기술적 데이터를 도메인 모델의 순수한 자아로 변환하는 '복원 공정'의 핵심 도구입니다. 특히 유스케이스별로 필요한 데이터 컴포넌트만 선택적으로 조회(Selective Loading)하여 성능을 최적화하고 모델의 정합성을 유지하는 것을 목적으로 합니다.

## 2. 주요 개념 및 구성 요소

### 1) ArFactory Interface
모든 도메인 팩토리는 `ArFactory<A, I, C>`를 상속받아 정의합니다.
- `A`: Aggregate Root 타입
- `I`: Aggregate Root Id 타입
- `C`: Component Marker Interface (선택적 로딩의 단위)

#### 유틸 메서드 
- `composer`: reconsititute 메서드 구현시 ResultSet에서 VO map 으로 변환할 때 사용합니다.
- `dynamicColumns`: resolveRequiredColumns 메서드 구현시 VO 정보로부터 column 명을 추출할 때 사용합니다.

### 2) Component (Value Objects)
애그리거트의 상태를 구성하는 필드들을 논리적인 단위로 묶은 Java Record입니다.
- Marker Interface: `sealed interface` 등을 사용하여 해당 애그리거트에 속한 컴포넌트임을 명시합니다.
- Selective Loading: 유스케이스에 따라 필요한 컴포넌트만 DB에서 조회하여 메모리 사용량과 쿼리 효율을 높입니다.
- `domain-vo-skill.md` 참고

### 3) ArComponentMapper Interface
ResultSet에서 개별 컬럼을 AR 필드로 변환할 때 사용하는 시그니처(functional interface)입니다. 'Factory에서 유틸성 wrap 메서드를 작성하는 법' 항목을 참고하십시오. 


## 3. 구현 표준 공정

- 도메인 계층: `domain-module-skill.md` 참고

### Step 1: Factory Port 정의 (Activity.out Layer)
유스케이스별로 자주 사용되는 컴포넌트 조합을 `default method`로 정의합니다. 필요시 추가 정의하여 사용합니다.

```java
public interface CartArFactory extends ArFactory<Cart, CartId, CartComponent> {

    // 유비쿼터스 언어를 활용한 컴포넌트 조합 선언 
    default List<Class<? extends CartComponent>> forDefault() {
        return List.of();
    }

    // repository에서 mapper 를 사용할 때 보일러플레이트 방지를 위한 Wrapper 메서드 
   public static Function<ResultSet, ? extends CartComponent> wrap(CartArMapper mapper) {
        return rs -> {
            try {
                return mapper.map(rs);
            } catch (SQLException e) {
                throw new CartException("처리할 수 없는 컬럼이 존재합니다.", e);
            }
        };
    } 
}
```

### Step 2: Factory Mapper 정의(Activity.out Layer)

```java
public interface CartArMapper extends ArComponentMapper<CartComponent> {
}
```

### Step 2: Factory Adapter 구현 (Infra.adapter Layer)
기술적 데이터를 도메인 모델로 매핑하는 상세 로직을 구현합니다.

1.  resolveRequiredColumns: 
    - 리플렉션을 사용하여 Record의 필드명을 DB 컬럼명(Snake Case)으로 자동 변환합니다.
    - 기본 컬럼(ID, 공통 속성)에 요청된 컴포넌트의 컬럼들을 병합합니다.
    - 구현시, 기본으로 제공되는 `composer` 메서드를 활용합니다.
2.  reconstitute:
    - `ResultSet` 형태의 로우 데이터를 받아 컴포넌트 객체를 생성합니다.
    - 최종적으로 AR의 정적 팩토리 메서드(`AR.reconstitute`)를 호출하여 객체를 복원합니다.
    - 구현시, 기본으로 제공되는 `dynamicColumns` 메서드를 활용합니다. 

### Step 3: Repository와의 협업 (Infra.dapter Layer)
Repository Adapter는 Factory를 통해 필요한 컬럼을 도출하고 쿼리를 실행합니다. RowMapper를 리턴하는 전용 mapper를 `factory.reconsitute` 메서드를 이용하여 구현하십시오. 

```java
    private final JdbcClient jdbcClient;
    private final CartArFactory factory;

    public Optional<Cart> findCartByCustomerId(UUID customerId) {
        var columns = factory.forDefault();
        var sql = """
                select %s from  tb_cart where customer_id = :customerId
                 """.formatted(factory.resolveRequiredColumns(columns));
        return jdbcClient.sql(sql).param("customerId", customerId)
                .query(cartMapper.apply(factory, columns)).optional();
    }

    private BiFunction<CartArFactory, List<Class<? extends CartComponent>>, RowMapper<Cart>> cartMapper =
            (factory, columns) -> {
                return (rs, _) -> factory
                        .reconsitute(new CartId(rs.getObject("cart_id", UUID.class)), columns, rs);
            };
```

## 4. Domain Factory의 이점
- 기술 격리: DB 테이블 구조나 SQL 컬럼 변경이 도메인 모델(AR)에 직접적인 영향을 주지 않도록 완충 작용을 합니다.
- 성능 최적화: `SELECT *`를 지양하고, 실제 비즈니스 로직 수행에 필요한 데이터만 정교하게 로드합니다.
- 유연성: 동일한 데이터 소스로부터 유스케이스의 맥락(Context)에 따라 서로 다른 형태의 컴포넌트 조합을 가진 모델을 복원할 수 있습니다.

## 5. Factory Adapter 구현 예시

```java
@Component("cartArFactory")
public class CartArFactoryAdapter implements CartArFactory {

    @Override
    public Cart reconsitute(CartId id, List<Class<? extends CartComponent>> requiredVos,
            ResultSet data) throws SQLException {
        Map<String, ? extends CartComponent> vos = composer(requiredVos, registry, data);

        return Cart.reconstitute(
                id, 
                data.getObject("customer_id", UUID.class),
                CartComponent.narrow(vos.get("CartMeta"), CartMeta.class), 
                Collections.emptyList(),
                data.getObject("created_at", Instant.class), data.getString("created_by"),
                data.getObject("modified_at", Instant.class), data.getString("modified_by"));
    }

    private Map<Class<?>, Function<ResultSet, ? extends CartComponent>> registry =
            Map.of(CartMeta.class, wrap(rs -> new CartMeta(rs.getString("tag"))));

    @Override
    public String resolveRequiredColumns(List<Class<? extends CartComponent>> columnTypes) {
        List<String> defaultColumns = List.of("cart_id", "customer_id",
                "created_at", "created_by", "modified_at", "modified_by");

        return Stream.concat(defaultColumns.stream(), dynamicColumns(columnTypes).stream())
                .collect(Collectors.joining(", "));
    }

}
```

## 6. wrap 메서드  

### Factory에서 유틸성 wrap 메서드를 작성하는 법
wrap 메서드는 도메인내 예외 처리를 일관성있게 유지시키고, 보일러플레이트 코드를 줄여줍니다.

```java
   public static Function<ResultSet, ? extends CartComponent> wrap(CartArMapper mapper) {
        return rs -> {
            try {
                return mapper.map(rs);
            } catch (SQLException e) {
                throw new CartException("처리할 수 없는 컬럼이 존재합니다.", e);
            }
        };
    } 
```

### Factory Adapter에서 reconsitute 메소드 구현시, registry map을 구현하는 방법
`factory.wrap` 메서드를 정의한 경우 registry map을 간결하게 유지할 수 있습니다.

```java
private Map<Class<?>, Function<ResultSet, ? extends CartComponent>> registry =
            Map.of(CartMeta.class, wrap(rs -> new CartMeta(rs.getString("tag"))));
```