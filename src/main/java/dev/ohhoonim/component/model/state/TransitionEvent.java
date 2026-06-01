package dev.ohhoonim.component.model.state;

import java.util.List;

public interface TransitionEvent <C> {
    
    public List<? extends PostAction<C>> actions();

    // 참고
    // actions를 record의 필드로 사용하면 외부로직을 주입 받을 수도 있다. 
}
