package com.manzhushaka.system.dto;

public class PageQuery {
    private long pageNum = 1;
    private long pageSize = 20;

    public long getPageNum() {
        return pageNum;
    }

    public void setPageNum(long pageNum) {
        this.pageNum = pageNum <= 0 ? 1 : pageNum;
    }

    public long getPageSize() {
        return pageSize;
    }

    public void setPageSize(long pageSize) {
        this.pageSize = pageSize <= 0 ? 20 : Math.min(pageSize, 200);
    }
}
