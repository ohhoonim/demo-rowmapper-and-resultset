package dev.ohhoonim.business.cart.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import java.math.BigDecimal;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import dev.ohhoonim.business.cart.model.CartComponent.Money;
import dev.ohhoonim.business.cart.model.CartComponent.OrderEstimatedAmount;
import dev.ohhoonim.business.cart.model.CartComponent.Product;
import dev.ohhoonim.business.cart.model.CartComponent.SelectedOption;

class CartTest {

    private final String OPERATOR = "test-user";
    private final UUID CUSTOMER_ID = UUID.randomUUID();

    @Test
    @DisplayName("장바구니에 상품을 새로 담거나 이미 있는 경우 수량을 합쳐야 한다")
    void add_product_test() {
        // Given
        Cart cart = new Cart(CartId.Creator.generate(), CUSTOMER_ID, OPERATOR);
        Product product = new Product(UUID.randomUUID(), "테스트 상품", Money.of(10000), "img.png");
        SelectedOption option = new SelectedOption(1L, "블랙/XL", Money.of(1000));

        // When: 신규 담기
        cart.addProduct(product, option, 2, OPERATOR);

        // Then
        assertThat(cart.getItems()).hasSize(1);
        assertThat(cart.getItems().get(0).quantity()).isEqualTo(2);

        // When: 동일 상품/옵션 추가 담기
        cart.addProduct(product, option, 3, OPERATOR);

        // Then: 수량 합산
        assertThat(cart.getItems()).hasSize(1);
        assertThat(cart.getItems().get(0).quantity()).isEqualTo(5);
    }

    @Test
    @DisplayName("장바구니 항목의 수량을 변경할 수 있어야 한다")
    void change_quantity_test() {
        // Given
        Cart cart = new Cart(CartId.Creator.generate(), CUSTOMER_ID, OPERATOR);
        Product product = new Product(UUID.randomUUID(), "테스트 상품", Money.of(10000), "img.png");
        SelectedOption option = new SelectedOption(1L, "블랙/XL", Money.of(1000));
        cart.addProduct(product, option, 2, OPERATOR);
        CartItemId itemId = cart.getItems().get(0).id();

        // When
        cart.changeItemQuantity(itemId, 5, OPERATOR);

        // Then
        assertThat(cart.getItems().get(0).quantity()).isEqualTo(5);
    }

    @Test
    @DisplayName("수량은 1개 미만으로 변경할 수 없다")
    void change_quantity_validation_test() {
        // Given
        Cart cart = new Cart(CartId.Creator.generate(), CUSTOMER_ID, OPERATOR);
        Product product = new Product(UUID.randomUUID(), "테스트 상품", Money.of(10000), "img.png");
        SelectedOption option = new SelectedOption(1L, "블랙/XL", Money.of(1000));
        cart.addProduct(product, option, 2, OPERATOR);
        CartItemId itemId = cart.getItems().get(0).id();

        // When & Then
        assertThatThrownBy(() -> cart.changeItemQuantity(itemId, 0, OPERATOR))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("수량은 1개 이상이어야 합니다");
    }

    @Test
    @DisplayName("장바구니에서 특정 항목을 삭제하거나 전체를 비울 수 있어야 한다")
    void remove_item_test() {
        // Given
        Cart cart = new Cart(CartId.Creator.generate(), CUSTOMER_ID, OPERATOR);
        Product p1 = new Product(UUID.randomUUID(), "상품1", Money.of(10000), "img1.png");
        Product p2 = new Product(UUID.randomUUID(), "상품2", Money.of(20000), "img2.png");
        SelectedOption opt = new SelectedOption(1L, "기본", Money.ZERO);
        
        cart.addProduct(p1, opt, 1, OPERATOR);
        cart.addProduct(p2, opt, 1, OPERATOR);
        CartItemId p1ItemId = cart.getItems().get(0).id();

        // When: 개별 삭제
        cart.removeItem(p1ItemId, OPERATOR);
        assertThat(cart.getItems()).hasSize(1);

        // When: 전체 비우기
        cart.clear(OPERATOR);
        assertThat(cart.getItems()).isEmpty();
    }

    @Test
    @DisplayName("예상 결제 금액이 정확하게 계산되어야 한다")
    void calculate_amount_test() {
        // Given
        Cart cart = new Cart(CartId.Creator.generate(), CUSTOMER_ID, OPERATOR);
        Product product = new Product(UUID.randomUUID(), "상품", Money.of(10000), "img.png");
        SelectedOption option = new SelectedOption(1L, "옵션", Money.of(2000));
        cart.addProduct(product, option, 2, OPERATOR); // (10000 + 2000) * 2 = 24000

        // 배송비 정책: 3만원 미만 3000원, 이상 무료
        DeliveryPolicy deliveryPolicy = (total) -> 
            total.amount().compareTo(new BigDecimal("30000")) < 0 ? Money.of(3000) : Money.ZERO;

        // When
        OrderEstimatedAmount estimatedAmount = cart.calculateEstimatedAmount(deliveryPolicy);

        // Then
        assertThat(estimatedAmount.totalProductPrice()).isEqualTo(Money.of(24000));
        assertThat(estimatedAmount.deliveryFee()).isEqualTo(Money.of(3000));
        assertThat(estimatedAmount.finalPaymentAmount()).isEqualTo(Money.of(27000));
    }

    @Test
    @DisplayName("Money 객체는 산술 연산 및 유효성 검증을 수행해야 한다")
    void money_test() {
        Money m1 = Money.of(1000);
        Money m2 = Money.of(500);

        assertThat(m1.plus(m2)).isEqualTo(Money.of(1500));
        assertThat(m1.minus(m2)).isEqualTo(Money.of(500));
        assertThat(m2.times(3)).isEqualTo(Money.of(1500));

        assertThatThrownBy(() -> new Money(new BigDecimal("-1")))
                .isInstanceOf(IllegalArgumentException.class);
    }
}
