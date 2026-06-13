package com.manzhushaka.system.service;

import com.manzhushaka.system.dto.cache.CacheEntryQuery;
import com.manzhushaka.system.vo.cache.CacheEntryDetailVO;
import com.manzhushaka.system.vo.cache.CacheEntryVO;

import java.util.List;

/**
 * 定义 CacheQueryService 服务能力。
 */
public interface CacheQueryService {
    /**
     * 查询 list Entries 结果。
     *
     * @param query 查询条件
     * @return 查询结果
     */
    List<CacheEntryVO> listEntries(CacheEntryQuery query);

    /**
     * 返回 entryDetail。
     *
     * @param key 键名
     * @return 字段值
     */
    CacheEntryDetailVO getEntryDetail(String key);
}
