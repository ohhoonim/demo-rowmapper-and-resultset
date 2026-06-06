package dev.ohhoonim.business.cart.activity.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import dev.ohhoonim.business.cart.model.Cart;
import dev.ohhoonim.business.cart.model.CartItem;

public interface CartRepository {

    Optional<Cart> findCartByCustomerId(UUID customerId);

    void save(Cart cart);

    List<CartItem> findItemsByCartId(UUID cartId);

}
