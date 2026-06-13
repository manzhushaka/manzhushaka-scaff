package com.manzhushaka.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.manzhushaka.db.monitor.SlowSqlMonitorStore;
import com.manzhushaka.db.monitor.SlowSqlRecord;
import com.manzhushaka.db.system.entity.SysJob;
import com.manzhushaka.db.system.entity.SysJobLog;
import com.manzhushaka.db.system.entity.SysMqMessage;
import com.manzhushaka.db.system.mapper.SysJobLogMapper;
import com.manzhushaka.db.system.mapper.SysJobMapper;
import com.manzhushaka.db.system.mapper.SysMqMessageMapper;
import com.manzhushaka.framework.monitor.ApplicationLogBuffer;
import com.manzhushaka.mq.properties.MqProperties;
import com.manzhushaka.system.vo.monitor.MonitorLogTailVO;
import com.manzhushaka.system.vo.monitor.MonitorSlowSqlVO;
import com.manzhushaka.system.vo.monitor.ServerMonitorVO;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.beans.factory.annotation.Autowired;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

/**
 * 实现 ServerMonitorService 业务服务。
 */
@Service
public class ServerMonitorService {
    /**
     * 执行 of Pattern 逻辑。
     *
     * @param HH:mm:ss" HH:mm:ss" 参数
     * @return 处理结果
     */
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final int DEFAULT_SLOW_SQL_LIMIT = 20;
    private static final int DEFAULT_LOG_LINE_LIMIT = 80;

    private final MeterRegistry meterRegistry;
    private final Environment environment;
    private final RedisConnectionFactory redisConnectionFactory;
    private final SysJobMapper jobMapper;
    private final SysJobLogMapper jobLogMapper;
    private final SysMqMessageMapper mqMessageMapper;
    private final SlowSqlMonitorStore slowSqlMonitorStore;
    private final ApplicationLogBuffer applicationLogBuffer;
    private final MqProperties mqProperties;
    /**
     * 返回 runtimeMXBean。
     *
     * @return 字段值
     */
    private final RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();
    /**
     * 返回 memoryMXBean。
     *
     * @return 字段值
     */
    private final MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();
    /**
     * 返回 threadMXBean。
     *
     * @return 字段值
     */
    private final ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
    private final com.sun.management.OperatingSystemMXBean operatingSystemMXBean =
        ManagementFactory.getPlatformMXBean(com.sun.management.OperatingSystemMXBean.class);

    /**
     * 创建仅包含基础运行态依赖的监控服务。
     *
     * @param meterRegistry 指标注册表
     * @param environment 环境变量读取器
     * @param redisConnectionFactory Redis 连接工厂
     */
    public ServerMonitorService(
        MeterRegistry meterRegistry,
        Environment environment,
        RedisConnectionFactory redisConnectionFactory
    ) {
        this(
            meterRegistry,
            environment,
            redisConnectionFactory,
            null,
            null,
            null,
            null,
            null,
            new MqProperties()
        );
    }

    /**
     * 创建完整运行监控服务。
     *
     * @param meterRegistry 指标注册表
     * @param environment 环境变量读取器
     * @param redisConnectionFactory Redis 连接工厂
     * @param jobMapper 定时任务 Mapper
     * @param jobLogMapper 定时任务日志 Mapper
     * @param mqMessageMapper 消息台账 Mapper
     * @param slowSqlMonitorStore 慢 SQL 存储器
     * @param applicationLogBuffer 应用日志缓冲区
     */
    public ServerMonitorService(
        MeterRegistry meterRegistry,
        Environment environment,
        RedisConnectionFactory redisConnectionFactory,
        SysJobMapper jobMapper,
        SysJobLogMapper jobLogMapper,
        SysMqMessageMapper mqMessageMapper,
        SlowSqlMonitorStore slowSqlMonitorStore,
        ApplicationLogBuffer applicationLogBuffer
    ) {
        this(
            meterRegistry,
            environment,
            redisConnectionFactory,
            jobMapper,
            jobLogMapper,
            mqMessageMapper,
            slowSqlMonitorStore,
            applicationLogBuffer,
            new MqProperties()
        );
    }

