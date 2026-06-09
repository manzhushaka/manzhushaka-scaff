package com.manzhushaka.common.model;

public class PageQuery {
    private long current = 1;
    private long size = 10;

    public long getCurrent() {
        return current;
    }

    public void setCurrent(long current) {
        this.current = current;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }
}
