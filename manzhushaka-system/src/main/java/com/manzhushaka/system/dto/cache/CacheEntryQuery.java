package com.manzhushaka.system.dto.cache;

public class CacheEntryQuery {
    private String keyword;
    private int limit = 20;

    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        if (limit <= 0) {
            this.limit = 20;
            return;
        }
        this.limit = Math.min(limit, 100);
    }
}
