package com.manzhushaka.system.vo.monitor;

import java.util.List;

/**
 * 承载 ServerMonitorVO 响应数据。
 */
public class ServerMonitorVO {
    private String applicationName;
    private String activeProfile;
    private String javaVersion;
    private String osName;
    private String osArch;
    private String startTime;
    private long uptimeMillis;
    private SystemInfo system;
    private JvmInfo jvm;
    private RedisInfo redis;
    private JobHealth jobHealth;
    private MessageBacklog messageBacklog;
    private SlowSqlSummary slowSql;
    private LogTailSummary logTail;

    /**
     * 返回应用名称。
     *
     * @return 应用名称
     */
    public String getApplicationName() {
        return applicationName;
    }

    /**
     * 设置应用名称。
     *
     * @param applicationName 应用名称
     */
    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    /**
     * 返回激活环境。
     *
     * @return 激活环境
     */
    public String getActiveProfile() {
        return activeProfile;
    }

    /**
     * 设置激活环境。
     *
     * @param activeProfile 激活环境
     */
    public void setActiveProfile(String activeProfile) {
        this.activeProfile = activeProfile;
    }

    /**
     * 返回 Java 版本。
     *
     * @return Java 版本
     */
    public String getJavaVersion() {
        return javaVersion;
    }

    /**
     * 设置 Java 版本。
     *
     * @param javaVersion Java 版本
     */
    public void setJavaVersion(String javaVersion) {
        this.javaVersion = javaVersion;
    }

    /**
     * 返回操作系统名称。
     *
     * @return 操作系统名称
     */
    public String getOsName() {
        return osName;
    }

    /**
     * 设置操作系统名称。
     *
     * @param osName 操作系统名称
     */
    public void setOsName(String osName) {
        this.osName = osName;
    }

    /**
     * 返回系统架构。
     *
     * @return 系统架构
     */
    public String getOsArch() {
        return osArch;
    }

    /**
     * 设置系统架构。
     *
     * @param osArch 系统架构
     */
    public void setOsArch(String osArch) {
        this.osArch = osArch;
    }

    /**
     * 返回启动时间。
     *
     * @return 启动时间
     */
    public String getStartTime() {
        return startTime;
    }

    /**
     * 设置启动时间。
     *
     * @param startTime 启动时间
     */
    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    /**
     * 返回运行时长。
     *
     * @return 运行时长，单位毫秒
     */
    public long getUptimeMillis() {
        return uptimeMillis;
    }

    /**
     * 设置运行时长。
     *
     * @param uptimeMillis 运行时长，单位毫秒
     */
    public void setUptimeMillis(long uptimeMillis) {
        this.uptimeMillis = uptimeMillis;
    }

    /**
     * 返回系统资源信息。
     *
     * @return 系统资源信息
     */
    public SystemInfo getSystem() {
        return system;
    }

    /**
     * 设置系统资源信息。
     *
     * @param system 系统资源信息
     */
    public void setSystem(SystemInfo system) {
        this.system = system;
    }

    /**
     * 返回 JVM 信息。
     *
     * @return JVM 信息
     */
    public JvmInfo getJvm() {
        return jvm;
    }

    /**
     * 设置 JVM 信息。
     *
     * @param jvm JVM 信息
     */
    public void setJvm(JvmInfo jvm) {
        this.jvm = jvm;
    }

    /**
     * 返回 Redis 信息。
     *
     * @return Redis 信息
     */
    public RedisInfo getRedis() {
        return redis;
    }

    /**
     * 设置 Redis 信息。
     *
     * @param redis Redis 信息
     */
    public void setRedis(RedisInfo redis) {
        this.redis = redis;
    }

    /**
     * 返回任务健康摘要。
     *
     * @return 任务健康摘要
     */
    public JobHealth getJobHealth() {
        return jobHealth;
    }

    /**
     * 设置任务健康摘要。
     *
     * @param jobHealth 任务健康摘要
     */
    public void setJobHealth(JobHealth jobHealth) {
        this.jobHealth = jobHealth;
    }

    /**
     * 返回消息积压摘要。
     *
     * @return 消息积压摘要
     */
    public MessageBacklog getMessageBacklog() {
        return messageBacklog;
    }

    /**
     * 设置消息积压摘要。
     *
     * @param messageBacklog 消息积压摘要
     */
    public void setMessageBacklog(MessageBacklog messageBacklog) {
        this.messageBacklog = messageBacklog;
    }

