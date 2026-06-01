package dev.ohhoonim.component.model.unit;

import java.time.Instant;
public sealed interface Created extends Unit permits BaseEntity {
    Instant getCreatedAt();
    String getCreatedBy();
}
