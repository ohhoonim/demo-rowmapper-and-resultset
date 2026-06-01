package dev.ohhoonim.component.model.unit;

import com.fasterxml.jackson.annotation.JsonValue;

public non-sealed interface EntityId<T> extends Unit {
    T getRawValue();

    String getPublicValue(); // 외부 노출용

    /**
     * Jackson 직렬화 시 호출되는 메서드.
     * 외부 노출용 값(PublicValue)을 반환하여 보안을 유지합니다.
     */
    @JsonValue
    default String toValue() {
        return getPublicValue();
    }

    interface Creator<T, E> {
        // 1. 완전한 복원용 (Internal + External)
        E from(T internalId, T externalId);

        // 2. 외부 유입 조회용 (External Only)
        E fromPublic(String publicId);

        // 3. 신규 생성용
        E generate();
    }
}
