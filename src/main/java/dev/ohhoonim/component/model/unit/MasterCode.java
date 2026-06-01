package dev.ohhoonim.component.model.unit;

import java.util.Arrays;
import java.util.Optional;
import com.fasterxml.jackson.annotation.JsonValue;

public non-sealed interface MasterCode extends Unit {

    public String groupCode();

    public String masterCode();

    public String langCode();

    public static <E extends MasterCode> Optional<E> enumCode(Class<E> enumClass,
            String masterCode) {
        if (enumClass == null || !enumClass.isEnum()) {
            return Optional.empty();
        }
        if (masterCode == null) {
            return Optional.empty();
        }
        return Arrays.stream(enumClass.getEnumConstants())
                .filter(constant -> masterCode.equals(constant.masterCode())).findFirst();
    }

    @JsonValue
    default String toValue() {
        return masterCode();
    }
}
