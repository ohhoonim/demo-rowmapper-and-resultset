package dev.ohhoonim.component.model.unit;

import java.time.Instant;
public sealed interface Modified extends Unit permits BaseEntity {
    Instant getModifiedAt();
    String getModifiedBy();
}
