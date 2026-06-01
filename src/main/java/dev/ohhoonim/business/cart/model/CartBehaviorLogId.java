package dev.ohhoonim.business.cart.model;

import dev.ohhoonim.component.model.unit.EntityId;

public record CartBehaviorLogId(Long internalId) implements EntityId<Long> {
    @Override
    public Long getRawValue() {
        return internalId;
    }

    @Override
    public String getPublicValue() {
        return String.valueOf(internalId());
    }

    public static class Creator implements EntityId.Creator<Long, CartBehaviorLogId> {
        @Override
        public CartBehaviorLogId from(Long internalId, Long publicId) {
            throw new CartException("not supported externalId");
        }

        @Override
        public CartBehaviorLogId fromPublic(String publicId) {
            return new CartBehaviorLogId(Long.valueOf(publicId));
        }

        @Override
        public CartBehaviorLogId generate() {
            // throw new CartException("not supported, generated always as identity ");
            return new CartBehaviorLogId(1L);
        }
    }
}
