package dev.ohhoonim.business.cart.endpoint;

import dev.ohhoonim.business.cart.model.CartComponent.Product;
import dev.ohhoonim.business.cart.model.CartComponent.SelectedOption;
import dev.ohhoonim.component.model.payload.Dto;

@Dto
public record AddToCartRequest(
    String customerId,
    Product product,
    SelectedOption option,
    int quantity
) {}
