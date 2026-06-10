import assert from 'node:assert/strict';
import { readFile } from 'node:fs/promises';
import { resolve } from 'node:path';
import { IconApps, IconDashboard, IconUser } from '@arco-design/web-vue/es/icon/index.js';
import { resolveMenuIcon } from '../src/layout/menu-icons.ts';

assert.equal(resolveMenuIcon('icon-dashboard'), IconDashboard);
assert.equal(resolveMenuIcon(' icon-user '), IconUser);
assert.equal(resolveMenuIcon('icon-missing'), IconApps);

const mainLayoutPath = resolve(import.meta.dirname, '../src/layout/MainLayout.vue');
const mainLayoutSource = await readFile(mainLayoutPath, 'utf8');

assert.match(
  mainLayoutSource,
  /\.arco-menu-inner\s*>\s*\.arco-menu-item\.arco-menu-selected/,
  'MainLayout.vue needs a dedicated selected style for top-level menu items.',
);

console.log('sidebar menu checks passed');
