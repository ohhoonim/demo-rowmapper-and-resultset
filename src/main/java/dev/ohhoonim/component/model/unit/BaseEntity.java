package dev.ohhoonim.component.model.unit;

import java.time.Instant;
import java.util.Objects;

public abstract non-sealed class BaseEntity<I extends EntityId<?>>
        implements Entity<I>, Created, Modified {

    private final I id;
    private Instant createdAt;
    private String createdBy;
    private Instant modifiedAt;
    private String modifiedBy;

    // 1. 초기 생성용 생성자: 생성 시점에 모든 Auditing 정보를 강제함
    protected BaseEntity(I id, String operator) {
        this.id = Objects.requireNonNull(id, "엔티티 식별자는 필수입니다.");
        Instant now = Instant.now();
        this.createdAt = now;
        this.createdBy = operator;
        this.modifiedAt = now;
        this.modifiedBy = operator;
    }

    // 2. DB 복원용 생성자: 외부(infra)에서 기존 데이터를 읽어올 때 사용
    protected BaseEntity(I id, Instant createdAt, String createdBy, 
                         Instant modifiedAt, String modifiedBy) {
        this.id = id;
        this.createdAt = createdAt;
        this.createdBy = createdBy;
        this.modifiedAt = modifiedAt;
        this.modifiedBy = modifiedBy;
    }

    // 3. 수정 기록 메서드: 비즈니스 로직(Activity) 완료 시 모델이 호출
    protected void recordModification(String operator) {
        this.modifiedAt = Instant.now();
        this.modifiedBy = Objects.requireNonNull(operator, "수정자 정보는 필수입니다.");
    }

    @Override public I getId() { return id; }
    @Override public Instant getCreatedAt() { return createdAt; }
    @Override public String getCreatedBy() { return createdBy; }
    @Override public Instant getModifiedAt() { return modifiedAt; }
    @Override public String getModifiedBy() { return modifiedBy; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        // 4. Proxy나 상속 구조를 고려하여 getClass() 비교보다 instanceof 권장 
        if (!(o instanceof BaseEntity<?> that)) return false;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
