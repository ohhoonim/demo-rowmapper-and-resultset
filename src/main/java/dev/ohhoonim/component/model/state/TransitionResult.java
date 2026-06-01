package dev.ohhoonim.component.model.state;

import java.util.List;

public interface TransitionResult<S, C> {
    public S status();

    public List<? extends PostAction<C>> actions();
} 
