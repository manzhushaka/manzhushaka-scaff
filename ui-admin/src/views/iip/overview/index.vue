<template>
   <div class="app-container iip-overview">
      <!-- 汇总指标：11 个指标卡片平铺，卡片之间不嵌套 -->
      <div v-loading="summaryLoading" class="metric-grid">
         <div v-for="item in metricDefs" :key="item.key" class="ui-panel-card metric-card" :class="item.tone">
            <div class="metric-card__icon" aria-hidden="true">
               <el-icon><component :is="item.icon" /></el-icon>
            </div>
            <div class="metric-card__body">
               <div class="metric-card__value">{{ formatNumber(summary[item.key]) }}</div>
               <div class="metric-card__label">{{ item.label }}</div>
            </div>
         </div>
      </div>

      <!-- 近7日趋势（echarts 已存在于 package.json，按缓存监控页同款模块化方式引入） -->
      <div class="ui-panel-card trend-panel">
         <div class="ui-card-header">
            <span class="ui-card-header-left">
               <el-icon class="ui-card-header-icon"><TrendCharts /></el-icon>
               近7日趋势
            </span>
         </div>
         <div v-loading="trendLoading" ref="trendChartRef" class="trend-chart"></div>
      </div>
   </div>
</template>

<script setup name="IipOverview">
import { getSummary, getTrend } from "@/api/iip/overview"
import { init, use } from 'echarts/core'
import { BarChart, LineChart } from 'echarts/charts'
import { GridComponent, LegendComponent, TooltipComponent } from 'echarts/components'
import { CanvasRenderer } from 'echarts/renderers'

use([BarChart, LineChart, GridComponent, LegendComponent, TooltipComponent, CanvasRenderer])

const summary = ref({})
const summaryLoading = ref(false)
const trendLoading = ref(false)
const trendChartRef = ref(null)
let trendChart = null

// 指标卡片定义（顺序即展示顺序，共 11 项）
const metricDefs = [
   { key: 'memberCount', label: '用户数', icon: 'User', tone: 'tone-primary' },
   { key: 'merchantCount', label: '商户数', icon: 'Shop', tone: 'tone-success' },
   { key: 'pendingMerchantCount', label: '待审核商户', icon: 'OfficeBuilding', tone: 'tone-warning' },
   { key: 'pendingInvoiceCount', label: '待审核发票', icon: 'Document', tone: 'tone-warning' },
   { key: 'approvedInvoiceCount', label: '已通过发票', icon: 'CircleCheck', tone: 'tone-success' },
   { key: 'rejectedInvoiceCount', label: '已驳回发票', icon: 'CircleClose', tone: 'tone-supplement' },
   { key: 'pointsIssued', label: '累计发放积分', icon: 'Star', tone: 'tone-primary' },
   { key: 'pointsConsumed', label: '累计消耗积分', icon: 'Coin', tone: 'tone-accent' },
   { key: 'couponExchangeCount', label: '兑换总数', icon: 'Goods', tone: 'tone-accent' },
   { key: 'verifiedCouponCount', label: '已核销数', icon: 'Checked', tone: 'tone-success' },
   { key: 'activeActivityCount', label: '进行中活动', icon: 'Flag', tone: 'tone-primary' }
]

// 图表配色与 theme-tokens.scss 的 --ui-chart-1/2/3 保持一致（echarts 画布需具体色值）
const CHART_COLORS = ['#ff6a2a', '#1f8a5b', '#b76e00']

/** 数字展示格式化 */
function formatNumber(value) {
   const num = Number(value)
   return Number.isFinite(num) ? num.toLocaleString('zh-CN') : '0'
}

/** 加载汇总指标 */
function loadSummary() {
   summaryLoading.value = true
   getSummary().then(response => {
      summary.value = response.data || {}
      summaryLoading.value = false
   }).catch(() => {
      summaryLoading.value = false
   })
}

/** 近 7 天日期轴（含今天，yyyy-MM-dd 升序） */
function buildLast7Days() {
   const days = []
   for (let i = 6; i >= 0; i--) {
      const date = new Date()
      date.setDate(date.getDate() - i)
      days.push(formatDay(date))
   }
   return days
}

