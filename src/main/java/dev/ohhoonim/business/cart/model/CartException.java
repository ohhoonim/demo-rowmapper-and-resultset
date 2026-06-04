package dev.ohhoonim.business.cart.model;

import dev.ohhoonim.component.model.unit.DomainException;

@DomainException
public class CartException extends RuntimeException {
    
    public CartException(String message) {
        super(message);
    }

    public CartException(String message, Throwable cause) {
        super(message, cause);
    }
}
