package com.manzhushaka.system.vo;

import java.util.List;

/**
 * 承载 PageResult 响应数据。
 */
public class PageResult<T> {
    private final long total;
    private final List<T> records;

    /**
     * 创建 PageResult 实例。
     *
     * @param total total 参数
     * @param records records 参数
     */
    public PageResult(long total, List<T> records) {
        this.total = total;
        this.records = records;
    }

    /**
     * 返回 total。
     *
     * @return 字段值
     */
    public long getTotal() {
        return total;
    }

    /**
     * 返回 records。
     *
     * @return 字段值
     */
    public List<T> getRecords() {
        return records;
    }
}