    /**
     * 创建完整运行监控服务。
     *
     * @param meterRegistry 指标注册表
     * @param environment 环境变量读取器
     * @param redisConnectionFactory Redis 连接工厂
     * @param jobMapper 定时任务 Mapper
     * @param jobLogMapper 定时任务日志 Mapper
     * @param mqMessageMapper 消息台账 Mapper
     * @param slowSqlMonitorStore 慢 SQL 存储器
     * @param applicationLogBuffer 应用日志缓冲区
     * @param mqProperties MQ 配置
     */
    @Autowired
    public ServerMonitorService(
        MeterRegistry meterRegistry,
        Environment environment,
        RedisConnectionFactory redisConnectionFactory,
        SysJobMapper jobMapper,
        SysJobLogMapper jobLogMapper,
        SysMqMessageMapper mqMessageMapper,
        SlowSqlMonitorStore slowSqlMonitorStore,
        ApplicationLogBuffer applicationLogBuffer,
        MqProperties mqProperties
    ) {
        this.meterRegistry = meterRegistry;
        this.environment = environment;
        this.redisConnectionFactory = redisConnectionFactory;
        this.jobMapper = jobMapper;
        this.jobLogMapper = jobLogMapper;
        this.mqMessageMapper = mqMessageMapper;
        this.slowSqlMonitorStore = slowSqlMonitorStore;
        this.applicationLogBuffer = applicationLogBuffer;
        this.mqProperties = mqProperties == null ? new MqProperties() : mqProperties;
    }

    /**
     * 构建监控总览数据。
     *
     * @return 运行监控总览
     */
    public ServerMonitorVO getServerMonitor() {
        ServerMonitorVO result = new ServerMonitorVO();
        result.setApplicationName(environment.getProperty("spring.application.name", "application"));
        result.setActiveProfile(resolveActiveProfile());
        result.setJavaVersion(System.getProperty("java.version", ""));
        result.setOsName(System.getProperty("os.name", ""));
        result.setOsArch(System.getProperty("os.arch", ""));
        result.setStartTime(formatDateTime(LocalDateTime.ofInstant(Instant.ofEpochMilli(runtimeMXBean.getStartTime()), ZoneId.systemDefault())));
        result.setUptimeMillis(runtimeMXBean.getUptime());
        result.setSystem(buildSystemInfo());
        result.setJvm(buildJvmInfo());
        result.setRedis(buildRedisInfo());
        result.setJobHealth(buildJobHealth());
        result.setMessageBacklog(buildMessageBacklog());
        result.setSlowSql(buildSlowSqlSummary());
        result.setLogTail(buildLogTailSummary());
        return result;
    }

    /**
     * 返回最近慢 SQL 列表。
     *
     * @param limit 查询条数
     * @return 最近慢 SQL 列表
     */
    public List<MonitorSlowSqlVO> listSlowSqlRecords(Integer limit) {
        if (slowSqlMonitorStore == null) {
            return List.of();
        }
        int normalizedLimit = limit == null ? DEFAULT_SLOW_SQL_LIMIT : limit;
        return slowSqlMonitorStore.listRecent(normalizedLimit).stream()
            .map(this::toSlowSqlVO)
            .toList();
    }

    /**
     * 返回在线日志 tail。
     *
     * @param limit 查询条数
     * @return 在线日志 tail
     */
    public MonitorLogTailVO getLogTail(Integer limit) {
        MonitorLogTailVO result = new MonitorLogTailVO();
        result.setAvailable(applicationLogBuffer != null);
        result.setGeneratedAt(formatDateTime(LocalDateTime.now()));
        if (applicationLogBuffer == null) {
            result.setLines(List.of());
            return result;
        }
        result.setLastEntryAt(formatDateTime(applicationLogBuffer.lastEntryAt()));
        result.setLines(applicationLogBuffer.tailLines(limit == null ? DEFAULT_LOG_LINE_LIMIT : limit));
        return result;
    }

    /**
     * 构建系统资源信息。
     *
     * @return 系统资源信息
     */
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

    /**
     * 构建 JVM 运行信息。
     *
     * @return JVM 运行信息
     */
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

    /**
     * 构建 Redis 状态摘要。
     *
     * @return Redis 状态摘要
     */
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
            Properties statsInfo = serverCommands.info("stats");

