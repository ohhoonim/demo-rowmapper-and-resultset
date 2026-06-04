package dev.ohhoonim.component.model.factory;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * ResultSet에서 개별 컬럼을 AR 필드로 변환할 때의 시그니처. 

 */
@FunctionalInterface
public interface ArComponentMapper<C> {
   C map(ResultSet rs) throws SQLException;
}

// 도메인별 mapper 선언 예
/*
public interface CartArMapper extends ArComponentMapper<CartComponent> {
}

*/

// Factory에서 유틸성 wrap 메서드를 작성하는 법
/*
public interface CartArFactory extends ArFactory<Cart, CartId, CartComponent> {

    default List<Class<? extends CartComponent>> forDefault() {
        return List.of();
    }

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

// Factory Adapter에서 reconsitute 메소드 구현시, registry map을 구현하는 방법
/*
    private Map<Class<?>, Function<ResultSet, ? extends CartComponent>> registry =
            Map.of(CartMeta.class, wrap(rs -> new CartMeta(rs.getString("tag"))));
*/