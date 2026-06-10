import test from 'node:test';
import assert from 'node:assert/strict';
import {
  buildMenuTree,
  findMenuSelectionAfterFilter,
  mapMenuDetail,
} from '../src/views/system/menus-support.ts';
import type { MenuVO } from '../src/types/system.ts';

const menus: MenuVO[] = [
  {
    id: 100,
    parentId: 0,
    menuType: 'DIR',
    menuName: '系统管理',
    routePath: '/system',
    routeName: 'system',
    component: null,
    icon: 'icon-settings',
    sort: 1,
    visible: 1,
    keepAlive: 0,
    perms: null,
    status: 1,
    createTime: null,
  },
  {
    id: 110,
    parentId: 100,
    menuType: 'MENU',
    menuName: '菜单管理',
    routePath: '/system/menus',
    routeName: 'menus',
    component: 'system/menus',
    icon: 'icon-menu',
    sort: 1,
    visible: 1,
    keepAlive: 1,
    perms: 'system:menu:list',
    status: 1,
    createTime: null,
  },
  {
    id: 111,
    parentId: 110,
    menuType: 'BUTTON',
    menuName: '新增菜单',
    routePath: null,
    routeName: null,
    component: null,
    icon: null,
    sort: 1,
    visible: 1,
    keepAlive: 0,
    perms: 'system:menu:create',
    status: 1,
    createTime: null,
  },
  {
    id: 120,
    parentId: 100,
    menuType: 'MENU',
    menuName: '角色管理',
    routePath: '/system/roles',
    routeName: 'roles',
    component: 'system/roles',
    icon: 'icon-user-group',
    sort: 2,
    visible: 1,
    keepAlive: 0,
    perms: 'system:role:list',
    status: 0,
    createTime: null,
  },
];

test('builds a menu tree that preserves original menu fields', () => {
  assert.deepEqual(buildMenuTree(menus), [
    {
      ...menus[0],
      children: [
        {
          ...menus[1],
          children: [
            {
              ...menus[2],
              children: [],
            },
          ],
        },
        {
          ...menus[3],
          children: [],
        },
      ],
    },
  ]);
});

test('degrades invalid parent relations to root nodes instead of dropping them', () => {
  assert.deepEqual(
    buildMenuTree([
      {
        ...menus[1],
        id: 210,
        parentId: 210,
        menuName: '自环菜单',
      },
      {
        ...menus[2],
        id: 220,
        parentId: 9999,
        menuName: '孤儿按钮',
      },
    ]),
    [
      {
        ...menus[1],
        id: 210,
        parentId: 210,
        menuName: '自环菜单',
        children: [],
      },
      {
        ...menus[2],
        id: 220,
        parentId: 9999,
        menuName: '孤儿按钮',
        children: [],
      },
    ],
  );
});

test('degrades a two-node cycle to root nodes instead of dropping either node', () => {
  assert.deepEqual(
    buildMenuTree([
      {
        ...menus[1],
        id: 310,
        parentId: 320,
        menuName: '成环菜单-A',
        sort: 2,
      },
      {
        ...menus[3],
        id: 320,
        parentId: 310,
        menuName: '成环菜单-B',
        sort: 1,
      },
    ]),
    [
      {
        ...menus[3],
        id: 320,
        parentId: 310,
        menuName: '成环菜单-B',
        sort: 1,
        children: [],
      },
      {
        ...menus[1],
        id: 310,
        parentId: 320,
        menuName: '成环菜单-A',
        sort: 2,
        children: [],
      },
    ],
  );
});

test('degrades a three-node cycle to root nodes instead of dropping any node', () => {
  assert.deepEqual(
    buildMenuTree([
      {
        ...menus[1],
        id: 410,
        parentId: 420,
        menuName: '成环菜单-A',
        sort: 3,
      },
      {
        ...menus[3],
        id: 420,
        parentId: 430,
        menuName: '成环菜单-B',
        sort: 1,
      },
      {
        ...menus[2],
        id: 430,
        parentId: 410,
        menuName: '成环菜单-C',
        sort: 2,
      },
    ]),
    [
      {
        ...menus[3],
        id: 420,
        parentId: 430,
        menuName: '成环菜单-B',
        sort: 1,
        children: [],
      },
      {
        ...menus[2],
        id: 430,
        parentId: 410,
        menuName: '成环菜单-C',
        sort: 2,
        children: [],
      },
      {
        ...menus[1],
        id: 410,
        parentId: 420,
        menuName: '成环菜单-A',
        sort: 3,
        children: [],
      },
    ],
  );
});

test('sorts tree nodes by sort then id to keep output stable', () => {
  assert.deepEqual(
    buildMenuTree([
      menus[3],
      menus[2],
      menus[0],
      {
        ...menus[1],
        id: 109,
        sort: 1,
        menuName: '菜单管理-同序更小 id',
      },
      menus[1],
    ]),
    [
      {
        ...menus[0],
        children: [
          {
            ...menus[1],
            id: 109,
            sort: 1,
            menuName: '菜单管理-同序更小 id',
            children: [],
          },
          {
            ...menus[1],
            children: [
              {
                ...menus[2],
                children: [],
              },
            ],
          },
          {
            ...menus[3],
            children: [],
          },
        ],
      },
    ],
  );
});

