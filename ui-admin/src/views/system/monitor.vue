<template>
  <div class="system-page">
    <PageHeaderCard mode="toolbar">
      <a-space wrap>
        <a-button v-permission="'system:monitor:refresh'" :loading="loading" @click="fetchMonitor(true)">刷新</a-button>
        <a-tag bordered :color="overallHealth.color">{{ overallHealth.label }}</a-tag>
        <a-tag bordered color="arcoblue">最后更新 {{ lastUpdatedText }}</a-tag>
      </a-space>
    </PageHeaderCard>

    <a-spin :loading="loading" class="monitor-spin">
      <div class="monitor-grid">
        <a-card class="page-card hero-card" :bordered="false">
          <div class="hero-shell">
            <div class="hero-main">
              <div class="hero-eyebrow">Runtime Observatory</div>
              <div class="hero-heading">
                <div>
                  <div class="hero-title">{{ monitor?.applicationName || '应用服务' }}</div>
                  <div class="hero-description">
                    面向运维和管理员的一屏运行态视图，聚焦当前实例的负载、内存水位与缓存连通性。
                  </div>
                </div>
                <a-tag bordered size="large" :color="overallHealth.color">
                  {{ overallHealth.label }}
                </a-tag>
              </div>

              <div class="hero-overview">
                <div v-for="item in overviewItems" :key="item.label" class="overview-item">
                  <div class="overview-label">{{ item.label }}</div>
                  <div class="overview-value">{{ item.value }}</div>
                </div>
              </div>
            </div>

            <div class="hero-side">
              <div v-for="item in heroStatusItems" :key="item.label" class="status-pill" :class="`status-pill--${item.tone}`">
                <div class="status-pill__label">{{ item.label }}</div>
                <div class="status-pill__value">{{ item.value }}</div>
                <div class="status-pill__detail">{{ item.detail }}</div>
              </div>
            </div>
          </div>
        </a-card>

        <div class="metric-grid">
          <a-card
            v-for="item in metricCards"
            :key="item.key"
            class="page-card metric-card"
            :class="`metric-card--${item.tone}`"
            :bordered="false"
          >
            <div class="metric-head">
              <div class="metric-heading">
                <div class="metric-icon" :class="`metric-icon--${item.tone}`">
                  <component :is="item.icon" />
                </div>
                <div>
                  <div class="metric-title">{{ item.title }}</div>
                  <div class="metric-subtitle">{{ item.subtitle }}</div>
                </div>
              </div>
              <a-tag bordered size="small" :color="item.tagColor">{{ item.tagText }}</a-tag>
            </div>

            <div class="metric-value">{{ item.value }}</div>
            <a-progress
              :percent="item.percent"
              :status="item.progressStatus"
              :stroke-width="10"
              :show-text="false"
            />
            <div class="metric-note">{{ item.note }}</div>
          </a-card>
        </div>

        <div class="detail-grid">
          <a-card class="page-card detail-card" :bordered="false">
            <div class="section-header">
              <div>
                <div class="section-title">资源详情</div>
                <div class="section-description">把物理内存和 JVM 堆使用情况拆成容量视图，线程与可用资源留在同一区域快速扫读。</div>
              </div>
              <a-tag bordered color="arcoblue">线程 {{ formatInteger(monitor?.jvm.liveThreadCount) }}</a-tag>
            </div>

            <div class="capacity-grid">
              <div
                v-for="item in capacityCards"
                :key="item.key"
                class="capacity-card"
                :class="`capacity-card--${item.tone}`"
              >
                <div class="capacity-ring">
                  <a-progress
                    type="circle"
                    :percent="item.percent"
                    :status="item.progressStatus"
                    :width="108"
                    :show-text="false"
                  />
                  <div class="capacity-ring__center">
                    <div class="capacity-ring__value">{{ item.value }}</div>
                  </div>
                </div>
                <div class="capacity-copy">
                  <div class="capacity-title">{{ item.title }}</div>
                  <div class="capacity-note">{{ item.note }}</div>
                </div>
              </div>
            </div>

            <div class="stat-grid">
              <div v-for="item in resourceStats" :key="item.label" class="stat-item">
                <div class="stat-label">{{ item.label }}</div>
                <div class="stat-value">{{ item.value }}</div>
                <div class="stat-hint">{{ item.hint }}</div>
              </div>
            </div>
          </a-card>

          <a-card class="page-card detail-card redis-card" :bordered="false">
            <div class="section-header">
              <div>
                <div class="section-title">Redis 状态</div>
                <div class="section-description">聚焦缓存实例连通性、内存爬升趋势和连接规模，不延展到业务 Key 级别排查。</div>
              </div>
              <a-tag bordered :color="redisStatus.color">{{ redisStatus.label }}</a-tag>
            </div>

            <div class="redis-summary">
              <div class="redis-summary__block">
                <div class="redis-summary__label">实例版本</div>
                <div class="redis-summary__value">Redis {{ monitor?.redis.version || '--' }}</div>
                <div class="redis-summary__detail">{{ redisStatus.detail }}</div>
              </div>
              <div class="redis-summary__block redis-summary__block--accent">
                <div class="redis-summary__label">当前内存 / 峰值</div>
                <div class="redis-summary__value">{{ formatPercent(redisPeakUsagePercent) }}</div>
                <div class="redis-summary__detail">
                  {{ formatBytes(monitor?.redis.usedMemory) }} / {{ formatBytes(monitor?.redis.usedMemoryPeak) }}
                </div>
                <a-progress
                  class="redis-summary__progress"
                  :percent="normalizePercent(redisPeakUsagePercent)"
                  :status="getProgressStatus(getLoadTone(redisPeakUsagePercent, 70, 90))"
                  :stroke-width="8"
                  :show-text="false"
                />
              </div>
            </div>

            <a-alert v-if="monitor?.redis.errorMessage" type="warning" :show-icon="true" class="redis-alert">
              {{ monitor.redis.errorMessage }}
            </a-alert>

            <div class="stat-grid redis-stat-grid">
              <div v-for="item in redisStats" :key="item.label" class="stat-item">
                <div class="stat-label">{{ item.label }}</div>
                <div class="stat-value">{{ item.value }}</div>
                <div class="stat-hint">{{ item.hint }}</div>
              </div>
            </div>
          </a-card>
        </div>

        <a-card class="page-card detail-card jvm-card" :bordered="false">
          <div class="section-header">
            <div>
              <div class="section-title">JVM 运行时</div>
              <div class="section-description">补充虚拟机版本、启动参数与堆边界信息，方便快速确认当前实例的 Java 启动配置。</div>
            </div>
            <a-tag bordered color="arcoblue">启动参数 {{ formatInteger(jvmInputArguments.length) }}</a-tag>
          </div>

          <div class="stat-grid jvm-runtime-grid">
            <div v-for="item in jvmRuntimeStats" :key="item.label" class="stat-item">
              <div class="stat-label">{{ item.label }}</div>
              <div class="stat-value">{{ item.value }}</div>
              <div class="stat-hint">{{ item.hint }}</div>
            </div>
          </div>

          <div class="stat-grid jvm-memory-grid">
            <div v-for="item in jvmMemoryStats" :key="item.label" class="stat-item">
              <div class="stat-label">{{ item.label }}</div>
              <div class="stat-value">{{ item.value }}</div>
              <div class="stat-hint">{{ item.hint }}</div>
            </div>
          </div>

          <div class="jvm-arguments">
            <div class="jvm-arguments__header">
              <div class="jvm-arguments__title">启动参数</div>
              <div class="jvm-arguments__subtitle">常见如 `-Xms`、`-Xmx`、GC 策略和系统属性都会出现在这里。</div>
            </div>

            <div v-if="jvmInputArguments.length > 0" class="jvm-arguments__tags">
              <a-tag
                v-for="argument in jvmInputArguments"
                :key="argument"
                bordered
                color="arcoblue"
                class="jvm-argument-tag"
              >
                {{ argument }}
              </a-tag>
            </div>
            <div v-else class="jvm-arguments__empty">当前 JVM 没有额外启动参数</div>
          </div>
        </a-card>
      </div>
    </a-spin>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, type Component } from 'vue';
