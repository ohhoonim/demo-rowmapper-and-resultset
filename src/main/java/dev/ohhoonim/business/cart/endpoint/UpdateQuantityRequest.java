package dev.ohhoonim.business.cart.endpoint;

import dev.ohhoonim.component.model.payload.Dto;

@Dto
public record UpdateQuantityRequest(
    String customerId,
    int quantity
) {}
