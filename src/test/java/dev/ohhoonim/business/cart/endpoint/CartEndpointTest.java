package dev.ohhoonim.business.cart.endpoint;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.restdocs.request.RequestDocumentation.queryParameters;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.restdocs.test.autoconfigure.AutoConfigureRestDocs;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import dev.ohhoonim.RestDocsConfiguration;
import dev.ohhoonim.business.cart.application.CartResponse;
import dev.ohhoonim.business.cart.application.CartService;
import dev.ohhoonim.business.cart.model.CartComponent.Money;
import dev.ohhoonim.business.cart.model.CartComponent.OrderEstimatedAmount;
import dev.ohhoonim.business.cart.model.CartComponent.Product;
import dev.ohhoonim.business.cart.model.CartComponent.SelectedOption;
import dev.ohhoonim.business.cart.model.CartItemId;
import tools.jackson.databind.ObjectMapper;

@WebMvcTest({CartRouter.class, CartHandler.class})
@AutoConfigureRestDocs
@Import(RestDocsConfiguration.class)
public class CartEndpointTest {

	@Autowired
	MockMvcTester mvc;

	@MockitoBean
	CartService cartService;

	@Autowired
	ObjectMapper objectMapper;

	@Test
	@DisplayName("장바구니 조회 API 테스트")
	void get_cart_test() {

		CartResponse mockResponse = new CartResponse(UUID.randomUUID(),
				List.of(new CartResponse.CartItemResponse(CartItemId.Creator.generate(),
						new Product(UUID.randomUUID(), "테스트 상품", Money.of(10000), "img.png"),
						new SelectedOption(1L, "옵션1", Money.ZERO), 2, Money.of(20000))),
				new OrderEstimatedAmount(Money.of(20000), Money.ZERO, Money.of(3000),
						Money.of(23000)),
				List.of());
		given(cartService.getCartView(any())).willReturn(mockResponse);

		var result =
				mvc.get().uri("/api/cart?customerId={1}", UUID.randomUUID().toString()).exchange();

		result.assertThat().apply(document("cart-view",
				queryParameters(parameterWithName("customerId").description("고객 ID")),
				responseFields(fieldWithPath("code").description("응답 코드"),
						fieldWithPath("data.customerId").description("고객 ID"),
						fieldWithPath("data.items").description("장바구니 항목 목록"),
						fieldWithPath("data.items[].itemId").description("항목 ID (Public)"),
						fieldWithPath("data.items[].product.productId").description("상품 ID"),
						fieldWithPath("data.items[].product.productName").description("상품명"),
						fieldWithPath("data.items[].product.productBasePrice.amount")
								.description("상품 기본가"),
						fieldWithPath("data.items[].product.productImageUrl").description("상품 이미지 URL"),
						fieldWithPath("data.items[].option.optionId").description("옵션 ID"),
						fieldWithPath("data.items[].option.optionName").description("옵션명"),
						fieldWithPath("data.items[].option.optionAdditionalPrice.amount")
								.description("옵션 추가 금액"),
						fieldWithPath("data.items[].quantity").description("수량"),
						fieldWithPath("data.items[].subTotal.amount").description("항목별 소계"),
						fieldWithPath("data.estimatedAmount.totalProductPrice.amount")
								.description("총 상품 금액"),
						fieldWithPath("data.estimatedAmount.totalDiscountPrice.amount")
								.description("총 할인 금액"),
						fieldWithPath("data.estimatedAmount.deliveryFee.amount").description("배송비"),
						fieldWithPath("data.estimatedAmount.finalPaymentAmount.amount")
								.description("최종 결제 예정 금액"),
						fieldWithPath("data.recommendedProducts").description("추천 상품 목록"))));

		assertThat(result).hasStatusOk();
	}

	@Test
	@DisplayName("장바구니 항목 추가 API 테스트")
	void add_to_cart_test() throws Exception {
		AddToCartRequest request = new AddToCartRequest(UUID.randomUUID().toString(),
				new Product(UUID.randomUUID(), "테스트 상품", Money.of(10000), "img.png"),
				new SelectedOption(1L, "옵션1", Money.ZERO), 2);
		doNothing().when(cartService).addToCart(any(), any(Product.class),
				any(SelectedOption.class), anyInt(), anyString());

		var result = mvc.post().uri("/api/cart/items").contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)).exchange();

		result.assertThat()
				.apply(document("cart-add-item",
						requestFields(fieldWithPath("customerId").description("고객 ID"),
								fieldWithPath("product.productId").description("상품 ID"),
								fieldWithPath("product.productName").description("상품명"),
								fieldWithPath("product.productBasePrice.amount").description("상품 기본가"),
								fieldWithPath("product.productImageUrl").description("상품 이미지 URL"),
								fieldWithPath("option.optionId").description("옵션 ID"),
								fieldWithPath("option.optionName").description("옵션명"),
								fieldWithPath("option.optionAdditionalPrice.amount").description(
										"옵션 추가 금액"),
								fieldWithPath("quantity").description("수량")),
						responseFields(fieldWithPath("code").description("응답 코드"),
								fieldWithPath("data").description("데이터 (성공 시 null)"))));

		assertThat(result).hasStatusOk();
	}

	@Test
	@DisplayName("장바구니 수량 변경 API 테스트")
	void update_quantity_test() throws Exception {

		UpdateQuantityRequest request = new UpdateQuantityRequest(UUID.randomUUID().toString(), 5);
		doNothing().when(cartService).updateQuantity(any(), anyString(), anyInt(), anyString());

		// When
		var result = mvc.patch().uri("/api/cart/items/{itemId}", "item-01")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)).exchange();

		result.assertThat()
				.apply(document("cart-update-quantity",
						pathParameters(parameterWithName("itemId").description("항목 ID")),
						requestFields(fieldWithPath("customerId").description("고객 ID"),
								fieldWithPath("quantity").description("변경할 수량")),
						responseFields(fieldWithPath("code").description("응답 코드"),
								fieldWithPath("data").description("데이터 (성공 시 null)"))));

		assertThat(result).hasStatusOk();
	}

	@Test
	@DisplayName("장바구니 항목 삭제 API 테스트")
	void remove_item_test() {
		doNothing().when(cartService).removeFromCart(any(), anyString(), anyString());

		var result = mvc.delete().uri("/api/cart/items/{itemId}?customerId={customerId}",
				UUID.randomUUID(), UUID.randomUUID()).exchange();

		result.assertThat()
				.apply(document("cart-remove-item",
						pathParameters(parameterWithName("itemId").description("항목 ID")),
						queryParameters(parameterWithName("customerId").description("고객 ID")),
						responseFields(fieldWithPath("code").description("응답 코드"),
								fieldWithPath("data").description("데이터 (성공 시 null)"))));

		assertThat(result).hasStatusOk();
	}

	@Test
	@DisplayName("장바구니 비우기 API 테스트")
	void clear_cart_test() {

		doNothing().when(cartService).clearCart(any(), anyString());

		var result = mvc.delete()
				.uri("/api/cart?customerId={customerId}", UUID.randomUUID().toString()).exchange();

		result.assertThat()
				.apply(document("cart-clear",
						queryParameters(parameterWithName("customerId").description("고객 ID")),
						responseFields(fieldWithPath("code").description("응답 코드"),
								fieldWithPath("data").description("데이터 (성공 시 null)"))));

		assertThat(result).hasStatusOk();
	}
}
