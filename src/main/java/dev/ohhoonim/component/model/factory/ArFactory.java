package dev.ohhoonim.component.model.factory;

import java.util.List;
import java.util.Map;

/*
A: aggregate root
I: aggregate root id
C: components of ar

*/

public interface ArFactory <A, I, C> {
    A reconsitute(I id, List<Class<? extends C>> requiredVos, Map<String, Object> data);
    
    List<String> resolveRequiredColumns(List<Class<? extends C>> requiredVos);
}
