<template>
  <div class="tree-sidebar" :class="{ collapsed: collapsed, resizing: isResizing, 'no-initial-transition': isLoadingFromStorage}" :style="{ width: sidebarWidth + 'px' }">
    <!-- 右侧拖动条 -->
    <div v-if="!collapsed" class="resize-handle" @mousedown="startResize" @touchstart="startResize" :class="{ active: isResizing }" />
    <div class="tree-header">
      <span class="tree-title" v-show="!collapsed">
        <el-icon><component :is="titleIcon" /></el-icon> {{ title }}
      </span>
      <div class="tree-actions" v-show="!collapsed">
        <el-tooltip :content="isExpandedAll ? '收起全部' : '展开全部'" placement="right">
          <el-icon class="tree-action-icon" @click="toggleExpandAll">
            <ArrowDown v-if="isExpandedAll" />
            <ArrowUp v-else />
          </el-icon>
        </el-tooltip>
        <el-tooltip content="刷新" placement="right">
          <el-icon class="tree-action-icon" @click="handleRefresh"><Refresh /></el-icon>
        </el-tooltip>
        <slot name="actions"></slot>
      </div>
    </div>
    
    <!-- 侧边栏展开/收起按钮 -->
    <div class="collapse-button-container">
      <el-tooltip :content="collapsed ? '展开' : '收起'" placement="right">
        <el-icon class="collapse-button" @click="toggleCollapsed">
          <DArrowRight v-if="collapsed" />
          <DArrowLeft v-else />
        </el-icon>
      </el-tooltip>
    </div>

    <div class="tree-search" v-show="!collapsed" v-if="showSearch">
      <el-input v-model="searchKeyword" :placeholder="searchPlaceholder" clearable>
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
    </div>

    <div class="tree-wrap" v-show="!collapsed">
      <el-tree 
        ref="treeRef" 
        :data="treeData" 
        :props="treeProps" 
        :expand-on-click-node="expandOnClickNode"
        :filter-node-method="filterNodeMethod"
        :default-expand-all="defaultExpandAll"
        :default-expanded-keys="defaultExpandedKeys"
        :node-key="nodeKey"
        :check-strictly="checkStrictly"
        :show-checkbox="showCheckbox"
        @node-click="onNodeClick"
        @check="onCheck"
        @node-expand="onNodeExpand"
        @node-collapse="onNodeCollapse"
      >
        <template #default="{ node, data }">
          <slot name="node" :node="node" :data="data">
            <span class="tree-node">
              <el-icon class="node-icon" :class="data.children && data.children.length ? 'folder-icon' : 'leaf-icon'">
                <Folder v-if="data.children && data.children.length" />
                <Document v-else />
              </el-icon>
              <span class="node-label" :title="node.label">{{ node.label }}</span>
            </span>
          </slot>
        </template>
      </el-tree>
    </div>
  </div>
</template>

<script setup>
import { ref, watch, nextTick, onMounted, onBeforeUnmount } from 'vue'
import { useTreePanelResize } from './useTreePanelResize'
import { useTreePanelSearch } from './useTreePanelSearch'
import { useTreePanelExpansion } from './useTreePanelExpansion'

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

// composables
const {
  collapsed, sidebarWidth, isResizing, isLoadingFromStorage,
  startResize, toggleCollapsed, resetWidth, getCurrentWidth, setWidth,
  cleanupResize, loadSavedWidth
} = useTreePanelResize(props)

const {
  searchKeyword, filterNodeMethod, clearSearch, filter
} = useTreePanelSearch(props, emit, treeRef)

const {
  expandedAll, isExpandedAll, toggleExpandAll, expandAllNodes, collapseAllNodes
} = useTreePanelExpansion(emit, treeRef)

// 事件处理
const handleRefresh = () => {
  emit('refresh')
}

const onNodeClick = (data, node, e) => {
  emit('node-click', data, node, e)
}

const onCheck = (data, checkedInfo) => {
  emit('check', data, checkedInfo)
}

const onNodeExpand = (data, node, e) => {
  emit('node-expand', data, node, e)
}

const onNodeCollapse = (data, node, e) => {
  emit('node-collapse', data, node, e)
}

const setCurrentKey = (key) => {
  if (treeRef.value) {
    treeRef.value.setCurrentKey(key)
  }
}

