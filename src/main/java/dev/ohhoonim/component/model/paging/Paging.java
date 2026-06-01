package dev.ohhoonim.component.model.paging;

public sealed interface Paging permits PageRequest, Paged {
   int pageNo();

   int pageSize();

}
