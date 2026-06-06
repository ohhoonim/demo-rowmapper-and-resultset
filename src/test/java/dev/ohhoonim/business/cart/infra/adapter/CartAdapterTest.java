package dev.ohhoonim.business.cart.infra.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.JdbcTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;
import dev.ohhoonim.business.cart.model.Cart;
import dev.ohhoonim.business.cart.model.CartBehaviorLog;
import dev.ohhoonim.business.cart.model.CartBehaviorLogId;
import dev.ohhoonim.business.cart.model.CartComponent.BehaviorType;
import dev.ohhoonim.business.cart.model.CartComponent.Money;
import dev.ohhoonim.business.cart.model.CartComponent.Product;
import dev.ohhoonim.business.cart.model.CartComponent.SelectedOption;
import dev.ohhoonim.business.cart.model.CartId;
import dev.ohhoonim.business.cart.model.CartItem;
import dev.ohhoonim.business.cart.model.CartItemId;

@Testcontainers
@JdbcTest
@Import({CartRepositoryAdapter.class, CartArFactoryAdapter.class})
public class CartAdapterTest {
    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres = new PostgreSQLContainer("postgres:18.1-alpine");

    @Autowired
    CartRepositoryAdapter adapter;

    @Test
    void findCartByCustomerIdTest(){

        var result = adapter.findCartByCustomerId(UUID.randomUUID());

        assertAll(
            () -> assertThat(result).isEmpty()
        );

    }

    @Test
    void behavior_log_test() {
        var customerId = UUID.randomUUID(); 
        var logId = CartBehaviorLogId.Creator.generate();
        var productId = UUID.randomUUID();
        var log = new CartBehaviorLog(logId, customerId, BehaviorType.ADD , productId, "SYSTEM");
        Long result = adapter.log(log);

        assertThat(result).isEqualTo(1L);
    }

    @Test
    void save_cart_test() {
        var product = new Product(UUID.randomUUID(), "hat", new Money(BigDecimal.valueOf(1000)), "system");
        var option = new SelectedOption(10L, "red", new Money(BigDecimal.valueOf(100L)));
        var items = List.of(new CartItem(CartItemId.Creator.generate(), product, option, 1));

        var cartId = CartId.Creator.generate();
        var customerId = UUID.randomUUID();
        var cart = Cart.reconstitute(cartId, customerId, null, 
            items, 
            Instant.now(), "system", Instant.now(), "system");

        adapter.save(cart);
    }

    @Autowired JdbcClient jdbcClient;

    @Test
    void find_items_by_cartId_test() {
        var cartId = CartId.Creator.generate();
        var customerId = UUID.randomUUID();

        insertCart(Cart.reconstitute(cartId, customerId, null, null, Instant.now(), "system", Instant.now(), "system"));

        var product = new Product(UUID.randomUUID(), "hat", new Money(BigDecimal.valueOf(1000)), "system");
        var option = new SelectedOption(10L, "red", new Money(BigDecimal.valueOf(100L)));

        var cartItems = List.of(
            new CartItem(CartItemId.Creator.generate(), product, option, 1)
        );
        insertCartItem(cartItems, cartId.getRawValue());

        var result = adapter.findItemsByCartId(cartId.getRawValue());

        assertThat(result).hasSize(1);
    }

    private void insertCart(Cart cart) {
        var sql =  """
               insert into tb_cart (cart_id, customer_id, tag, created_at, created_by, modified_at, modified_by)
               values(:cartId,:customerId,:tag,:createdAt,:createdBy,:modifiedAt,:modifiedBy) 
                """;
        jdbcClient.sql(sql).paramSource(cartParams.apply(cart)).update();
    }

    Function<Cart, SqlParameterSource>  cartParams = cart -> new MapSqlParameterSource()
        .addValue("cartId", cart.getId().getRawValue())
        .addValue("customerId", cart.getCustomerId())
        .addValue("tag", cart.getMeta() != null ? cart.getMeta().tag(): null)
        .addValue("createdAt", cart.getCreatedAt().atOffset(ZoneOffset.UTC))
        .addValue("createdBy", cart.getCreatedBy())
        .addValue("modifiedAt", cart.getModifiedAt().atOffset(ZoneOffset.UTC))
        .addValue("modifiedBy", cart.getModifiedBy());

    private void insertCartItem(List<CartItem> cartItems, UUID cartId) {

        var sql = """
            insert into tb_cart_item (cart_item_id, cart_id, 
                product_id, product_name, product_base_price, product_image_url,
                option_id, option_name, option_additional_price, quantity 
                )
            values (:cartItemId, :cartId, 
                :productId, :productName, :productBasePrice, :productImageUrl,
                :optionId, :optionName, :optionAdditionalPrice, :quantity)    
                """;
        cartItems.stream().forEach(cartItem -> 
            jdbcClient.sql(sql).paramSource(cartItemParams.apply(cartItem, cartId)).update()
        );
    }

    BiFunction<CartItem, UUID, SqlParameterSource> cartItemParams = (item, cartId) -> new MapSqlParameterSource()
        .addValue("cartItemId", item.id().getRawValue())
        .addValue("cartId", cartId)
        .addValue("productId", item.product().productId())
        .addValue("productName", item.product().productName())
        .addValue("productBasePrice", item.product().productBasePrice().amount())
        .addValue("productImageUrl", item.product().productImageUrl())
        .addValue("optionId", item.option().optionId())
        .addValue("optionName", item.option().optionName())
        .addValue("optionAdditionalPrice", item.option().optionAdditionalPrice().amount())
        .addValue("quantity", item.quantity());
}
