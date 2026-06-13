package com.manzhushaka.common.model;

/**
 * 定义 PageQuery。
 */
public class PageQuery {
    private long current = 1;
    private long size = 10;

    /**
     * 返回 current。
     *
     * @return 字段值
     */
    public long getCurrent() {
        return current;
    }

    /**
     * 设置 current。
     *
     * @param current current 参数
     */
    public void setCurrent(long current) {
        this.current = current;
    }

    /**
     * 返回 size。
     *
     * @return 字段值
     */
    public long getSize() {
        return size;
    }

    /**
     * 设置 size。
     *
     * @param size size 参数
     */
    public void setSize(long size) {
        this.size = size;
    }
}
