package dev.ohhoonim.component.model.paging;

public class PagingException extends RuntimeException {
    public PagingException(String message) {
        super(message);
    }

    public PagingException(String message, Throwable e) {
        super(message, e);
    }


}