import { Message } from '@arco-design/web-vue';
import {
  IconCheckCircleFill,
  IconClockCircle,
  IconCode,
  IconDesktop,
  IconExclamationCircleFill,
  IconStorage,
  IconThunderbolt,
} from '@arco-design/web-vue/es/icon';
import PageHeaderCard from '@/components/PageHeaderCard.vue';
import { systemApi } from '@/api/system';
import type { ServerMonitorVO } from '@/types/system';

type MonitorTone = 'healthy' | 'attention' | 'danger' | 'neutral';
type ProgressStatus = 'normal' | 'success' | 'warning' | 'danger';

interface OverviewItem {
  label: string;
  value: string;
}

interface StatusItem {
  label: string;
  value: string;
  detail: string;
  tone: MonitorTone;
}

interface MetricCardItem {
  key: string;
  title: string;
  subtitle: string;
  value: string;
  note: string;
  percent: number;
  tone: MonitorTone;
  progressStatus: ProgressStatus;
  tagColor: string;
  tagText: string;
  icon: Component;
}

interface CapacityCardItem {
  key: string;
  title: string;
  value: string;
  note: string;
  percent: number;
  tone: MonitorTone;
  progressStatus: ProgressStatus;
}

interface StatItem {
  label: string;
  value: string;
  hint: string;
}

