package com.manzhushaka.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
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

/**
 * 提供 CacheController 相关的 Web 接口。
 */
@RestController
@RequestMapping({"/system/cache", "/api/system/cache"})
public class CacheController {

    private final CacheQueryService cacheQueryService;

    /**
     * 创建 CacheController 实例。
     *
     * @param cacheQueryService cacheQueryService 参数
     */
    public CacheController(CacheQueryService cacheQueryService) {
        this.cacheQueryService = cacheQueryService;
    }

    /**
     * 查询 list Entries 结果。
     *
     * @param query 查询条件
     * @return 查询结果
     */
    @GetMapping("/entries")
    @SaCheckPermission("system:cache:query")
    public ApiResponse<List<CacheEntryVO>> listEntries(CacheEntryQuery query) {
        return ApiResponse.success(cacheQueryService.listEntries(query));
    }

    /**
     * 返回 entryDetail。
     *
     * @param key 键名
     * @return 字段值
     */
    @GetMapping("/entries/detail")
    @SaCheckPermission("system:cache:detail")
    public ApiResponse<CacheEntryDetailVO> getEntryDetail(@RequestParam("key") String key) {
        return ApiResponse.success(cacheQueryService.getEntryDetail(key));
    }
}
