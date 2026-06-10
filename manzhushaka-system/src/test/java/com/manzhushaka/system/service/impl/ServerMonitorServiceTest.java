package com.manzhushaka.system.service.impl;

import com.manzhushaka.system.vo.monitor.ServerMonitorVO;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisServerCommands;

import java.util.Properties;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ServerMonitorServiceTest {

    @Test
    void shouldBuildServerMonitorOverviewFromMetricsAndRedis() {
        SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();
        AtomicReference<Double> systemCpuUsage = new AtomicReference<>(0.42D);
        AtomicReference<Double> processCpuUsage = new AtomicReference<>(0.16D);
        Gauge.builder("system.cpu.usage", systemCpuUsage, AtomicReference::get).register(meterRegistry);
        Gauge.builder("process.cpu.usage", processCpuUsage, AtomicReference::get).register(meterRegistry);

        Environment environment = mock(Environment.class);
        when(environment.getProperty("spring.application.name", "application")).thenReturn("manzhushaka-admin");
        when(environment.getActiveProfiles()).thenReturn(new String[] {"dev"});

        RedisConnectionFactory redisConnectionFactory = mock(RedisConnectionFactory.class);
        RedisConnection redisConnection = mock(RedisConnection.class);
        RedisServerCommands serverCommands = mock(RedisServerCommands.class);
        when(redisConnectionFactory.getConnection()).thenReturn(redisConnection);
        when(redisConnection.serverCommands()).thenReturn(serverCommands);

        Properties serverInfo = new Properties();
        serverInfo.setProperty("redis_version", "7.2.5");
        Properties clientsInfo = new Properties();
        clientsInfo.setProperty("connected_clients", "12");
        Properties memoryInfo = new Properties();
        memoryInfo.setProperty("used_memory", "1048576");
        memoryInfo.setProperty("used_memory_peak", "2097152");
        when(serverCommands.info("server")).thenReturn(serverInfo);
        when(serverCommands.info("clients")).thenReturn(clientsInfo);
        when(serverCommands.info("memory")).thenReturn(memoryInfo);
        when(serverCommands.dbSize()).thenReturn(64L);

        ServerMonitorService service = new ServerMonitorService(meterRegistry, environment, redisConnectionFactory);

        ServerMonitorVO result = service.getServerMonitor();

        assertEquals("manzhushaka-admin", result.getApplicationName());
        assertEquals("dev", result.getActiveProfile());
        assertNotNull(result.getStartTime());
        assertTrue(result.getUptimeMillis() >= 0L);
        assertEquals(42.0D, result.getSystem().getSystemCpuUsage(), 0.0001D);
        assertEquals(16.0D, result.getSystem().getProcessCpuUsage(), 0.0001D);
        assertTrue(result.getJvm().getHeapUsed() >= 0L);
        assertNotNull(result.getJvm().getVmName());
        assertNotNull(result.getJvm().getVmVendor());
        assertNotNull(result.getJvm().getVmVersion());
        assertNotNull(result.getJvm().getInputArguments());
        assertTrue(result.getJvm().getHeapInit() >= -1L);
        assertTrue(result.getJvm().getNonHeapCommitted() >= -1L);
        assertTrue(result.getJvm().getNonHeapMax() >= -1L);
        assertEquals(true, result.getRedis().getAvailable());
        assertEquals("7.2.5", result.getRedis().getVersion());
        assertEquals(12, result.getRedis().getConnectedClients());
        assertEquals(1048576L, result.getRedis().getUsedMemory());
        assertEquals(2097152L, result.getRedis().getUsedMemoryPeak());
        assertEquals(64L, result.getRedis().getDbSize());
        verify(redisConnection).close();
    }
}