const loading = ref(false);
const monitor = ref<ServerMonitorVO | null>(null);
const lastUpdatedText = ref('等待首次采集');

const usedPhysicalMemory = computed(() => {
  if (!monitor.value?.system.totalPhysicalMemory || monitor.value.system.freePhysicalMemory == null) {
    return null;
  }
  return monitor.value.system.totalPhysicalMemory - monitor.value.system.freePhysicalMemory;
});

const physicalMemoryUsagePercent = computed(() => {
  if (!monitor.value?.system.totalPhysicalMemory || usedPhysicalMemory.value == null) {
    return null;
  }
  return (usedPhysicalMemory.value / monitor.value.system.totalPhysicalMemory) * 100;
});

const heapCapacity = computed(() => {
  if (!monitor.value?.jvm.heapMax || monitor.value.jvm.heapMax <= 0) {
    return monitor.value?.jvm.heapCommitted ?? null;
  }
  return monitor.value.jvm.heapMax;
});

const heapUsagePercent = computed(() => {
  if (monitor.value?.jvm.heapUsed == null || !heapCapacity.value) {
    return null;
  }
  return (monitor.value.jvm.heapUsed / heapCapacity.value) * 100;
});

const redisPeakUsagePercent = computed(() => {
  if (!monitor.value?.redis.usedMemoryPeak || monitor.value.redis.usedMemory == null) {
    return null;
  }
  return (monitor.value.redis.usedMemory / monitor.value.redis.usedMemoryPeak) * 100;
});

const jvmInputArguments = computed(() => monitor.value?.jvm.inputArguments ?? []);

const systemCpuTone = computed(() => getLoadTone(monitor.value?.system.systemCpuUsage, 55, 80));
const processCpuTone = computed(() => getLoadTone(monitor.value?.system.processCpuUsage, 45, 70));
const physicalMemoryTone = computed(() => getLoadTone(physicalMemoryUsagePercent.value, 70, 88));
const heapTone = computed(() => getLoadTone(heapUsagePercent.value, 72, 90));

const overallHealth = computed(() => {
  const severity = getToneSeverity(systemCpuTone.value, processCpuTone.value, physicalMemoryTone.value, heapTone.value);
  if (monitor.value?.redis.available === false) {
    return {
      tone: 'danger' as const,
      label: '缓存异常',
      color: 'red',
      description: 'Redis 连通性异常，建议优先排查缓存实例与网络链路。',
    };
  }
  if (severity === 'danger') {
    return {
      tone: 'danger' as const,
      label: '高压运行',
      color: 'red',
      description: '核心资源逼近上限，建议尽快关注 CPU 或内存负载。',
    };
  }
  if (severity === 'attention') {
    return {
      tone: 'attention' as const,
      label: '负载偏高',
      color: 'gold',
      description: '当前实例可用，但部分资源水位正在抬升。',
    };
  }
  if (monitor.value) {
    return {
      tone: 'healthy' as const,
      label: '稳定运行',
      color: 'green',
      description: '核心链路与资源占用保持在健康区间。',
    };
  }
  return {
    tone: 'neutral' as const,
    label: '等待采集',
    color: 'arcoblue',
    description: '正在等待运行监控数据返回。',
  };
});

const redisStatus = computed(() => {
  if (monitor.value?.redis.available) {
    return {
      label: '连接正常',
      color: 'green',
      detail: '缓存实例可访问，当前连接链路状态正常。',
    };
  }
  if (monitor.value?.redis.available === false) {
    return {
      label: '连接异常',
      color: 'red',
      detail: '缓存访问失败，建议检查连接参数、实例状态和网络链路。',
    };
  }
  return {
    label: '待确认',
    color: 'arcoblue',
    detail: '尚未获取到 Redis 运行状态。',
  };
});

