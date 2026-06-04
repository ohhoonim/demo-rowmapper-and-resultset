package dev.ohhoonim.business.cart.application;

import java.util.List;
import java.util.UUID;
import dev.ohhoonim.business.cart.model.CartComponent.Money;
import dev.ohhoonim.business.cart.model.CartComponent.OrderEstimatedAmount;
import dev.ohhoonim.business.cart.model.CartComponent.Product;
import dev.ohhoonim.business.cart.model.CartComponent.SelectedOption;
import dev.ohhoonim.business.cart.model.CartItemId;
import dev.ohhoonim.component.model.payload.Dto;

@Dto
public record CartResponse(
    UUID customerId,
    List<CartItemResponse> items,
    OrderEstimatedAmount estimatedAmount,
    List<Product> recommendedProducts
) {
    @Dto
    public record CartItemResponse(
        CartItemId itemId,
        Product product,
        SelectedOption option,
        int quantity,
        Money subTotal
    ) {}
}
