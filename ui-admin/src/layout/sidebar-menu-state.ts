import type { MenuItem } from '../types/auth.ts';

function collectVisibleChildren(menu: MenuItem) {
  return (menu.children ?? []).filter((item) => item.type !== 'BUTTON' && !item.hidden);
}

export function collectExpandableMenuPaths(menus: MenuItem[]) {
  const paths: string[] = [];

  const walk = (items: MenuItem[]) => {
    for (const item of items) {
      if (item.type === 'BUTTON' || item.hidden) {
        continue;
      }
      const visibleChildren = collectVisibleChildren(item);
      if (!visibleChildren.length) {
        continue;
      }
      if (item.path) {
        paths.push(item.path);
      }
      walk(visibleChildren);
    }
  };

  walk(menus);
  return paths;
}

export function collectAncestorMenuPaths(menus: MenuItem[], targetPath: string) {
  const walk = (items: MenuItem[], ancestors: string[]): string[] | null => {
    for (const item of items) {
      if (item.type === 'BUTTON' || item.hidden) {
        continue;
      }
      if (item.path === targetPath) {
        return ancestors;
      }
      const visibleChildren = collectVisibleChildren(item);
      if (!visibleChildren.length) {
        continue;
      }
      const nextAncestors = item.path ? [...ancestors, item.path] : ancestors;
      const matchedAncestors = walk(visibleChildren, nextAncestors);
      if (matchedAncestors) {
        return matchedAncestors;
      }
    }
    return null;
  };

  return walk(menus, []) ?? [];
}
