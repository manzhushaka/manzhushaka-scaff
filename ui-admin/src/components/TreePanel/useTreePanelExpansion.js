import { ref, computed } from 'vue'

/**
 * TreePanel 展开/折叠节点管理 composable
 *
 * 职责：
 * - 展开全部/收起全部状态（isExpandedAll）
 * - 切换展开全部/收起全部（toggleExpandAll）
 * - 展开所有节点（expandAllNodes）
 * - 收起所有节点（collapseAllNodes）
 * - 节点遍历工具（getAllNodes）
 */
export function useTreePanelExpansion(emit, treeRef) {
  const expandedAll = ref(false)

  /** 当前是否全部展开 */
  const isExpandedAll = computed({
    get: () => expandedAll.value,
    set: (val) => {
      expandedAll.value = val
    }
  })

  /** 获取所有节点 */
  const getAllNodes = (rootNode) => {
    const nodes = []
    const traverse = (node) => {
      if (!node) return
      nodes.push(node)
      if (node.childNodes && node.childNodes.length) {
        node.childNodes.forEach(child => traverse(child))
      }
    }
    traverse(rootNode)
    return nodes
  }

  /** 展开所有节点 */
  const expandAllNodes = () => {
    if (!treeRef.value) return
    const allNodes = getAllNodes(treeRef.value.root)
    allNodes.forEach(node => {
      if (node.expanded !== undefined && !node.expanded) {
        node.expanded = true
      }
    })
  }

  /** 收起所有节点 */
  const collapseAllNodes = () => {
    if (!treeRef.value) return
    const allNodes = getAllNodes(treeRef.value.root)
    allNodes.forEach(node => {
      if (node.expanded !== undefined && node.expanded) {
        node.expanded = false
      }
    })
  }

  /** 切换展开/折叠所有节点 */
  const toggleExpandAll = () => {
    expandedAll.value = !expandedAll.value
  }

  return {
    expandedAll,
    isExpandedAll,
    toggleExpandAll,
    expandAllNodes,
    collapseAllNodes
  }
}