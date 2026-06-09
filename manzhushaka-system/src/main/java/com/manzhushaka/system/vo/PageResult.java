package com.manzhushaka.system.vo;

import java.util.List;

public class PageResult<T> {
    private final long total;
    private final List<T> records;

    public PageResult(long total, List<T> records) {
        this.total = total;
        this.records = records;
    }

    public long getTotal() {
        return total;
    }

    public List<T> getRecords() {
        return records;
    }
}
