package com.manzhushaka.system.vo.cache;

import java.time.LocalDateTime;

public class CacheEntryVO {
    private String key;
    private String type;
    private Long ttlSeconds;
    private LocalDateTime expireAt;
    private String valuePreview;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getTtlSeconds() {
        return ttlSeconds;
    }

    public void setTtlSeconds(Long ttlSeconds) {
        this.ttlSeconds = ttlSeconds;
    }

    public LocalDateTime getExpireAt() {
        return expireAt;
    }

    public void setExpireAt(LocalDateTime expireAt) {
        this.expireAt = expireAt;
    }

    public String getValuePreview() {
        return valuePreview;
    }

    public void setValuePreview(String valuePreview) {
        this.valuePreview = valuePreview;
    }
}
