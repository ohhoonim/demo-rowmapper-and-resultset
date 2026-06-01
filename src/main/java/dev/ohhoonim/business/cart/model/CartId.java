package dev.ohhoonim.business.cart.model;

import java.util.UUID;
import com.github.f4b6a3.ulid.Ulid;
import com.github.f4b6a3.ulid.UlidCreator;
import dev.ohhoonim.component.model.unit.EntityId;

public record CartId(UUID cartId) implements EntityId<UUID> {
    
    @Override
    public UUID getRawValue() {
        return cartId;
    }

    @Override
    public String getPublicValue() {
        return Ulid.from(cartId()).toString();
    }

    public static Creator<UUID, CartId> Creator = new Creator<>() {
        @Override
        public CartId from(UUID internalId, UUID publicId) {
            throw new CartException("not supported externalId");
        }

        @Override
        public CartId fromPublic(String cartId) {
            return new CartId(Ulid.from(cartId).toUuid());
        }

        @Override
        public CartId generate() {
            return new CartId(UlidCreator.getMonotonicUlid().toUuid());
        }
    };
}
