package dev.ohhoonim.business.cart.model;

public class CartException extends RuntimeException {
    
    public CartException(String message) {
        super(message);
    }

    public CartException(String message, Throwable cause) {
        super(message, cause);
    }
}
