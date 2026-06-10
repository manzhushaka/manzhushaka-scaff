package com.manzhushaka.system.controller;

import com.manzhushaka.system.service.impl.ServerMonitorService;
import com.manzhushaka.system.vo.monitor.ServerMonitorVO;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ServerMonitorControllerTest {

    @Test
    void shouldReturnServerMonitorOverview() throws Exception {
        ServerMonitorService serverMonitorService = mock(ServerMonitorService.class);
        ServerMonitorController controller = new ServerMonitorController(serverMonitorService);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        ServerMonitorVO monitor = new ServerMonitorVO();
        monitor.setApplicationName("manzhushaka-admin");
        monitor.setActiveProfile("dev");
        monitor.setUptimeMillis(120000L);
        ServerMonitorVO.SystemInfo systemInfo = new ServerMonitorVO.SystemInfo();
        systemInfo.setAvailableProcessors(8);
        systemInfo.setSystemCpuUsage(36.5D);
        monitor.setSystem(systemInfo);
        ServerMonitorVO.RedisInfo redisInfo = new ServerMonitorVO.RedisInfo();
        redisInfo.setAvailable(true);
        redisInfo.setDbSize(32L);
        monitor.setRedis(redisInfo);
        when(serverMonitorService.getServerMonitor()).thenReturn(monitor);

        mockMvc.perform(get("/system/monitor/server"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.applicationName").value("manzhushaka-admin"))
            .andExpect(jsonPath("$.data.activeProfile").value("dev"))
            .andExpect(jsonPath("$.data.system.availableProcessors").value(8))
            .andExpect(jsonPath("$.data.redis.dbSize").value(32L));

        verify(serverMonitorService).getServerMonitor();
    }
}
