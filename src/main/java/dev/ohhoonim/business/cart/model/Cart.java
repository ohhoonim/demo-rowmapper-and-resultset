package dev.ohhoonim.business.cart.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;
import org.jspecify.annotations.Nullable;
import dev.ohhoonim.business.cart.model.CartComponent.CartMeta;
import dev.ohhoonim.business.cart.model.CartComponent.Money;
import dev.ohhoonim.business.cart.model.CartComponent.OrderEstimatedAmount;
import dev.ohhoonim.business.cart.model.CartComponent.Product;
import dev.ohhoonim.business.cart.model.CartComponent.SelectedOption;
import dev.ohhoonim.component.model.unit.BaseEntity;

public class Cart extends BaseEntity<CartId> {
    private UUID customerId;
    private CartMeta meta;
    private List<CartItem> items;

    // 초기 생성용 생성자
    public Cart(CartId id, UUID customerId, String operator) {
        super(id, operator);
        this.customerId = customerId;
        this.items = new ArrayList<>();
    }

    // DB 복원용 생성자
    private Cart(CartId id, UUID customerId, @Nullable CartMeta meta, @Nullable List<CartItem> items,
            Instant createdAt, String createdBy, Instant modifiedAt, String modifiedBy) {
        super(id, createdAt, createdBy, modifiedAt, modifiedBy);
        this.customerId = customerId;
        this.meta = meta;
        this.items = (items != null) ? new ArrayList<>(items) : new ArrayList<>();
    }

    public static Cart reconstitute(CartId id, UUID customerId, @Nullable CartMeta meta, @Nullable List<CartItem> items,
            Instant createdAt, String createdBy, Instant modifiedAt, String modifiedBy) {
        return new Cart(id, customerId, meta, items, createdAt, createdBy, modifiedAt, modifiedBy);
    }

    public List<CartItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public void addProduct(Product product, SelectedOption option, int quantity, String operator) {
        int index = IntStream.range(0, items.size())
                .filter(i -> items.get(i).isSameItem(product, option)).findFirst().orElse(-1);

        if (index != -1) {
            items.set(index, items.get(index).addQuantity(quantity));
        } else {
            items.add(new CartItem(CartItemId.Creator.generate(), product, option, quantity));
        }
        recordModification(operator);
    }

    public void changeItemQuantity(CartItemId itemId, int quantity, String operator) {
        int index= IntStream.range(0, items.size()).filter(i -> items.get(i).id().equals(itemId))
                .findFirst().orElseThrow(() -> new CartException(
                        "장바구니 항목을 찾을 수 없습니다: " + itemId.getPublicValue()));
        if (index > -1) {
            items.set(index, items.get(index).changeQuantity(quantity));

        }
        recordModification(operator);
    }

    public void removeItem(CartItemId itemId, String operator) {
        items.removeIf(item -> item.id().equals(itemId));
        recordModification(operator);
    }

    public void clear(String operator) {
        items.clear();
        recordModification(operator);
    }

    public OrderEstimatedAmount calculateEstimatedAmount(DeliveryPolicy deliveryPolicy) {
        Money totalProductPrice =
                items.stream().map(CartItem::calculateSubTotal).reduce(Money.ZERO, Money::plus);

        // TODO: 할인 정책(DiscountPolicy)이 요구사항에 명확히 정의되면 추가 가능
        Money totalDiscountPrice = Money.ZERO;

        Money deliveryFee = deliveryPolicy.calculateDeliveryFee(totalProductPrice);

        Money finalPaymentAmount = totalProductPrice.minus(totalDiscountPrice).plus(deliveryFee);

        return new OrderEstimatedAmount(totalProductPrice, totalDiscountPrice, deliveryFee,
                finalPaymentAmount);
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public CartMeta getMeta() {
        return meta;
    }
}
