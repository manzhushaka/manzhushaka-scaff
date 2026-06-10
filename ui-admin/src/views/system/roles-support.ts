import type { TreeNodeData } from '@arco-design/web-vue';
import type { MenuVO } from '@/types/system';

interface RoleMenuTreeNode {
  id: number;
  children: RoleMenuTreeNode[];
}

type RoleMenuCheckState = 'none' | 'partial' | 'full';

export function buildRoleMenuTreeData(menus: MenuVO[]): TreeNodeData[] {
  const nodeMap = new Map<number, TreeNodeData>();
  const roots: TreeNodeData[] = [];

  for (const menu of menus) {
    nodeMap.set(menu.id, {
      key: menu.id,
      title: menu.menuName,
      children: [],
    });
  }

  for (const menu of menus) {
    const node = nodeMap.get(menu.id);
    if (!node) {
      continue;
    }
    const parentId = normalizeParentId(menu.parentId);
    const parent = parentId === 0 ? undefined : nodeMap.get(parentId);
    if (!parent) {
      roots.push(node);
      continue;
    }
    parent.children = [...(parent.children ?? []), node];
  }

  return roots;
}

export function buildRoleMenuCheckedKeys(menus: MenuVO[], savedMenuIds: number[]): number[] {
  const selectedMenuIds = new Set(savedMenuIds);
  return buildRoleMenuModel(menus).flatMap((node) => collectCheckedKeys(node, selectedMenuIds).checkedKeys);
}

export function normalizeRoleMenuIds(
  checkedKeys: Array<string | number>,
  halfCheckedKeys: Array<string | number>,
): number[] {
  return Array.from(
    new Set(
      [...checkedKeys, ...halfCheckedKeys]
        .map((key) => Number(key))
        .filter((key) => Number.isInteger(key) && key > 0),
    ),
  );
}

function buildRoleMenuModel(menus: MenuVO[]): RoleMenuTreeNode[] {
  const nodeMap = new Map<number, RoleMenuTreeNode>();
  const roots: RoleMenuTreeNode[] = [];

  for (const menu of menus) {
    nodeMap.set(menu.id, {
      id: menu.id,
      children: [],
    });
  }

  for (const menu of menus) {
    const node = nodeMap.get(menu.id);
    if (!node) {
      continue;
    }
    const parentId = normalizeParentId(menu.parentId);
    const parent = parentId === 0 ? undefined : nodeMap.get(parentId);
    if (!parent) {
      roots.push(node);
      continue;
    }
    parent.children.push(node);
  }

  return roots;
}

function collectCheckedKeys(
  node: RoleMenuTreeNode,
  selectedMenuIds: Set<number>,
): { state: RoleMenuCheckState; checkedKeys: number[] } {
  if (!node.children.length) {
    return selectedMenuIds.has(node.id)
      ? { state: 'full', checkedKeys: [node.id] }
      : { state: 'none', checkedKeys: [] };
  }

  const childResults = node.children.map((child) => collectCheckedKeys(child, selectedMenuIds));
  const allChildrenFull = childResults.length > 0 && childResults.every((result) => result.state === 'full');
  const anyChildSelected = childResults.some((result) => result.state !== 'none');

  if (allChildrenFull && selectedMenuIds.has(node.id)) {
    return {
      state: 'full',
      checkedKeys: [node.id],
    };
  }

  if (anyChildSelected) {
    return {
      state: 'partial',
      checkedKeys: childResults.flatMap((result) => result.checkedKeys),
    };
  }

  return selectedMenuIds.has(node.id)
    ? { state: 'full', checkedKeys: [node.id] }
    : { state: 'none', checkedKeys: [] };
}

function normalizeParentId(parentId: number | null) {
  return parentId == null ? 0 : parentId;
}
