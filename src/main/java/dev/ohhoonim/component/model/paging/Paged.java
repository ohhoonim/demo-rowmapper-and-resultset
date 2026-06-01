package dev.ohhoonim.component.model.paging;

/**
  * 인프라/활동 계층에서 반환하는 페이징 데이터 래퍼
  */
public record Paged(int pageNo, int pageSize, long totalCount, int totalPages /* 자동계산됨 */)
      implements Paging {

   public Paged {
      totalPages = pageSize == 0 ? 0 : (int) Math.ceil((double) totalCount / pageSize);
   }

   public Paged(int pageNo, int pageSize, long totalCount) {
      this(pageNo, pageSize, totalCount, 0);
   }

   public static Paged init() {
      return new Paged(1, 10, 0, 0);
   }
}
