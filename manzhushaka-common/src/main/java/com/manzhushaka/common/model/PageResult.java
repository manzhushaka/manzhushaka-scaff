package com.manzhushaka.common.model;

import java.util.List;

/**
 * 定义 PageResult。
 */
public class PageResult<T> {
    private long total;
    private List<T> records;

    /**
     * 创建 PageResult 实例。
     */
    public PageResult() {
    }

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
     * 设置 total。
     *
     * @param total total 参数
     */
    public void setTotal(long total) {
        this.total = total;
    }

    /**
     * 返回 records。
     *
     * @return 字段值
     */
    public List<T> getRecords() {
        return records;
    }

    /**
     * 设置 records。
     *
     * @param records records 参数
     */
    public void setRecords(List<T> records) {
        this.records = records;
    }
}
