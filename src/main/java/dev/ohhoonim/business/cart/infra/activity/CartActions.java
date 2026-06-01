package dev.ohhoonim.business.cart.infra.activity;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import dev.ohhoonim.business.cart.activity.CartActivity;
import dev.ohhoonim.business.cart.activity.out.CartBehaviorLogRepository;
import dev.ohhoonim.business.cart.activity.out.CartRepository;
import dev.ohhoonim.business.cart.model.Cart;
import dev.ohhoonim.business.cart.model.CartBehaviorLog;
import dev.ohhoonim.business.cart.model.CartComponent.Product;
import dev.ohhoonim.business.cart.model.CartId;
import dev.ohhoonim.component.model.unit.Activity;

@Activity
public class CartActions implements CartActivity {

    private final CartRepository cartRepository;
    private final CartBehaviorLogRepository cartBehaviorRepository;

    public CartActions(CartRepository cartRepository,
            CartBehaviorLogRepository cartBehaviorLogRepository) {
        this.cartRepository = cartRepository;
        this.cartBehaviorRepository = cartBehaviorLogRepository;
    }

    @Override
    public Optional<Cart> loadCart(UUID customerId) {
        var resultMap = cartRepository.findCartByCustomerId(customerId);

        return resultMap.map(cart -> Cart.reconstitute(null, customerId, null, null, null, null, null)) ;
    }

    @Override
    public void saveCart(Cart cart) {
        cartRepository.save(cart);
    }

    @Override
    public void logBehavior(CartBehaviorLog log) {
        cartBehaviorRepository.log(log);
    }

    @Override
    public List<Product> getRecommendedProducts(UUID customerId) {
        // TODO 상품 추천 엔진 연결하기
        return Collections.emptyList();
    }

}