const overviewItems = computed<OverviewItem[]>(() => [
  { label: '运行环境', value: monitor.value?.activeProfile || '--' },
  { label: '启动时间', value: monitor.value?.startTime || '--' },
  { label: '运行时长', value: formatDuration(monitor.value?.uptimeMillis) },
  { label: 'Java 版本', value: monitor.value?.javaVersion || '--' },
  { label: '宿主系统', value: monitor.value ? `${monitor.value.osName} / ${monitor.value.osArch}` : '--' },
  { label: '最后刷新', value: lastUpdatedText.value },
]);

const heroStatusItems = computed<StatusItem[]>(() => [
  {
    label: '整体状态',
    value: overallHealth.value.label,
    detail: overallHealth.value.description,
    tone: overallHealth.value.tone,
  },
  {
    label: '内存压力',
    value: formatPercent(physicalMemoryUsagePercent.value),
    detail: `${formatBytes(usedPhysicalMemory.value)} / ${formatBytes(monitor.value?.system.totalPhysicalMemory)}`,
    tone: physicalMemoryTone.value,
  },
  {
    label: '缓存连通',
    value: redisStatus.value.label,
    detail: `Redis ${monitor.value?.redis.version || '--'}`,
    tone: monitor.value?.redis.available ? 'healthy' : monitor.value?.redis.available === false ? 'danger' : 'neutral',
  },
]);

const metricCards = computed<MetricCardItem[]>(() => [
  createMetricCard({
    key: 'system-cpu',
    title: '系统 CPU',
    subtitle: '宿主机整体负载',
    value: formatPercent(monitor.value?.system.systemCpuUsage),
    note: `${formatInteger(monitor.value?.system.availableProcessors)} 核可用`,
    percent: normalizePercent(monitor.value?.system.systemCpuUsage),
    tone: systemCpuTone.value,
    tagText: getToneText(systemCpuTone.value),
    icon: IconThunderbolt,
  }),
  createMetricCard({
    key: 'process-cpu',
    title: '进程 CPU',
    subtitle: '当前应用进程占用',
    value: formatPercent(monitor.value?.system.processCpuUsage),
    note: '用于观察应用线程与计算热点',
    percent: normalizePercent(monitor.value?.system.processCpuUsage),
    tone: processCpuTone.value,
    tagText: getToneText(processCpuTone.value),
    icon: IconDesktop,
  }),
  createMetricCard({
    key: 'physical-memory',
    title: '物理内存已用',
    subtitle: '宿主机内存水位',
    value: formatPercent(physicalMemoryUsagePercent.value),
    note: `${formatBytes(usedPhysicalMemory.value)} / ${formatBytes(monitor.value?.system.totalPhysicalMemory)}`,
    percent: normalizePercent(physicalMemoryUsagePercent.value),
    tone: physicalMemoryTone.value,
    tagText: getToneText(physicalMemoryTone.value),
    icon: IconStorage,
  }),
  createMetricCard({
    key: 'heap-memory',
    title: 'JVM 堆已用',
    subtitle: '应用堆空间消耗',
    value: formatPercent(heapUsagePercent.value),
    note: `${formatBytes(monitor.value?.jvm.heapUsed)} / ${formatBytes(heapCapacity.value)}`,
    percent: normalizePercent(heapUsagePercent.value),
    tone: heapTone.value,
    tagText: getToneText(heapTone.value),
    icon: IconCode,
  }),
]);

const capacityCards = computed<CapacityCardItem[]>(() => [
  {
    key: 'physical',
    title: '物理内存',
    value: formatPercent(physicalMemoryUsagePercent.value),
    note: `${formatBytes(usedPhysicalMemory.value)} / ${formatBytes(monitor.value?.system.totalPhysicalMemory)}`,
    percent: normalizePercent(physicalMemoryUsagePercent.value),
    tone: physicalMemoryTone.value,
    progressStatus: getProgressStatus(physicalMemoryTone.value),
  },
  {
    key: 'heap',
    title: 'JVM 堆内存',
    value: formatPercent(heapUsagePercent.value),
    note: `${formatBytes(monitor.value?.jvm.heapUsed)} / ${formatBytes(heapCapacity.value)}`,
    percent: normalizePercent(heapUsagePercent.value),
    tone: heapTone.value,
    progressStatus: getProgressStatus(heapTone.value),
  },
]);