    /**
     * 返回慢 SQL 摘要。
     *
     * @return 慢 SQL 摘要
     */
    public SlowSqlSummary getSlowSql() {
        return slowSql;
    }

    /**
     * 设置慢 SQL 摘要。
     *
     * @param slowSql 慢 SQL 摘要
     */
    public void setSlowSql(SlowSqlSummary slowSql) {
        this.slowSql = slowSql;
    }

    /**
     * 返回在线日志摘要。
     *
     * @return 在线日志摘要
     */
    public LogTailSummary getLogTail() {
        return logTail;
    }

    /**
     * 设置在线日志摘要。
     *
     * @param logTail 在线日志摘要
     */
    public void setLogTail(LogTailSummary logTail) {
        this.logTail = logTail;
    }

    public static class SystemInfo {
        private Integer availableProcessors;
        private Double systemCpuUsage;
        private Double processCpuUsage;
        private Long totalPhysicalMemory;
        private Long freePhysicalMemory;

        /**
         * 返回可用 CPU 核数。
         *
         * @return 可用 CPU 核数
         */
        public Integer getAvailableProcessors() {
            return availableProcessors;
        }

        /**
         * 设置可用 CPU 核数。
         *
         * @param availableProcessors 可用 CPU 核数
         */
        public void setAvailableProcessors(Integer availableProcessors) {
            this.availableProcessors = availableProcessors;
        }

        /**
         * 返回系统 CPU 使用率。
         *
         * @return 系统 CPU 使用率
         */
        public Double getSystemCpuUsage() {
            return systemCpuUsage;
        }

        /**
         * 设置系统 CPU 使用率。
         *
         * @param systemCpuUsage 系统 CPU 使用率
         */
        public void setSystemCpuUsage(Double systemCpuUsage) {
            this.systemCpuUsage = systemCpuUsage;
        }

        /**
         * 返回进程 CPU 使用率。
         *
         * @return 进程 CPU 使用率
         */
        public Double getProcessCpuUsage() {
            return processCpuUsage;
        }

        /**
         * 设置进程 CPU 使用率。
         *
         * @param processCpuUsage 进程 CPU 使用率
         */
        public void setProcessCpuUsage(Double processCpuUsage) {
            this.processCpuUsage = processCpuUsage;
        }

        /**
         * 返回物理内存总量。
         *
         * @return 物理内存总量
         */
        public Long getTotalPhysicalMemory() {
            return totalPhysicalMemory;
        }

        /**
         * 设置物理内存总量。
         *
         * @param totalPhysicalMemory 物理内存总量
         */
        public void setTotalPhysicalMemory(Long totalPhysicalMemory) {
            this.totalPhysicalMemory = totalPhysicalMemory;
        }

        /**
         * 返回空闲物理内存。
         *
         * @return 空闲物理内存
         */
        public Long getFreePhysicalMemory() {
            return freePhysicalMemory;
        }

        /**
         * 设置空闲物理内存。
         *
         * @param freePhysicalMemory 空闲物理内存
         */
        public void setFreePhysicalMemory(Long freePhysicalMemory) {
            this.freePhysicalMemory = freePhysicalMemory;
        }
    }

    public static class JvmInfo {
        private String vmName;
        private String vmVendor;
        private String vmVersion;
        private List<String> inputArguments;
        private Long heapInit;
        private Long heapUsed;
        private Long heapCommitted;
        private Long heapMax;
        private Long nonHeapUsed;
        private Long nonHeapCommitted;
        private Long nonHeapMax;
        private Integer liveThreadCount;
        private Integer daemonThreadCount;

        /**
         * 返回虚拟机名称。
         *
         * @return 虚拟机名称
         */
        public String getVmName() {
            return vmName;
        }

        /**
         * 设置虚拟机名称。
         *
         * @param vmName 虚拟机名称
         */
        public void setVmName(String vmName) {
            this.vmName = vmName;
        }

        /**
         * 返回虚拟机供应商。
         *
         * @return 虚拟机供应商
         */
        public String getVmVendor() {
            return vmVendor;
        }

        /**
         * 设置虚拟机供应商。
         *
         * @param vmVendor 虚拟机供应商
         */
        public void setVmVendor(String vmVendor) {
            this.vmVendor = vmVendor;
        }

