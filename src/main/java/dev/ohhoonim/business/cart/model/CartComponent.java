package dev.ohhoonim.business.cart.model;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;
import dev.ohhoonim.component.model.unit.ValueObject;

@ValueObject
public sealed interface CartComponent permits CartComponent.Product, CartComponent.SelectedOption,
        CartComponent.OrderEstimatedAmount, CartComponent.BehaviorType, CartComponent.Money,
        CartComponent.CartMeta {

    public static <T extends CartComponent> T narrow(CartComponent component, Class<T> targetType) {
        Object matched = switch (component) {
            case Product p -> p;
            case SelectedOption o -> o;
            case OrderEstimatedAmount a -> a;
            case BehaviorType b -> b;
            case Money m -> m;
            case CartMeta meta -> meta;
            case null -> null;
        };

        return targetType.cast(matched);
    }

    record Product(UUID productId, String productName, Money productBasePrice, String productImageUrl)
            implements CartComponent {
        public Product {
            Objects.requireNonNull(productId, "상품 ID는 필수입니다.");
            Objects.requireNonNull(productName, "상품명은 필수입니다.");
            Objects.requireNonNull(productBasePrice, "기본 가격은 필수입니다.");
        }
    }

    record SelectedOption(Long optionId, String optionName, Money optionAdditionalPrice)
            implements CartComponent {
        public SelectedOption {
            Objects.requireNonNull(optionId, "옵션 ID는 필수입니다.");
            Objects.requireNonNull(optionName, "옵션명은 필수입니다.");
            Objects.requireNonNull(optionAdditionalPrice, "추가 가격은 필수입니다.");
        }
    }

    record OrderEstimatedAmount(Money totalProductPrice, Money totalDiscountPrice,
            Money deliveryFee, Money finalPaymentAmount) implements CartComponent {
        public OrderEstimatedAmount {
            Objects.requireNonNull(totalProductPrice, "총 상품 금액은 필수입니다.");
            Objects.requireNonNull(totalDiscountPrice, "총 할인 금액은 필수입니다.");
            Objects.requireNonNull(deliveryFee, "배송비는 필수입니다.");
            Objects.requireNonNull(finalPaymentAmount, "최종 결제 금액은 필수입니다.");
        }
    }

    public enum BehaviorType implements CartComponent {
        ADD, REMOVE, QUANTITY_CHANGE
    }

    record Money(BigDecimal amount) implements CartComponent {
        public static final Money ZERO = new Money(BigDecimal.ZERO);

        public Money {
            Objects.requireNonNull(amount, "금액은 필수입니다.");
            if (amount.compareTo(BigDecimal.ZERO) < 0) {
                throw new IllegalArgumentException("금액은 0보다 커야 합니다.");
            }
        }

        public static Money of(long amount) {
            return new Money(BigDecimal.valueOf(amount));
        }

        public Money plus(Money other) {
            return new Money(this.amount.add(other.amount));
        }

        public Money minus(Money other) {
            return new Money(this.amount.subtract(other.amount));
        }

        public Money times(int multiplier) {
            return new Money(this.amount.multiply(BigDecimal.valueOf(multiplier)));
        }
    }

    public record CartMeta(String tag) implements CartComponent {
    }


}
