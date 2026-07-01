<template>
  <div class="app-container">
    <el-row :gutter="12">
      <el-col :span="24" class="card-box">
        <section class="ui-panel-card cache-panel">
          <div class="cache-panel__header">
            <Monitor class="cache-panel__icon" />
            <span>基本信息</span>
          </div>
          <div class="cache-info-grid">
            <div v-for="row in basicInfoRows" :key="row.label" class="cache-info-item">
              <span class="cache-info-label">{{ row.label }}</span>
              <span class="cache-info-value">{{ formatText(row.value) }}</span>
            </div>
          </div>
        </section>
      </el-col>

      <el-col :xs="24" :sm="24" :md="12" class="card-box">
        <section class="ui-panel-card cache-panel">
          <div class="cache-panel__header">
            <PieChart class="cache-panel__icon" />
            <span>命令统计</span>
          </div>
          <div ref="commandstats" class="cache-chart" />
        </section>
      </el-col>

      <el-col :xs="24" :sm="24" :md="12" class="card-box">
        <section class="ui-panel-card cache-panel">
          <div class="cache-panel__header">
            <Odometer class="cache-panel__icon" />
            <span>内存信息</span>
          </div>
          <div ref="usedmemory" class="cache-chart" />
        </section>
      </el-col>
    </el-row>
  </div>
</template>

<script setup name="Cache">
import { computed } from 'vue'
import { getCache } from '@/api/monitor/cache'
import * as echarts from 'echarts'

const cache = ref({})
const commandstats = ref(null)
const usedmemory = ref(null)
const { proxy } = getCurrentInstance()

const basicInfoRows = computed(() => [
  { label: 'Redis版本', value: cache.value.info?.redis_version },
  { label: '运行模式', value: formatRedisMode(cache.value.info?.redis_mode) },
  { label: '端口', value: cache.value.info?.tcp_port },
  { label: '客户端数', value: cache.value.info?.connected_clients },
  { label: '运行时间(天)', value: cache.value.info?.uptime_in_days },
  { label: '使用内存', value: cache.value.info?.used_memory_human },
  { label: '使用CPU', value: formatFixed(cache.value.info?.used_cpu_user_children, 2) },
  { label: '内存配置', value: cache.value.info?.maxmemory_human },
  { label: 'AOF是否开启', value: formatEnabled(cache.value.info?.aof_enabled) },
  { label: 'RDB是否成功', value: cache.value.info?.rdb_last_bgsave_status },
  { label: 'Key数量', value: cache.value.dbSize },
  { label: '网络入口/出口', value: formatNetwork(cache.value.info) }
])

function getList() {
  proxy.$modal.loading("正在加载缓存监控数据，请稍候！")
  getCache().then(response => {
    proxy.$modal.closeLoading()
    const data = response.data || {}
    const info = data.info || {}
    cache.value = data

    const commandstatsIntance = echarts.init(commandstats.value, "macarons")
    commandstatsIntance.setOption({
      tooltip: {
        trigger: "item",
        formatter: "{a} <br/>{b} : {c} ({d}%)"
      },
      series: [
        {
          name: "命令",
          type: "pie",
          roseType: "radius",
          radius: [15, 95],
          center: ["50%", "38%"],
          data: data.commandStats || [],
          animationEasing: "cubicInOut",
          animationDuration: 1000
        }
      ]
    })
    const usedmemoryInstance = echarts.init(usedmemory.value, "macarons")
    usedmemoryInstance.setOption({
      tooltip: {
        formatter: "{b} <br/>{a} : " + formatText(info.used_memory_human)
      },
      series: [
        {
          name: "峰值",
          type: "gauge",
          min: 0,
          max: 1000,
          detail: {
            formatter: formatText(info.used_memory_human)
          },
          data: [
            {
              value: formatNumber(info.used_memory_human),
              name: "内存消耗"
            }
          ]
        }
      ]
    })
    window.addEventListener("resize", () => {
      commandstatsIntance.resize()
      usedmemoryInstance.resize()
    })
  })
}

function formatText(value) {
  return value === undefined || value === null || value === "" ? "-" : value
}

function formatFixed(value, digits) {
  return value === undefined || value === null || value === "" || Number.isNaN(Number(value)) ? "-" : Number(value).toFixed(digits)
}

function formatNumber(value) {
  return value === undefined || value === null || value === "" || Number.isNaN(Number.parseFloat(value)) ? 0 : Number.parseFloat(value)
}

function formatRedisMode(value) {
  if (value === undefined || value === null || value === "") {
    return "-"
  }
  return value === "standalone" ? "单机" : "集群"
}

function formatEnabled(value) {
  if (value === undefined || value === null || value === "") {
    return "-"
  }
  return value === "0" ? "否" : "是"
}

function formatNetwork(info) {
  if (!info) {
    return "-"
  }
  return `${formatText(info.instantaneous_input_kbps)}kps/${formatText(info.instantaneous_output_kbps)}kps`
}

getList()
</script>

<style lang="scss" scoped>
.cache-panel {
  height: 100%;
  padding: 0;
  overflow: hidden;
}

.cache-panel__header {
  display: flex;
  align-items: center;
  gap: 8px;
  min-height: 44px;
  padding: 0 16px;
  color: var(--ui-text-primary);
  font-size: 14px;
  font-weight: 700;
  border-bottom: 1px solid var(--ui-border);
  background: var(--ui-bg-panel-muted);
}

.cache-panel__icon {
  width: 16px;
  height: 16px;
  color: var(--ui-primary);
}

.cache-info-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 12px;
  padding: 16px;
}

.cache-info-item {
  display: flex;
  flex-direction: column;
  gap: 8px;
  min-height: 78px;
  padding: 14px 16px;
  border: 1px solid var(--ui-border);
  border-radius: var(--ui-radius-control);
  background: var(--ui-bg-panel-muted);
}

.cache-info-label {
  color: var(--ui-text-secondary);
  font-size: 13px;
  font-weight: 600;
  line-height: 1.35;
}

.cache-info-value {
  color: var(--ui-text-primary);
  font-size: 15px;
  font-weight: 600;
  line-height: 1.4;
  font-variant-numeric: tabular-nums;
  overflow-wrap: anywhere;
  word-break: break-word;
}

.cache-chart {
  height: 420px;
  min-height: 320px;
}

@media (max-width: 1400px) {
  .cache-info-grid {
    grid-template-columns: repeat(3, minmax(0, 1fr));
  }
}

@media (max-width: 992px) {
  .cache-info-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 576px) {
  .cache-info-grid {
    grid-template-columns: 1fr;
  }
}
</style>
