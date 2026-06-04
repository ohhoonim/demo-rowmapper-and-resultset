package dev.ohhoonim.business.cart.application;

import dev.ohhoonim.business.cart.activity.CartActivity;
import dev.ohhoonim.business.cart.model.*;
import dev.ohhoonim.business.cart.model.CartComponent.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock private CartActivity cartActivity;
    @Mock private DeliveryPolicy deliveryPolicy;

    @InjectMocks private CartService cartService;

    private final UUID CUSTOMER_ID = UUID.randomUUID();
    private final String OPERATOR = "test-user";

    @Test
    @DisplayName("상품을 장바구니에 담을 때 모델 로직을 수행하고 저장 및 로그를 남겨야 한다")
    void add_to_cart_test() {
        // Given
        Product product = new Product(UUID.randomUUID(), "상품", Money.of(10000), "img.png");
        SelectedOption option = new SelectedOption(1L, "옵션", Money.ZERO);
        Cart cart = new Cart(CartId.Creator.generate(), CUSTOMER_ID, OPERATOR);
        
        given(cartActivity.loadCart(CUSTOMER_ID)).willReturn(Optional.of(cart));

        // When
        cartService.addToCart(CUSTOMER_ID, product, option, 2, OPERATOR);

        // Then
        verify(cartActivity).saveCart(any(Cart.class));
        verify(cartActivity).logBehavior(any(CartBehaviorLog.class));
        assertThat(cart.getItems()).hasSize(1);
    }

    @Test
    @DisplayName("장바구니 조회 시 금액 계산 및 추천 상품을 포함해야 한다")
    void get_cart_view_test() {
        // Given
        Cart cart = new Cart(CartId.Creator.generate(), CUSTOMER_ID, OPERATOR);
        given(cartActivity.loadCart(CUSTOMER_ID)).willReturn(Optional.of(cart));
        given(deliveryPolicy.calculateDeliveryFee(any())).willReturn(Money.of(3000));

        // When
        CartResponse response = cartService.getCartView(CUSTOMER_ID);

        // Then
        assertThat(response.customerId()).isEqualTo(CUSTOMER_ID);
        assertThat(response.estimatedAmount().deliveryFee()).isEqualTo(Money.of(3000));
        verify(cartActivity).getRecommendedProducts(CUSTOMER_ID);
    }
}
