package com.manzhushaka.web.vo.monitor;

import com.manzhushaka.common.utils.StringUtils;

/**
 * 缓存监控展示信息。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
public class CacheInfoVO
{
    private String cacheName;
    private String cacheKey;
    private String cacheValue;
    private String remark;

    public CacheInfoVO(String cacheName, String remark)
    {
        this.cacheName = cacheName;
        this.cacheKey = "";
        this.cacheValue = "";
        this.remark = remark;
    }

    public CacheInfoVO(String cacheName, String cacheKey, String cacheValue)
    {
        this.cacheName = StringUtils.replace(cacheName, ":", "");
        this.cacheKey = StringUtils.replace(cacheKey, cacheName, "");
        this.cacheValue = cacheValue;
        this.remark = "";
    }

    public String getCacheName()
    {
        return cacheName;
    }

    public String getCacheKey()
    {
        return cacheKey;
    }

    public String getCacheValue()
    {
        return cacheValue;
    }

    public String getRemark()
    {
        return remark;
    }

    @Override
    public String toString()
    {
        return "CacheInfoVO[cacheName=" + cacheName + ", cacheKey=" + cacheKey + ", remark=" + remark + "]";
    }
}