const resourceStats = computed<StatItem[]>(() => [
  {
    label: 'CPU 核数',
    value: formatInteger(monitor.value?.system.availableProcessors),
    hint: '可供当前节点调度的逻辑处理器数量',
  },
  {
    label: '活动线程',
    value: formatInteger(monitor.value?.jvm.liveThreadCount),
    hint: '应用当前活跃线程总数',
  },
  {
    label: '守护线程',
    value: formatInteger(monitor.value?.jvm.daemonThreadCount),
    hint: '后台守护线程规模',
  },
  {
    label: '堆内存已提交',
    value: formatBytes(monitor.value?.jvm.heapCommitted),
    hint: 'JVM 已向系统申请的堆空间',
  },
  {
    label: '堆外内存已用',
    value: formatBytes(monitor.value?.jvm.nonHeapUsed),
    hint: '类元数据、代码缓存等区域',
  },
  {
    label: '可用物理内存',
    value: formatBytes(monitor.value?.system.freePhysicalMemory),
    hint: '宿主机剩余可分配内存',
  },
]);

const redisStats = computed<StatItem[]>(() => [
  {
    label: '当前 Key 数',
    value: formatInteger(monitor.value?.redis.dbSize),
    hint: '缓存实例当前持有的 Key 总量',
  },
  {
    label: '客户端连接数',
    value: formatInteger(monitor.value?.redis.connectedClients),
    hint: '活跃客户端连接规模',
  },
  {
    label: '已用内存',
    value: formatBytes(monitor.value?.redis.usedMemory),
    hint: 'Redis 当前实际内存占用',
  },
  {
    label: '峰值内存',
    value: formatBytes(monitor.value?.redis.usedMemoryPeak),
    hint: '历史峰值可帮助判断增长趋势',
  },
]);

const jvmRuntimeStats = computed<StatItem[]>(() => [
  {
    label: 'JVM 名称',
    value: monitor.value?.jvm.vmName || '--',
    hint: '当前进程所运行的虚拟机实现',
  },
  {
    label: 'JVM 厂商',
    value: monitor.value?.jvm.vmVendor || '--',
    hint: 'JDK / JVM 发行方信息',
  },
  {
    label: 'JVM 版本',
    value: monitor.value?.jvm.vmVersion || '--',
    hint: '用于核对线上运行时补丁版本',
  },
]);

const jvmMemoryStats = computed<StatItem[]>(() => [
  {
    label: '堆初始值',
    value: formatJvmBytes(monitor.value?.jvm.heapInit),
    hint: '对应 JVM 启动后的初始堆保留值',
  },
  {
    label: '堆上限',
    value: formatJvmBytes(monitor.value?.jvm.heapMax),
    hint: '若配置了 -Xmx，这里通常会对应最大堆容量',
  },
  {
    label: '非堆已提交',
    value: formatJvmBytes(monitor.value?.jvm.nonHeapCommitted),
    hint: '元空间、代码缓存等区域已向系统申请的容量',
  },
  {
    label: '非堆上限',
    value: formatJvmBytes(monitor.value?.jvm.nonHeapMax),
    hint: '部分虚拟机区域可能显示为未限制',
  },
]);

async function fetchMonitor(showSuccess = false) {
  loading.value = true;
  try {
    monitor.value = await systemApi.getServerMonitor();
    lastUpdatedText.value = new Date().toLocaleString('zh-CN', { hour12: false });
    if (showSuccess) {
      Message.success('运行监控已刷新');
    }
  } finally {
    loading.value = false;
  }
}

function createMetricCard(input: Omit<MetricCardItem, 'progressStatus' | 'tagColor'>): MetricCardItem {
  return {
    ...input,
    progressStatus: getProgressStatus(input.tone),
    tagColor: getTagColor(input.tone),
  };
}

function getToneSeverity(...tones: MonitorTone[]) {
  if (tones.includes('danger')) {
    return 'danger';
  }
  if (tones.includes('attention')) {
    return 'attention';
  }
  if (tones.includes('healthy')) {
    return 'healthy';
  }
  return 'neutral';
}

function getLoadTone(value: number | null | undefined, attention = 60, danger = 85): MonitorTone {
  if (value == null || Number.isNaN(value)) {
    return 'neutral';
  }
  if (value >= danger) {
    return 'danger';
  }
  if (value >= attention) {
    return 'attention';
  }
  return 'healthy';
}

function getProgressStatus(tone: MonitorTone): ProgressStatus {
  if (tone === 'danger') {
    return 'danger';
  }
  if (tone === 'attention') {
    return 'warning';
  }
  if (tone === 'healthy') {
    return 'success';
  }
  return 'normal';
}

