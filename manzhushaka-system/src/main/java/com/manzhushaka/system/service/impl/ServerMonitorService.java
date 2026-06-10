package com.manzhushaka.system.service.impl;

import com.manzhushaka.system.vo.monitor.ServerMonitorVO;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisServerCommands;
import org.springframework.stereotype.Service;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.RuntimeMXBean;
import java.lang.management.ThreadMXBean;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Arrays;
import java.util.Properties;

@Service
public class ServerMonitorService {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final MeterRegistry meterRegistry;
    private final Environment environment;
    private final RedisConnectionFactory redisConnectionFactory;
    private final RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
    private final MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
    private final ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
    private final com.sun.management.OperatingSystemMXBean operatingSystemMXBean =
        ManagementFactory.getPlatformMXBean(com.sun.management.OperatingSystemMXBean.class);

    public ServerMonitorService(
        MeterRegistry meterRegistry,
        Environment environment,
        RedisConnectionFactory redisConnectionFactory
    ) {
        this.meterRegistry = meterRegistry;
        this.environment = environment;
        this.redisConnectionFactory = redisConnectionFactory;
    }

    public ServerMonitorVO getServerMonitor() {
        ServerMonitorVO result = new ServerMonitorVO();
        result.setApplicationName(environment.getProperty("spring.application.name", "application"));
        result.setActiveProfile(resolveActiveProfile());
        result.setJavaVersion(System.getProperty("java.version", ""));
        result.setOsName(System.getProperty("os.name", ""));
        result.setOsArch(System.getProperty("os.arch", ""));
        result.setStartTime(LocalDateTime.ofInstant(Instant.ofEpochMilli(runtimeMXBean.getStartTime()), ZoneId.systemDefault())
            .format(DATE_TIME_FORMATTER));
        result.setUptimeMillis(runtimeMXBean.getUptime());
        result.setSystem(buildSystemInfo());
        result.setJvm(buildJvmInfo());
        result.setRedis(buildRedisInfo());
        return result;
    }

    private ServerMonitorVO.SystemInfo buildSystemInfo() {
        ServerMonitorVO.SystemInfo systemInfo = new ServerMonitorVO.SystemInfo();
        systemInfo.setAvailableProcessors(operatingSystemMXBean == null
            ? Runtime.getRuntime().availableProcessors()
            : operatingSystemMXBean.getAvailableProcessors());
        systemInfo.setSystemCpuUsage(toPercent(readGaugeValue("system.cpu.usage")));
        systemInfo.setProcessCpuUsage(toPercent(readGaugeValue("process.cpu.usage")));
        if (operatingSystemMXBean != null) {
            systemInfo.setTotalPhysicalMemory(operatingSystemMXBean.getTotalMemorySize());
            systemInfo.setFreePhysicalMemory(operatingSystemMXBean.getFreeMemorySize());
        }
        return systemInfo;
    }

    private ServerMonitorVO.JvmInfo buildJvmInfo() {
        MemoryUsage heapUsage = memoryMXBean.getHeapMemoryUsage();
        MemoryUsage nonHeapUsage = memoryMXBean.getNonHeapMemoryUsage();
        ServerMonitorVO.JvmInfo jvmInfo = new ServerMonitorVO.JvmInfo();
        jvmInfo.setVmName(runtimeMXBean.getVmName());
        jvmInfo.setVmVendor(runtimeMXBean.getVmVendor());
        jvmInfo.setVmVersion(runtimeMXBean.getVmVersion());
        jvmInfo.setInputArguments(List.copyOf(runtimeMXBean.getInputArguments()));
        jvmInfo.setHeapInit(heapUsage.getInit());
        jvmInfo.setHeapUsed(heapUsage.getUsed());
        jvmInfo.setHeapCommitted(heapUsage.getCommitted());
        jvmInfo.setHeapMax(heapUsage.getMax());
        jvmInfo.setNonHeapUsed(nonHeapUsage.getUsed());
        jvmInfo.setNonHeapCommitted(nonHeapUsage.getCommitted());
        jvmInfo.setNonHeapMax(nonHeapUsage.getMax());
        jvmInfo.setLiveThreadCount(threadMXBean.getThreadCount());
        jvmInfo.setDaemonThreadCount(threadMXBean.getDaemonThreadCount());
        return jvmInfo;
    }

    private ServerMonitorVO.RedisInfo buildRedisInfo() {
        ServerMonitorVO.RedisInfo redisInfo = new ServerMonitorVO.RedisInfo();
        if (redisConnectionFactory == null) {
            redisInfo.setAvailable(false);
            redisInfo.setErrorMessage("Redis 未配置连接工厂");
            return redisInfo;
        }

        RedisConnection connection = null;
        try {
            connection = redisConnectionFactory.getConnection();
            RedisServerCommands serverCommands = connection.serverCommands();
            Properties serverInfo = serverCommands.info("server");
            Properties clientsInfo = serverCommands.info("clients");
            Properties memoryInfo = serverCommands.info("memory");

            redisInfo.setAvailable(true);
            redisInfo.setVersion(readProperty(serverInfo, "redis_version"));
            redisInfo.setConnectedClients(parseInteger(readProperty(clientsInfo, "connected_clients")));
            redisInfo.setUsedMemory(parseLong(readProperty(memoryInfo, "used_memory")));
            redisInfo.setUsedMemoryPeak(parseLong(readProperty(memoryInfo, "used_memory_peak")));
            redisInfo.setDbSize(serverCommands.dbSize());
        } catch (RuntimeException exception) {
            redisInfo.setAvailable(false);
            redisInfo.setErrorMessage(exception.getMessage());
        } finally {
            if (connection != null) {
                connection.close();
            }
        }
        return redisInfo;
    }

    private String resolveActiveProfile() {
        String[] profiles = environment.getActiveProfiles();
        if (profiles.length == 0) {
            return "default";
        }
        return String.join(", ", Arrays.asList(profiles));
    }

    private Double readGaugeValue(String name) {
        Gauge gauge = meterRegistry.find(name).gauge();
        if (gauge == null) {
            return null;
        }
        double value = gauge.value();
        return Double.isNaN(value) ? null : value;
    }

    private Double toPercent(Double value) {
        if (value == null) {
            return null;
        }
        return Math.round(value * 10000D) / 100D;
    }

    private String readProperty(Properties properties, String key) {
        return properties == null ? null : properties.getProperty(key);
    }

    private Integer parseInteger(String value) {
        return value == null || value.isBlank() ? null : Integer.parseInt(value);
    }

    private Long parseLong(String value) {
        return value == null || value.isBlank() ? null : Long.parseLong(value);
    }
}