            redisInfo.setAvailable(true);
            redisInfo.setVersion(readProperty(serverInfo, "redis_version"));
            redisInfo.setConnectedClients(parseInteger(readProperty(clientsInfo, "connected_clients")));
            redisInfo.setUsedMemory(parseLong(readProperty(memoryInfo, "used_memory")));
            redisInfo.setUsedMemoryPeak(parseLong(readProperty(memoryInfo, "used_memory_peak")));
            redisInfo.setDbSize(serverCommands.dbSize());
            redisInfo.setKeyspaceHits(parseLong(readProperty(statsInfo, "keyspace_hits")));
            redisInfo.setKeyspaceMisses(parseLong(readProperty(statsInfo, "keyspace_misses")));
            redisInfo.setExpiredKeys(parseLong(readProperty(statsInfo, "expired_keys")));
            redisInfo.setEvictedKeys(parseLong(readProperty(statsInfo, "evicted_keys")));
            redisInfo.setHitRate(calculateHitRate(redisInfo.getKeyspaceHits(), redisInfo.getKeyspaceMisses()));
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

    /**
     * 构建任务健康摘要。
     *
     * @return 任务健康摘要
     */
    private ServerMonitorVO.JobHealth buildJobHealth() {
        List<SysJob> jobs = listJobs();
        List<SysJobLog> logs = listJobLogs();
        ServerMonitorVO.JobHealth summary = new ServerMonitorVO.JobHealth();
        summary.setTotalJobs((long) jobs.size());
        summary.setEnabledJobs(jobs.stream().filter(job -> Objects.equals(job.getStatus(), 1)).count());
        summary.setPausedJobs(jobs.stream().filter(job -> Objects.equals(job.getStatus(), 0)).count());

        LocalDateTime last24Hours = LocalDateTime.now().minusHours(24);
        long successCount = logs.stream()
            .filter(log -> log.getStartTime() != null && !log.getStartTime().isBefore(last24Hours))
            .filter(log -> "SUCCESS".equalsIgnoreCase(log.getRunStatus()))
            .count();
        long failCount = logs.stream()
            .filter(log -> log.getStartTime() != null && !log.getStartTime().isBefore(last24Hours))
            .filter(log -> "FAIL".equalsIgnoreCase(log.getRunStatus()))
            .count();
        summary.setRecentSuccessCount(successCount);
        summary.setRecentFailCount(failCount);
        summary.setRecentSuccessRate(calculateRate(successCount, successCount + failCount));
        summary.setRecentFailures(logs.stream()
            .filter(log -> "FAIL".equalsIgnoreCase(log.getRunStatus()))
            .sorted(Comparator.comparing(SysJobLog::getStartTime, Comparator.nullsLast(Comparator.reverseOrder())))
            .limit(5)
            .map(this::toJobFailureItem)
            .toList());
        return summary;
    }

    /**
     * 构建消息积压摘要。
     *
     * @return 消息积压摘要
     */
    private ServerMonitorVO.MessageBacklog buildMessageBacklog() {
        List<SysMqMessage> messages = listMqMessages();
        ServerMonitorVO.MessageBacklog summary = new ServerMonitorVO.MessageBacklog();
        List<SysMqMessage> pendingMessages = messages.stream().filter(this::isPendingMessage).toList();
        summary.setPendingCount((long) pendingMessages.size());
        summary.setProcessingCount(messages.stream().filter(message -> "PROCESSING".equalsIgnoreCase(message.getStatus())).count());
        summary.setFailCount(messages.stream().filter(message -> "FAIL".equalsIgnoreCase(message.getStatus())).count());
        summary.setInitCount(messages.stream().filter(message -> "INIT".equalsIgnoreCase(message.getStatus())).count());
        summary.setPublishedCount(messages.stream().filter(message -> "PUBLISHED".equalsIgnoreCase(message.getStatus())).count());
        summary.setTimedOutCount(messages.stream().filter(this::isMessageTimedOut).count());

        SysMqMessage oldestPending = pendingMessages.stream()
            .min(Comparator.comparing(SysMqMessage::getCreateTime, Comparator.nullsLast(Comparator.naturalOrder())))
            .orElse(null);
        if (oldestPending != null) {
            summary.setOldestPendingEventId(oldestPending.getEventId());
            summary.setOldestPendingCreateTime(formatDateTime(oldestPending.getCreateTime()));
        }

        summary.setStreams(buildStreamBacklogItems(messages));
        return summary;
    }