function getTagColor(tone: MonitorTone) {
  if (tone === 'danger') {
    return 'red';
  }
  if (tone === 'attention') {
    return 'gold';
  }
  if (tone === 'healthy') {
    return 'green';
  }
  return 'arcoblue';
}

function getToneText(tone: MonitorTone) {
  if (tone === 'danger') {
    return '告警';
  }
  if (tone === 'attention') {
    return '关注';
  }
  if (tone === 'healthy') {
    return '平稳';
  }
  return '待定';
}

function normalizePercent(value: number | null | undefined) {
  if (value == null || Number.isNaN(value)) {
    return 0;
  }
  return Math.max(0, Math.min(100, Number(value.toFixed(2))));
}

function formatPercent(value: number | null | undefined) {
  if (value == null || Number.isNaN(value)) {
    return '--';
  }
  return `${value.toFixed(2)}%`;
}

function formatInteger(value: number | null | undefined) {
  if (value == null || Number.isNaN(value)) {
    return '--';
  }
  return value.toLocaleString('zh-CN');
}

function formatBytes(value: number | null | undefined) {
  if (value == null || Number.isNaN(value)) {
    return '--';
  }
  const units = ['B', 'KB', 'MB', 'GB', 'TB'];
  let current = value;
  let unitIndex = 0;
  while (current >= 1024 && unitIndex < units.length - 1) {
    current /= 1024;
    unitIndex += 1;
  }
  return `${current.toFixed(current >= 10 || unitIndex === 0 ? 0 : 2)} ${units[unitIndex]}`;
}

function formatJvmBytes(value: number | null | undefined) {
  if (value == null || Number.isNaN(value)) {
    return '--';
  }
  if (value < 0) {
    return '未限制';
  }
  return formatBytes(value);
}

function formatDuration(value: number | null | undefined) {
  if (value == null || Number.isNaN(value)) {
    return '--';
  }
  const totalSeconds = Math.floor(value / 1000);
  const days = Math.floor(totalSeconds / 86400);
  const hours = Math.floor((totalSeconds % 86400) / 3600);
  const minutes = Math.floor((totalSeconds % 3600) / 60);
  const seconds = totalSeconds % 60;
  const parts: string[] = [];
  if (days > 0) {
    parts.push(`${days}天`);
  }
  if (hours > 0 || days > 0) {
    parts.push(`${hours}小时`);
  }
  if (minutes > 0 || hours > 0 || days > 0) {
    parts.push(`${minutes}分钟`);
  }
  parts.push(`${seconds}秒`);
  return parts.join(' ');
}

void fetchMonitor();
</script>

<style scoped>
.system-page {
  display: grid;
  gap: 18px;
}

.monitor-spin {
  width: 100%;
}

.monitor-grid {
  display: grid;
  gap: 18px;
}

.hero-card {
  position: relative;
}

.hero-card::before {
  content: '';
  position: absolute;
  inset: 0;
  background:
    radial-gradient(circle at top left, rgba(36, 91, 219, 0.18), transparent 34%),
    radial-gradient(circle at 88% 22%, rgba(82, 170, 255, 0.14), transparent 28%);
  pointer-events: none;
}

.hero-shell {
  position: relative;
  display: grid;
  grid-template-columns: minmax(0, 1.5fr) minmax(300px, 0.9fr);
  gap: 20px;
}

.hero-main,
.hero-side {
  display: grid;
  gap: 18px;
}

.hero-eyebrow {
  color: #5371b8;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.hero-heading {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.hero-title {
  color: #12213b;
  font-size: 32px;
  font-weight: 800;
  line-height: 1.1;
}

.hero-description {
  max-width: 720px;
  margin-top: 10px;
  color: #5e6e86;
  font-size: 14px;
  line-height: 1.8;
}

.hero-overview {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
}

.overview-item {
  min-height: 88px;
  padding: 16px 18px;
  background: rgba(255, 255, 255, 0.66);
  border: 1px solid rgba(36, 91, 219, 0.08);
  border-radius: 18px;
  backdrop-filter: blur(8px);
}

.overview-label {
  color: #7686a0;
  font-size: 12px;
}

.overview-value {
  margin-top: 8px;
  color: #152340;
  font-size: 16px;
  font-weight: 700;
  line-height: 1.5;
}

.hero-side {
  align-content: start;
}

.status-pill {
  padding: 16px 18px;
  border-radius: 18px;
  border: 1px solid rgba(15, 23, 42, 0.08);
  background: rgba(255, 255, 255, 0.78);
}

