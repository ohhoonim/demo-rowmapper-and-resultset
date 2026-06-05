package dev.ohhoonim.business.cart.model;

import java.util.Objects;
import dev.ohhoonim.business.cart.model.CartComponent.Money;
import dev.ohhoonim.business.cart.model.CartComponent.Product;
import dev.ohhoonim.business.cart.model.CartComponent.SelectedOption;
import dev.ohhoonim.component.model.unit.ValueObject;

@ValueObject
public record CartItem (
    CartItemId id,
    Product product,
    SelectedOption option,
    int quantity
) {
    public CartItem {
        id = Objects.requireNonNull(id, "CartItem ID는 필수입니다.");
        product = Objects.requireNonNull(product, "상품 정보는 필수입니다.");
        option = Objects.requireNonNull(option, "옵션 정보는 필수입니다.");
        validateQuantity(quantity);
    }

    public CartItem addQuantity(int quantity) {
        validateQuantity(quantity);
        return new CartItem(id, product, option, this.quantity + quantity);
    }

    public CartItem changeQuantity(int quantity) {
        validateQuantity(quantity);
        return new CartItem(id, product, option, quantity);
    }

    public Money calculateSubTotal() {
        Money unitPrice = product.basePrice().plus(option.additionalPrice());
        return unitPrice.times(quantity);
    }

    private void validateQuantity(int quantity) {
        if (quantity < 1) {
            throw new IllegalArgumentException("수량은 1개 이상이어야 합니다.");
        }
    }

    public boolean isSameItem(Product product, SelectedOption option) {
        return this.product.id().equals(product.id()) && 
               this.option.optionId().equals(option.optionId());
    }
}