    /**
     * 构建慢 SQL 摘要。
     *
     * @return 慢 SQL 摘要
     */
    private ServerMonitorVO.SlowSqlSummary buildSlowSqlSummary() {
        ServerMonitorVO.SlowSqlSummary summary = new ServerMonitorVO.SlowSqlSummary();
        summary.setAvailable(slowSqlMonitorStore != null);
        if (slowSqlMonitorStore == null) {
            summary.setRecentCount(0);
            return summary;
        }
        SlowSqlRecord latest = slowSqlMonitorStore.latest();
        summary.setRecentCount(slowSqlMonitorStore.size());
        summary.setThresholdMs(slowSqlMonitorStore.getThresholdMs());
        if (latest != null) {
            summary.setLatestCostMs(latest.getCostMs());
            summary.setLatestStatementId(latest.getStatementId());
            summary.setLatestExecuteTime(formatDateTime(latest.getExecuteTime()));
        }
        return summary;
    }

    /**
     * 构建在线日志摘要。
     *
     * @return 在线日志摘要
     */
    private ServerMonitorVO.LogTailSummary buildLogTailSummary() {
        ServerMonitorVO.LogTailSummary summary = new ServerMonitorVO.LogTailSummary();
        summary.setAvailable(applicationLogBuffer != null);
        if (applicationLogBuffer == null) {
            summary.setEntryCount(0);
            return summary;
        }
        summary.setEntryCount(applicationLogBuffer.size());
        summary.setCapacity(applicationLogBuffer.capacity());
        summary.setLastEntryAt(formatDateTime(applicationLogBuffer.lastEntryAt()));
        return summary;
    }

    /**
     * 查询任务列表。
     *
     * @return 任务列表
     */
    private List<SysJob> listJobs() {
        if (jobMapper == null) {
            return List.of();
        }
        return jobMapper.selectList(new LambdaQueryWrapper<SysJob>().orderByDesc(SysJob::getId));
    }

    /**
     * 查询任务日志列表。
     *
     * @return 任务日志列表
     */
    private List<SysJobLog> listJobLogs() {
        if (jobLogMapper == null) {
            return List.of();
        }
        return jobLogMapper.selectList(new LambdaQueryWrapper<SysJobLog>().orderByDesc(SysJobLog::getStartTime, SysJobLog::getId));
    }

    /**
     * 查询消息台账列表。
     *
     * @return 消息台账列表
     */
    private List<SysMqMessage> listMqMessages() {
        if (mqMessageMapper == null) {
            return List.of();
        }
        return mqMessageMapper.selectList(new LambdaQueryWrapper<SysMqMessage>().orderByDesc(SysMqMessage::getCreateTime, SysMqMessage::getId));
    }

    /**
     * 判断消息是否仍处于待处理状态。
     *
     * @param message 消息台账
     * @return 是否待处理
     */
    private boolean isPendingMessage(SysMqMessage message) {
        String status = message.getStatus();
        return "INIT".equalsIgnoreCase(status)
            || "PUBLISHED".equalsIgnoreCase(status)
            || "PROCESSING".equalsIgnoreCase(status);
    }

    /**
     * 判断消息是否超时。
     *
     * @param message 消息台账
     * @return 是否超时
     */
    private boolean isMessageTimedOut(SysMqMessage message) {
        LocalDateTime now = LocalDateTime.now();
        if ("PROCESSING".equalsIgnoreCase(message.getStatus())) {
            return message.getProcessingDeadlineAt() != null && !message.getProcessingDeadlineAt().isAfter(now);
        }
        if ("PUBLISHED".equalsIgnoreCase(message.getStatus())) {
            return message.getPublishedAt() != null
                && !message.getPublishedAt().plusSeconds(mqProperties.getProcessingTimeoutSeconds()).isAfter(now);
        }
        return false;
    }

    /**
     * 构造流级别积压视图。
     *
     * @param messages 消息台账列表
     * @return 流级别积压视图
     */
    private List<ServerMonitorVO.StreamBacklogItem> buildStreamBacklogItems(List<SysMqMessage> messages) {
        Map<String, ServerMonitorVO.StreamBacklogItem> items = new LinkedHashMap<>();
        for (SysMqMessage message : messages) {
            ServerMonitorVO.StreamBacklogItem item = items.computeIfAbsent(defaultText(message.getStreamKey(), "--"), key -> {
                ServerMonitorVO.StreamBacklogItem created = new ServerMonitorVO.StreamBacklogItem();
                created.setStreamKey(key);
                created.setPendingCount(0L);
                created.setFailCount(0L);
                return created;
            });
            if (isPendingMessage(message)) {
                item.setPendingCount(item.getPendingCount() + 1);
            }
            if ("FAIL".equalsIgnoreCase(message.getStatus())) {
                item.setFailCount(item.getFailCount() + 1);
            }
        }
        return items.values().stream()
            .sorted(Comparator.comparing(ServerMonitorVO.StreamBacklogItem::getPendingCount, Comparator.reverseOrder())
                .thenComparing(ServerMonitorVO.StreamBacklogItem::getFailCount, Comparator.reverseOrder()))
            .limit(6)
            .toList();
    }

