<template>
  <section class="bi-panel hainan-map">
    <div class="bi-panel__header hainan-map__header">
      <div class="hainan-map__crumb">
        <el-button link type="primary" icon="Back" :disabled="level === 'city'" @click="backToCity">返回</el-button>
        <span>海南省</span>
        <span v-if="level === 'district'">/ {{ currentRegionName }}</span>
      </div>
      <small>{{ aggregateItems.length }} 个区域</small>
    </div>
    <div ref="mapRef" class="hainan-map__canvas"></div>
  </section>
</template>

<script setup name="HainanMap">
import * as echarts from 'echarts'
import { useRoute, useRouter } from 'vue-router'
import { getBiDeptAggregate } from '@/api/pii/bi'

const props = defineProps({
  queryParams: {
    type: Object,
    required: true
  }
})

const route = useRoute()
const router = useRouter()
const mapRef = ref(null)
const level = ref(route.query.level || 'city')
const parentDeptId = ref(Number(route.query.regionId || 200))
const currentRegionName = ref(route.query.regionName || '')
const aggregateItems = ref([])
let mapChart
let geoJsonLoaded = false

watch(() => ({ ...props.queryParams }), () => {
  loadAggregate()
}, { deep: true })

function loadMap() {
  return fetch('/static/hainan.json')
    .then(response => response.json())
    .then(geoJson => {
      echarts.registerMap('hainan', geoJson)
      geoJsonLoaded = true
    })
}

function loadAggregate() {
  return getBiDeptAggregate({
    ...props.queryParams,
    level: level.value,
    parentDeptId: parentDeptId.value
  }).then(response => {
    aggregateItems.value = response.data?.items || []
    renderMap()
  })
}

function renderMap() {
  if (!mapRef.value || !geoJsonLoaded) return
  mapChart = mapChart || echarts.init(mapRef.value)
  const values = aggregateItems.value.map(item => item.amount || 0)
  const max = Math.max(...values, 1)
  mapChart.setOption({
    tooltip: {
      trigger: 'item',
      formatter: params => {
        const item = aggregateItems.value.find(row => row.deptName === params.name)
        if (!item) return `${params.name}<br/>0.00 元`
        return `${item.deptName}<br/>交易金额：${formatCentAmount(item.amount)} 元<br/>订单：${item.count || 0} 笔<br/>商户：${item.merchantCount || 0} 家`
      }
    },
    visualMap: {
      min: 0,
      max,
      left: 12,
      bottom: 12,
      calculable: true,
      text: ['高', '低'],
      inRange: { color: ['#dceafe', '#6ea8fe', '#246bfe'] }
    },
    series: [{
      name: '海南',
      type: 'map',
      map: 'hainan',
      roam: true,
      zoom: 1.12,
      emphasis: { label: { show: true } },
      label: { show: true, fontSize: 11, color: '#172b4d' },
      itemStyle: { borderColor: '#fff', borderWidth: 1 },
      data: aggregateItems.value.map(item => ({
        name: item.deptName,
        value: item.amount || 0,
        deptId: item.deptId,
        regionCode: item.regionCode
      }))
    }]
  })
  mapChart.off('click')
  mapChart.on('click', handleMapClick)
}

function handleMapClick(params) {
  if (level.value === 'district') return
  const item = aggregateItems.value.find(row => row.deptName === params.name)
  if (!item) return
  level.value = 'district'
  parentDeptId.value = item.deptId
  currentRegionName.value = item.deptName
  syncUrl()
  loadAggregate()
}

function backToCity() {
  level.value = 'city'
  parentDeptId.value = 200
  currentRegionName.value = ''
  syncUrl()
  loadAggregate()
}

function syncUrl() {
  router.replace({
    query: {
      ...route.query,
      level: level.value,
      regionId: parentDeptId.value,
      regionName: currentRegionName.value || undefined
    }
  })
}

function resizeMap() {
  mapChart && mapChart.resize()
}

function formatCentAmount(amount) {
  return (Number(amount || 0) / 100).toFixed(2)
}

onMounted(() => {
  loadMap().then(loadAggregate)
  window.addEventListener('resize', resizeMap)
})

onBeforeUnmount(() => {
  window.removeEventListener('resize', resizeMap)
  mapChart && mapChart.dispose()
})
</script>

<style scoped>
.bi-panel {
  border: 1px solid #e6ebf2;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 8px 24px rgba(23, 43, 77, 0.04);
  padding: 16px;
}

.bi-panel__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  color: #172b4d;
  font-size: 16px;
  font-weight: 600;
}

.bi-panel__header small {
  color: #6b778c;
  font-size: 13px;
  font-weight: 400;
}

.hainan-map {
  margin-top: 14px;
}

.hainan-map__header {
  margin-bottom: 8px;
}

.hainan-map__crumb {
  display: flex;
  align-items: center;
  gap: 8px;
}

.hainan-map__canvas {
  height: 420px;
}

@media (max-width: 768px) {
  .hainan-map__canvas {
    height: 340px;
  }
}
</style>
