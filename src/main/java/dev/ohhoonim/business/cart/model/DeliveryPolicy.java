package dev.ohhoonim.business.cart.model;

import static dev.ohhoonim.business.cart.model.CartComponent.*;
import dev.ohhoonim.component.model.unit.Policy;

public interface DeliveryPolicy {
    Money calculateDeliveryFee(Money totalProductPrice);
}
