package com.manzhushaka.system.service;

import com.manzhushaka.system.dto.cache.CacheEntryQuery;
import com.manzhushaka.system.vo.cache.CacheEntryDetailVO;
import com.manzhushaka.system.vo.cache.CacheEntryVO;

import java.util.List;

public interface CacheQueryService {
    List<CacheEntryVO> listEntries(CacheEntryQuery query);

    CacheEntryDetailVO getEntryDetail(String key);
}