        /**
         * 返回虚拟机版本。
         *
         * @return 虚拟机版本
         */
        public String getVmVersion() {
            return vmVersion;
        }

        /**
         * 设置虚拟机版本。
         *
         * @param vmVersion 虚拟机版本
         */
        public void setVmVersion(String vmVersion) {
            this.vmVersion = vmVersion;
        }

        /**
         * 返回 JVM 启动参数。
         *
         * @return JVM 启动参数
         */
        public List<String> getInputArguments() {
            return inputArguments;
        }

        /**
         * 设置 JVM 启动参数。
         *
         * @param inputArguments JVM 启动参数
         */
        public void setInputArguments(List<String> inputArguments) {
            this.inputArguments = inputArguments;
        }

        /**
         * 返回堆初始大小。
         *
         * @return 堆初始大小
         */
        public Long getHeapInit() {
            return heapInit;
        }

        /**
         * 设置堆初始大小。
         *
         * @param heapInit 堆初始大小
         */
        public void setHeapInit(Long heapInit) {
            this.heapInit = heapInit;
        }

        /**
         * 返回堆已使用大小。
         *
         * @return 堆已使用大小
         */
        public Long getHeapUsed() {
            return heapUsed;
        }

        /**
         * 设置堆已使用大小。
         *
         * @param heapUsed 堆已使用大小
         */
        public void setHeapUsed(Long heapUsed) {
            this.heapUsed = heapUsed;
        }

        /**
         * 返回堆已提交大小。
         *
         * @return 堆已提交大小
         */
        public Long getHeapCommitted() {
            return heapCommitted;
        }

        /**
         * 设置堆已提交大小。
         *
         * @param heapCommitted 堆已提交大小
         */
        public void setHeapCommitted(Long heapCommitted) {
            this.heapCommitted = heapCommitted;
        }

        /**
         * 返回堆最大大小。
         *
         * @return 堆最大大小
         */
        public Long getHeapMax() {
            return heapMax;
        }

        /**
         * 设置堆最大大小。
         *
         * @param heapMax 堆最大大小
         */
        public void setHeapMax(Long heapMax) {
            this.heapMax = heapMax;
        }

        /**
         * 返回非堆已使用大小。
         *
         * @return 非堆已使用大小
         */
        public Long getNonHeapUsed() {
            return nonHeapUsed;
        }

        /**
         * 设置非堆已使用大小。
         *
         * @param nonHeapUsed 非堆已使用大小
         */
        public void setNonHeapUsed(Long nonHeapUsed) {
            this.nonHeapUsed = nonHeapUsed;
        }

        /**
         * 返回非堆已提交大小。
         *
         * @return 非堆已提交大小
         */
        public Long getNonHeapCommitted() {
            return nonHeapCommitted;
        }

        /**
         * 设置非堆已提交大小。
         *
         * @param nonHeapCommitted 非堆已提交大小
         */
        public void setNonHeapCommitted(Long nonHeapCommitted) {
            this.nonHeapCommitted = nonHeapCommitted;
        }

        /**
         * 返回非堆最大大小。
         *
         * @return 非堆最大大小
         */
        public Long getNonHeapMax() {
            return nonHeapMax;
        }

        /**
         * 设置非堆最大大小。
         *
         * @param nonHeapMax 非堆最大大小
         */
        public void setNonHeapMax(Long nonHeapMax) {
            this.nonHeapMax = nonHeapMax;
        }

        /**
         * 返回活动线程数。
         *
         * @return 活动线程数
         */
        public Integer getLiveThreadCount() {
            return liveThreadCount;
        }

        /**
         * 设置活动线程数。
         *
         * @param liveThreadCount 活动线程数
         */
        public void setLiveThreadCount(Integer liveThreadCount) {
            this.liveThreadCount = liveThreadCount;
        }

        /**
         * 返回守护线程数。
         *
         * @return 守护线程数
         */
        public Integer getDaemonThreadCount() {
            return daemonThreadCount;
        }

        /**
         * 设置守护线程数。
         *
         * @param daemonThreadCount 守护线程数
         */
        public void setDaemonThreadCount(Integer daemonThreadCount) {
            this.daemonThreadCount = daemonThreadCount;
        }
    }

    public static class RedisInfo {
        private Boolean available;
        private String version;
        private Integer connectedClients;
        private Long usedMemory;
        private Long usedMemoryPeak;
        private Long dbSize;
        private Long keyspaceHits;
        private Long keyspaceMisses;
        private Double hitRate;
        private Long expiredKeys;
        private Long evictedKeys;
        private String errorMessage;

