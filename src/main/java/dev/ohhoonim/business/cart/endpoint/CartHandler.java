package dev.ohhoonim.business.cart.endpoint;

import java.io.IOException;
import java.util.UUID;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.function.ServerRequest;
import org.springframework.web.servlet.function.ServerResponse;
import dev.ohhoonim.business.cart.application.CartService;
import dev.ohhoonim.business.cart.model.CartException;
import dev.ohhoonim.component.model.unit.Endpoint;
import jakarta.servlet.ServletException;

@Component
public class CartHandler implements Endpoint {

    private final CartService cartService;

    public CartHandler(CartService cartService) {
        this.cartService = cartService;
    }

    public ServerResponse getCart(ServerRequest request) {
        String customerId = request.param("customerId")
                .orElseThrow(() -> new CartException("customerId는 필수입니다."));
        var response = cartService.getCartView(UUID.fromString(customerId));
        return ServerResponse.ok().body(response);
    }

    public ServerResponse addToCart(ServerRequest request) throws ServletException, IOException {
        AddToCartRequest body = request.body(AddToCartRequest.class);
        String operator = request.attribute("operator").map(String::valueOf).orElse("system");
        
        cartService.addToCart(
                UUID.fromString(body.customerId()),
                body.product(),
                body.option(),
                body.quantity(),
                operator
        );
        return ServerResponse.ok().build();
    }

    public ServerResponse updateQuantity(ServerRequest request) throws ServletException, IOException {
        String itemId = request.pathVariable("itemId");
        UpdateQuantityRequest body = request.body(UpdateQuantityRequest.class);
        String operator = request.attribute("operator").map(String::valueOf).orElse("system");

        cartService.updateQuantity(UUID.fromString(body.customerId()), itemId, body.quantity(), operator);
        return ServerResponse.ok().build();
    }

    public ServerResponse removeItem(ServerRequest request) {
        String itemId = request.pathVariable("itemId");
        String customerId = request.param("customerId")
                .orElseThrow(() -> new IllegalArgumentException("customerId는 필수입니다."));
        String operator = request.attribute("operator").map(String::valueOf).orElse("system");

        cartService.removeFromCart(UUID.fromString(customerId), itemId, operator);
        return ServerResponse.ok().build();
    }

    public ServerResponse clearCart(ServerRequest request) {
        String customerId = request.param("customerId")
                .orElseThrow(() -> new IllegalArgumentException("customerId는 필수입니다."));
        String operator = request.attribute("operator").map(String::valueOf).orElse("system");

        cartService.clearCart(UUID.fromString(customerId), operator);
        return ServerResponse.ok().build();
    }
}
