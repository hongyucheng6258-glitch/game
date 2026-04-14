package com.dianjing.common;

import lombok.Data;
import java.util.List;

@Data
public class PageResult<T> {
    private long total;
    private int pages;
    private int current;
    private List<T> records;

    public PageResult(long total, int pages, int current, List<T> records) {
        this.total = total;
        this.pages = pages;
        this.current = current;
        this.records = records;
    }
}
