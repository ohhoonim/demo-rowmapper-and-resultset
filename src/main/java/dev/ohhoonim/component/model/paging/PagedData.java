package dev.ohhoonim.component.model.paging;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public record PagedData<T>(List<T> contents, Paged paged) {

    public PagedData {
        contents = List.copyOf(contents);
    }

    public static PagedData<Map<String, Object>> empty() {
        return new PagedData<Map<String, Object>>(Collections.emptyList(), Paged.init());
    }

    
}
