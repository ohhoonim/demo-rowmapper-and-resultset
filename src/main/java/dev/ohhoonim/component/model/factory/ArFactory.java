package dev.ohhoonim.component.model.factory;

import java.lang.reflect.RecordComponent;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;
import tools.jackson.databind.PropertyNamingStrategies;

/*
 * A: aggregate root I: aggregate root id C: components of ar
 * 
 */

public interface ArFactory<A, I, C> {

    Map<Class<?>, Function<ResultSet, ? extends C>> registry();

    A reconsitute(I id, List<Class<? extends C>> requiredVos, ResultSet rs) throws SQLException;

    /**
     * reconsititute 메서드 구현시 ResultSet에서 VO map 으로 변환할 때 사용.
     * @param requiredVos
     * @param registry
     * @param rs
     * @return
     */
    default Map<String, ? extends C> composer(List<Class<? extends C>> requiredVos,
            Map<Class<?>, Function<ResultSet, ? extends C>> registry, ResultSet rs) {
        return requiredVos.stream().map(registry::get).filter(Objects::nonNull)
                .map(func -> func.apply(rs))
                .collect(Collectors.toMap(vo -> vo.getClass().getSimpleName(), vo -> vo,
                        (existing, replacement) -> existing));
    }

    String resolveRequiredColumns(List<Class<? extends C>> columnTypes);

    /**
     * resolveRequiredColumns 메서드 구현시 VO 정보로부터 column 명을 추출할 때 사용.
     * @param columnTypes
     * @return
     */
    default List<String> dynamicColumns(List<Class<? extends C>> columnTypes) {
        return columnTypes.stream().filter(Class::isRecord)
                .flatMap(type -> Arrays.stream(type.getRecordComponents()))
                .map(RecordComponent::getName)
                .map(name -> PropertyNamingStrategies.SNAKE_CASE.nameForField(null, null, name))
                .toList();
    }
}


// 도메인별 Factory interface 구현 예
/*
public interface CartArFactory extends ArFactory<Cart, CartId, CartComponent> {

    default List<Class<? extends CartComponent>> forDefault() {
        return List.of();
    }

    // mapper 구현시 빠른 예외 발견을 위한 wrapper 유틸. (보일러플레이트 코드 방지 겸용)
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
*/

// Factory Adapter 구현 예
/*

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
*/

// JdbcClient 사용시 RowMapper 활용법
/*
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
*/