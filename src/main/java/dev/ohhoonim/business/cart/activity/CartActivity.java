package dev.ohhoonim.business.cart.activity;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import dev.ohhoonim.business.cart.model.Cart;
import dev.ohhoonim.business.cart.model.CartBehaviorLog;
import dev.ohhoonim.business.cart.model.CartComponent.Product;

public interface CartActivity {
    Optional<Cart> loadCart(UUID customerId);
    void saveCart(Cart cart);
    void logBehavior(CartBehaviorLog log);
    List<Product> getRecommendedProducts(UUID customerId);
}
