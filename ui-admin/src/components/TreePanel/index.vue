<template>
  <div class="tree-sidebar" :class="{ collapsed: collapsed, resizing: isResizing, 'no-initial-transition': isLoadingFromStorage}" :style="{ width: sidebarWidth + 'px' }">
    <!-- 右侧拖动条 -->
    <div v-if="!collapsed" class="resize-handle" @mousedown="startResize" @touchstart="startResize" :class="{ active: isResizing }" />
    <div class="tree-header">
      <span class="tree-title" v-show="!collapsed">
        <span><component :is="titleIcon" /></span> {{ title }}
      </span>
      <div class="tree-actions" v-show="!collapsed">
        <a-tooltip :content="isExpandedAll ? '收起全部' : '展开全部'" position="right">
          <span class="tree-action-icon" @click="toggleExpandAll">
            <ArrowDown v-if="isExpandedAll" />
            <ArrowUp v-else />
          </span>
        </a-tooltip>
        <a-tooltip content="刷新" position="right">
          <span class="tree-action-icon" @click="handleRefresh"><Refresh /></span>
        </a-tooltip>
        <slot name="actions"></slot>
      </div>
    </div>

    <!-- 侧边栏展开/收起按钮 -->
    <div class="collapse-button-container">
      <a-tooltip :content="collapsed ? '展开' : '收起'" position="right">
        <span class="collapse-button" @click="toggleCollapsed">
          <DArrowRight v-if="collapsed" />
          <DArrowLeft v-else />
        </span>
      </a-tooltip>
    </div>

    <div class="tree-search" v-show="!collapsed" v-if="showSearch">
      <a-input v-model="searchKeyword" :placeholder="searchPlaceholder" allow-clear>
        <template #prefix>
          <span><Search /></span>
        </template>
      </a-input>
    </div>

    <div class="tree-wrap" v-show="!collapsed">
      <a-tree
        ref="treeRef"
        v-model:selected-keys="selectedKeys"
        v-model:checked-keys="checkedKeys"
        :data="filteredTreeData"
        :field-names="fieldNames"
        :action-on-node-click="expandOnClickNode ? 'expand' : undefined"
        :default-expand-all="defaultExpandAll"
        :default-expanded-keys="defaultExpandedKeys"
        :check-strictly="checkStrictly"
        :checkable="showCheckbox"
        @select="onNodeClick"
        @check="onCheck"
        @expand="onExpand"
      >
        <template #title="{ title }">
          <slot name="node" :title="title">
            <span class="tree-node">
              <span class="node-icon leaf-icon"><Document /></span>
              <span class="node-label" :title="title">{{ title }}</span>
            </span>
          </slot>
        </template>
      </a-tree>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, nextTick, onMounted, onBeforeUnmount, computed } from 'vue'
import { useTreePanelResize } from './useTreePanelResize'

const props = defineProps({
  // 树形数据
  treeData: {
    type: Array,
    default: () => []
  },
  // 标题
  title: {
    type: String,
    default: '树形结构'
  },
  // 标题图标
  titleIcon: {
    type: [String, Object],
    default: 'OfficeBuilding'
  },
  // 是否显示搜索框
  showSearch: {
    type: Boolean,
    default: true
  },
  // 搜索框占位符
  searchPlaceholder: {
    type: String,
    default: '请输入名称'
  },
  // 是否默认收起侧边栏
  defaultCollapsed: {
    type: Boolean,
    default: false
  },
  // 树配置项
  treeProps: {
    type: Object,
    default: () => ({
      children: "children",
      label: "label"
    })
  },
  // 节点唯一标识字段
  nodeKey: {
    type: String,
    default: 'id'
  },
  // 是否在点击节点时展开或收起
  expandOnClickNode: {
    type: Boolean,
    default: false
  },
  // 是否显示复选框
  showCheckbox: {
    type: Boolean,
    default: false
  },
  // 是否严格的遵循父子不互相关联
  checkStrictly: {
    type: Boolean,
    default: false
  },
  // 是否默认展开所有节点
  defaultExpandAll: {
    type: Boolean,
    default: false
  },
  // 默认展开的节点的key数组
  defaultExpandedKeys: {
    type: Array,
    default: () => []
  },
  // 默认宽度
  defaultWidth: {
    type: Number,
    default: 220
  },
  // 收起时的宽度
  collapsedWidth: {
    type: Number,
    default: 20
  },
  // 最小宽度
  minWidth: {
    type: Number,
    default: 180
  },
  // 最大宽度
  maxWidth: {
    type: Number,
    default: 400
  },
  // 本地存储的宽度key
  storageKey: {
    type: String,
    default: 'tree-sidebar-width'
  },
  // 是否启用本地存储宽度
  enableStorage: {
    type: Boolean,
    default: true
  },
  // 自定义过滤方法
  filterMethod: {
    type: Function,
    default: null
  }
})

