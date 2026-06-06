package dev.ohhoonim.business.cart.infra.adapter;

import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import dev.ohhoonim.business.cart.activity.out.CartArFactory;
import dev.ohhoonim.business.cart.activity.out.CartBehaviorLogRepository;
import dev.ohhoonim.business.cart.activity.out.CartRepository;
import dev.ohhoonim.business.cart.model.Cart;
import dev.ohhoonim.business.cart.model.CartBehaviorLog;
import dev.ohhoonim.business.cart.model.CartComponent;
import dev.ohhoonim.business.cart.model.CartComponent.Product;
import dev.ohhoonim.business.cart.model.CartComponent.SelectedOption;
import dev.ohhoonim.business.cart.model.CartId;
import dev.ohhoonim.business.cart.model.CartItem;
import dev.ohhoonim.business.cart.model.CartItemId;
import dev.ohhoonim.component.model.unit.Adapter;

@Adapter
public class CartRepositoryAdapter implements CartRepository, CartBehaviorLogRepository {

    private final JdbcClient jdbcClient;
    private final CartArFactory factory;

public CartRepositoryAdapter(JdbcClient jdbcClient, @Qualifier("cartArFactory") CartArFactory factory) {
        this.jdbcClient = jdbcClient;
        this.factory = factory;
    }

    @Override
    public Optional<Cart> findCartByCustomerId(UUID customerId) {
        var columns = factory.forDefault();
        var sql = """
                select %s from  tb_cart where customer_id = :customerId
                 """.formatted(factory.resolveRequiredColumns(columns));
        return jdbcClient.sql(sql).param("customerId", customerId)
                .query(cartMapper.apply(factory, columns)).optional();
    }

    private BiFunction<CartArFactory, List<Class<? extends CartComponent>>, RowMapper<Cart>> cartMapper =
            (factory, columns) -> {
                return (rs, _) -> factory
                        .reconsitute(new CartId(rs.getObject("cart_id", UUID.class)), columns, rs);
            };

    @Override
    public Long log(CartBehaviorLog log) {
        var keyHolder = new GeneratedKeyHolder();
        var sql = """
               insert into tb_cart_behavior_log 
               (customer_id, behavior_type, product_id, created_at, created_by)
               values(:customerId, :behaviorType, :productId, :createdAt, :createdBy) 
                """;
        var params = new MapSqlParameterSource()
            .addValue("customerId", log.getCustomerId())
            .addValue("behaviorType", log.getBehaviorType().name())
            .addValue("productId", log.getProductId())
            .addValue("createdAt", log.getCreatedAt().atOffset(ZoneOffset.UTC))
            .addValue("createdBy", log.getCreatedBy());

        jdbcClient.sql(sql).paramSource(params).update(keyHolder);
        return (Long)keyHolder.getKeys().get("log_id");
    }


    @Override
    public void save(Cart cart) {
        Optional<UUID> cartId = selectCart(cart.getId().getRawValue());
        if(cartId.isPresent()) {
            updateCart(cart);
            removeCartItem(cartId.get());
            insertCartItem(cart.getItems(), cart.getId().getRawValue());
        } else {
            insertCart(cart);
            insertCartItem(cart.getItems(), cart.getId().getRawValue());
        }
    }

    private Optional<UUID> selectCart(UUID cartId) {
        var sql = """
                select cart_id from tb_cart where cart_id = :id
                """;
        return jdbcClient.sql(sql).param("id", cartId).query(UUID.class).optional();
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

    private void updateCart(Cart cart) {
        var sql = """
               update tb_cart set
                    tag = :tag,
                    modified_at = :modifiedAt,
                    modified_by = :modifiedBy
                where cart_id = :cartId
                """;
        jdbcClient.sql(sql).paramSource(cartParams.apply(cart)).update();

    }

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

    private void removeCartItem(UUID cartId) {
        var sql = """
               delete tb_cart_item where cart_id = :cartId 
                """;
        jdbcClient.sql(sql).param("cartId", cartId).update();
    }

    @Override
    public List<CartItem> findItemsByCartId(UUID cartId) {
    
        var sql = """
               select %s from tb_cart_item where cart_id = :cartId 
                """.formatted(factory.resolveRequiredColumns(factory.forItem()));
        return  jdbcClient.sql(sql).param("cartId", cartId).query(cartItemMapper.apply(factory)).list();
        
    }
    private Function<CartArFactory, RowMapper<CartItem>> cartItemMapper = factory -> (rs, _) -> {
        var cartItemId = new CartItemId(rs.getObject("cart_item_id", UUID.class)); 
        Map<String, ? extends CartComponent> vos = factory.composer(factory.forItem(), 
            factory.registry(), rs);

        return new CartItem(cartItemId, 
            CartComponent.narrow(vos.get("Product"), Product.class), 
            CartComponent.narrow(vos.get("SelectedOption"), SelectedOption.class), 
            rs.getObject("quantity", Integer.class));
    };

}