.status-pill--healthy {
  background: linear-gradient(180deg, rgba(241, 252, 247, 0.96), rgba(255, 255, 255, 0.86));
  border-color: rgba(15, 138, 95, 0.14);
}

.status-pill--attention {
  background: linear-gradient(180deg, rgba(255, 247, 234, 0.96), rgba(255, 255, 255, 0.88));
  border-color: rgba(184, 106, 18, 0.16);
}

.status-pill--danger {
  background: linear-gradient(180deg, rgba(255, 241, 241, 0.96), rgba(255, 255, 255, 0.88));
  border-color: rgba(195, 63, 63, 0.16);
}

.status-pill--neutral {
  background: linear-gradient(180deg, rgba(242, 247, 255, 0.96), rgba(255, 255, 255, 0.88));
  border-color: rgba(36, 91, 219, 0.1);
}

.status-pill__label {
  color: #73819a;
  font-size: 12px;
}

.status-pill__value {
  margin-top: 8px;
  color: #16233d;
  font-size: 22px;
  font-weight: 800;
}

.status-pill__detail {
  margin-top: 6px;
  color: #607088;
  font-size: 13px;
  line-height: 1.7;
}

.metric-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 18px;
  align-items: start;
}

.metric-card {
  display: grid;
  gap: 14px;
  align-content: start;
}

.metric-card::before {
  content: '';
  position: absolute;
  inset: 0;
  opacity: 0.9;
  pointer-events: none;
}

.metric-card--healthy::before {
  background: linear-gradient(180deg, rgba(15, 138, 95, 0.07), transparent 38%);
}

.metric-card--attention::before {
  background: linear-gradient(180deg, rgba(184, 106, 18, 0.08), transparent 38%);
}

.metric-card--danger::before {
  background: linear-gradient(180deg, rgba(195, 63, 63, 0.08), transparent 38%);
}

.metric-card--neutral::before {
  background: linear-gradient(180deg, rgba(36, 91, 219, 0.06), transparent 38%);
}

.metric-head,
.metric-heading {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 12px;
}

.metric-heading {
  justify-content: flex-start;
  flex: 1;
}

.metric-icon {
  display: grid;
  place-items: center;
  width: 42px;
  height: 42px;
  border-radius: 14px;
  font-size: 18px;
}

.metric-icon--healthy {
  color: #0f8a5f;
  background: rgba(15, 138, 95, 0.12);
}

.metric-icon--attention {
  color: #b86a12;
  background: rgba(184, 106, 18, 0.12);
}

.metric-icon--danger {
  color: #c33f3f;
  background: rgba(195, 63, 63, 0.12);
}

.metric-icon--neutral {
  color: #245bdb;
  background: rgba(36, 91, 219, 0.12);
}

.metric-title {
  color: #16233d;
  font-size: 15px;
  font-weight: 700;
}

.metric-subtitle {
  margin-top: 4px;
  color: #7887a0;
  font-size: 12px;
}

.metric-value {
  color: #14223e;
  font-size: 34px;
  font-weight: 800;
  letter-spacing: -0.03em;
}

.metric-note {
  color: #66768f;
  font-size: 12px;
  line-height: 1.7;
}

.detail-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.25fr) minmax(0, 1fr);
  gap: 18px;
}

.detail-card {
  display: grid;
  gap: 18px;
}

.section-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.section-title {
  margin-bottom: 0;
  color: #17233c;
  font-size: 18px;
  font-weight: 800;
}

.section-description {
  margin-top: 8px;
  color: #687892;
  font-size: 13px;
  line-height: 1.8;
}

.capacity-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 16px;
}

.capacity-card {
  display: grid;
  justify-items: center;
  gap: 16px;
  padding: 18px 16px;
  border-radius: 20px;
  border: 1px solid rgba(15, 23, 42, 0.06);
  background: linear-gradient(180deg, rgba(248, 250, 255, 0.98), rgba(255, 255, 255, 0.9));
}

.capacity-card--healthy {
  background: linear-gradient(180deg, rgba(242, 252, 247, 0.94), rgba(255, 255, 255, 0.9));
}

.capacity-card--attention {
  background: linear-gradient(180deg, rgba(255, 247, 236, 0.94), rgba(255, 255, 255, 0.9));
}

.capacity-card--danger {
  background: linear-gradient(180deg, rgba(255, 241, 241, 0.94), rgba(255, 255, 255, 0.9));
}

