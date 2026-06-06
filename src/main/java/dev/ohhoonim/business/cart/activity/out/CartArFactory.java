package dev.ohhoonim.business.cart.activity.out;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.function.Function;
import dev.ohhoonim.business.cart.model.Cart;
import dev.ohhoonim.business.cart.model.CartComponent;
import dev.ohhoonim.business.cart.model.CartException;
import dev.ohhoonim.business.cart.model.CartId;
import dev.ohhoonim.business.cart.model.CartComponent.Product;
import dev.ohhoonim.business.cart.model.CartComponent.SelectedOption;
import dev.ohhoonim.component.model.factory.ArFactory;
import dev.ohhoonim.component.model.unit.Factory;

@Factory
public interface CartArFactory extends ArFactory<Cart, CartId, CartComponent> {

    default List<Class<? extends CartComponent>> forDefault() {
        return List.of();
    }

    default List<Class<? extends CartComponent>> forItem() {
        return List.of(Product.class, SelectedOption.class);
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
