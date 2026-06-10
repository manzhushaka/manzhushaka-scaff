package com.manzhushaka.system.controller;

import com.manzhushaka.common.model.ApiResponse;
import com.manzhushaka.system.dto.cache.CacheEntryQuery;
import com.manzhushaka.system.service.CacheQueryService;
import com.manzhushaka.system.vo.cache.CacheEntryDetailVO;
import com.manzhushaka.system.vo.cache.CacheEntryVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping({"/system/cache", "/api/system/cache"})
public class CacheController {

    private final CacheQueryService cacheQueryService;

    public CacheController(CacheQueryService cacheQueryService) {
        this.cacheQueryService = cacheQueryService;
    }

    @GetMapping("/entries")
    public ApiResponse<List<CacheEntryVO>> listEntries(CacheEntryQuery query) {
        return ApiResponse.success(cacheQueryService.listEntries(query));
    }

    @GetMapping("/entries/detail")
    public ApiResponse<CacheEntryDetailVO> getEntryDetail(@RequestParam("key") String key) {
        return ApiResponse.success(cacheQueryService.getEntryDetail(key));
    }
}
