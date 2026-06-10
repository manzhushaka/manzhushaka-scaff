import test from 'node:test';
import assert from 'node:assert/strict';
import {
  buildDashboardMetrics,
  dashboardQuickEntries,
  filterQuickEntriesByPermission,
  formatDashboardRoleCodes,
} from '../src/views/dashboard/dashboard-support.ts';
import type { MenuItem } from '../src/types/auth.ts';

test('buildDashboardMetrics returns four overview cards in stable order', () => {
  const metrics = buildDashboardMetrics({
    menuCount: 4,
    permissionCount: 47,
    roleCount: 1,
    quickEntryCount: 3,
  });

  assert.deepEqual(
    metrics.map((item) => item.title),
    ['菜单总数', '权限点', '角色数', '常用入口'],
  );
  assert.equal(metrics[0]?.value, 4);
  assert.equal(metrics[1]?.value, 47);
  assert.equal(metrics[2]?.value, 1);
  assert.equal(metrics[3]?.value, 3);
});

test('filterQuickEntriesByPermission hides entries without matching permission', () => {
  const visible = filterQuickEntriesByPermission(dashboardQuickEntries, [
    'system:user:list',
    'system:menu:list',
  ], []);

  assert.deepEqual(
    visible.map((item) => item.title),
    ['用户管理', '菜单管理'],
  );
});

test('filterQuickEntriesByPermission keeps entries that are visible in the menu tree', () => {
  const menus: MenuItem[] = [
    {
      id: 10,
      name: 'System',
      type: 'DIR',
      path: '/system',
      title: '系统管理',
      children: [
        {
          id: 11,
          name: 'Users',
          type: 'MENU',
          path: '/system/users',
          title: '用户管理',
          permission: 'system:user:list',
        },
        {
          id: 12,
          name: 'Roles',
          type: 'MENU',
          path: '/system/roles',
          title: '角色管理',
          permission: 'system:role:list',
        },
      ],
    },
  ];

  const visible = filterQuickEntriesByPermission(dashboardQuickEntries, [], menus);

  assert.deepEqual(
    visible.map((item) => item.title),
    ['用户管理', '角色管理'],
  );
});

test('formatDashboardRoleCodes joins role codes and falls back to placeholder', () => {
  assert.equal(formatDashboardRoleCodes(['SUPER_ADMIN', 'OPS_ADMIN']), 'SUPER_ADMIN, OPS_ADMIN');
  assert.equal(formatDashboardRoleCodes([]), '--');
  assert.equal(formatDashboardRoleCodes(undefined), '--');
});
