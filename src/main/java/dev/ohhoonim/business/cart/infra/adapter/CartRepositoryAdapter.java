package dev.ohhoonim.business.cart.infra.adapter;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import dev.ohhoonim.business.cart.activity.out.CartArFactory;
import dev.ohhoonim.business.cart.activity.out.CartBehaviorLogRepository;
import dev.ohhoonim.business.cart.activity.out.CartRepository;
import dev.ohhoonim.business.cart.model.Cart;
import dev.ohhoonim.business.cart.model.CartBehaviorLog;
import dev.ohhoonim.business.cart.model.CartComponent;
import dev.ohhoonim.business.cart.model.CartId;
import dev.ohhoonim.component.model.unit.Adapter;

@Adapter
public class CartRepositoryAdapter implements CartRepository, CartBehaviorLogRepository {

    private final JdbcClient jdbcClient;
    private final CartArFactory factory;

public CartRepositoryAdapter(JdbcClient jdbcClient, @Qualifier("cartArFactory") CartArFactory factory) {
        this.jdbcClient = jdbcClient;
        this.factory = factory;
    }

    @Override
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

    @Override
    public void log(CartBehaviorLog log) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'log'");
    }

    @Override
    public void save(Cart cart) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'save'");
    }

}