    /**
     * 映射任务失败项视图。
     *
     * @param log 任务日志
     * @return 任务失败项视图
     */
    private ServerMonitorVO.JobFailureItem toJobFailureItem(SysJobLog log) {
        ServerMonitorVO.JobFailureItem item = new ServerMonitorVO.JobFailureItem();
        item.setJobId(log.getJobId());
        item.setJobName(log.getJobNameSnapshot());
        item.setRunStatus(log.getRunStatus());
        item.setErrorMsg(log.getErrorMsg());
        item.setStartTime(formatDateTime(log.getStartTime()));
        return item;
    }

    /**
     * 映射慢 SQL 视图。
     *
     * @param record 慢 SQL 记录
     * @return 慢 SQL 视图
     */
    private MonitorSlowSqlVO toSlowSqlVO(SlowSqlRecord record) {
        MonitorSlowSqlVO vo = new MonitorSlowSqlVO();
        vo.setStatementId(record.getStatementId());
        vo.setSql(record.getSql());
        vo.setCostMs(record.getCostMs());
        vo.setResultSize(record.getResultSize());
        vo.setExecuteTime(formatDateTime(record.getExecuteTime()));
        return vo;
    }

    /**
     * 解析当前环境标识。
     *
     * @return 当前环境标识
     */
    private String resolveActiveProfile() {
        String[] profiles = environment.getActiveProfiles();
        if (profiles.length == 0) {
            return "default";
        }
        return String.join(", ", Arrays.asList(profiles));
    }

    /**
     * 读取 Micrometer Gauge 值。
     *
     * @param name 指标名称
     * @return 指标值
     */
    private Double readGaugeValue(String name) {
        Gauge gauge = meterRegistry.find(name).gauge();
        if (gauge == null) {
            return null;
        }
        double value = gauge.value();
        return Double.isNaN(value) ? null : value;
    }

    /**
     * 转换百分比数值。
     *
     * @param value 原始值
     * @return 百分比数值
     */
    private Double toPercent(Double value) {
        if (value == null) {
            return null;
        }
        return Math.round(value * 10000D) / 100D;
    }

    /**
     * 计算比例值。
     *
     * @param numerator 分子
     * @param denominator 分母
     * @return 比例值
     */
    private Double calculateRate(long numerator, long denominator) {
        if (denominator <= 0) {
            return null;
        }
        return Math.round((numerator * 10000D / denominator)) / 100D;
    }

    /**
     * 计算缓存命中率。
     *
     * @param hits 命中次数
     * @param misses 未命中次数
     * @return 命中率
     */
    private Double calculateHitRate(Long hits, Long misses) {
        if (hits == null || misses == null) {
            return null;
        }
        long total = hits + misses;
        if (total <= 0) {
            return null;
        }
        return Math.round((hits * 10000D / total)) / 100D;
    }

    /**
     * 读取 Redis INFO 属性。
     *
     * @param properties INFO 结果
     * @param key 属性键
     * @return 属性值
     */
    private String readProperty(Properties properties, String key) {
        return properties == null ? null : properties.getProperty(key);
    }

    /**
     * 解析整数字符串。
     *
     * @param value 原始文本
     * @return 整数值
     */
    private Integer parseInteger(String value) {
        return value == null || value.isBlank() ? null : Integer.parseInt(value);
    }

    /**
     * 解析长整数字符串。
     *
     * @param value 原始文本
     * @return 长整数值
     */
    private Long parseLong(String value) {
        return value == null || value.isBlank() ? null : Long.parseLong(value);
    }

    /**
     * 格式化时间文本。
     *
     * @param value 原始时间
     * @return 格式化时间文本
     */
    private String formatDateTime(LocalDateTime value) {
        return value == null ? null : value.format(DATE_TIME_FORMATTER);
    }

    /**
     * 兜底文本值。
     *
     * @param value 原始文本
     * @param fallback 兜底文本
     * @return 兜底后的文本
     */
    private String defaultText(String value, String fallback) {
        return value == null || value.isBlank() ? fallback : value;
    }
}