        /**
         * 返回 Redis 是否可用。
         *
         * @return Redis 是否可用
         */
        public Boolean getAvailable() {
            return available;
        }

        /**
         * 设置 Redis 是否可用。
         *
         * @param available Redis 是否可用
         */
        public void setAvailable(Boolean available) {
            this.available = available;
        }

        /**
         * 返回 Redis 版本。
         *
         * @return Redis 版本
         */
        public String getVersion() {
            return version;
        }

        /**
         * 设置 Redis 版本。
         *
         * @param version Redis 版本
         */
        public void setVersion(String version) {
            this.version = version;
        }

        /**
         * 返回客户端连接数。
         *
         * @return 客户端连接数
         */
        public Integer getConnectedClients() {
            return connectedClients;
        }

        /**
         * 设置客户端连接数。
         *
         * @param connectedClients 客户端连接数
         */
        public void setConnectedClients(Integer connectedClients) {
            this.connectedClients = connectedClients;
        }

        /**
         * 返回当前内存占用。
         *
         * @return 当前内存占用
         */
        public Long getUsedMemory() {
            return usedMemory;
        }

        /**
         * 设置当前内存占用。
         *
         * @param usedMemory 当前内存占用
         */
        public void setUsedMemory(Long usedMemory) {
            this.usedMemory = usedMemory;
        }

        /**
         * 返回峰值内存占用。
         *
         * @return 峰值内存占用
         */
        public Long getUsedMemoryPeak() {
            return usedMemoryPeak;
        }

        /**
         * 设置峰值内存占用。
         *
         * @param usedMemoryPeak 峰值内存占用
         */
        public void setUsedMemoryPeak(Long usedMemoryPeak) {
            this.usedMemoryPeak = usedMemoryPeak;
        }

        /**
         * 返回 DB key 数量。
         *
         * @return DB key 数量
         */
        public Long getDbSize() {
            return dbSize;
        }

        /**
         * 设置 DB key 数量。
         *
         * @param dbSize DB key 数量
         */
        public void setDbSize(Long dbSize) {
            this.dbSize = dbSize;
        }

        /**
         * 返回缓存命中次数。
         *
         * @return 缓存命中次数
         */
        public Long getKeyspaceHits() {
            return keyspaceHits;
        }

        /**
         * 设置缓存命中次数。
         *
         * @param keyspaceHits 缓存命中次数
         */
        public void setKeyspaceHits(Long keyspaceHits) {
            this.keyspaceHits = keyspaceHits;
        }

        /**
         * 返回缓存未命中次数。
         *
         * @return 缓存未命中次数
         */
        public Long getKeyspaceMisses() {
            return keyspaceMisses;
        }

        /**
         * 设置缓存未命中次数。
         *
         * @param keyspaceMisses 缓存未命中次数
         */
        public void setKeyspaceMisses(Long keyspaceMisses) {
            this.keyspaceMisses = keyspaceMisses;
        }

        /**
         * 返回缓存命中率。
         *
         * @return 缓存命中率
         */
        public Double getHitRate() {
            return hitRate;
        }

        /**
         * 设置缓存命中率。
         *
         * @param hitRate 缓存命中率
         */
        public void setHitRate(Double hitRate) {
            this.hitRate = hitRate;
        }

        /**
         * 返回过期 key 数量。
         *
         * @return 过期 key 数量
         */
        public Long getExpiredKeys() {
            return expiredKeys;
        }

        /**
         * 设置过期 key 数量。
         *
         * @param expiredKeys 过期 key 数量
         */
        public void setExpiredKeys(Long expiredKeys) {
            this.expiredKeys = expiredKeys;
        }

        /**
         * 返回淘汰 key 数量。
         *
         * @return 淘汰 key 数量
         */
        public Long getEvictedKeys() {
            return evictedKeys;
        }

        /**
         * 设置淘汰 key 数量。
         *
         * @param evictedKeys 淘汰 key 数量
         */
        public void setEvictedKeys(Long evictedKeys) {
            this.evictedKeys = evictedKeys;
        }

        /**
         * 返回异常信息。
         *
         * @return 异常信息
         */
        public String getErrorMessage() {
            return errorMessage;
        }