const emit = defineEmits([
  'collapsed-change',
  'expanded-all-change',
  'refresh',
  'node-click',
  'check',
  'node-expand',
  'node-collapse',
  'search'
])

const treeRef = ref(null)
const searchKeyword = ref('')
const selectedKeys = ref([])
const checkedKeys = ref([])
const expandedAll = ref(props.defaultExpandAll)
const isExpandedAll = computed(() => expandedAll.value)
const fieldNames = computed(() => ({
  key: props.nodeKey,
  title: props.treeProps.label || 'label',
  children: props.treeProps.children || 'children'
}))
const filteredTreeData = computed(() => filterTree(props.treeData, searchKeyword.value))

// composables
const {
  collapsed, sidebarWidth, isResizing, isLoadingFromStorage,
  startResize, toggleCollapsed, resetWidth, getCurrentWidth, setWidth,
  cleanupResize, loadSavedWidth
} = useTreePanelResize(props)

// 事件处理
const handleRefresh = () => {
  emit('refresh')
}

const onNodeClick = (keys, event) => {
  emit('node-click', event.node, event)
}

const onCheck = (keys, event) => {
  emit('check', keys, event)
}

const onExpand = (keys, event) => {
  emit(event.expanded ? 'node-expand' : 'node-collapse', event.node, event)
}

const setCurrentKey = (key) => {
  selectedKeys.value = key === null || key === undefined ? [] : [key]
}

const getCurrentNode = () => {
  return treeRef.value?.getSelectedNodes()?.[0] || null
}

const getCurrentKey = () => {
  return selectedKeys.value[0]
}

const setCheckedKeys = (keys) => {
  checkedKeys.value = props.showCheckbox ? keys : []
}

const getCheckedKeys = () => {
  return props.showCheckbox ? checkedKeys.value : []
}

const getCheckedNodes = () => {
  if (treeRef.value && props.showCheckbox) {
    return treeRef.value.getCheckedNodes()
  }
  return []
}

const clearSearch = () => {
  searchKeyword.value = ''
}

const filter = (value) => {
  searchKeyword.value = value || ''
}

const toggleExpandAll = () => {
  expandedAll.value = !expandedAll.value
}

const expandAllNodes = () => {
  treeRef.value?.expandAll(true)
}

const collapseAllNodes = () => {
  treeRef.value?.expandAll(false)
}

function filterTree(nodes, keyword) {
  if (!keyword) {
    return nodes
  }
  return nodes.reduce((result, node) => {
    const childrenKey = props.treeProps.children || 'children'
    const labelKey = props.treeProps.label || 'label'
    const children = filterTree(node[childrenKey] || [], keyword)
    const matches = props.filterMethod
      ? props.filterMethod(keyword, node)
      : String(node[labelKey] || '').includes(keyword)
    if (matches || children.length) {
      result.push({ ...node, [childrenKey]: children })
    }
    return result
  }, [])
}

defineExpose({
  setCurrentKey,
  getCurrentNode,
  getCurrentKey,
  setCheckedKeys,
  getCheckedKeys,
  getCheckedNodes,
  clearSearch,
  filter,
  resetWidth,
  getCurrentWidth,
  setWidth,
  expandAllNodes,
  collapseAllNodes,
  toggleCollapsed,
  treeRef
})

// 监听折叠状态
watch(collapsed, (newVal, oldVal) => {
  if (newVal !== oldVal) {
    emit('collapsed-change', newVal)
  }
})

// 监听全部展开状态变化
watch(expandedAll, (newVal) => {
  nextTick(() => {
    if (newVal) {
      expandAllNodes()
    } else {
      collapseAllNodes()
    }
  })
  emit('expanded-all-change', newVal)
})

// 监听搜索关键词
watch(searchKeyword, (val) => {
  emit('search', val)
})

onMounted(() => {
  isLoadingFromStorage.value = true
  if (!collapsed.value && props.enableStorage) {
    loadSavedWidth()
  }
  nextTick(() => {
    isLoadingFromStorage.value = false
  })
  if (expandedAll.value) {
    nextTick(() => {
      expandAllNodes()
    })
  }
})

onBeforeUnmount(() => {
  cleanupResize()
})
</script>

<style lang="scss" scoped>
.tree-sidebar {
  flex-shrink: 0;
  width: 220px;
  background: var(--ui-bg-panel);
  border-right: 1px solid var(--ui-border);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  position: relative;
  transition: width 0.25s ease;

  &.collapsed {
    width: 42px;
  }

  &.resizing {
    transition: none;
    will-change: width;

    * {
      pointer-events: none !important;
    }
  }

  &.no-initial-transition {
    transition: none;
  }
}

