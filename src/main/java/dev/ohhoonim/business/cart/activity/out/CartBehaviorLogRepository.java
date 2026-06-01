package dev.ohhoonim.business.cart.activity.out;

import dev.ohhoonim.business.cart.model.CartBehaviorLog;

public interface CartBehaviorLogRepository {

    void log(CartBehaviorLog log);

}
