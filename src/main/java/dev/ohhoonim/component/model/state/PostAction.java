package dev.ohhoonim.component.model.state;

@FunctionalInterface
public interface PostAction<C> {
    
    void fowloowUp(C context);
}
