package com.cpt202.result;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class PageResult <T> {
    private Long total;
    private List<T> records;
    private Integer pageNum;
    private Integer pageSize;
    private Integer totalPages;

    public PageResult(Long total, List<T> records) {
        this(total, records, null, null, null);
    }

    public PageResult(Long total, List<T> records, Integer pageNum, Integer pageSize, Integer totalPages) {
        this.total = total;
        this.records = records;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.totalPages = totalPages;
    }
}
