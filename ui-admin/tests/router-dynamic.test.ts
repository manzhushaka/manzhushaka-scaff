import test from 'node:test';
import assert from 'node:assert/strict';
import { collectAncestorMenuPaths } from '../src/layout/sidebar-menu-state.ts';
import type { MenuItem } from '../src/types/auth.ts';

const menus: MenuItem[] = [
  {
    id: 1,
    name: 'Dashboard',
    type: 'MENU',
    path: '/dashboard',
    component: 'dashboard/index',
    title: '工作台',
    icon: 'icon-dashboard',
  },
  {
    id: 10,
    name: 'System',
    type: 'DIR',
    path: '/system',
    title: '系统管理',
    icon: 'icon-settings',
    children: [
      {
        id: 11,
        name: 'Users',
        type: 'MENU',
        path: '/system/users',
        component: 'system/users',
        title: '用户管理',
      },
      {
        id: 12,
        name: 'Access',
        type: 'DIR',
        path: '/system/access',
        title: '访问控制',
        children: [
          {
            id: 13,
            name: 'Menus',
            type: 'MENU',
            path: '/system/access/menus',
            component: 'system/menus',
            title: '菜单管理',
          },
          {
            id: 14,
            name: 'MenuCreate',
            type: 'BUTTON',
            path: '',
            title: '菜单新增',
            permission: 'system:menu:add',
          },
        ],
      },
      {
        id: 15,
        name: 'HiddenFeature',
        type: 'MENU',
        path: '/system/hidden',
        component: 'system/hidden',
        title: '隐藏菜单',
        hidden: true,
      },
    ],
  },
];

test('collects only the ancestor submenu paths for the current route', () => {
  assert.deepEqual(collectAncestorMenuPaths(menus, '/dashboard'), []);
  assert.deepEqual(collectAncestorMenuPaths(menus, '/system/users'), ['/system']);
  assert.deepEqual(collectAncestorMenuPaths(menus, '/system/access/menus'), ['/system', '/system/access']);
});

test('returns an empty list when the current route is not rendered in the sidebar', () => {
  assert.deepEqual(collectAncestorMenuPaths(menus, '/system/hidden'), []);
  assert.deepEqual(collectAncestorMenuPaths(menus, '/missing'), []);
});
