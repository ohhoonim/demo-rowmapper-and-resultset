package dev.ohhoonim.business.cart.activity.out;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import dev.ohhoonim.business.cart.model.Cart;

public interface CartRepository {

    Optional<Map<String, Object>> findCartByCustomerId(UUID customerId);

    void save(Cart cart);

}