test('falls back to the first visible root node when keyword is empty', () => {
  assert.deepEqual(findMenuSelectionAfterFilter(menus, '', null), {
    tree: buildMenuTree(menus),
    selectedId: 100,
  });
});

test('keeps a deep selected id when keyword is empty and the node remains visible', () => {
  assert.deepEqual(findMenuSelectionAfterFilter(menus, '', 111), {
    tree: buildMenuTree(menus),
    selectedId: 111,
  });
});

test('filters by keyword, keeps ancestor chain, and selects the first matched leaf node', () => {
  assert.deepEqual(findMenuSelectionAfterFilter(menus, '新增', null), {
    tree: [
      {
        ...menus[0],
        children: [
          {
            ...menus[1],
            children: [
              {
                ...menus[2],
                children: [],
              },
            ],
          },
        ],
      },
    ],
    selectedId: 111,
  });
});

test('selects the directly matched normal menu node when it is the first visible match', () => {
  assert.deepEqual(findMenuSelectionAfterFilter(menus, '角色', null), {
    tree: [
      {
        ...menus[0],
        children: [
          {
            ...menus[3],
            children: [],
          },
        ],
      },
    ],
    selectedId: 120,
  });
});

test('keeps the current selected id when it remains visible after filtering', () => {
  assert.deepEqual(findMenuSelectionAfterFilter(menus, '菜单', 110), {
    tree: [
      {
        ...menus[0],
        children: [
          {
            ...menus[1],
            children: [
              {
                ...menus[2],
                children: [],
              },
            ],
          },
        ],
      },
    ],
    selectedId: 110,
  });
});

test('does not keep the old selected id when it is only retained by the ancestor chain', () => {
  assert.deepEqual(findMenuSelectionAfterFilter(menus, '菜单', 111), {
    tree: [
      {
        ...menus[0],
        children: [
          {
            ...menus[1],
            children: [
              {
                ...menus[2],
                children: [],
              },
            ],
          },
        ],
      },
    ],
    selectedId: 110,
  });
});

test('prefers the directly matched ancestor when ancestor and child both match the keyword', () => {
  assert.deepEqual(findMenuSelectionAfterFilter(menus, '菜单', null), {
    tree: [
      {
        ...menus[0],
        children: [
          {
            ...menus[1],
            children: [
              {
                ...menus[2],
                children: [],
              },
            ],
          },
        ],
      },
    ],
    selectedId: 110,
  });
});

test('falls back to the first matched menu node instead of the root when current selection disappears', () => {
  assert.deepEqual(findMenuSelectionAfterFilter(menus, '角色', 111), {
    tree: [
      {
        ...menus[0],
        children: [
          {
            ...menus[3],
            children: [],
          },
        ],
      },
    ],
    selectedId: 120,
  });
});

test('returns null selected id when the filter result is empty', () => {
  assert.deepEqual(findMenuSelectionAfterFilter(menus, '不存在', 110), {
    tree: [],
    selectedId: null,
  });
});

test('maps menu detail for page consumption with raw tag and flat groups', () => {
  assert.deepEqual(mapMenuDetail(menus[1], '系统管理'), {
    title: '菜单管理',
    tag: 'MENU',
    groups: [
      ['上级菜单', '系统管理'],
      ['路由地址', '/system/menus'],
      ['组件路径', 'system/menus'],
      ['权限标识', 'system:menu:list'],
      ['显示状态', '显示'],
      ['缓存策略', '缓存'],
      ['状态', '启用'],
    ],
  });
});

test('uses placeholder text when optional detail fields are missing', () => {
  assert.deepEqual(mapMenuDetail(menus[2], '菜单管理'), {
    title: '新增菜单',
    tag: 'BUTTON',
    groups: [
      ['上级菜单', '菜单管理'],
      ['路由地址', '--'],
      ['组件路径', '--'],
      ['权限标识', 'system:menu:create'],
      ['显示状态', '显示'],
      ['缓存策略', '不缓存'],
      ['状态', '启用'],
    ],
  });
});

test('returns null when menu detail is requested without an active menu', () => {
  assert.equal(mapMenuDetail(), null);
});

test('uses root menu as the default parent name', () => {
  assert.deepEqual(mapMenuDetail(menus[0]), {
    title: '系统管理',
    tag: 'DIR',
    groups: [
      ['上级菜单', '根菜单'],
      ['路由地址', '/system'],
      ['组件路径', '--'],
      ['权限标识', '--'],
      ['显示状态', '显示'],
      ['缓存策略', '不缓存'],
      ['状态', '启用'],
    ],
  });
});
