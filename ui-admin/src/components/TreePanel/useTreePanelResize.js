import { ref, watch } from 'vue'

/**
 * TreePanel 宽度/拖拽/折叠状态管理 composable
 *
 * 职责：
 * - 宽度管理（sidebarWidth）
 * - 拖拽调整宽度（startResize / 鼠标/触摸事件）
 * - 折叠/展开（collapsed、toggleCollapsed）
 * - 宽度持久化（localStorage 读写）
 * - 清理（cleanupResize）
 */
export function useTreePanelResize(props) {
  // ──── 响应式状态 ────
  const collapsed = ref(props.defaultCollapsed)
  const sidebarWidth = ref(props.defaultCollapsed ? props.collapsedWidth : props.defaultWidth)
  const isResizing = ref(false)
  const isLoadingFromStorage = ref(false)
  const startX = ref(0)
  const startWidth = ref(0)
  const saveWidthTimer = ref(null)
  const rafId = ref(null)

  // ──── 内部函数 ────

  /** 获取本地存储中保存的宽度 */
  const getSavedWidth = () => {
    if (!props.enableStorage) {
      return null
    }
    try {
      const savedWidth = localStorage.getItem(props.storageKey)
      if (savedWidth) {
        const width = parseInt(savedWidth, 10)
        if (!isNaN(width) && width >= props.minWidth && width <= props.maxWidth) {
          return width
        }
      }
    } catch (error) {
      console.warn(`Failed to load sidebar width from storage with key ${props.storageKey}:`, error)
    }
    return null
  }

  /** 保存宽度到本地存储 */
  const saveWidthToStorage = () => {
    if (collapsed.value || !props.enableStorage) return
    try {
      localStorage.setItem(props.storageKey, sidebarWidth.value.toString())
    } catch (error) {
      console.warn(`Failed to save sidebar width to storage with key ${props.storageKey}:`, error)
    }
  }

  /** 处理收起/展开状态变化 */
  const handleCollapseChange = (isCollapsed) => {
    if (isCollapsed) {
      saveWidthToStorage()
      sidebarWidth.value = props.collapsedWidth
    } else {
      const savedWidth = getSavedWidth()
      sidebarWidth.value = savedWidth !== null ? savedWidth : props.defaultWidth
    }
  }

  /** 拖拽 - 开始 */
  const startResize = (e) => {
    e.preventDefault()
    e.stopPropagation()
    isResizing.value = true
    startX.value = e.type === 'mousedown' ? e.clientX : e.touches[0].clientX
    startWidth.value = sidebarWidth.value

    if (e.type === 'mousedown') {
      document.addEventListener('mousemove', handleResizeMove)
      document.addEventListener('mouseup', stopResize)
    } else {
      document.addEventListener('touchmove', handleResizeMove, { passive: false })
      document.addEventListener('touchend', stopResize)
    }
    disableUserSelect()
  }

  /** 拖拽 - 移动 */
  const handleResizeMove = (e) => {
    if (!isResizing.value) return
    if (rafId.value) {
      cancelAnimationFrame(rafId.value)
    }
    rafId.value = requestAnimationFrame(() => {
      e.preventDefault()
      e.stopPropagation()
      const clientX = e.type === 'mousemove' ? e.clientX : e.touches[0].clientX
      const deltaX = clientX - startX.value
      const newWidth = startWidth.value + deltaX
      const clampedWidth = Math.max(props.minWidth, Math.min(props.maxWidth, newWidth))
      if (Math.abs(clampedWidth - sidebarWidth.value) >= 1) {
        sidebarWidth.value = clampedWidth
      }
    })
  }

  /** 拖拽 - 结束 */
  const stopResize = () => {
    if (!isResizing.value) return
    isResizing.value = false
    if (rafId.value) {
      cancelAnimationFrame(rafId.value)
      rafId.value = null
    }
    startX.value = 0
    startWidth.value = 0
    document.removeEventListener('mousemove', handleResizeMove)
    document.removeEventListener('mouseup', stopResize)
    document.removeEventListener('touchmove', handleResizeMove)
    document.removeEventListener('touchend', stopResize)
    enableUserSelect()
    saveWidthToStorage()
  }

  /** 禁止用户选择（拖拽时） */
  const disableUserSelect = () => {
    document.body.style.userSelect = 'none'
    document.body.style.webkitUserSelect = 'none'
    document.body.style.mozUserSelect = 'none'
    document.body.style.msUserSelect = 'none'
  }

  /** 恢复用户选择（拖拽结束） */
  const enableUserSelect = () => {
    document.body.style.userSelect = ''
    document.body.style.webkitUserSelect = ''
    document.body.style.mozUserSelect = ''
    document.body.style.msUserSelect = ''
  }

  /** 折叠/展开切换 */
  const toggleCollapsed = () => {
    collapsed.value = !collapsed.value
  }

  /** 重置宽度为默认值 */
  const resetWidth = () => {
    sidebarWidth.value = props.defaultWidth
    saveWidthToStorage()
  }

  /** 获取当前宽度 */
  const getCurrentWidth = () => {
    return sidebarWidth.value
  }

  /** 设置宽度（带范围校验） */
  const setWidth = (width) => {
    if (typeof width === 'number' && width >= props.minWidth && width <= props.maxWidth) {
      sidebarWidth.value = width
      if (!collapsed.value) {
        saveWidthToStorage()
      }
    }
  }

  /** 清理定时器和动画帧 */
  const cleanupResize = () => {
    if (rafId.value) {
      cancelAnimationFrame(rafId.value)
      rafId.value = null
    }
    if (saveWidthTimer.value) {
      clearTimeout(saveWidthTimer.value)
      saveWidthTimer.value = null
    }
  }

  /** 从本地存储加载宽度 */
  const loadSavedWidth = () => {
    const savedWidth = getSavedWidth()
    if (savedWidth !== null) {
      sidebarWidth.value = savedWidth
    }
  }

  return {
    // 状态
    collapsed,
    sidebarWidth,
    isResizing,
    isLoadingFromStorage,
    // 拖拽
    startResize,
    // 折叠
    toggleCollapsed,
    // 宽度管理
    resetWidth,
    getCurrentWidth,
    setWidth,
    // 存储
    loadSavedWidth,
    // 清理
    cleanupResize
  }
}