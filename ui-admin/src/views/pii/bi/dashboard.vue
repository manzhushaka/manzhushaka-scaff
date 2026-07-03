<template>
  <div class="app-container pii-bi-page">
    <el-form :model="queryParams" ref="queryRef" :inline="true" label-width="80px" class="bi-filter">
      <el-form-item label="商户ID" prop="merchantId">
        <el-input-number v-model="queryParams.merchantId" :min="1" controls-position="right" style="width: 160px" />
      </el-form-item>
      <el-form-item label="统计时间">
        <el-date-picker
          v-model="timeRange"
          type="datetimerange"
          value-format="YYYY-MM-DD HH:mm:ss"
          start-placeholder="开始时间"
          end-placeholder="结束时间"
          range-separator="-"
          style="width: 360px"
        />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" icon="Search" @click="handleQuery">搜索</el-button>
        <el-button icon="Refresh" @click="resetQuery">重置</el-button>
      </el-form-item>
    </el-form>

    <el-row :gutter="14" class="bi-kpis">
      <el-col :xs="24" :sm="12" :lg="6" v-for="item in kpiItems" :key="item.key">
        <div class="bi-kpi" :class="item.key">
          <div class="bi-kpi__meta">
            <span>{{ item.label }}</span>
            <strong>{{ item.value }}</strong>
          </div>
          <div class="bi-kpi__unit">{{ item.unit }}</div>
        </div>
      </el-col>
    </el-row>

    <el-row :gutter="14" class="bi-chart-row">
      <el-col :xs="24" :lg="15">
        <section class="bi-panel">
          <div class="bi-panel__header">
            <span>交易额趋势</span>
            <small>{{ rangeLabel }}</small>
          </div>
          <div ref="trendChartRef" class="bi-chart"></div>
        </section>
      </el-col>
      <el-col :xs="24" :lg="9">
        <section class="bi-panel">
          <div class="bi-panel__header">
            <span>税目占比</span>
            <small>{{ taxItemTotalText }}</small>
          </div>
          <div ref="taxChartRef" class="bi-chart"></div>
        </section>
      </el-col>
    </el-row>

    <HainanMap :query-params="queryParams" />

    <el-row :gutter="14" class="bi-rank-row">
      <el-col :xs="24" :lg="10">
        <section class="bi-panel">
          <div class="bi-panel__header">
            <span>商户排行</span>
            <small>Top 10</small>
          </div>
          <div ref="rankChartRef" class="bi-rank-chart"></div>
        </section>
      </el-col>
      <el-col :xs="24" :lg="14">
        <section class="bi-panel">
          <el-collapse v-model="activePanels">
            <el-collapse-item name="abnormal">
              <template #title>
                <div class="bi-collapse-title">
                  <span>异常订单明细</span>
                  <small>{{ abnormalOrders.length }} 笔</small>
                </div>
              </template>
              <el-table :data="abnormalOrders" height="306">
                <el-table-column label="订单号" prop="outTradeNo" min-width="180" :show-overflow-tooltip="true" />
                <el-table-column label="金额" align="right" width="120">
                  <template #default="scope">{{ formatCentAmount(scope.row.amount) }} 元</template>
                </el-table-column>
                <el-table-column label="支付状态" prop="payStatus" width="110" />
                <el-table-column label="发票状态" prop="invoiceStatus" width="110" />
              </el-table>
            </el-collapse-item>
          </el-collapse>
        </section>
      </el-col>
    </el-row>
  </div>
</template>

<script setup name="PiiBiDashboard">
import * as echarts from 'echarts'
import { getBiData } from '@/api/pii/bi'
import HainanMap from './components/HainanMap.vue'

const { proxy } = getCurrentInstance()
const loading = ref(false)
const dashboard = ref({})
const timeRange = ref(defaultTimeRange())
const trendChartRef = ref(null)
const taxChartRef = ref(null)
const rankChartRef = ref(null)
const activePanels = ref(['abnormal'])
let trendChart
let taxChart
let rankChart

