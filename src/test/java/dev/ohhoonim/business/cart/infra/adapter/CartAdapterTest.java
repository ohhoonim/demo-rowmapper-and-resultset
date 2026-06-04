package dev.ohhoonim.business.cart.infra.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.JdbcTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

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
}