.resize-handle {
  position: absolute;
  top: 0;
  right: 0;
  width: 6px;
  height: 100%;
  cursor: col-resize;
  z-index: 20;
  background: transparent;
  transition: background 0.2s;

  &:hover {
    background: var(--ui-primary-soft);
  }

  &.active {
    background: color-mix(in srgb, var(--ui-primary) 28%, transparent);
  }
}

.collapse-button-container {
  position: absolute;
  top: 50%;
  right: 0;
  transform: translateY(-50%);
  z-index: 100;
  display: flex;
  align-items: center;
  justify-content: center;
  width: 15px;
  height: 20px;
  background: var(--ui-bg-panel);
  border-radius: 0 4px 4px 0;
  box-shadow: var(--ui-shadow-panel, 0 1px 3px rgba(0, 0, 0, 0.1));
  transition: all 0.2s ease;

  .tree-sidebar.collapsed & {
    right: 0;
    background: var(--ui-bg-panel-soft);
    border-radius: 0 4px 4px 0;
  }

  .tree-sidebar.resizing & {
    pointer-events: none;
  }
}

.collapse-button {
  font-size: 20px;
  color: var(--ui-text-secondary);
  cursor: pointer;
  padding: 4px;
  border-radius: 4px;
  transition: all 0.2s;

  &:hover {
    color: var(--ui-primary);
    background: var(--ui-primary-soft);
  }
}

.tree-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 10px;
  height: 40px;
  border-bottom: 1px solid var(--ui-border);
  background: var(--ui-bg-panel-muted);
  flex-shrink: 0;

  .tree-title {
    font-size: 13px;
    font-weight: 600;
    color: var(--ui-text-primary);
    white-space: nowrap;
    overflow: hidden;
    display: flex;
    align-items: center;
    gap: 5px;

    .arco-icon {
      color: var(--ui-primary);
      font-size: 16px;
    }
  }

  .tree-actions {
    display: flex;
    align-items: center;
    gap: 4px;
    flex-shrink: 0;
  }
}

.tree-action-icon {
  font-size: 20px;
  color: var(--ui-text-secondary);
  cursor: pointer;
  padding: 4px;
  border-radius: 4px;
  transition: all 0.2s;

  &:hover {
    color: var(--ui-primary);
    background: var(--ui-primary-soft);
  }
}

.tree-search {
  padding: 12px 14px 8px;
  flex-shrink: 0;

  :deep(.arco-input-wrapper) {
    width: 100%;
  }

  :deep(.arco-input-wrapper) {
    min-height: 36px;
    border-radius: 7px;
    background: var(--ui-bg-panel-soft);
    box-shadow: 0 0 0 1px var(--ui-border-control) inset;
    transition: box-shadow 0.2s ease, background 0.2s ease;

    &:hover {
      box-shadow: 0 0 0 1px var(--ui-border-control-hover) inset;
    }

    &:focus-within {
      background: var(--ui-bg-panel);
      box-shadow: 0 0 0 1px var(--ui-border-focus) inset, var(--ui-focus-ring) !important;
    }
  }

  :deep(.arco-input-prefix) {
    color: var(--ui-text-muted);
    margin-right: 6px;
  }

  :deep(.arco-input) {
    height: 36px;
    background: transparent;
    color: var(--ui-text-primary);
  }
}

.tree-wrap {
  flex: 1;
  overflow-y: auto;
  padding: 6px 6px 12px;

  .tree-sidebar.resizing & {
    overflow: hidden;
  }

  &::-webkit-scrollbar {
    width: 4px;
  }

  &::-webkit-scrollbar-thumb {
    background: var(--ui-border);
    border-radius: 4px;

    &:hover {
      background: var(--ui-border-strong);
    }
  }

  :deep(.arco-tree-node-title) {
    height: 34px;
    border-radius: 4px;
    margin-bottom: 1px;
    padding-right: 8px;

    &:hover {
      background: var(--ui-bg-hover);
    }
  }

  :deep(.arco-tree-node-switcher) {
    flex: 0 0 18px;
    width: 18px;
    margin-right: 2px;
    color: var(--ui-text-muted);
  }

  :deep(.arco-tree-node-is-leaf .arco-tree-node-switcher) {
    color: transparent;
  }

  :deep(.arco-tree-node-selected .arco-tree-node-title) {
    background: var(--ui-primary-soft);
    color: var(--ui-primary-active);
    font-weight: 600;

    .node-icon {
      color: var(--ui-primary-active) !important;
    }
  }
}

.tree-node {
  display: flex;
  align-items: center;
  gap: 7px;
  min-width: 0;
  font-size: 14px;
  line-height: 1;
  overflow: hidden;

  .node-icon {
    width: 16px;
    height: 16px;
    font-size: 16px;
    flex-shrink: 0;
  }

  .folder-icon {
    color: var(--ui-warning);
  }

  .leaf-icon {
    color: var(--ui-warning);
    font-size: 15px;
  }

  .node-label {
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}
</style>
