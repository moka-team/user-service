package com.mokaform.userservice.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;
import lombok.Builder;
import lombok.Getter;
import org.springframework.data.domain.Page;

import java.util.List;

@Getter
@JsonInclude(Include.NON_NULL)
public class PageResponse<T> {

    private final List<T> content;
    private final int numberOfElements;
    private final long totalElements;
    private final int totalPages;
    private final long offset;
    private final int pageSize;
    private final int pageNumber;
    private final boolean first;
    private final boolean last;

    @Builder
    public PageResponse(Page<T> page) {
        this.content = page.getContent();
        this.numberOfElements = page.getNumberOfElements();
        this.totalElements = page.getTotalElements();
        this.totalPages = page.getTotalPages();
        this.offset = page.getPageable().getOffset();
        this.pageSize = page.getPageable().getPageSize();
        this.pageNumber = page.getPageable().getPageNumber();
        this.first = page.isFirst();
        this.last = page.isLast();
    }
}
