package dev.ohhoonim.component.model.unit;

public non-sealed interface Entity<I extends EntityId<?>> extends Unit {
    public EntityId<?> getId(); 

    // 식별자 기반 동일성 비교
    default boolean sameIdentityAs(Entity<I> other) {
        return other != null && this.getId().equals(other.getId());
    }
}
