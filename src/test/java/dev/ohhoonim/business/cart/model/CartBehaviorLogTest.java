package dev.ohhoonim.business.cart.model;

import static org.assertj.core.api.Assertions.assertThat;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import dev.ohhoonim.business.cart.model.CartComponent.BehaviorType;

class CartBehaviorLogTest {

    @Test
    @DisplayName("장바구니 행동 로그가 올바르게 생성 및 복원되어야 한다")
    void behavior_log_test() {
        // Given
        CartBehaviorLogId logId = new CartBehaviorLogId(1L); // generated always as identity
        UUID customerId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        String operator = "test-user";

        // When: 초기 생성
        CartBehaviorLog log = new CartBehaviorLog(logId, customerId, BehaviorType.ADD, productId, operator);

        // Then
        assertThat(log.getId()).isEqualTo(logId);
        assertThat(log.getCustomerId()).isEqualTo(customerId);
        assertThat(log.getType()).isEqualTo(BehaviorType.ADD);
        assertThat(log.getProductId()).isEqualTo(productId);
        assertThat(log.getCreatedBy()).isEqualTo(operator);

        // When: 복원
        Instant now = Instant.now();
        CartBehaviorLog reconstituted = CartBehaviorLog.reconstitute(
                logId, customerId, BehaviorType.REMOVE, productId,
                now, operator, now, operator
        );

        // Then
        assertThat(reconstituted.getType()).isEqualTo(BehaviorType.REMOVE);
        assertThat(reconstituted.getCreatedAt()).isEqualTo(now);
    }
}