        /**
         * 设置异常信息。
         *
         * @param errorMessage 异常信息
         */
        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }
    }

    public static class JobHealth {
        private Long totalJobs;
        private Long enabledJobs;
        private Long pausedJobs;
        private Long recentSuccessCount;
        private Long recentFailCount;
        private Double recentSuccessRate;
        private List<JobFailureItem> recentFailures;

        /**
         * 返回 totalJobs。
         *
         * @return 字段值
         */
        public Long getTotalJobs() {
            return totalJobs;
        }

        /**
         * 设置 totalJobs。
         *
         * @param totalJobs totalJobs 参数
         */
        public void setTotalJobs(Long totalJobs) {
            this.totalJobs = totalJobs;
        }

        /**
         * 返回 enabledJobs。
         *
         * @return 字段值
         */
        public Long getEnabledJobs() {
            return enabledJobs;
        }

        /**
         * 设置 enabledJobs。
         *
         * @param enabledJobs enabledJobs 参数
         */
        public void setEnabledJobs(Long enabledJobs) {
            this.enabledJobs = enabledJobs;
        }

        /**
         * 返回 pausedJobs。
         *
         * @return 字段值
         */
        public Long getPausedJobs() {
            return pausedJobs;
        }

        /**
         * 设置 pausedJobs。
         *
         * @param pausedJobs pausedJobs 参数
         */
        public void setPausedJobs(Long pausedJobs) {
            this.pausedJobs = pausedJobs;
        }

        /**
         * 返回 recentSuccessCount。
         *
         * @return 字段值
         */
        public Long getRecentSuccessCount() {
            return recentSuccessCount;
        }

        /**
         * 设置 recentSuccessCount。
         *
         * @param recentSuccessCount recentSuccessCount 参数
         */
        public void setRecentSuccessCount(Long recentSuccessCount) {
            this.recentSuccessCount = recentSuccessCount;
        }

        /**
         * 返回 recentFailCount。
         *
         * @return 字段值
         */
        public Long getRecentFailCount() {
            return recentFailCount;
        }

        /**
         * 设置 recentFailCount。
         *
         * @param recentFailCount recentFailCount 参数
         */
        public void setRecentFailCount(Long recentFailCount) {
            this.recentFailCount = recentFailCount;
        }

        /**
         * 返回 recentSuccessRate。
         *
         * @return 字段值
         */
        public Double getRecentSuccessRate() {
            return recentSuccessRate;
        }

        /**
         * 设置 recentSuccessRate。
         *
         * @param recentSuccessRate recentSuccessRate 参数
         */
        public void setRecentSuccessRate(Double recentSuccessRate) {
            this.recentSuccessRate = recentSuccessRate;
        }

        /**
         * 返回 recentFailures。
         *
         * @return 字段值
         */
        public List<JobFailureItem> getRecentFailures() {
            return recentFailures;
        }

        /**
         * 设置 recentFailures。
         *
         * @param recentFailures recentFailures 参数
         */
        public void setRecentFailures(List<JobFailureItem> recentFailures) {
            this.recentFailures = recentFailures;
        }
    }

    public static class JobFailureItem {
        private Long jobId;
        private String jobName;
        private String runStatus;
        private String errorMsg;
        private String startTime;

        /**
         * 返回 jobId。
         *
         * @return 字段值
         */
        public Long getJobId() {
            return jobId;
        }

        /**
         * 设置 jobId。
         *
         * @param jobId jobId 标识
         */
        public void setJobId(Long jobId) {
            this.jobId = jobId;
        }

        /**
         * 返回 jobName。
         *
         * @return 字段值
         */
        public String getJobName() {
            return jobName;
        }

        /**
         * 设置 jobName。
         *
         * @param jobName jobName 参数
         */
        public void setJobName(String jobName) {
            this.jobName = jobName;
        }

        /**
         * 返回 runStatus。
         *
         * @return 字段值
         */
        public String getRunStatus() {
            return runStatus;
        }

        /**
         * 设置 runStatus。
         *
         * @param runStatus runStatus 参数
         */
        public void setRunStatus(String runStatus) {
            this.runStatus = runStatus;
        }

        /**
         * 返回 errorMsg。
         *
         * @return 字段值
         */
        public String getErrorMsg() {
            return errorMsg;
        }

        /**
         * 设置 errorMsg。
         *
         * @param errorMsg errorMsg 参数
         */
        public void setErrorMsg(String errorMsg) {
            this.errorMsg = errorMsg;
        }

        /**
         * 返回 startTime。
         *
         * @return 字段值
         */
        public String getStartTime() {
            return startTime;
        }

        /**
         * 设置 startTime。
         *
         * @param startTime startTime 参数
         */
        public void setStartTime(String startTime) {
            this.startTime = startTime;
        }
    }

    public static class MessageBacklog {
        private Long pendingCount;
        private Long processingCount;
        private Long failCount;
        private Long initCount;
        private Long publishedCount;
        private Long timedOutCount;
        private String oldestPendingEventId;
        private String oldestPendingCreateTime;
        private List<StreamBacklogItem> streams;

        /**
         * 返回 pendingCount。
         *
         * @return 字段值
         */
        public Long getPendingCount() {
            return pendingCount;
        }

        /**
         * 设置 pendingCount。
         *
         * @param pendingCount pendingCount 参数
         */
        public void setPendingCount(Long pendingCount) {
            this.pendingCount = pendingCount;
        }

        /**
         * 返回 processingCount。
         *
         * @return 字段值
         */
        public Long getProcessingCount() {
            return processingCount;
        }

        /**
         * 设置 processingCount。
         *
         * @param processingCount processingCount 参数
         */
        public void setProcessingCount(Long processingCount) {
            this.processingCount = processingCount;
        }

        /**
         * 返回 failCount。
         *
         * @return 字段值
         */
        public Long getFailCount() {
            return failCount;
        }

        /**
         * 设置 failCount。
         *
         * @param failCount failCount 参数
         */
        public void setFailCount(Long failCount) {
            this.failCount = failCount;
        }

        /**
         * 返回 initCount。
         *
         * @return 字段值
         */
        public Long getInitCount() {
            return initCount;
        }

        /**
         * 设置 initCount。
         *
         * @param initCount initCount 参数
         */
        public void setInitCount(Long initCount) {
            this.initCount = initCount;
        }

        /**
         * 返回 publishedCount。
         *
         * @return 字段值
         */
        public Long getPublishedCount() {
            return publishedCount;
        }

        /**
         * 设置 publishedCount。
         *
         * @param publishedCount publishedCount 参数
         */
        public void setPublishedCount(Long publishedCount) {
            this.publishedCount = publishedCount;
        }

        /**
         * 返回 timedOutCount。
         *
         * @return 字段值
         */
        public Long getTimedOutCount() {
            return timedOutCount;
        }

        /**
         * 设置 timedOutCount。
         *
         * @param timedOutCount timedOutCount 参数
         */
        public void setTimedOutCount(Long timedOutCount) {
            this.timedOutCount = timedOutCount;
        }

        /**
         * 返回 oldestPendingEventId。
         *
         * @return 字段值
         */
        public String getOldestPendingEventId() {
            return oldestPendingEventId;
        }

        /**
         * 设置 oldestPendingEventId。
         *
         * @param oldestPendingEventId oldestPendingEventId 标识
         */
        public void setOldestPendingEventId(String oldestPendingEventId) {
            this.oldestPendingEventId = oldestPendingEventId;
        }

        /**
         * 返回 oldestPendingCreateTime。
         *
         * @return 字段值
         */
        public String getOldestPendingCreateTime() {
            return oldestPendingCreateTime;
        }

        /**
         * 设置 oldestPendingCreateTime。
         *
         * @param oldestPendingCreateTime oldestPendingCreateTime 参数
         */
        public void setOldestPendingCreateTime(String oldestPendingCreateTime) {
            this.oldestPendingCreateTime = oldestPendingCreateTime;
        }

        /**
         * 返回 streams。
         *
         * @return 字段值
         */
        public List<StreamBacklogItem> getStreams() {
            return streams;
        }

        /**
         * 设置 streams。
         *
         * @param streams streams 参数
         */
        public void setStreams(List<StreamBacklogItem> streams) {
            this.streams = streams;
        }
    }

    public static class StreamBacklogItem {
        private String streamKey;
        private Long pendingCount;
        private Long failCount;

        /**
         * 返回 streamKey。
         *
         * @return 字段值
         */
        public String getStreamKey() {
            return streamKey;
        }

        /**
         * 设置 streamKey。
         *
         * @param streamKey streamKey 参数
         */
        public void setStreamKey(String streamKey) {
            this.streamKey = streamKey;
        }

        /**
         * 返回 pendingCount。
         *
         * @return 字段值
         */
        public Long getPendingCount() {
            return pendingCount;
        }

        /**
         * 设置 pendingCount。
         *
         * @param pendingCount pendingCount 参数
         */
        public void setPendingCount(Long pendingCount) {
            this.pendingCount = pendingCount;
        }

        /**
         * 返回 failCount。
         *
         * @return 字段值
         */
        public Long getFailCount() {
            return failCount;
        }

        /**
         * 设置 failCount。
         *
         * @param failCount failCount 参数
         */
        public void setFailCount(Long failCount) {
            this.failCount = failCount;
        }
    }

    public static class SlowSqlSummary {
        private Boolean available;
        private Integer recentCount;
        private Long latestCostMs;
        private String latestStatementId;
        private String latestExecuteTime;
        private Long thresholdMs;

        /**
         * 返回 available。
         *
         * @return 字段值
         */
        public Boolean getAvailable() {
            return available;
        }

        /**
         * 设置 available。
         *
         * @param available available 参数
         */
        public void setAvailable(Boolean available) {
            this.available = available;
        }

        /**
         * 返回 recentCount。
         *
         * @return 字段值
         */
        public Integer getRecentCount() {
            return recentCount;
        }

        /**
         * 设置 recentCount。
         *
         * @param recentCount recentCount 参数
         */
        public void setRecentCount(Integer recentCount) {
            this.recentCount = recentCount;
        }

        /**
         * 返回 latestCostMs。
         *
         * @return 字段值
         */
        public Long getLatestCostMs() {
            return latestCostMs;
        }

        /**
         * 设置 latestCostMs。
         *
         * @param latestCostMs latestCostMs 参数
         */
        public void setLatestCostMs(Long latestCostMs) {
            this.latestCostMs = latestCostMs;
        }

        /**
         * 返回 latestStatementId。
         *
         * @return 字段值
         */
        public String getLatestStatementId() {
            return latestStatementId;
        }

        /**
         * 设置 latestStatementId。
         *
         * @param latestStatementId latestStatementId 标识
         */
        public void setLatestStatementId(String latestStatementId) {
            this.latestStatementId = latestStatementId;
        }

        /**
         * 返回 latestExecuteTime。
         *
         * @return 字段值
         */
        public String getLatestExecuteTime() {
            return latestExecuteTime;
        }

        /**
         * 设置 latestExecuteTime。
         *
         * @param latestExecuteTime latestExecuteTime 参数
         */
        public void setLatestExecuteTime(String latestExecuteTime) {
            this.latestExecuteTime = latestExecuteTime;
        }

        /**
         * 返回 thresholdMs。
         *
         * @return 字段值
         */
        public Long getThresholdMs() {
            return thresholdMs;
        }

        /**
         * 设置 thresholdMs。
         *
         * @param thresholdMs thresholdMs 参数
         */
        public void setThresholdMs(Long thresholdMs) {
            this.thresholdMs = thresholdMs;
        }
    }

    public static class LogTailSummary {
        private Boolean available;
        private Integer entryCount;
        private Integer capacity;
        private String lastEntryAt;

        /**
         * 返回 available。
         *
         * @return 字段值
         */
        public Boolean getAvailable() {
            return available;
        }

        /**
         * 设置 available。
         *
         * @param available available 参数
         */
        public void setAvailable(Boolean available) {
            this.available = available;
        }

        /**
         * 返回 entryCount。
         *
         * @return 字段值
         */
        public Integer getEntryCount() {
            return entryCount;
        }

        /**
         * 设置 entryCount。
         *
         * @param entryCount entryCount 参数
         */
        public void setEntryCount(Integer entryCount) {
            this.entryCount = entryCount;
        }

        /**
         * 返回 capacity。
         *
         * @return 字段值
         */
        public Integer getCapacity() {
            return capacity;
        }

        /**
         * 设置 capacity。
         *
         * @param capacity capacity 参数
         */
        public void setCapacity(Integer capacity) {
            this.capacity = capacity;
        }

        /**
         * 返回 lastEntryAt。
         *
         * @return 字段值
         */
        public String getLastEntryAt() {
            return lastEntryAt;
        }

        /**
         * 设置 lastEntryAt。
         *
         * @param lastEntryAt lastEntryAt 参数
         */
        public void setLastEntryAt(String lastEntryAt) {
            this.lastEntryAt = lastEntryAt;
        }
    }
}
