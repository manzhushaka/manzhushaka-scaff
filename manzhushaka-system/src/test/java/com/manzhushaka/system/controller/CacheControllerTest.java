package com.manzhushaka.system.controller;

import com.manzhushaka.system.service.CacheQueryService;
import com.manzhushaka.system.vo.cache.CacheEntryDetailVO;
import com.manzhushaka.system.vo.cache.CacheEntryVO;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CacheControllerTest {

    @Test
    void shouldReturnCacheEntrySummaries() throws Exception {
        CacheQueryService cacheQueryService = mock(CacheQueryService.class);
        CacheController controller = new CacheController(cacheQueryService);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        CacheEntryVO entry = new CacheEntryVO();
        entry.setKey("auth:captcha:test");
        entry.setType("string");
        entry.setTtlSeconds(120L);
        entry.setValuePreview("ABCD");
        when(cacheQueryService.listEntries(any())).thenReturn(List.of(entry));

        mockMvc.perform(get("/system/cache/entries").param("keyword", "captcha").param("limit", "20"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data[0].key").value("auth:captcha:test"))
            .andExpect(jsonPath("$.data[0].type").value("string"))
            .andExpect(jsonPath("$.data[0].ttlSeconds").value(120L))
            .andExpect(jsonPath("$.data[0].valuePreview").value("ABCD"));

        verify(cacheQueryService).listEntries(any());
    }

    @Test
    void shouldReturnCacheEntryDetail() throws Exception {
        CacheQueryService cacheQueryService = mock(CacheQueryService.class);
        CacheController controller = new CacheController(cacheQueryService);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        CacheEntryDetailVO detail = new CacheEntryDetailVO();
        detail.setKey("session:user:1");
        detail.setType("hash");
        detail.setTtlSeconds(-1L);
        detail.setValuePreview("共 2 项");
        detail.setValue(Map.of("token", "abc", "loginTime", "2026-06-10 09:00:00"));
        when(cacheQueryService.getEntryDetail(eq("session:user:1"))).thenReturn(detail);

        mockMvc.perform(get("/system/cache/entries/detail").param("key", "session:user:1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.key").value("session:user:1"))
            .andExpect(jsonPath("$.data.type").value("hash"))
            .andExpect(jsonPath("$.data.value.token").value("abc"))
            .andExpect(jsonPath("$.data.value.loginTime").value("2026-06-10 09:00:00"));

        verify(cacheQueryService).getEntryDetail("session:user:1");
    }
}
