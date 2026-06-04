package dev.ohhoonim.business.cart.infra.adapter;

import static dev.ohhoonim.business.cart.activity.out.CartArFactory.wrap;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.stereotype.Component;
import dev.ohhoonim.business.cart.activity.out.CartArFactory;
import dev.ohhoonim.business.cart.model.Cart;
import dev.ohhoonim.business.cart.model.CartComponent;
import dev.ohhoonim.business.cart.model.CartComponent.CartMeta;
import dev.ohhoonim.business.cart.model.CartId;
import dev.ohhoonim.component.model.unit.Factory;

@Factory
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
