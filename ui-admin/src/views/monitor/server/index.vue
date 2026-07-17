<template>
  <div class="app-container server-monitor-page" v-loading="loading">
    <div class="server-monitor-actions">
      <el-button type="primary" icon="Refresh" :loading="loading" @click="getList">刷新</el-button>
    </div>

    <el-alert
      v-if="loadError"
      class="server-monitor-alert"
      title="宿主机监控数据加载失败"
      description="请检查后端服务、登录状态或网络连接后重试。"
      type="error"
      show-icon
      :closable="false"
    />

    <el-row v-if="!loadError" :gutter="12">
      <el-col :xs="24" :sm="24" :md="12" class="server-monitor-col">
        <section class="ui-panel-card server-panel">
          <div class="server-panel__header">
            <Cpu class="server-panel__icon" />
            <span>CPU</span>
          </div>
          <div class="server-info-table">
            <div class="server-info-row server-info-row--head">
              <span>属性</span>
              <span>值</span>
            </div>
            <div v-for="row in cpuRows" :key="row.label" class="server-info-row">
              <span class="server-info-label">{{ row.label }}</span>
              <span class="server-info-value">{{ formatText(row.value) }}</span>
            </div>
          </div>
        </section>
      </el-col>

      <el-col :xs="24" :sm="24" :md="12" class="server-monitor-col">
        <section class="ui-panel-card server-panel">
          <div class="server-panel__header">
            <Tickets class="server-panel__icon" />
            <span>内存</span>
          </div>
          <div class="server-info-table server-info-table--three">
            <div class="server-info-row server-info-row--head">
              <span>属性</span>
              <span>内存</span>
              <span>JVM</span>
            </div>
            <div v-for="row in memoryRows" :key="row.label" class="server-info-row">
              <span class="server-info-label">{{ row.label }}</span>
              <span class="server-info-value" :class="{ 'is-danger': row.memDanger }">{{ row.mem }}</span>
              <span class="server-info-value" :class="{ 'is-danger': row.jvmDanger }">{{ row.jvm }}</span>
            </div>
          </div>
        </section>
      </el-col>

      <el-col :span="24" class="server-monitor-col">
        <section class="ui-panel-card server-panel">
          <div class="server-panel__header">
            <Monitor class="server-panel__icon" />
            <span>服务器信息</span>
          </div>
          <div class="server-description-grid">
            <div v-for="row in systemRows" :key="row.label" class="server-description-item">
              <span class="server-info-label">{{ row.label }}</span>
              <span class="server-info-value">{{ formatText(row.value) }}</span>
            </div>
          </div>
        </section>
      </el-col>

      <el-col :span="24" class="server-monitor-col">
        <section class="ui-panel-card server-panel">
          <div class="server-panel__header">
            <CoffeeCup class="server-panel__icon" />
            <span>Java 虚拟机信息</span>
          </div>
          <div class="server-description-grid">
            <div
              v-for="row in jvmRows"
              :key="row.label"
              class="server-description-item"
              :class="{ 'server-description-item--wide': row.wide }"
            >
              <span class="server-info-label">{{ row.label }}</span>
              <span class="server-info-value server-info-value--wrap">{{ formatText(row.value) }}</span>
            </div>
          </div>
        </section>
      </el-col>

      <el-col :span="24" class="server-monitor-col">
        <section class="ui-panel-card server-panel">
          <div class="server-panel__header">
            <MessageBox class="server-panel__icon" />
            <span>磁盘状态</span>
          </div>
          <div class="server-disk-table">
            <el-table :data="server.sysFiles || []">
              <el-table-column label="盘符路径" prop="dirName" min-width="180" show-overflow-tooltip />
              <el-table-column label="文件系统" prop="sysTypeName" min-width="120" show-overflow-tooltip />
              <el-table-column label="盘符类型" prop="typeName" min-width="180" show-overflow-tooltip />
              <el-table-column label="总大小" prop="total" width="110" />
              <el-table-column label="可用大小" prop="free" width="110" />
              <el-table-column label="已用大小" prop="used" width="110" />
              <el-table-column label="已用百分比" width="120">
                <template #default="scope">
                  <span class="server-info-value" :class="{ 'is-danger': isDangerUsage(scope.row.usage) }">
                    {{ formatPercent(scope.row.usage) }}
                  </span>
                </template>
              </el-table-column>
            </el-table>
          </div>
        </section>
      </el-col>
    </el-row>
  </div>
</template>

<script setup name="ServerMonitor">
import { computed } from 'vue'
import { getServer } from '@/api/monitor/server'

const server = ref({})
const loading = ref(false)
const loadError = ref(false)

function getList() {
  loading.value = true
  loadError.value = false
  getServer().then(response => {
    server.value = response.data || {}
  }).catch(() => {
    loadError.value = true
  }).finally(() => {
    loading.value = false
  })
}

