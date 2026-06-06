package dev.ohhoonim.business.cart.infra.adapter;

import static dev.ohhoonim.business.cart.activity.out.CartArFactory.wrap;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
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
import dev.ohhoonim.business.cart.model.CartComponent.Money;
import dev.ohhoonim.business.cart.model.CartComponent.Product;
import dev.ohhoonim.business.cart.model.CartComponent.SelectedOption;
import dev.ohhoonim.business.cart.model.CartId;
import dev.ohhoonim.component.model.unit.Factory;

@Factory
@Component("cartArFactory")
public class CartArFactoryAdapter implements CartArFactory {

    private final Map<Class<?>, Function<ResultSet, ? extends CartComponent>> registry =
            Map.of(CartMeta.class, wrap(rs -> new CartMeta(rs.getString("tag"))), Product.class,
                    wrap(rs -> {
                        return new Product(rs.getObject("product_id", UUID.class),
                                rs.getString("product_name"),
                                new Money(rs.getObject("product_base_price", BigDecimal.class)),
                                rs.getString("product_image_url"));
                    }), SelectedOption.class, wrap(rs -> {
                        return new SelectedOption(rs.getObject("option_id", Long.class),
                                rs.getString("option_name"), new Money(
                                        rs.getObject("option_additional_price", BigDecimal.class)));
                    }));


    @Override
    public Cart reconsitute(CartId id, List<Class<? extends CartComponent>> requiredVos,
            ResultSet data) throws SQLException {
        Map<String, ? extends CartComponent> vos = composer(requiredVos, registry(), data);

        return Cart.reconstitute(id, data.getObject("customer_id", UUID.class),
                CartComponent.narrow(vos.get("CartMeta"), CartMeta.class), Collections.emptyList(),
                data.getObject("created_at", Instant.class), data.getString("created_by"),
                data.getObject("modified_at", Instant.class), data.getString("modified_by"));
    }

    @Override
    public Map<Class<?>, Function<ResultSet, ? extends CartComponent>> registry() {
        return registry;
    }

    @Override
    public String resolveRequiredColumns(List<Class<? extends CartComponent>> columnTypes) {
        List<String> defaultColumns = new ArrayList<>();
        if (columnTypes.containsAll(forItem())) {
            defaultColumns.addAll(List.of("cart_item_id", "cart_id", "quantity"));
        } else {
            defaultColumns.addAll(List.of("cart_id", "customer_id", "created_at", "created_by",
                    "modified_at", "modified_by"));
        }

        return Stream.concat(defaultColumns.stream(), dynamicColumns(columnTypes).stream())
                .collect(Collectors.joining(", "));
    }

}