const getCurrentNode = () => {
  if (treeRef.value) {
    return treeRef.value.getCurrentNode()
  }
  return null
}

const getCurrentKey = () => {
  if (treeRef.value) {
    return treeRef.value.getCurrentKey()
  }
  return null
}

const setCheckedKeys = (keys) => {
  if (treeRef.value && props.showCheckbox) {
    treeRef.value.setCheckedKeys(keys)
  }
}

const getCheckedKeys = () => {
  if (treeRef.value && props.showCheckbox) {
    return treeRef.value.getCheckedKeys()
  }
  return []
}

const getCheckedNodes = () => {
  if (treeRef.value && props.showCheckbox) {
    return treeRef.value.getCheckedNodes()
  }
  return []
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
  if (treeRef.value) {
    treeRef.value.filter(val)
    emit('search', val)
  }
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
    background: var(--ui-primary-soft, rgba(14, 165, 233, 0.3));
  }
  
  &.active {
    background: var(--ui-primary-soft-active, rgba(14, 165, 233, 0.5));
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
    background: var(--ui-bg-panel-soft, #f7f8fa);
    border-radius: 0 4px 4px 0;
  }
  
  .tree-sidebar.resizing & {
    pointer-events: none;
  }
}

.collapse-button {
  font-size: 20px;
  color: var(--ui-text-secondary, #909399);
  cursor: pointer;
  padding: 4px;
  border-radius: 4px;
  transition: all 0.2s;
  
  &:hover {
    color: var(--ui-primary);
    background: var(--ui-primary-bg-subtle, #ecf5ff);
  }
}

.tree-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 10px;
  height: 40px;
  border-bottom: 1px solid #e8eaed;
  background: #f7f8fa;
  flex-shrink: 0;

  .tree-title {
    font-size: 13px;
    font-weight: 600;
    color: #303133;
    white-space: nowrap;
    overflow: hidden;
    display: flex;
    align-items: center;
    gap: 5px;

    .el-icon {
      color: #409eff;
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
  color: #909399;
  cursor: pointer;
  padding: 4px;
  border-radius: 4px;
  transition: all 0.2s;

  &:hover {
    color: #409eff;
    background: #ecf5ff;
  }
}

.tree-search {
  padding: 12px 14px 8px;
  flex-shrink: 0;

  :deep(.el-input) {
    width: 100%;
  }

  :deep(.el-input__wrapper) {
    min-height: 36px;
    border-radius: 7px;
    background: var(--ui-bg-panel-soft, #f6f9fc);
    box-shadow: 0 0 0 1px var(--ui-border, #dcdfe6) inset;
    transition: box-shadow 0.2s ease, background 0.2s ease;

    &:hover {
      box-shadow: 0 0 0 1px var(--ui-border-strong, #c7d2df) inset;
    }

    &.is-focus {
      background: var(--ui-bg-panel, #ffffff);
      box-shadow: var(--ui-focus-ring, 0 0 0 2px rgba(64, 158, 255, 0.18)) !important;
    }
  }

  :deep(.el-input__prefix) {
    color: var(--ui-text-muted, #a8abb2);
    margin-right: 6px;
  }

  :deep(.el-input__inner) {
    height: 36px;
    background: transparent;
    color: var(--ui-text-primary, #303133);
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
    background: #dcdfe6;
    border-radius: 4px;
    
    &:hover {
      background: #c0c4cc;
    }
  }

  :deep(.el-tree-node__content) {
    height: 34px;
    border-radius: 4px;
    margin-bottom: 1px;
    padding-right: 8px;

    &:hover {
      background: #f0f7ff;
    }
  }

  :deep(.el-tree-node__expand-icon) {
    flex: 0 0 18px;
    width: 18px;
    margin-right: 2px;
    color: var(--ui-text-muted, #a8abb2);
  }

  :deep(.el-tree-node__expand-icon.is-leaf) {
    color: transparent;
  }

  :deep(.el-tree-node.is-current > .el-tree-node__content) {
    background: #e6f0fd;
    color: #409eff;
    font-weight: 600;

    .node-icon {
      color: #409eff !important;
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
    color: #f59e0b;
  }

  .leaf-icon {
    color: #f59e0b;
    font-size: 15px;
  }

  .node-label {
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
  }
}
</style>