.capacity-ring {
  position: relative;
  display: grid;
  place-items: center;
  width: 108px;
  height: 108px;
}

.capacity-ring__center {
  position: absolute;
  inset: 0;
  display: grid;
  place-items: center;
  padding: 22px;
  text-align: center;
}

.capacity-ring__value {
  color: #152340;
  font-size: 18px;
  font-weight: 800;
  line-height: 1.3;
}

.capacity-copy {
  display: grid;
  gap: 6px;
  justify-items: center;
  text-align: center;
}

.capacity-title {
  color: #17233c;
  font-size: 15px;
  font-weight: 700;
}

.capacity-note {
  color: #687892;
  font-size: 12px;
  line-height: 1.7;
}

.stat-grid {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 14px;
}

.stat-item {
  min-height: 126px;
  padding: 16px 18px;
  background: rgba(248, 250, 255, 0.96);
  border: 1px solid rgba(15, 23, 42, 0.06);
  border-radius: 18px;
}

.stat-label {
  color: #7a89a1;
  font-size: 12px;
}

.stat-value {
  margin-top: 10px;
  color: #13213d;
  font-size: 24px;
  font-weight: 800;
  line-height: 1.3;
  word-break: break-word;
}

.stat-hint {
  margin-top: 8px;
  color: #687892;
  font-size: 12px;
  line-height: 1.7;
}

.redis-card {
  align-content: start;
}

.redis-summary {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.redis-summary__block {
  display: grid;
  gap: 8px;
  padding: 18px;
  border-radius: 18px;
  background: rgba(248, 250, 255, 0.96);
  border: 1px solid rgba(15, 23, 42, 0.06);
}

.redis-summary__block--accent {
  background:
    radial-gradient(circle at top right, rgba(36, 91, 219, 0.12), transparent 32%),
    linear-gradient(180deg, rgba(245, 249, 255, 0.98), rgba(255, 255, 255, 0.92));
}

.redis-summary__label {
  color: #7b89a1;
  font-size: 12px;
}

.redis-summary__value {
  color: #152340;
  font-size: 24px;
  font-weight: 800;
  line-height: 1.3;
}

.redis-summary__detail {
  color: #687892;
  font-size: 12px;
  line-height: 1.7;
}

.redis-summary__progress {
  margin-top: 4px;
}

.redis-alert {
  margin-top: -2px;
}

.redis-stat-grid {
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.jvm-card {
  align-content: start;
}

.jvm-runtime-grid {
  grid-template-columns: repeat(3, minmax(0, 1fr));
}

.jvm-memory-grid {
  grid-template-columns: repeat(4, minmax(0, 1fr));
}

.jvm-runtime-grid .stat-item,
.jvm-memory-grid .stat-item {
  min-height: 138px;
}

.jvm-runtime-grid .stat-value {
  font-size: 20px;
}

.jvm-arguments {
  display: grid;
  gap: 14px;
  padding: 18px;
  border-radius: 22px;
  border: 1px solid rgba(36, 91, 219, 0.08);
  background:
    radial-gradient(circle at top right, rgba(36, 91, 219, 0.12), transparent 32%),
    linear-gradient(180deg, rgba(246, 250, 255, 0.98), rgba(255, 255, 255, 0.92));
}

.jvm-arguments__header {
  display: grid;
  gap: 6px;
}

.jvm-arguments__title {
  color: #17233c;
  font-size: 15px;
  font-weight: 700;
}

.jvm-arguments__subtitle,
.jvm-arguments__empty {
  color: #687892;
  font-size: 12px;
  line-height: 1.7;
}

.jvm-arguments__tags {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.jvm-argument-tag {
  max-width: 100%;
  font-family: 'SFMono-Regular', 'JetBrains Mono', 'Menlo', monospace;
  white-space: normal;
  word-break: break-all;
}

@media (max-width: 1280px) {
  .hero-shell,
  .detail-grid {
    grid-template-columns: 1fr;
  }

  .metric-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 900px) {
  .hero-overview,
  .capacity-grid,
  .stat-grid,
  .redis-summary,
  .redis-stat-grid,
  .jvm-runtime-grid,
  .jvm-memory-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .metric-grid {
    grid-template-columns: 1fr;
  }

  .hero-heading,
  .section-header {
    flex-direction: column;
  }

  .hero-title {
    font-size: 26px;
  }

  .metric-value {
    font-size: 30px;
  }

  .status-pill__value,
  .redis-summary__value,
  .stat-value {
    font-size: 22px;
  }
}
</style>
