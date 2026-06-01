package dev.ohhoonim.business.cart.model;

import java.time.Instant;
import java.util.UUID;
import dev.ohhoonim.business.cart.model.CartComponent.BehaviorType;
import dev.ohhoonim.component.model.unit.BaseEntity;

public class CartBehaviorLog extends BaseEntity<CartBehaviorLogId> {
    private final UUID customerId;
    private final BehaviorType type;
    private final UUID productId;

    // 초기 생성용 생성자
    public CartBehaviorLog(CartBehaviorLogId id, UUID customerId, BehaviorType type, UUID productId, String operator) {
        super(id, operator);
        this.customerId = customerId;
        this.type = type;
        this.productId = productId;
    }

    // DB 복원용 생성자
    private CartBehaviorLog(CartBehaviorLogId id, UUID customerId, BehaviorType type, UUID productId,
                            Instant createdAt, String createdBy, Instant modifiedAt, String modifiedBy) {
        super(id, createdAt, createdBy, modifiedAt, modifiedBy);
        this.customerId = customerId;
        this.type = type;
        this.productId = productId;
    }

    public static CartBehaviorLog reconstitute(CartBehaviorLogId id, UUID customerId, BehaviorType type, UUID productId,
                                               Instant createdAt, String createdBy, Instant modifiedAt, String modifiedBy) {
        return new CartBehaviorLog(id, customerId, type, productId, createdAt, createdBy, modifiedAt, modifiedBy);
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public BehaviorType getType() {
        return type;
    }

    public UUID getProductId() {
        return productId;
    }
}
