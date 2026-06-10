import assert from 'node:assert/strict';

globalThis.window = globalThis;

const { listEntities, mockMenus } = await import('../src/api/mock.ts');

function flattenMenus(menus) {
  return menus.flatMap((menu) => [menu, ...(menu.children ? flattenMenus(menu.children) : [])]);
}

const authMenus = flattenMenus(await mockMenus());
const managementMenus = await listEntities('menus');

assert.equal(
  managementMenus.length,
  authMenus.length,
  '菜单管理 mock 数据应与左侧菜单使用同一套菜单记录。',
);

const authMenuIds = authMenus.map((item) => item.id).sort((left, right) => left - right);
const managementMenuIds = managementMenus.map((item) => item.id).sort((left, right) => left - right);

assert.deepEqual(
  managementMenuIds,
  authMenuIds,
  '菜单管理 mock 数据缺少部分左侧菜单项。',
);

assert.ok(
  managementMenus.some((item) => item.name === '访问控制'),
  '菜单管理 mock 数据需要包含访问控制目录。',
);

console.log('mock menu consistency checks passed');
