package dev.ohhoonim.business.cart.infra.adapter;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.simple.JdbcClient;
import dev.ohhoonim.business.cart.activity.out.CartBehaviorLogRepository;
import dev.ohhoonim.business.cart.activity.out.CartRepository;
import dev.ohhoonim.business.cart.model.Cart;
import dev.ohhoonim.business.cart.model.CartBehaviorLog;
import dev.ohhoonim.component.model.unit.Adapter;

@Adapter
public class CartAdapter implements CartRepository, CartBehaviorLogRepository {

    private final JdbcClient jdbcClient;

    public CartAdapter(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    @Override
    public Optional<Map<String, Object>> findCartByCustomerId(UUID customerId) {
        var sql = """
               select cart_id from  tb_cart where customer_id = :customerId
                """;
        
       return jdbcClient.sql(sql).param("customerId", customerId).query(new ColumnMapRowMapper()).optional(); 
    }

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
