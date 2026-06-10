import test from 'node:test';
import assert from 'node:assert/strict';
import { buildRoleMenuCheckedKeys, buildRoleMenuTreeData } from '../src/views/system/roles-support.ts';

test('maps flat menus to a tree for the role form', () => {
  assert.deepEqual(
    buildRoleMenuTreeData([
      {
        id: 10,
        parentId: 0,
        menuType: 'DIR',
        menuName: '系统管理',
        routePath: '/system',
        routeName: 'system',
        component: null,
        icon: null,
        sort: 1,
        visible: 1,
        keepAlive: 0,
        perms: null,
        status: 1,
        createTime: null,
      },
      {
        id: 11,
        parentId: 10,
        menuType: 'MENU',
        menuName: '角色管理',
        routePath: '/system/roles',
        routeName: 'roles',
        component: 'system/roles',
        icon: null,
        sort: 2,
        visible: 1,
        keepAlive: 0,
        perms: 'system:role:list',
        status: 1,
        createTime: null,
      },
    ]),
    [
      {
        key: 10,
        title: '系统管理',
        children: [
          {
            key: 11,
            title: '角色管理',
            children: [],
          },
        ],
      },
    ],
  );
});

test('compresses saved menu ids into checked keys that preserve partial parents', () => {
  const menus = [
    {
      id: 10,
      parentId: 0,
      menuType: 'DIR',
      menuName: '系统管理',
      routePath: '/system',
      routeName: 'system',
      component: null,
      icon: null,
      sort: 1,
      visible: 1,
      keepAlive: 0,
      perms: null,
      status: 1,
      createTime: null,
    },
    {
      id: 11,
      parentId: 10,
      menuType: 'MENU',
      menuName: '角色管理',
      routePath: '/system/roles',
      routeName: 'roles',
      component: 'system/roles',
      icon: null,
      sort: 2,
      visible: 1,
      keepAlive: 0,
      perms: 'system:role:list',
      status: 1,
      createTime: null,
    },
    {
      id: 12,
      parentId: 10,
      menuType: 'MENU',
      menuName: '用户管理',
      routePath: '/system/users',
      routeName: 'users',
      component: 'system/users',
      icon: null,
      sort: 3,
      visible: 1,
      keepAlive: 0,
      perms: 'system:user:list',
      status: 1,
      createTime: null,
    },
  ];

  assert.deepEqual(buildRoleMenuCheckedKeys(menus, [10, 11]), [11]);
  assert.deepEqual(buildRoleMenuCheckedKeys(menus, [10, 11, 12]), [10]);
});
