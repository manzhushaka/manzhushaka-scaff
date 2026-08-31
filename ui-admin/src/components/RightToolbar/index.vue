<template>
  <div class="top-right-btn" :style="style">
    <a-space size="mini">
      <a-tooltip v-if="search" :content="showSearch ? '隐藏搜索' : '显示搜索'" position="top">
        <a-button shape="circle" :aria-label="showSearch ? '隐藏搜索' : '显示搜索'" @click="toggleSearch">
          <template #icon><icon-search /></template>
        </a-button>
      </a-tooltip>
      <a-tooltip content="刷新" position="top">
        <a-button shape="circle" aria-label="刷新" @click="refresh">
          <template #icon><icon-refresh /></template>
        </a-button>
      </a-tooltip>
      <a-tooltip v-if="columnEntries.length > 0" content="显隐列" position="top">
        <a-button v-if="showColumnsType === 'transfer'" shape="circle" aria-label="显隐列" @click="showColumn">
          <template #icon><icon-list /></template>
        </a-button>
        <a-dropdown v-else trigger="click" :hide-on-select="false" position="br">
          <a-button shape="circle" aria-label="显隐列">
            <template #icon><icon-list /></template>
          </a-button>
          <template #content>
            <div class="column-menu" @click.stop>
              <a-checkbox
                :model-value="isChecked"
                :indeterminate="isIndeterminate"
                @change="toggleCheckAll"
              >
                列展示
              </a-checkbox>
              <a-divider :margin="8" />
              <a-checkbox
                v-for="column in columnEntries"
                :key="column.key"
                :model-value="column.visible"
                @change="visible => checkboxChange(visible, column.key)"
              >
                {{ column.label }}
              </a-checkbox>
            </div>
          </template>
        </a-dropdown>
      </a-tooltip>
    </a-space>

    <a-modal v-model:visible="open" title="显示/隐藏列" :footer="false" width="420px">
      <div class="column-dialog-list">
        <a-checkbox
          v-for="column in columnEntries"
          :key="column.key"
          :model-value="column.visible"
          @change="visible => checkboxChange(visible, column.key)"
        >
          {{ column.label }}
        </a-checkbox>
      </div>
    </a-modal>
  </div>
</template>

<script setup>
import { IconList, IconRefresh, IconSearch } from '@arco-design/web-vue/es/icon'
import cache from '@/plugins/cache'

const props = defineProps({
  showSearch: {
    type: Boolean,
    default: true
  },
  columns: {
    type: [Array, Object],
    default: () => ({})
  },
  search: {
    type: Boolean,
    default: true
  },
  showColumnsType: {
    type: String,
    default: 'checkbox'
  },
  gutter: {
    type: Number,
    default: 10
  },
  storageKey: {
    type: String,
    default: ''
  }
})

const emits = defineEmits(['update:showSearch', 'queryTable'])
const open = ref(false)
const { proxy } = getCurrentInstance()

const style = computed(() => props.gutter ? { marginRight: `${props.gutter / 2}px` } : {})
const columnEntries = computed(() => {
  if (Array.isArray(props.columns)) {
    return props.columns.map((column, index) => ({
      key: column.key ?? index,
      label: column.label,
      visible: column.visible
    }))
  }
  return Object.keys(props.columns).map(key => ({
    key,
    label: props.columns[key].label,
    visible: props.columns[key].visible
  }))
})
const isChecked = computed(() => columnEntries.value.length > 0 && columnEntries.value.every(column => column.visible))
const isIndeterminate = computed(() => columnEntries.value.some(column => column.visible) && !isChecked.value)

/**
 * 显示或隐藏当前页面的筛选区域。
 */
function toggleSearch() {
  let element = proxy.$el
  let formElement = null
  while ((element = element.parentElement) && element !== document.body) {
    formElement = element.querySelector('.arco-form')
    if (formElement) {
      break
    }
  }
  if (!formElement) {
    emits('update:showSearch', !props.showSearch)
    return
  }
  animateSearch(formElement, props.showSearch)
}

/**
 * 执行筛选区域的展开或收起动画。
 *
 * @param {HTMLElement} element 筛选表单元素
 * @param {boolean} isHide 是否隐藏
 */
function animateSearch(element, isHide) {
  const duration = 260
  const transition = 'max-height 0.25s ease, opacity 0.2s ease'
  const clear = () => Object.assign(element.style, { transition: '', maxHeight: '', opacity: '', overflow: '' })
  Object.assign(element.style, { overflow: 'hidden', transition: '' })
  if (isHide) {
    Object.assign(element.style, { maxHeight: `${element.scrollHeight}px`, opacity: '1', transition })
    requestAnimationFrame(() => Object.assign(element.style, { maxHeight: '0', opacity: '0' }))
    setTimeout(() => {
      emits('update:showSearch', false)
      clear()
    }, duration)
    return
  }
  emits('update:showSearch', true)
  nextTick(() => {
    Object.assign(element.style, { maxHeight: '0', opacity: '0' })
    requestAnimationFrame(() => requestAnimationFrame(() => {
      Object.assign(element.style, { transition, maxHeight: `${element.scrollHeight}px`, opacity: '1' })
    }))
    setTimeout(clear, duration)
  })
}

/**
 * 触发表格刷新。
 */
function refresh() {
  emits('queryTable')
}

/**
 * 打开列设置弹窗。
 */
function showColumn() {
  open.value = true
}

/**
 * 更新单列可见状态。
 *
 * @param {boolean} visible 是否可见
 * @param {string|number} key 列标识
 */
function checkboxChange(visible, key) {
  if (Array.isArray(props.columns)) {
    const targetColumn = props.columns.find((column, index) => (column.key ?? index) === key)
    if (targetColumn) {
      targetColumn.visible = visible
    }
  } else if (props.columns[key]) {
    props.columns[key].visible = visible
  }
  saveStorage()
}

/**
 * 切换全部列的可见状态。
 */
function toggleCheckAll() {
  const visible = !isChecked.value
  if (Array.isArray(props.columns)) {
    props.columns.forEach(column => { column.visible = visible })
  } else {
    Object.values(props.columns).forEach(column => { column.visible = visible })
  }
  saveStorage()
}

/**
 * 将列可见状态保存到本地缓存。
 */
function saveStorage() {
  if (!props.storageKey) {
    return
  }
  const state = {}
  if (Array.isArray(props.columns)) {
    props.columns.forEach((column, index) => { state[index] = column.visible })
  } else {
    Object.keys(props.columns).forEach(key => { state[key] = props.columns[key].visible })
  }
  cache.local.setJSON(props.storageKey, state)
}

if (props.storageKey) {
  const saved = cache.local.getJSON(props.storageKey)
  if (saved && typeof saved === 'object') {
    if (Array.isArray(props.columns)) {
      props.columns.forEach((column, index) => {
        if (saved[index] !== undefined) {
          column.visible = saved[index]
        }
      })
    } else {
      Object.keys(props.columns).forEach(key => {
        if (saved[key] !== undefined) {
          props.columns[key].visible = saved[key]
        }
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.column-menu {
  display: flex;
  flex-direction: column;
  gap: 8px;
  min-width: 168px;
  max-height: 320px;
  padding: 10px 12px;
  overflow-y: auto;
}

.column-dialog-list {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px 16px;
}

@media (max-width: 640px) {
  .column-dialog-list {
    grid-template-columns: 1fr;
  }
}
</style>
