package dev.ohhoonim.business.cart.application;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static dev.ohhoonim.business.cart.model.CartComponent.*;
import static org.assertj.core.api.Assertions.assertThat;

class StandardDeliveryPolicyTest {

    private final StandardDeliveryPolicy policy = new StandardDeliveryPolicy();

    @Test
    @DisplayName("5만원 미만인 경우 배송비 3000원이 부과되어야 한다")
    void below_threshold_test() {
        Money total = Money.of(49900);
        Money fee = policy.calculateDeliveryFee(total);
        assertThat(fee).isEqualTo(Money.of(3000));
    }

    @Test
    @DisplayName("5만원 이상인 경우 배송비가 무료여야 한다")
    void above_threshold_test() {
        Money total = Money.of(50000);
        Money fee = policy.calculateDeliveryFee(total);
        assertThat(fee).isEqualTo(Money.ZERO);
    }
}
