package dev.ohhoonim.business.cart.model;

import java.util.UUID;
import com.github.f4b6a3.ulid.Ulid;
import com.github.f4b6a3.ulid.UlidCreator;
import dev.ohhoonim.component.model.unit.EntityId;

public record CartItemId(UUID cartItemId) implements EntityId<UUID> {
    @Override
    public UUID getRawValue() {
        return cartItemId;
    }

    @Override
    public String getPublicValue() {
        return Ulid.from(cartItemId()).toString();
    }

    public static Creator<UUID, CartItemId> Creator = new Creator<>() {

        @Override
        public CartItemId from(UUID internalId, UUID publicId) {
            throw new CartException("외부 ID를 지원하지 않습니다.");
        }

        @Override
        public CartItemId fromPublic(String cartItemId) {
            return new CartItemId(Ulid.from(cartItemId).toUuid());
        }

        @Override
        public CartItemId generate() {
            return new CartItemId(UlidCreator.getMonotonicUlid().toUuid());
        }
    };
}
