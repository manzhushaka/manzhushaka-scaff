<template>
  <a-drawer :visible="visible" size="700px" render-to-body :footer="false" @update:visible="$emit('update:visible', $event)">
    <!-- 自定义标题 -->
    <template #header>
      <div class="drawer-head">
        <span class="drawer-head-icon"><List /></span>
        <span class="drawer-head-name">{{ row.dictName }}</span>
        <span class="drawer-head-type">{{ row.dictType }}</span>
      </div>
    </template>

    <div class="drawer-wrap">
      <!-- 加载中 -->
      <div v-if="loading" class="drawer-loading">
        <span class="is-loading"><Loading /></span>
        <span>加载中...</span>
      </div>

      <!-- 空数据 -->
      <div v-else-if="!dataList.length" class="drawer-empty">
        <span style="font-size:36px;"><Document /></span>
        <div>暂无字典数据</div>
      </div>

      <template v-else>
        <!-- 统计卡片 -->
        <a-row :gutter="12" class="stat-row">
          <a-col :span="disabledCount > 0 ? 8 : 12">
            <div class="stat-card">
              <div class="stat-num">{{ dataList.length }}</div>
              <div class="stat-label">共计条目</div>
            </div>
          </a-col>
          <a-col :span="disabledCount > 0 ? 8 : 12">
            <div class="stat-card">
              <div class="stat-num success">{{ normalCount }}</div>
              <div class="stat-label">正常</div>
            </div>
          </a-col>
          <a-col v-if="disabledCount > 0" :span="8">
            <div class="stat-card">
              <div class="stat-num danger">{{ disabledCount }}</div>
              <div class="stat-label">停用</div>
            </div>
          </a-col>
        </a-row>

        <!-- 数据列表 -->
        <div v-for="item in dataList" :key="item.dictCode" class="dict-item">
          <div class="dict-cell">
            <div class="dict-cell-key">标签</div>
            <div class="dict-cell-val">
              <a-tag v-if="item.listClass && item.listClass !== 'default'" :color="getTagColor(item.listClass)" size="small">{{ item.dictLabel }}</a-tag>
              <span v-else>{{ item.dictLabel }}</span>
            </div>
          </div>
          <div class="dict-cell">
            <div class="dict-cell-key">键值</div>
            <div class="dict-cell-val">{{ item.dictValue }}</div>
          </div>
          <div class="dict-cell">
            <div class="dict-cell-key">状态</div>
            <div class="dict-cell-val">
              <a-tag :color="item.status === '0' ? 'green' : 'red'" size="small">
                {{ item.status === '0' ? '正常' : '停用' }}
              </a-tag>
            </div>
          </div>
        </div>
      </template>
    </div>
  </a-drawer>
</template>

<script setup>
import { listData } from '@/api/system/dict/data'

const props = defineProps({
  visible: { type: Boolean, default: false },
  row: { type: Object, default: () => ({}) }
})

const emit = defineEmits(['update:visible'])

const loading = ref(false)
const dataList = ref([])

const normalCount = computed(() => dataList.value.filter(r => r.status === '0').length)
const disabledCount = computed(() => dataList.value.filter(r => r.status !== '0').length)

function getTagColor(type) {
  return { primary: 'arcoblue', success: 'green', warning: 'orange', danger: 'red', info: 'gray' }[type]
}

watch(() => props.visible, (val) => {
  if (val) {
    loadData()
  } else {
    dataList.value = []
  }
})

function loadData() {
  if (!props.row?.dictType) return
  loading.value = true
  dataList.value = []
  listData({ dictType: props.row.dictType, pageSize: 100, pageNum: 1 }).then(response => {
    dataList.value = response.rows || []
  }).catch(() => {}).finally(() => {
    loading.value = false
  })
}
</script>

<style scoped>
.drawer-head {
  display: flex;
  align-items: center;
}
.drawer-head-icon {
  margin-right: 8px;
  color: var(--ui-primary);
}
.drawer-head-name {
  font-size: 16px;
  font-weight: 600;
  color: var(--ui-text-primary);
  margin-right: 8px;
}
.drawer-head-type {
  font-size: 14px;
  color: var(--ui-text-secondary);
  font-family: monospace;
}
.drawer-wrap {
  padding: 0 20px 20px;
}
.drawer-loading {
  display: flex;
  align-items: center;
  justify-content: center;
  height: 120px;
  color: var(--ui-text-secondary);
  font-size: 13px;
  gap: 8px;
}
.drawer-empty {
  text-align: center;
  color: var(--ui-text-secondary);
  padding: 60px 0;
  font-size: 13px;
}
.drawer-empty .arco-icon {
  display: block;
  margin: 0 auto 8px;
}
.stat-row {
  margin-bottom: 16px;
}
.stat-card {
  background: var(--ui-bg-panel-soft);
  border: 1px solid var(--ui-border);
  border-radius: var(--ui-radius-panel);
  padding: 10px 14px;
  text-align: center;
}
.stat-num {
  font-size: 22px;
  font-weight: 700;
  color: var(--ui-text-primary);
}
.stat-num.success { color: var(--ui-success); }
.stat-num.danger  { color: var(--ui-danger); }
.stat-label {
  font-size: 11px;
  color: var(--ui-text-secondary);
  margin-top: 4px;
}
.dict-item {
  display: grid;
  grid-template-columns: 1fr 1fr 1fr;
  border: 1px solid var(--ui-border);
  border-radius: var(--ui-radius-panel);
  overflow: hidden;
  margin-bottom: 8px;
}
.dict-cell {
  display: grid;
  grid-template-columns: 70px 1fr;
  border-right: 1px solid var(--ui-border);
}
.dict-cell:last-child {
  border-right: 0;
}
.dict-cell-key {
  padding: 9px 14px;
  font-size: 12px;
  color: var(--ui-text-secondary);
  background: var(--ui-bg-panel-soft);
  border-right: 1px solid var(--ui-border);
}
.dict-cell-val {
  padding: 9px 14px;
  font-size: 13px;
  color: var(--ui-text-primary);
  word-break: break-all;
  display: flex;
  align-items: center;
}
</style>
