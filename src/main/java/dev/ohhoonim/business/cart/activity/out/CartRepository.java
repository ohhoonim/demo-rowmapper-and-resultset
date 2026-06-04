package dev.ohhoonim.business.cart.activity.out;

import java.util.Optional;
import java.util.UUID;
import dev.ohhoonim.business.cart.model.Cart;

public interface CartRepository {

    Optional<Cart> findCartByCustomerId(UUID customerId);

    void save(Cart cart);

}
