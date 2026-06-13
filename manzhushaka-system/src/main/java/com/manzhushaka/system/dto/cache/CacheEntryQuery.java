package com.manzhushaka.system.dto.cache;

/**
 * 承载 CacheEntryQuery 请求参数。
 */
public class CacheEntryQuery {
    private String keyword;
    private int limit = 20;

    /**
     * 返回 keyword。
     *
     * @return 字段值
     */
    public String getKeyword() {
        return keyword;
    }

    /**
     * 设置 keyword。
     *
     * @param keyword keyword 参数
     */
    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    /**
     * 返回 limit。
     *
     * @return 字段值
     */
    public int getLimit() {
        return limit;
    }

    /**
     * 设置 limit。
     *
     * @param limit limit 参数
     */
    public void setLimit(int limit) {
        if (limit <= 0) {
            this.limit = 20;
            return;
        }
        this.limit = Math.min(limit, 100);
    }
}
