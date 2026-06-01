package dev.ohhoonim.component.model.state;

public interface Status <S, T, C> {
   public TransitionResult<S, C> trigger(T event); 

    default String toValue() {
        return this.getClass().getSimpleName().toUpperCase();
    }
}