const data = reactive({
  queryParams: {
    merchantId: undefined,
    startTime: timeRange.value[0],
    endTime: timeRange.value[1]
  }
})

const { queryParams } = toRefs(data)

const kpiItems = computed(() => [
  { key: 'amount', label: '交易金额', value: formatCentAmount(dashboard.value.totalAmount), unit: '元' },
  { key: 'invoice', label: '开票金额', value: formatCentAmount(dashboard.value.totalInvoiceAmount), unit: '元' },
  { key: 'orders', label: '订单数量', value: formatNumber(dashboard.value.totalOrderCount), unit: '笔' },
  { key: 'abnormal', label: '异常订单', value: formatNumber(dashboard.value.abnormalOrderCount), unit: '笔' }
])

const rangeLabel = computed(() => `${queryParams.value.startTime || '-'} 至 ${queryParams.value.endTime || '-'}`)
const taxItemTotalText = computed(() => `${(dashboard.value.taxItemRatio || []).length} 个税目`)
const abnormalOrders = computed(() => dashboard.value.abnormalOrders || [])

function getData() {
  loading.value = true
  syncTimeRange()
  getBiData(queryParams.value).then(response => {
    dashboard.value = response.data || {}
    renderCharts()
  }).finally(() => {
    loading.value = false
  })
}

function handleQuery() {
  getData()
}

function resetQuery() {
  timeRange.value = defaultTimeRange()
  proxy.resetForm('queryRef')
  handleQuery()
}

function syncTimeRange() {
  queryParams.value.startTime = timeRange.value && timeRange.value.length === 2 ? timeRange.value[0] : undefined
  queryParams.value.endTime = timeRange.value && timeRange.value.length === 2 ? timeRange.value[1] : undefined
}

function renderCharts() {
  nextTick(() => {
    renderTrendChart()
    renderTaxChart()
    renderRankChart()
  })
}

function renderTrendChart() {
  if (!trendChartRef.value) return
  trendChart = trendChart || echarts.init(trendChartRef.value)
  const rows = dashboard.value.trend || []
  trendChart.setOption({
    color: ['#246BFE'],
    tooltip: { trigger: 'axis', valueFormatter: value => formatCentAmount(value) },
    grid: { left: 18, right: 18, top: 30, bottom: 20, containLabel: true },
    xAxis: { type: 'category', data: rows.map(item => item.day), axisTick: { show: false } },
    yAxis: { type: 'value', axisLabel: { formatter: value => `${(value / 100).toFixed(0)}` }, splitLine: { lineStyle: { color: '#eef1f5' } } },
    series: [{
      name: '交易金额',
      type: 'line',
      smooth: true,
      showSymbol: false,
      areaStyle: { opacity: 0.12 },
      lineStyle: { width: 3 },
      data: rows.map(item => item.amount || 0)
    }]
  })
}

function renderTaxChart() {
  if (!taxChartRef.value) return
  taxChart = taxChart || echarts.init(taxChartRef.value)
  const rows = dashboard.value.taxItemRatio || []
  taxChart.setOption({
    color: ['#246BFE', '#00A870', '#F59E0B', '#D94841', '#626AEF', '#14B8A6'],
    tooltip: { trigger: 'item', formatter: item => `${item.name}<br/>${formatCentAmount(item.value)} (${item.percent}%)` },
    legend: { bottom: 0, type: 'scroll' },
    series: [{
      name: '税目占比',
      type: 'pie',
      radius: ['48%', '72%'],
      center: ['50%', '44%'],
      avoidLabelOverlap: true,
      label: { formatter: '{b}' },
      data: rows.map(item => ({ name: item.taxItemName || item.taxItemId, value: item.amount || 0 }))
    }]
  })
}

