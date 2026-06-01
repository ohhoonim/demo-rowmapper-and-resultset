package dev.ohhoonim.business.cart.endpoint;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.function.RouterFunction;
import org.springframework.web.servlet.function.RouterFunctions;
import org.springframework.web.servlet.function.ServerResponse;
import dev.ohhoonim.component.model.payload.DefaultEndpointHandler;
import dev.ohhoonim.component.model.unit.Endpoint;

@Configuration
public class CartRouter implements Endpoint {

    @Bean
    public RouterFunction<ServerResponse> cartRoutes(CartHandler handler) {
        return RouterFunctions.route().path("/api/cart",
                builder -> builder.GET("", handler::getCart).POST("/items", handler::addToCart)
                        .PATCH("/items/{itemId}", handler::updateQuantity)
                        .DELETE("/items/{itemId}", handler::removeItem)
                        .DELETE("", handler::clearCart))
                .filter(new DefaultEndpointHandler()).build();
    }
}
