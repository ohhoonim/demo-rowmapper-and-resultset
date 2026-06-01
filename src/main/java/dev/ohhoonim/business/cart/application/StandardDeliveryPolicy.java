package dev.ohhoonim.business.cart.application;

import dev.ohhoonim.business.cart.model.CartComponent.Money;
import dev.ohhoonim.business.cart.model.DeliveryPolicy;
import dev.ohhoonim.component.model.unit.Policy;

@Policy
public class StandardDeliveryPolicy implements DeliveryPolicy {

    private static final Money THRESHOLD = Money.of(50000);
    private static final Money DELIVERY_FEE = Money.of(3000);

    @Override
    public Money calculateDeliveryFee(Money totalProductPrice) {
        if (totalProductPrice.amount().compareTo(THRESHOLD.amount()) >= 0) {
            return Money.ZERO;
        }
        return DELIVERY_FEE;
    }
}
