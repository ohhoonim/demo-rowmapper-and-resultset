package dev.ohhoonim.business.cart.application;

import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import dev.ohhoonim.business.cart.activity.CartActivity;
import dev.ohhoonim.business.cart.model.Cart;
import dev.ohhoonim.business.cart.model.CartBehaviorLog;
import dev.ohhoonim.business.cart.model.CartBehaviorLogId;
import dev.ohhoonim.business.cart.model.CartComponent.BehaviorType;
import dev.ohhoonim.business.cart.model.CartComponent.Product;
import dev.ohhoonim.business.cart.model.CartComponent.SelectedOption;
import dev.ohhoonim.business.cart.model.CartId;
import dev.ohhoonim.business.cart.model.CartItemId;
import dev.ohhoonim.business.cart.model.DeliveryPolicy;

@Service
public class CartService {

    private final CartActivity cartActivity;
    private final DeliveryPolicy deliveryPolicy;

    public CartService(CartActivity cartActivity, DeliveryPolicy deliveryPolicy) {
        this.cartActivity = cartActivity;
        this.deliveryPolicy = deliveryPolicy;
    }

    @Transactional
    public void addToCart(UUID customerId, Product product, SelectedOption option, int quantity, String operator) {

        Cart cart = cartActivity.loadCart(customerId)
                .orElseGet(() -> new Cart(CartId.Creator.generate(), customerId, operator));
        cart.addProduct(product, option, quantity, operator);
        cartActivity.saveCart(cart);

        CartBehaviorLog log = new CartBehaviorLog(
                CartBehaviorLogId.Creator.generate(),
                customerId,
                BehaviorType.ADD,
                product.productId(),
                operator
        );
        cartActivity.logBehavior(log);
    }

    @Transactional
    public void updateQuantity(UUID customerId, String publicItemId, int quantity, String operator) {
        Cart cart = getCart(customerId);
        CartItemId itemId = CartItemId.Creator.fromPublic(publicItemId);

        cart.changeItemQuantity(itemId, quantity, operator);
        cartActivity.saveCart(cart);

        // Log behavior for first item changed (simplified for sample)
        cart.getItems().stream()
                .filter(item -> item.id().getPublicValue().equals(publicItemId))
                .findFirst()
                .ifPresent(item -> {
                    CartBehaviorLog log = new CartBehaviorLog(
                            CartBehaviorLogId.Creator.generate(),
                            customerId,
                            BehaviorType.QUANTITY_CHANGE,
                            item.product().productId(),
                            operator
                    );
                    cartActivity.logBehavior(log);
                });
    }

    @Transactional
    public void removeFromCart(UUID customerId, String publicItemId, String operator) {
        Cart cart = getCart(customerId);
        CartItemId itemId = CartItemId.Creator.fromPublic(publicItemId);

        // Find product ID for logging before removal
        UUID productId = cart.getItems().stream()
                .filter(item -> item.id().getPublicValue().equals(publicItemId))
                .map(item -> item.product().productId())
                .findFirst()
                .orElse(null);

        cart.removeItem(itemId, operator);
        cartActivity.saveCart(cart);

        if (productId != null) {
            CartBehaviorLog log = new CartBehaviorLog(
                    CartBehaviorLogId.Creator.generate(),
                    customerId,
                    BehaviorType.REMOVE,
                    productId,
                    operator
            );
            cartActivity.logBehavior(log);
        }
    }

    @Transactional
    public void clearCart(UUID customerId, String operator) {
        Cart cart = getCart(customerId);
        cart.clear(operator);
        cartActivity.saveCart(cart);
    }

    @Transactional(readOnly = true)
    public CartResponse getCartView(UUID customerId) {
        Cart cart = getCart(customerId);
        List<Product> recommendations = cartActivity.getRecommendedProducts(customerId);

        List<CartResponse.CartItemResponse> itemResponses = cart.getItems().stream()
                .map(item -> new CartResponse.CartItemResponse(
                        item.id(),
                        item.product(),
                        item.option(),
                        item.quantity(),
                        item.calculateSubTotal()))
                .toList();

        return new CartResponse(
                customerId,
                itemResponses,
                cart.calculateEstimatedAmount(deliveryPolicy),
                recommendations
        );
    }

    private Cart getCart(UUID customerId) {
        return cartActivity.loadCart(customerId)
                .orElseThrow(() -> new IllegalArgumentException("장바구니가 존재하지 않습니다. 고객 ID: " + customerId));
    }
}
