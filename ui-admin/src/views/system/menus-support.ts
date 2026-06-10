import type { MenuVO } from '../../types/system.ts';

export interface MenuTreeNode extends MenuVO {
  children: MenuTreeNode[];
}

export interface MenuDetailGroup {
  0: string;
  1: string;
}

export interface MenuDetailView {
  title: string;
  tag: string;
  groups: MenuDetailGroup[];
}

export interface MenuFilterResult {
  tree: MenuTreeNode[];
  selectedId: number | null;
}

export function buildMenuTree(menus: MenuVO[]): MenuTreeNode[] {
  const nodeMap = new Map<number, MenuTreeNode>();
  const parentMap = new Map<number, number>();
  const roots: MenuTreeNode[] = [];

  for (const menu of menus) {
    nodeMap.set(menu.id, {
      ...menu,
      children: [],
    });
    parentMap.set(menu.id, normalizeParentId(menu.parentId));
  }

  for (const menu of menus) {
    const node = nodeMap.get(menu.id);
    if (!node) {
      continue;
    }
    const parentId = normalizeParentId(menu.parentId);
    if (parentId === node.id || isCyclicParentChain(node.id, parentMap)) {
      roots.push(node);
      continue;
    }
    const parent = parentId === 0 ? undefined : nodeMap.get(parentId);
    if (!parent) {
      roots.push(node);
      continue;
    }
    parent.children.push(node);
  }

  return sortTreeNodes(roots);
}

export function findMenuSelectionAfterFilter(
  menus: MenuVO[],
  keyword: string,
  selectedId?: number | null,
): MenuFilterResult {
  const tree = buildMenuTree(menus);
  const normalizedKeyword = keyword.trim();
  const filteredTree = normalizedKeyword ? tree.flatMap((node) => filterNode(node, normalizedKeyword)) : tree;
  const fallbackSelectedId = normalizedKeyword
    ? findFirstDirectMatchedNodeId(filteredTree, normalizedKeyword)
    : findFirstVisibleNodeId(filteredTree);
  const nextSelectedId =
    selectedId != null &&
    containsNode(filteredTree, selectedId) &&
    (!normalizedKeyword || shouldKeepSelectedId(filteredTree, selectedId, normalizedKeyword))
      ? selectedId
      : fallbackSelectedId;

  return {
    tree: filteredTree,
    selectedId: nextSelectedId,
  };
}

export function mapMenuDetail(menu?: MenuVO, parentName = '根菜单'): MenuDetailView | null {
  if (!menu) {
    return null;
  }

  return {
    title: menu.menuName,
    tag: menu.menuType,
    groups: [
      ['上级菜单', toText(parentName)],
      ['路由地址', toText(menu.routePath)],
      ['组件路径', toText(menu.component)],
      ['权限标识', toText(menu.perms)],
      ['显示状态', toVisibleText(menu.visible)],
      ['缓存策略', toKeepAliveText(menu.keepAlive)],
      ['状态', toStatusText(menu.status)],
    ],
  };
}

export function collectExpandedMenuKeys(tree: MenuTreeNode[]): number[] {
  const expandedKeys: number[] = [];

  for (const node of tree) {
    if (!node.children.length) {
      continue;
    }
    expandedKeys.push(node.id, ...collectExpandedMenuKeys(node.children));
  }

  return expandedKeys;
}

export function collectAncestorMenuKeys(tree: MenuTreeNode[], targetId: number): number[] {
  return findNodePath(tree, targetId)
    .slice(0, -1)
    .map((node) => node.id);
}

export function isRootMenuParentId(parentId: number | null | undefined): parentId is null | undefined | 0 {
  return parentId == null || parentId === 0;
}

function filterNode(node: MenuTreeNode, keyword: string): MenuTreeNode[] {
  const matchedChildren = node.children.flatMap((child) => filterNode(child, keyword));
  const matchedSelf = includesKeyword(node, keyword);

  if (!matchedSelf && matchedChildren.length === 0) {
    return [];
  }

  return [
    {
      ...node,
      children: matchedChildren,
    },
  ];
}

function includesKeyword(menu: MenuVO, keyword: string) {
  return [menu.menuName, menu.routePath, menu.component, menu.perms].some((value) =>
    value?.toLowerCase().includes(keyword.toLowerCase()),
  );
}

function sortTreeNodes(nodes: MenuTreeNode[]): MenuTreeNode[] {
  return nodes
    .map((node) => ({
      ...node,
      children: sortTreeNodes(node.children),
    }))
    .sort(compareMenuNode);
}

function compareMenuNode(left: MenuTreeNode, right: MenuTreeNode) {
  if (left.sort !== right.sort) {
    return left.sort - right.sort;
  }
  return left.id - right.id;
}

function containsNode(tree: MenuTreeNode[], selectedId: number): boolean {
  return tree.some((node) => node.id === selectedId || containsNode(node.children, selectedId));
}

function shouldKeepSelectedId(tree: MenuTreeNode[], selectedId: number, keyword: string) {
  const path = findNodePath(tree, selectedId);
  if (!path.length) {
    return false;
  }
  const selectedNode = path[path.length - 1];
  if (!includesKeyword(selectedNode, keyword)) {
    return false;
  }
  return !path.slice(0, -1).some((node) => includesKeyword(node, keyword));
}

function findFirstDirectMatchedNodeId(tree: MenuTreeNode[], keyword: string): number | null {
  for (const node of tree) {
    if (includesKeyword(node, keyword)) {
      return node.id;
    }
    const childMatchedId = findFirstDirectMatchedNodeId(node.children, keyword);
    if (childMatchedId != null) {
      return childMatchedId;
    }
  }
  return null;
}

function findFirstVisibleNodeId(tree: MenuTreeNode[]): number | null {
  const firstNode = tree[0];
  return firstNode?.id ?? null;
}

function findNodePath(tree: MenuTreeNode[], targetId: number): MenuTreeNode[] {
  for (const node of tree) {
    if (node.id === targetId) {
      return [node];
    }
    const childPath = findNodePath(node.children, targetId);
    if (childPath.length) {
      return [node, ...childPath];
    }
  }
  return [];
}

function normalizeParentId(parentId: number | null) {
  return isRootMenuParentId(parentId) ? 0 : parentId;
}

function isCyclicParentChain(menuId: number, parentMap: Map<number, number>) {
  const visited = new Set<number>([menuId]);
  let currentParentId = parentMap.get(menuId) ?? 0;

  while (currentParentId !== 0) {
    if (visited.has(currentParentId)) {
      return true;
    }
    visited.add(currentParentId);
    currentParentId = parentMap.get(currentParentId) ?? 0;
  }

  return false;
}

function toText(value: string | null | undefined) {
  return value?.trim() ? value : '--';
}

function toVisibleText(visible: number | null | undefined) {
  return visible === 1 ? '显示' : '隐藏';
}

function toKeepAliveText(keepAlive: number | null | undefined) {
  return keepAlive === 1 ? '缓存' : '不缓存';
}

function toStatusText(status: number | null | undefined) {
  return status === 1 ? '启用' : '停用';
}
