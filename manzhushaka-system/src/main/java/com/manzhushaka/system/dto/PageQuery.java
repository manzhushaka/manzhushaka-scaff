package com.manzhushaka.system.dto;

/**
 * 承载 PageQuery 请求参数。
 */
public class PageQuery {
    private long pageNum = 1;
    private long pageSize = 20;

    /**
     * 返回 pageNum。
     *
     * @return 字段值
     */
    public long getPageNum() {
        return pageNum;
    }

    /**
     * 设置 pageNum。
     *
     * @param pageNum pageNum 参数
     */
    public void setPageNum(long pageNum) {
        this.pageNum = pageNum <= 0 ? 1 : pageNum;
    }

    /**
     * 返回 pageSize。
     *
     * @return 字段值
     */
    public long getPageSize() {
        return pageSize;
    }

    /**
     * 设置 pageSize。
     *
     * @param pageSize pageSize 参数
     */
    public void setPageSize(long pageSize) {
        this.pageSize = pageSize <= 0 ? 20 : Math.min(pageSize, 200);
    }
}
