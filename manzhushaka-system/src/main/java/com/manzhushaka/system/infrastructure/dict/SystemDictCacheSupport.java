package com.manzhushaka.system.infrastructure.dict;

import java.util.Collection;
import java.util.List;
import com.alibaba.fastjson2.JSONArray;
import com.manzhushaka.common.constant.CacheConstants;
import com.manzhushaka.common.core.redis.RedisCache;
import com.manzhushaka.common.utils.StringUtils;
import com.manzhushaka.common.utils.spring.SpringUtils;
import com.manzhushaka.system.infrastructure.persistence.entity.SysDictData;

/**
 * 系统字典缓存支持
 * <p>
 * 封装字典数据的缓存读写逻辑，取代原先的 {@code DictUtils} 中的缓存操作方法。
 * </p>
 *
 * @author manzhushaka
 */
public final class SystemDictCacheSupport
{
    private SystemDictCacheSupport()
    {
    }

    /**
     * 设置字典缓存
     *
     * @param key       参数键
     * @param dictDatas 字典数据列表
     */
    public static void setDictCache(String key, List<SysDictData> dictDatas)
    {
        SpringUtils.getBean(RedisCache.class).setCacheObject(getCacheKey(key), dictDatas);
    }

    /**
     * 获取字典缓存
     *
     * @param key 参数键
     * @return 字典数据列表
     */
    public static List<SysDictData> getDictCache(String key)
    {
        JSONArray arrayCache = SpringUtils.getBean(RedisCache.class).getCacheObject(getCacheKey(key));
        if (StringUtils.isNotNull(arrayCache))
        {
            return arrayCache.toList(SysDictData.class);
        }
        return null;
    }

    /**
     * 删除指定字典缓存
     *
     * @param key 字典键
     */
    public static void removeDictCache(String key)
    {
        SpringUtils.getBean(RedisCache.class).deleteObject(getCacheKey(key));
    }

    /**
     * 清空字典缓存
     */
    public static void clearDictCache()
    {
        Collection<String> keys = SpringUtils.getBean(RedisCache.class).keys(CacheConstants.SYS_DICT_KEY + "*");
        SpringUtils.getBean(RedisCache.class).deleteObject(keys);
    }

    /**
     * 设置 cache key
     *
     * @param configKey 参数键
     * @return 缓存键 key
     */
    static String getCacheKey(String configKey)
    {
        return CacheConstants.SYS_DICT_KEY + configKey;
    }
}