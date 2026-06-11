import { computed, ref, type Component } from 'vue';
import { useRouter } from 'vue-router';
import { Message, type TableColumnData } from '@arco-design/web-vue';
import {
  IconClockCircle,
  IconCode,
  IconStorage,
  IconThunderbolt,
} from '@arco-design/web-vue/es/icon';
import { systemApi } from '@/api/system';
import type {
  MonitorLogTailVO,
  MonitorSlowSqlVO,
  ServerMonitorVO,
} from '@/types/system';
import { formatCurrentDateTime } from '@/utils/date-time';

export type MonitorTone = 'healthy' | 'attention' | 'danger' | 'neutral';
type ProgressStatus = 'normal' | 'success' | 'warning' | 'danger';

export interface OverviewItem {
  label: string;
  value: string;
}

export interface StatusItem {
  label: string;
  value: string;
  detail: string;
  tone: MonitorTone;
}

export interface MetricCardItem {
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

export interface ServiceDomainCardStat {
  label: string;
  value: string;
  note: string;
}

export interface ServiceDomainCardItem {
  key: string;
  title: string;
  description: string;
  label: string;
  value: string;
  note: string;
  tone: MonitorTone;
  icon: Component;
  stats: ServiceDomainCardStat[];
  actionLabel: string;
  path: string;
  permission: string;
}

export const slowSqlColumns: TableColumnData[] = [
  { dataIndex: 'statementId', title: '语句标识', width: 320, ellipsis: true, tooltip: true },
  { dataIndex: 'costMs', title: '耗时', width: 120, slotName: 'costCell' },
  { dataIndex: 'resultSize', title: '结果规模', width: 120 },
  { dataIndex: 'executeTime', title: '执行时间', width: 180 },
  { dataIndex: 'sql', title: 'SQL', ellipsis: true, tooltip: true },
];

export const logLineOptions = [
  { label: '最近 50 行', value: 50 },
  { label: '最近 80 行', value: 80 },
  { label: '最近 120 行', value: 120 },
  { label: '最近 200 行', value: 200 },
];

/**
 * 构建硬件监控页所需的宿主机视角数据。
 *
 * @returns 宿主机资源页展示所需的状态、卡片与跳转方法
 */
export function useHardwareMonitorViewModel() {
  const runtime = useMonitorRuntime();

  const hardwareHealth = computed(() => {
    const severity = getToneSeverity(runtime.systemCpuTone.value, runtime.physicalMemoryTone.value);
    if (severity === 'danger') {
      return { label: '资源告警', color: 'red', tone: 'danger' as const };
    }
    if (severity === 'attention') {
      return { label: '资源偏高', color: 'gold', tone: 'attention' as const };
    }
    if (runtime.monitor.value) {
      return { label: '资源平稳', color: 'green', tone: 'healthy' as const };
    }
    return { label: '等待采集', color: 'arcoblue', tone: 'neutral' as const };
  });

  const hardwareOverviewItems = computed<OverviewItem[]>(() => [
    { label: '宿主系统', value: runtime.monitor.value?.osName || '--' },
    { label: '系统架构', value: runtime.monitor.value?.osArch || '--' },
    { label: '逻辑核数', value: formatInteger(runtime.monitor.value?.system.availableProcessors) },
    { label: '总物理内存', value: formatBytes(runtime.monitor.value?.system.totalPhysicalMemory) },
    { label: '空闲物理内存', value: formatBytes(runtime.monitor.value?.system.freePhysicalMemory) },
    { label: '最后刷新', value: runtime.lastUpdatedText.value },
  ]);

  const hardwareStatusItems = computed<StatusItem[]>(() => [
    {
      label: '系统 CPU',
      value: formatPercent(runtime.monitor.value?.system.systemCpuUsage),
      detail: '只看宿主机整体负载，不混入当前应用进程的占用。',
      tone: runtime.systemCpuTone.value,
    },
    {
      label: '物理内存水位',
      value: formatPercent(runtime.physicalMemoryUsagePercent.value),
      detail: `${formatBytes(runtime.usedPhysicalMemory.value)} / ${formatBytes(runtime.monitor.value?.system.totalPhysicalMemory)}`,
      tone: runtime.physicalMemoryTone.value,
    },
    {
      label: '空闲资源',
      value: formatBytes(runtime.monitor.value?.system.freePhysicalMemory),
      detail: `预计余量 ${formatPercent(runtime.memoryFreePercent.value)}，更适合判断宿主机还有没有喘息空间。`,
      tone: runtime.memoryFreePercentTone.value,
    },
    {
      label: '资源余量',
      value: formatPercent(runtime.resourceHeadroomPercent.value),
      detail: '把 CPU 和内存余量合在一起看，适合值班时先做一眼判断。',
      tone: runtime.resourceHeadroomTone.value,
    },
  ]);

  const hardwareRuntimeCards = computed<MetricCardItem[]>(() => [
    createMetricCard({
      key: 'process-cpu',
      title: '进程 CPU',
      subtitle: '当前应用进程占用',
      value: formatPercent(runtime.monitor.value?.system.processCpuUsage),
      note: '用于判断是不是当前服务把宿主机资源压上去了。',
      percent: normalizePercent(runtime.monitor.value?.system.processCpuUsage),
      tone: runtime.processCpuTone.value,
      tagText: getToneText(runtime.processCpuTone.value),
      icon: IconThunderbolt,
    }),
    createMetricCard({
      key: 'heap-memory',
      title: 'JVM 堆使用',
      subtitle: '应用堆空间消耗',
      value: formatPercent(runtime.heapUsagePercent.value),
      note: `${formatBytes(runtime.monitor.value?.jvm.heapUsed)} / ${formatBytes(runtime.heapCapacity.value)}`,
      percent: normalizePercent(runtime.heapUsagePercent.value),
      tone: runtime.heapTone.value,
      tagText: getToneText(runtime.heapTone.value),
      icon: IconCode,
    }),
    createMetricCard({
      key: 'live-threads',
      title: 'Live 线程',
      subtitle: '当前 JVM 线程规模',
      value: formatInteger(runtime.monitor.value?.jvm.liveThreadCount),
      note: `守护线程 ${formatInteger(runtime.monitor.value?.jvm.daemonThreadCount)}`,
      percent: normalizeLinearPercent(runtime.monitor.value?.jvm.liveThreadCount, 320),
      tone: runtime.threadTone.value,
      tagText: getToneText(runtime.threadTone.value),
      icon: IconClockCircle,
    }),
    createMetricCard({
      key: 'non-heap',
      title: '非堆内存',
      subtitle: '元空间与代码缓存',
      value: formatJvmBytes(runtime.monitor.value?.jvm.nonHeapUsed),
      note: `${formatJvmBytes(runtime.monitor.value?.jvm.nonHeapUsed)} / ${formatJvmBytes(runtime.nonHeapCapacity.value)}`,
      percent: normalizePercent(runtime.nonHeapUsagePercent.value),
      tone: runtime.nonHeapTone.value,
      tagText: getToneText(runtime.nonHeapTone.value),
      icon: IconStorage,
    }),
  ]);

  return {
    loading: runtime.loading,
    lastUpdatedText: runtime.lastUpdatedText,
    monitor: runtime.monitor,
    hardwareHealth,
    hardwareOverviewItems,
    hardwareStatusItems,
    hardwareRuntimeCards,
    refreshMonitor: runtime.refreshMonitor,
    openRoute: runtime.openRoute,
  };
}

/**
 * 构建服务监控页所需的应用运行态和链路视角数据。
 *
 * @returns 服务页展示所需的状态、卡片与跳转方法
 */
export function useServiceMonitorViewModel() {
  const runtime = useMonitorRuntime();

  const serviceHealth = computed(() => {
    if (runtime.monitor.value?.redis.available === false) {
      return { label: '缓存异常', color: 'red', tone: 'danger' as const };
    }
    if ((runtime.monitor.value?.messageBacklog.timedOutCount ?? 0) > 0) {
      return { label: '消息超时', color: 'red', tone: 'danger' as const };
    }
    if ((runtime.monitor.value?.jobHealth.recentFailCount ?? 0) > 0) {
      return { label: '任务告警', color: 'gold', tone: 'attention' as const };
    }
    if ((runtime.monitor.value?.slowSql.recentCount ?? 0) > 0) {
      return { label: '链路关注', color: 'gold', tone: 'attention' as const };
    }
    if (runtime.monitor.value) {
      return { label: '服务平稳', color: 'green', tone: 'healthy' as const };
    }
    return { label: '等待采集', color: 'arcoblue', tone: 'neutral' as const };
  });

  const serviceOverviewItems = computed<OverviewItem[]>(() => [
    { label: '运行环境', value: runtime.monitor.value?.activeProfile || '--' },
    { label: '启动时间', value: runtime.monitor.value?.startTime || '--' },
    { label: '运行时长', value: formatDuration(runtime.monitor.value?.uptimeMillis) },
    { label: '慢 SQL 采样', value: formatInteger(runtime.monitor.value?.slowSql.recentCount) },
  ]);

  const serviceStatusItems = computed<StatusItem[]>(() => [
    {
      label: '服务状态',
      value: serviceHealth.value.label,
      detail: `任务失败 ${formatInteger(runtime.monitor.value?.jobHealth.recentFailCount)}，消息超时 ${formatInteger(runtime.monitor.value?.messageBacklog.timedOutCount)}`,
      tone: serviceHealth.value.tone,
    },
    {
      label: 'Redis 状态',
      value: getRedisLabel(runtime.monitor.value),
      detail: getRedisDetail(runtime.monitor.value),
      tone: getRedisTone(runtime.monitor.value),
    },
    {
      label: '任务健康',
      value: formatPercent(runtime.monitor.value?.jobHealth.recentSuccessRate),
      detail: `24h 成功 ${formatInteger(runtime.monitor.value?.jobHealth.recentSuccessCount)} / 失败 ${formatInteger(runtime.monitor.value?.jobHealth.recentFailCount)}`,
      tone: (runtime.monitor.value?.jobHealth.recentFailCount ?? 0) > 0 ? 'attention' : 'healthy',
    },
    {
      label: '消息积压',
      value: formatInteger(runtime.monitor.value?.messageBacklog.pendingCount),
      detail: `处理中 ${formatInteger(runtime.monitor.value?.messageBacklog.processingCount)}，失败 ${formatInteger(runtime.monitor.value?.messageBacklog.failCount)}`,
      tone: (runtime.monitor.value?.messageBacklog.timedOutCount ?? 0) > 0 ? 'danger' : (runtime.monitor.value?.messageBacklog.pendingCount ?? 0) > 0 ? 'attention' : 'healthy',
    },
  ]);

  const serviceDomainCards = computed<ServiceDomainCardItem[]>(() => [
    {
      key: 'cache',
      title: '缓存监控',
      description: 'Redis 是否可用、命中率是否健康、容量是否开始吃紧。',
      label: 'Redis 状态',
      value: getRedisLabel(runtime.monitor.value),
      note: getRedisDetail(runtime.monitor.value),
      tone: getRedisTone(runtime.monitor.value),
      icon: IconStorage,
      stats: [
        { label: '命中率', value: formatPercent(runtime.monitor.value?.redis.hitRate), note: '持续偏低时要检查缓存键设计。' },
        { label: 'Key 总数', value: formatInteger(runtime.monitor.value?.redis.dbSize), note: '用于观察缓存总量是否异常抬升。' },
        { label: '客户端连接', value: formatInteger(runtime.monitor.value?.redis.connectedClients), note: '有助于判断接入压力变化。' },
        { label: '淘汰 Key', value: formatInteger(runtime.monitor.value?.redis.evictedKeys), note: '大于 0 往往意味着容量偏紧。' },
      ],
      actionLabel: '缓存详情',
      path: '/system/cache',
      permission: 'system:cache:query',
    },
    {
      key: 'jobs',
      title: '任务健康',
      description: '只看平台任务执行状态，不掺杂宿主机资源信号。',
      label: '24h 成功率',
      value: formatPercent(runtime.monitor.value?.jobHealth.recentSuccessRate),
      note: runtime.monitor.value?.jobHealth.recentFailures[0]?.jobName || '最近 24 小时暂无失败任务',
      tone: (runtime.monitor.value?.jobHealth.recentFailCount ?? 0) > 0 ? 'attention' : 'healthy',
      icon: IconClockCircle,
      stats: [
        { label: '任务总数', value: formatInteger(runtime.monitor.value?.jobHealth.totalJobs), note: '平台当前已注册任务规模。' },
        { label: '启用 / 暂停', value: `${formatInteger(runtime.monitor.value?.jobHealth.enabledJobs)} / ${formatInteger(runtime.monitor.value?.jobHealth.pausedJobs)}`, note: '便于看是否存在人为暂停。' },
        { label: '成功次数', value: formatInteger(runtime.monitor.value?.jobHealth.recentSuccessCount), note: '和成功率一起看更直观。' },
        { label: '失败次数', value: formatInteger(runtime.monitor.value?.jobHealth.recentFailCount), note: '失败大于 0 建议进入任务页核对日志。' },
      ],
      actionLabel: '任务页',
      path: '/system/jobs',
      permission: 'system:job:query',
    },
    {
      key: 'messages',
      title: '消息积压',
      description: '聚焦消费链路是否拥堵，而不是数据库或机器资源。',
      label: '待处理消息',
      value: formatInteger(runtime.monitor.value?.messageBacklog.pendingCount),
      note: runtime.monitor.value?.messageBacklog.oldestPendingEventId || '当前没有长期积压的消息',
      tone: (runtime.monitor.value?.messageBacklog.timedOutCount ?? 0) > 0 ? 'danger' : (runtime.monitor.value?.messageBacklog.pendingCount ?? 0) > 0 ? 'attention' : 'healthy',
      icon: IconThunderbolt,
      stats: [
        { label: '处理中', value: formatInteger(runtime.monitor.value?.messageBacklog.processingCount), note: '长期不降时优先排查消费者线程。' },
        { label: '失败消息', value: formatInteger(runtime.monitor.value?.messageBacklog.failCount), note: '失败消息建议在台账页查看 payload。' },
        { label: '超时消息', value: formatInteger(runtime.monitor.value?.messageBacklog.timedOutCount), note: '超时优先排查消费阻塞或外部依赖。' },
        { label: '最早积压时间', value: runtime.monitor.value?.messageBacklog.oldestPendingCreateTime || '--', note: '判断积压是否已经跨多个调度周期。' },
      ],
      actionLabel: '消息台账',
      path: '/logs/mq-messages',
      permission: 'system:mq-message:query',
    },
  ]);

  return {
    loading: runtime.loading,
    lastUpdatedText: runtime.lastUpdatedText,
    monitor: runtime.monitor,
    serviceHealth,
    serviceOverviewItems,
    serviceStatusItems,
    serviceDomainCards,
    refreshMonitor: runtime.refreshMonitor,
    openRoute: runtime.openRoute,
  };
}

/**
 * 管理服务监控页中的慢 SQL 和在线日志明细。
 *
 * @returns 诊断明细的查询状态与刷新方法
 */
export function useMonitorDiagnostics() {
  const slowSqlLoading = ref(false);
  const logLoading = ref(false);
  const logLineLimit = ref(80);
  const slowSqlRows = ref<MonitorSlowSqlVO[]>([]);
  const logTail = ref<MonitorLogTailVO | null>(null);

  const logTailText = computed(() => (logTail.value?.lines ?? []).join('\n'));

  /**
   * 查询当前实例的慢 SQL 明细。
   *
   * @param showSuccess 是否显示刷新成功提示
   * @returns 查询完成后的 Promise
   */
  async function fetchSlowSql(showSuccess = false) {
    slowSqlLoading.value = true;
    try {
      slowSqlRows.value = await systemApi.listMonitorSlowSql(20);
      if (showSuccess) {
        Message.success('慢 SQL 已刷新');
      }
    } finally {
      slowSqlLoading.value = false;
    }
  }

  /**
   * 查询当前实例的在线日志 tail。
   *
   * @param showSuccess 是否显示刷新成功提示
   * @returns 查询完成后的 Promise
   */
  async function fetchLogTail(showSuccess = false) {
    logLoading.value = true;
    try {
      logTail.value = await systemApi.getMonitorLogTail(logLineLimit.value);
      if (showSuccess) {
        Message.success('在线日志已刷新');
      }
    } finally {
      logLoading.value = false;
    }
  }

  return {
    slowSqlLoading,
    logLoading,
    logLineLimit,
    slowSqlRows,
    logTail,
    logTailText,
    fetchSlowSql,
    fetchLogTail,
  };
}

/**
 * 构建慢 SQL 独立页面的数据模型。
 *
 * @returns 慢 SQL 页面展示与刷新所需的状态
 */
export function useMonitorSlowSqlPage() {
  const runtime = useMonitorRuntime();
  const diagnostics = useMonitorDiagnostics();

  /**
   * 同步刷新运行监控摘要与慢 SQL 明细。
   *
   * @param showSuccess 是否显示刷新成功提示
   * @returns 刷新完成后的 Promise
   */
  async function refreshAll(showSuccess = false) {
    await Promise.all([
      runtime.refreshMonitor(false),
      diagnostics.fetchSlowSql(false),
    ]);
    if (showSuccess) {
      Message.success('慢 SQL 页面已刷新');
    }
  }

  void diagnostics.fetchSlowSql();

  return {
    loading: runtime.loading,
    lastUpdatedText: runtime.lastUpdatedText,
    monitor: runtime.monitor,
    openRoute: runtime.openRoute,
    refreshAll,
    slowSqlLoading: diagnostics.slowSqlLoading,
    slowSqlRows: diagnostics.slowSqlRows,
  };
}

/**
 * 构建在线日志独立页面的数据模型。
 *
 * @returns 在线日志页面展示与刷新所需的状态
 */
export function useMonitorLiveLogPage() {
  const runtime = useMonitorRuntime();
  const diagnostics = useMonitorDiagnostics();

  /**
   * 同步刷新运行监控摘要与在线日志明细。
   *
   * @param showSuccess 是否显示刷新成功提示
   * @returns 刷新完成后的 Promise
   */
  async function refreshAll(showSuccess = false) {
    await Promise.all([
      runtime.refreshMonitor(false),
      diagnostics.fetchLogTail(false),
    ]);
    if (showSuccess) {
      Message.success('在线日志页面已刷新');
    }
  }

  void diagnostics.fetchLogTail();

  return {
    loading: runtime.loading,
    lastUpdatedText: runtime.lastUpdatedText,
    monitor: runtime.monitor,
    openRoute: runtime.openRoute,
    refreshAll,
    logLoading: diagnostics.logLoading,
    logLineLimit: diagnostics.logLineLimit,
    logTail: diagnostics.logTail,
    logTailText: diagnostics.logTailText,
    fetchLogTail: diagnostics.fetchLogTail,
  };
}

/**
 * 构建两类监控页面共用的基础运行态。
 *
 * @returns 共享的监控实体、派生指标和跳转方法
 */
function useMonitorRuntime() {
  const router = useRouter();
  const loading = ref(false);
  const lastUpdatedText = ref('等待首次采集');
  const monitor = ref<ServerMonitorVO | null>(null);

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

  const memoryFreePercent = computed(() => {
    if (!monitor.value?.system.totalPhysicalMemory || monitor.value.system.freePhysicalMemory == null) {
      return null;
    }
    return (monitor.value.system.freePhysicalMemory / monitor.value.system.totalPhysicalMemory) * 100;
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

  const nonHeapCapacity = computed(() => {
    if (monitor.value?.jvm.nonHeapMax && monitor.value.jvm.nonHeapMax > 0) {
      return monitor.value.jvm.nonHeapMax;
    }
    return monitor.value?.jvm.nonHeapCommitted ?? null;
  });

  const nonHeapUsagePercent = computed(() => {
    if (monitor.value?.jvm.nonHeapUsed == null || !nonHeapCapacity.value) {
      return null;
    }
    return (monitor.value.jvm.nonHeapUsed / nonHeapCapacity.value) * 100;
  });

  const resourceHeadroomPercent = computed(() => {
    const cpuHeadroomPercent = toHeadroomPercent(monitor.value?.system.systemCpuUsage);
    if (cpuHeadroomPercent == null || memoryFreePercent.value == null) {
      return null;
    }
    return Math.min(cpuHeadroomPercent, memoryFreePercent.value);
  });

  const systemCpuTone = computed(() => getLoadTone(monitor.value?.system.systemCpuUsage, 55, 80));
  const processCpuTone = computed(() => getLoadTone(monitor.value?.system.processCpuUsage, 45, 70));
  const physicalMemoryTone = computed(() => getLoadTone(physicalMemoryUsagePercent.value, 70, 88));
  const heapTone = computed(() => getLoadTone(heapUsagePercent.value, 72, 90));
  const nonHeapTone = computed(() => getLoadTone(nonHeapUsagePercent.value, 78, 92));
  const threadTone = computed(() => getLoadTone(monitor.value?.jvm.liveThreadCount, 140, 220));
  const memoryFreePercentTone = computed(() => getInverseLoadTone(memoryFreePercent.value, 12, 30));
  const resourceHeadroomTone = computed(() => getInverseLoadTone(resourceHeadroomPercent.value, 14, 32));

  /**
   * 重新拉取一次运行监控摘要。
   *
   * @param showSuccess 是否显示刷新成功提示
   * @returns 刷新完成后的 Promise
   */
  async function refreshMonitor(showSuccess = false) {
    loading.value = true;
    try {
      monitor.value = await systemApi.getServerMonitor();
      lastUpdatedText.value = formatCurrentDateTime();
      if (showSuccess) {
        Message.success('运行监控已刷新');
      }
    } finally {
      loading.value = false;
    }
  }

  /**
   * 页面内跳转到指定监控或运维页面。
   *
   * @param path 目标路由路径
   * @returns 无返回值
   */
  function openRoute(path: string) {
    if (router.currentRoute.value.path === path) {
      return;
    }
    void router.push(path);
  }

  void refreshMonitor();

  return {
    loading,
    lastUpdatedText,
    monitor,
    usedPhysicalMemory,
    physicalMemoryUsagePercent,
    memoryFreePercent,
    heapCapacity,
    heapUsagePercent,
    nonHeapCapacity,
    nonHeapUsagePercent,
    resourceHeadroomPercent,
    systemCpuTone,
    processCpuTone,
    physicalMemoryTone,
    heapTone,
    nonHeapTone,
    threadTone,
    memoryFreePercentTone,
    resourceHeadroomTone,
    refreshMonitor,
    openRoute,
  };
}

/**
 * 为资源指标卡补齐统一的进度条和标签语义。
 *
 * @param input 基础指标卡数据
 * @returns 补齐展示字段后的指标卡数据
 */
function createMetricCard(input: Omit<MetricCardItem, 'progressStatus' | 'tagColor'>): MetricCardItem {
  return {
    ...input,
    progressStatus: getProgressStatus(input.tone),
    tagColor: getTagColor(input.tone),
  };
}

/**
 * 汇总多项监控色阶，返回当前页面的最高严重级别。
 *
 * @param tones 多个监控色阶
 * @returns 最高优先级的色阶
 */
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

/**
 * 根据阈值区间把监控值映射成统一色阶。
 *
 * @param value 当前监控值
 * @param attention 关注阈值
 * @param danger 告警阈值
 * @returns 对应的监控色阶
 */
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

/**
 * 根据“余量类”指标把高分映射为更健康的色阶。
 *
 * @param value 当前余量值
 * @param danger 告警下限
 * @param healthy 健康下限
 * @returns 对应的监控色阶
 */
function getInverseLoadTone(value: number | null | undefined, danger = 15, healthy = 35): MonitorTone {
  if (value == null || Number.isNaN(value)) {
    return 'neutral';
  }
  if (value <= danger) {
    return 'danger';
  }
  if (value <= healthy) {
    return 'attention';
  }
  return 'healthy';
}

/**
 * 将监控色阶映射为进度条状态。
 *
 * @param tone 当前监控色阶
 * @returns Arco 进度条状态
 */
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

/**
 * 将监控色阶映射为标签颜色。
 *
 * @param tone 当前监控色阶
 * @returns Arco 标签颜色值
 */
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

/**
 * 将监控色阶映射为中文文案。
 *
 * @param tone 当前监控色阶
 * @returns 中文状态文案
 */
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

/**
 * 提取 Redis 状态的主要标签文案。
 *
 * @param monitor 当前服务监控数据
 * @returns Redis 状态标签
 */
function getRedisLabel(monitor: ServerMonitorVO | null) {
  if (monitor?.redis.available) {
    return '连接正常';
  }
  if (monitor?.redis.available === false) {
    return '连接异常';
  }
  return '待确认';
}

/**
 * 提取 Redis 状态的补充说明文案。
 *
 * @param monitor 当前服务监控数据
 * @returns Redis 状态说明
 */
function getRedisDetail(monitor: ServerMonitorVO | null) {
  if (monitor?.redis.available) {
    return `命中率 ${formatPercent(monitor.redis.hitRate)}，峰值内存 ${formatBytes(monitor.redis.usedMemoryPeak)}`;
  }
  if (monitor?.redis.available === false) {
    return monitor.redis.errorMessage || '请检查 Redis 连通性与配置。';
  }
  return '等待 Redis 指标返回。';
}

/**
 * 提取 Redis 状态对应的展示色阶。
 *
 * @param monitor 当前服务监控数据
 * @returns Redis 状态色阶
 */
function getRedisTone(monitor: ServerMonitorVO | null): MonitorTone {
  if (monitor?.redis.available) {
    return 'healthy';
  }
  if (monitor?.redis.available === false) {
    return 'danger';
  }
  return 'neutral';
}

/**
 * 把使用率换算成“剩余余量”百分比。
 *
 * @param value 百分比型使用率
 * @returns 余量百分比
 */
function toHeadroomPercent(value: number | null | undefined) {
  if (value == null || Number.isNaN(value)) {
    return null;
  }
  return Math.max(0, 100 - value);
}

/**
 * 归一化百分比数值，避免进度条出现非法范围。
 *
 * @param value 原始百分比
 * @returns 0 到 100 之间的百分比
 */
function normalizePercent(value: number | null | undefined) {
  if (value == null || Number.isNaN(value)) {
    return 0;
  }
  return Math.max(0, Math.min(100, Number(value.toFixed(2))));
}

/**
 * 将线性数值压缩到进度条可用的百分比区间。
 *
 * @param value 原始数值
 * @param maxValue 归一化上限
 * @returns 0 到 100 之间的线性百分比
 */
function normalizeLinearPercent(value: number | null | undefined, maxValue: number) {
  if (value == null || Number.isNaN(value) || maxValue <= 0) {
    return 0;
  }
  return Math.max(0, Math.min(100, Number(((value / maxValue) * 100).toFixed(2))));
}

/**
 * 格式化百分比展示文案。
 *
 * @param value 原始百分比
 * @returns 百分比字符串
 */
export function formatPercent(value: number | null | undefined) {
  if (value == null || Number.isNaN(value)) {
    return '--';
  }
  return `${value.toFixed(2)}%`;
}

/**
 * 格式化整数展示文案。
 *
 * @param value 原始整数
 * @returns 本地化后的整数文本
 */
export function formatInteger(value: number | null | undefined) {
  if (value == null || Number.isNaN(value)) {
    return '--';
  }
  return value.toLocaleString('zh-CN');
}

/**
 * 将字节数转换成人类可读的容量文案。
 *
 * @param value 原始字节数
 * @returns 可读性更高的容量字符串
 */
export function formatBytes(value: number | null | undefined) {
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

/**
 * 格式化 JVM 容量字段，兼容未限制场景。
 *
 * @param value 原始容量字节数
 * @returns JVM 容量展示文案
 */
export function formatJvmBytes(value: number | null | undefined) {
  if (value == null || Number.isNaN(value)) {
    return '--';
  }
  if (value < 0) {
    return '未限制';
  }
  return formatBytes(value);
}

/**
 * 将毫秒运行时长转换为中文时长文本。
 *
 * @param value 运行时长毫秒数
 * @returns 中文时长字符串
 */
export function formatDuration(value: number | null | undefined) {
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

/**
 * 格式化耗时文案。
 *
 * @param value 耗时毫秒数
 * @returns 带单位的耗时字符串
 */
export function formatCost(value: number | null | undefined) {
  if (value == null || Number.isNaN(value)) {
    return '--';
  }
  return `${value} ms`;
}

/**
 * 根据慢 SQL 耗时返回标签颜色。
 *
 * @param value SQL 耗时毫秒数
 * @returns 慢 SQL 标签颜色
 */
export function toSlowSqlColor(value: number | null | undefined) {
  if (value == null || Number.isNaN(value)) {
    return 'arcoblue';
  }
  if (value >= 300) {
    return 'red';
  }
  if (value >= 150) {
    return 'gold';
  }
  return 'green';
}
