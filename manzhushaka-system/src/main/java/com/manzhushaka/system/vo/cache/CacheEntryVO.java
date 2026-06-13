package com.manzhushaka.system.vo.cache;

import java.time.LocalDateTime;

/**
 * 承载 CacheEntryVO 响应数据。
 */
public class CacheEntryVO {
    private String key;
    private String type;
    private Long ttlSeconds;
    private LocalDateTime expireAt;
    private String valuePreview;

    /**
     * 返回 key。
     *
     * @return 字段值
     */
    public String getKey() {
        return key;
    }

    /**
     * 设置 key。
     *
     * @param key 键名
     */
    public void setKey(String key) {
        this.key = key;
    }

    /**
     * 返回 type。
     *
     * @return 字段值
     */
    public String getType() {
        return type;
    }

    /**
     * 设置 type。
     *
     * @param type type 参数
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * 返回 ttlSeconds。
     *
     * @return 字段值
     */
    public Long getTtlSeconds() {
        return ttlSeconds;
    }

    /**
     * 设置 ttlSeconds。
     *
     * @param ttlSeconds ttlSeconds 参数
     */
    public void setTtlSeconds(Long ttlSeconds) {
        this.ttlSeconds = ttlSeconds;
    }

    /**
     * 返回 expireAt。
     *
     * @return 字段值
     */
    public LocalDateTime getExpireAt() {
        return expireAt;
    }

    /**
     * 设置 expireAt。
     *
     * @param expireAt expireAt 参数
     */
    public void setExpireAt(LocalDateTime expireAt) {
        this.expireAt = expireAt;
    }

    /**
     * 返回 valuePreview。
     *
     * @return 字段值
     */
    public String getValuePreview() {
        return valuePreview;
    }

    /**
     * 设置 valuePreview。
     *
     * @param valuePreview valuePreview 参数
     */
    public void setValuePreview(String valuePreview) {
        this.valuePreview = valuePreview;
    }
}
