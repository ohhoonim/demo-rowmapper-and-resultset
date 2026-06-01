package dev.ohhoonim.component.model.paging;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public record PageRequest(int pageNo, int pageSize, Map<String, String> lastSeen, List<OrderBy> sort)
         implements Paging {

      public PageRequest {
         if (pageNo < 1) {
            throw new PagingException("Page number must be at least 1");
         }
         if (pageSize < 1) {
            throw new PagingException("Page size must be at least 1");
         }
         lastSeen = lastSeen == null ? Map.copyOf(Map.of()) : Map.copyOf(lastSeen);
         sort = sort == null ? Collections.emptyList() : List.copyOf(sort);
      }

      public PageRequest(int pageNo, int pageSize) {
         this(pageNo, pageSize, null, null);
      }

      public int offset() {
         return (pageNo - 1) * pageSize;
      }

      public Paged toPaged(int totalCount) {
         return new Paged(pageNo, pageSize, totalCount);
      }
   }