/** 日期格式化 yyyy-MM-dd */
function formatDay(date) {
   const year = date.getFullYear()
   const month = String(date.getMonth() + 1).padStart(2, '0')
   const day = String(date.getDate()).padStart(2, '0')
   return `${year}-${month}-${day}`
}

/** 按日期轴补全零值日（趋势接口可能只返回有数据的日期） */
function fillSeries(dayAxis, list) {
   const countMap = new Map((list || []).map(item => [item.day, Number(item.cnt) || 0]))
   return dayAxis.map(day => countMap.get(day) || 0)
}

/** 加载趋势并渲染图表 */
function loadTrend() {
   trendLoading.value = true
   getTrend().then(response => {
      trendLoading.value = false
      renderTrend(response.data || {})
   }).catch(() => {
      trendLoading.value = false
   })
}

/** 渲染近 7 日趋势图 */
function renderTrend(trend) {
   const dayAxis = buildLast7Days()
   const axisLabels = dayAxis.map(day => day.slice(5))
   const invoiceData = fillSeries(dayAxis, trend.invoiceTrend)
   const pointsData = fillSeries(dayAxis, trend.pointsTrend)
   const exchangeData = fillSeries(dayAxis, trend.exchangeTrend)

   nextTick(() => {
      if (!trendChartRef.value) {
         return
      }
      if (!trendChart) {
         trendChart = init(trendChartRef.value)
      }
      trendChart.setOption({
         color: CHART_COLORS,
         tooltip: { trigger: 'axis' },
         legend: { data: ['发票上传量', '积分发放量', '兑换量'], bottom: 0 },
         grid: { left: 12, right: 16, top: 24, bottom: 36, containLabel: true },
         xAxis: {
            type: 'category',
            data: axisLabels,
            axisTick: { alignWithLabel: true }
         },
         yAxis: { type: 'value', minInterval: 1 },
         series: [
            { name: '发票上传量', type: 'bar', barMaxWidth: 22, data: invoiceData },
            { name: '积分发放量', type: 'line', smooth: true, data: pointsData },
            { name: '兑换量', type: 'bar', barMaxWidth: 22, data: exchangeData }
         ]
      })
      trendChart.resize()
   })
}

/** 窗口尺寸变化时重绘 */
function handleResize() {
   if (trendChart) {
      trendChart.resize()
   }
}

onMounted(() => {
   loadSummary()
   loadTrend()
   window.addEventListener('resize', handleResize)
})

onBeforeUnmount(() => {
   window.removeEventListener('resize', handleResize)
   if (trendChart) {
      trendChart.dispose()
      trendChart = null
   }
})
</script>

<style lang="scss" scoped>
.iip-overview {
   display: flex;
   flex-direction: column;
   gap: 14px;
}

.metric-grid {
   display: grid;
   grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
   gap: 12px;
}

.metric-card {
   display: flex;
   align-items: center;
   gap: 12px;
   padding: 14px 16px;
}

.metric-card__icon {
   display: grid;
   flex: 0 0 40px;
   width: 40px;
   height: 40px;
   place-items: center;
   color: var(--tone-color);
   background: var(--tone-bg);
   border-radius: var(--ui-radius-control);
   font-size: 20px;
}

.metric-card__body {
   min-width: 0;
}

.metric-card__value {
   color: var(--ui-text-primary);
   font-size: 22px;
   font-weight: 700;
   line-height: 1.3;
   font-variant-numeric: tabular-nums;
}

.metric-card__label {
   margin-top: 2px;
   color: var(--ui-text-secondary);
   font-size: 12px;
   line-height: 1.4;
}

.trend-panel {
   padding: 16px;
}

.trend-chart {
   width: 100%;
   height: 320px;
}

@media screen and (max-width: 991px) {
   .metric-grid {
      grid-template-columns: repeat(auto-fill, minmax(160px, 1fr));
   }
}

@media screen and (max-width: 640px) {
   .metric-grid {
      grid-template-columns: repeat(2, 1fr);
   }

   .metric-card {
      padding: 12px;
   }

   .metric-card__icon {
      flex-basis: 32px;
      width: 32px;
      height: 32px;
      font-size: 16px;
   }

   .metric-card__value {
      font-size: 18px;
   }

   .trend-chart {
      height: 260px;
   }
}
</style>
