import { ref } from 'vue'

/**
 * TreePanel 搜索/过滤逻辑 composable
 *
 * 职责：
 * - 搜索关键词状态（searchKeyword）
 * - 树节点过滤方法（filterNodeMethod）
 * - 清除搜索（clearSearch）
 * - 设置过滤值并触发搜索（filter）
 */
export function useTreePanelSearch(props, emit, treeRef) {
  const searchKeyword = ref('')

  /** 节点过滤方法 */
  const filterNodeMethod = (value, data) => {
    if (props.filterMethod) {
      return props.filterMethod(value, data)
    }
    if (!value) return true
    return data.label && data.label.indexOf(value) !== -1
  }

  /** 清除搜索 */
  const clearSearch = () => {
    searchKeyword.value = ''
    if (treeRef.value) {
      treeRef.value.filter('')
    }
  }

  /** 设置过滤值 */
  const filter = (value) => {
    searchKeyword.value = value
  }

  return {
    searchKeyword,
    filterNodeMethod,
    clearSearch,
    filter
  }
}