const cpuRows = computed(() => [
  { label: '核心数', value: server.value.cpu?.cpuNum },
  { label: '用户使用率', value: formatPercent(server.value.cpu?.used) },
  { label: '系统使用率', value: formatPercent(server.value.cpu?.sys) },
  { label: '当前空闲率', value: formatPercent(server.value.cpu?.free) }
])

const memoryRows = computed(() => [
  { label: '总内存', mem: formatSize(server.value.mem?.total, 'G'), jvm: formatSize(server.value.jvm?.total, 'M') },
  { label: '已用内存', mem: formatSize(server.value.mem?.used, 'G'), jvm: formatSize(server.value.jvm?.used, 'M') },
  { label: '剩余内存', mem: formatSize(server.value.mem?.free, 'G'), jvm: formatSize(server.value.jvm?.free, 'M') },
  {
    label: '使用率',
    mem: formatPercent(server.value.mem?.usage),
    jvm: formatPercent(server.value.jvm?.usage),
    memDanger: isDangerUsage(server.value.mem?.usage),
    jvmDanger: isDangerUsage(server.value.jvm?.usage)
  }
])

const systemRows = computed(() => [
  { label: '服务器名称', value: server.value.sys?.computerName },
  { label: '操作系统', value: server.value.sys?.osName },
  { label: '服务器 IP', value: server.value.sys?.computerIp },
  { label: '系统架构', value: server.value.sys?.osArch }
])

const jvmRows = computed(() => [
  { label: 'Java 名称', value: server.value.jvm?.name },
  { label: 'Java 版本', value: server.value.jvm?.version },
  { label: '启动时间', value: server.value.jvm?.startTime },
  { label: '运行时长', value: server.value.jvm?.runTime },
  { label: '安装路径', value: server.value.jvm?.home, wide: true },
  { label: '项目路径', value: server.value.sys?.userDir, wide: true },
  { label: '运行参数', value: server.value.jvm?.inputArgs, wide: true }
])

function formatPercent(value) {
  return value === undefined || value === null ? '-' : `${value}%`
}

function formatSize(value, unit) {
  return value === undefined || value === null ? '-' : `${value}${unit}`
}

function formatText(value) {
  return value === undefined || value === null || value === '' ? '-' : value
}

function isDangerUsage(value) {
  return Number(value) > 80
}

getList()
</script>

<style scoped>
.server-monitor-page {
  min-height: 100%;
}

.server-monitor-actions {
  display: flex;
  justify-content: flex-end;
  margin-bottom: 12px;
}

.server-monitor-alert {
  margin-bottom: 12px;
}

.server-monitor-col {
  margin-bottom: 12px;
}

.server-panel {
  height: 100%;
  overflow: hidden;
}

.server-panel__header {
  display: flex;
  align-items: center;
  gap: 8px;
  min-height: 46px;
  padding: 0 16px;
  color: var(--ui-text-primary);
  font-size: 14px;
  font-weight: 700;
  border-bottom: 1px solid var(--ui-border);
  background: var(--ui-bg-panel-muted);
}

.server-panel__icon {
  width: 16px;
  height: 16px;
  color: var(--ui-primary);
}

.server-info-table {
  display: grid;
  grid-template-columns: minmax(120px, 0.8fr) minmax(0, 1.2fr);
}

.server-info-table--three {
  grid-template-columns: minmax(110px, 0.8fr) minmax(0, 1fr) minmax(0, 1fr);
}

.server-info-row {
  display: contents;
}

.server-info-row > span {
  min-height: 42px;
  padding: 11px 14px;
  color: var(--ui-text-regular);
  line-height: 1.45;
  border-bottom: 1px solid var(--ui-table-border);
}

.server-info-row:last-child > span {
  border-bottom: 0;
}

.server-info-row--head > span {
  min-height: 40px;
  color: var(--ui-text-secondary);
  font-size: 13px;
  font-weight: 700;
  background: var(--ui-table-header-bg);
}

.server-info-label {
  color: var(--ui-text-secondary);
  font-weight: 600;
}

.server-info-value {
  color: var(--ui-text-primary);
  font-variant-numeric: tabular-nums;
}

.server-info-value.is-danger {
  color: var(--ui-danger);
  font-weight: 700;
}

.server-info-value--wrap {
  overflow-wrap: anywhere;
  word-break: break-word;
}

.server-description-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
}

.server-description-item {
  display: grid;
  grid-template-columns: 120px minmax(0, 1fr);
  gap: 12px;
  min-height: 44px;
  padding: 12px 16px;
  border-bottom: 1px solid var(--ui-table-border);
}

.server-description-item:nth-child(odd) {
  border-right: 1px solid var(--ui-table-border);
}

.server-description-item--wide {
  grid-column: 1 / -1;
  border-right: 0;
}

.server-disk-table {
  overflow-x: auto;
}

:deep(.server-disk-table .el-table) {
  min-width: 900px;
}

@media (max-width: 768px) {
  .server-description-grid {
    grid-template-columns: 1fr;
  }

  .server-description-item {
    grid-template-columns: 96px minmax(0, 1fr);
  }

  .server-description-item:nth-child(odd) {
    border-right: 0;
  }
}
</style>
