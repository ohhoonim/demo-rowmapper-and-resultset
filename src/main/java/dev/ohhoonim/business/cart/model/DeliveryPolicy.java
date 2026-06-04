package dev.ohhoonim.business.cart.model;

import dev.ohhoonim.business.cart.model.CartComponent.Money;

public interface DeliveryPolicy {
    Money calculateDeliveryFee(Money totalProductPrice);
}
