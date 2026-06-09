import test from 'node:test';
import assert from 'node:assert/strict';
import { createTabsManager } from '../src/store/tabs.ts';

test('adds a new tab and avoids duplicates for the same path', () => {
  const manager = createTabsManager();

  manager.visit({ path: '/dashboard', title: '工作台', closable: false });
  manager.visit({ path: '/dashboard', title: '工作台', closable: false });
  manager.visit({ path: '/system/users', title: '用户管理', closable: true });

  assert.deepEqual(manager.items, [
    { path: '/dashboard', title: '工作台', closable: false },
    { path: '/system/users', title: '用户管理', closable: true },
  ]);
  assert.equal(manager.activePath, '/system/users');
});

test('falls back to the previous tab when closing the active tab', () => {
  const manager = createTabsManager();

  manager.visit({ path: '/dashboard', title: '工作台', closable: false });
  manager.visit({ path: '/system/users', title: '用户管理', closable: true });
  manager.visit({ path: '/system/roles', title: '角色管理', closable: true });

  const nextPath = manager.close('/system/roles');

  assert.equal(nextPath, '/system/users');
  assert.deepEqual(manager.items, [
    { path: '/dashboard', title: '工作台', closable: false },
    { path: '/system/users', title: '用户管理', closable: true },
  ]);
  assert.equal(manager.activePath, '/system/users');
});

test('keeps the home tab even when asked to close it', () => {
  const manager = createTabsManager();

  manager.visit({ path: '/dashboard', title: '工作台', closable: false });
  const nextPath = manager.close('/dashboard');

  assert.equal(nextPath, '/dashboard');
  assert.deepEqual(manager.items, [{ path: '/dashboard', title: '工作台', closable: false }]);
  assert.equal(manager.activePath, '/dashboard');
});

test('keeps the current tab active when closing another tab', () => {
  const manager = createTabsManager(
    [
      { path: '/dashboard', title: '工作台', closable: false },
      { path: '/system/users', title: '用户管理', closable: true },
      { path: '/system/roles', title: '角色管理', closable: true },
    ],
    '/system/roles',
  );

  const nextPath = manager.close('/system/users');

  assert.equal(nextPath, '/system/roles');
  assert.deepEqual(manager.items, [
    { path: '/dashboard', title: '工作台', closable: false },
    { path: '/system/roles', title: '角色管理', closable: true },
  ]);
  assert.equal(manager.activePath, '/system/roles');
});
