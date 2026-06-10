package com.manzhushaka.system.vo.monitor;

import java.util.List;

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

    public String getApplicationName() {
        return applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public String getActiveProfile() {
        return activeProfile;
    }

    public void setActiveProfile(String activeProfile) {
        this.activeProfile = activeProfile;
    }

    public String getJavaVersion() {
        return javaVersion;
    }

    public void setJavaVersion(String javaVersion) {
        this.javaVersion = javaVersion;
    }

    public String getOsName() {
        return osName;
    }

    public void setOsName(String osName) {
        this.osName = osName;
    }

    public String getOsArch() {
        return osArch;
    }

    public void setOsArch(String osArch) {
        this.osArch = osArch;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public long getUptimeMillis() {
        return uptimeMillis;
    }

    public void setUptimeMillis(long uptimeMillis) {
        this.uptimeMillis = uptimeMillis;
    }

    public SystemInfo getSystem() {
        return system;
    }

    public void setSystem(SystemInfo system) {
        this.system = system;
    }

    public JvmInfo getJvm() {
        return jvm;
    }

    public void setJvm(JvmInfo jvm) {
        this.jvm = jvm;
    }

    public RedisInfo getRedis() {
        return redis;
    }

    public void setRedis(RedisInfo redis) {
        this.redis = redis;
    }

    public static class SystemInfo {
        private Integer availableProcessors;
        private Double systemCpuUsage;
        private Double processCpuUsage;
        private Long totalPhysicalMemory;
        private Long freePhysicalMemory;

        public Integer getAvailableProcessors() {
            return availableProcessors;
        }

        public void setAvailableProcessors(Integer availableProcessors) {
            this.availableProcessors = availableProcessors;
        }

        public Double getSystemCpuUsage() {
            return systemCpuUsage;
        }

        public void setSystemCpuUsage(Double systemCpuUsage) {
            this.systemCpuUsage = systemCpuUsage;
        }

        public Double getProcessCpuUsage() {
            return processCpuUsage;
        }

        public void setProcessCpuUsage(Double processCpuUsage) {
            this.processCpuUsage = processCpuUsage;
        }

        public Long getTotalPhysicalMemory() {
            return totalPhysicalMemory;
        }

        public void setTotalPhysicalMemory(Long totalPhysicalMemory) {
            this.totalPhysicalMemory = totalPhysicalMemory;
        }

        public Long getFreePhysicalMemory() {
            return freePhysicalMemory;
        }

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

        public String getVmName() {
            return vmName;
        }

        public void setVmName(String vmName) {
            this.vmName = vmName;
        }

        public String getVmVendor() {
            return vmVendor;
        }

        public void setVmVendor(String vmVendor) {
            this.vmVendor = vmVendor;
        }

        public String getVmVersion() {
            return vmVersion;
        }

        public void setVmVersion(String vmVersion) {
            this.vmVersion = vmVersion;
        }

        public List<String> getInputArguments() {
            return inputArguments;
        }

        public void setInputArguments(List<String> inputArguments) {
            this.inputArguments = inputArguments;
        }

        public Long getHeapInit() {
            return heapInit;
        }

        public void setHeapInit(Long heapInit) {
            this.heapInit = heapInit;
        }

        public Long getHeapUsed() {
            return heapUsed;
        }

        public void setHeapUsed(Long heapUsed) {
            this.heapUsed = heapUsed;
        }

        public Long getHeapCommitted() {
            return heapCommitted;
        }

        public void setHeapCommitted(Long heapCommitted) {
            this.heapCommitted = heapCommitted;
        }

        public Long getHeapMax() {
            return heapMax;
        }

        public void setHeapMax(Long heapMax) {
            this.heapMax = heapMax;
        }

        public Long getNonHeapUsed() {
            return nonHeapUsed;
        }

        public void setNonHeapUsed(Long nonHeapUsed) {
            this.nonHeapUsed = nonHeapUsed;
        }

        public Long getNonHeapCommitted() {
            return nonHeapCommitted;
        }

        public void setNonHeapCommitted(Long nonHeapCommitted) {
            this.nonHeapCommitted = nonHeapCommitted;
        }

        public Long getNonHeapMax() {
            return nonHeapMax;
        }

        public void setNonHeapMax(Long nonHeapMax) {
            this.nonHeapMax = nonHeapMax;
        }

        public Integer getLiveThreadCount() {
            return liveThreadCount;
        }

        public void setLiveThreadCount(Integer liveThreadCount) {
            this.liveThreadCount = liveThreadCount;
        }

        public Integer getDaemonThreadCount() {
            return daemonThreadCount;
        }

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
        private String errorMessage;

        public Boolean getAvailable() {
            return available;
        }

        public void setAvailable(Boolean available) {
            this.available = available;
        }

        public String getVersion() {
            return version;
        }

        public void setVersion(String version) {
            this.version = version;
        }

        public Integer getConnectedClients() {
            return connectedClients;
        }

        public void setConnectedClients(Integer connectedClients) {
            this.connectedClients = connectedClients;
        }

        public Long getUsedMemory() {
            return usedMemory;
        }

        public void setUsedMemory(Long usedMemory) {
            this.usedMemory = usedMemory;
        }

        public Long getUsedMemoryPeak() {
            return usedMemoryPeak;
        }

        public void setUsedMemoryPeak(Long usedMemoryPeak) {
            this.usedMemoryPeak = usedMemoryPeak;
        }

        public Long getDbSize() {
            return dbSize;
        }

        public void setDbSize(Long dbSize) {
            this.dbSize = dbSize;
        }

        public String getErrorMessage() {
            return errorMessage;
        }

        public void setErrorMessage(String errorMessage) {
            this.errorMessage = errorMessage;
        }
    }
}