function renderRankChart() {
  if (!rankChartRef.value) return
  rankChart = rankChart || echarts.init(rankChartRef.value)
  const rows = dashboard.value.merchantTop10 || []
  rankChart.setOption({
    color: ['#00A870'],
    tooltip: { trigger: 'axis', valueFormatter: value => `${formatCentAmount(value)} 元` },
    grid: { left: 18, right: 18, top: 24, bottom: 20, containLabel: true },
    xAxis: { type: 'value', axisLabel: { formatter: value => `${(value / 100).toFixed(0)}` }, splitLine: { lineStyle: { color: '#eef1f5' } } },
    yAxis: { type: 'category', data: rows.map(item => item.merchantName).reverse(), axisTick: { show: false } },
    series: [{
      name: '交易金额',
      type: 'bar',
      barWidth: 14,
      data: rows.map(item => item.amount || 0).reverse()
    }]
  })
}

function resizeCharts() {
  trendChart && trendChart.resize()
  taxChart && taxChart.resize()
  rankChart && rankChart.resize()
}

function formatCentAmount(amount) {
  return (Number(amount || 0) / 100).toFixed(2)
}

function formatNumber(value) {
  return Number(value || 0).toLocaleString()
}

function defaultTimeRange() {
  const end = new Date()
  const start = new Date()
  start.setDate(end.getDate() - 6)
  start.setHours(0, 0, 0, 0)
  end.setHours(23, 59, 59, 0)
  return [formatDateTime(start), formatDateTime(end)]
}

function formatDateTime(date) {
  const pad = value => String(value).padStart(2, '0')
  return `${date.getFullYear()}-${pad(date.getMonth() + 1)}-${pad(date.getDate())} ${pad(date.getHours())}:${pad(date.getMinutes())}:${pad(date.getSeconds())}`
}

onMounted(() => {
  getData()
  window.addEventListener('resize', resizeCharts)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', resizeCharts)
  trendChart && trendChart.dispose()
  taxChart && taxChart.dispose()
  rankChart && rankChart.dispose()
})
</script>

<style scoped>
.pii-bi-page {
  background: #f6f8fb;
  min-height: calc(100vh - 84px);
}

.bi-filter,
.bi-panel,
.bi-kpi {
  border: 1px solid #e6ebf2;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 8px 24px rgba(23, 43, 77, 0.04);
}

.bi-filter {
  padding: 18px 18px 0;
  margin-bottom: 14px;
}

.bi-kpis {
  margin-bottom: 14px;
}

.bi-kpi {
  min-height: 112px;
  padding: 18px;
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  position: relative;
  overflow: hidden;
}

.bi-kpi::before {
  content: "";
  position: absolute;
  inset: 0 0 auto 0;
  height: 4px;
  background: #246bfe;
}

.bi-kpi.invoice::before {
  background: #00a870;
}

.bi-kpi.orders::before {
  background: #f59e0b;
}

.bi-kpi.abnormal::before {
  background: #d94841;
}

.bi-kpi__meta {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.bi-kpi__meta span,
.bi-panel__header small,
.bi-kpi__unit {
  color: #6b778c;
  font-size: 13px;
}

.bi-kpi__meta strong {
  color: #172b4d;
  font-size: 28px;
  line-height: 1;
  font-weight: 700;
}

.bi-kpi__unit {
  margin-bottom: 2px;
}

.bi-chart-row {
  row-gap: 14px;
}

.bi-rank-row {
  margin-top: 14px;
  row-gap: 14px;
}

.bi-panel {
  padding: 16px;
}

.bi-panel__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
  color: #172b4d;
  font-size: 16px;
  font-weight: 600;
}

.bi-chart {
  height: 340px;
}

.bi-rank-chart {
  height: 344px;
}

.bi-collapse-title {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding-right: 16px;
  color: #172b4d;
  font-size: 16px;
  font-weight: 600;
}

.bi-collapse-title small {
  color: #6b778c;
  font-size: 13px;
  font-weight: 400;
}

@media (max-width: 768px) {
  .bi-kpi {
    margin-bottom: 12px;
  }

  .bi-panel__header {
    align-items: flex-start;
    flex-direction: column;
    gap: 6px;
  }

  .bi-chart {
    height: 300px;
  }
}
</style>
