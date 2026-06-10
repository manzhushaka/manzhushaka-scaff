# 菜单管理双栏结构实现计划

> **面向 AI 代理的工作者：** 必需子技能：使用 superpowers:subagent-driven-development（推荐）或 superpowers:executing-plans 逐任务实现此计划。步骤使用复选框（`- [ ]`）语法来跟踪进度。

**目标：** 将系统管理中的菜单管理页面改造成“左侧菜单结构树 + 右侧菜单详情”的双栏工作台，同时保留现有菜单新增、编辑、删除能力。

**架构：** 前端保持现有接口与弹窗表单不变，把层级树构建、搜索过滤、默认选中和详情映射抽到 `menus-support.ts`，并通过单元测试锁定行为；页面 `menus.vue` 只负责双栏布局、树节点操作和详情联动，避免把结构规则揉进组件里。

**技术栈：** Vue 3、TypeScript、Arco Design Vue、Node test runner、Vite

---

## 文件结构与职责

- 创建：`ui-admin/src/views/system/menus-support.ts`
  负责菜单树构建、关键字过滤、默认选中、详情映射等纯函数。
- 创建：`ui-admin/tests/menus-support.test.ts`
  负责菜单树与详情联动规则的失败测试与回归测试。
- 修改：`ui-admin/src/views/system/menus.vue`
  负责双栏界面、节点选中、右侧详情展示和现有增删改交互整合。

---

### 任务 1：抽离菜单树支持逻辑并补足单元测试

**文件：**
- 创建：`ui-admin/src/views/system/menus-support.ts`
- 创建：`ui-admin/tests/menus-support.test.ts`

- [ ] **步骤 1：编写失败的菜单树支持测试**

```ts
import test from 'node:test';
import assert from 'node:assert/strict';
import {
  buildMenuTree,
  findMenuSelectionAfterFilter,
  mapMenuDetail,
} from '../src/views/system/menus-support.ts';

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
    menuName: '菜单管理',
    routePath: '/system/menus',
    routeName: 'systemMenus',
    component: 'system/menus',
    icon: null,
    sort: 1,
    visible: 1,
    keepAlive: 1,
    perms: 'system:menu:list',
    status: 1,
    createTime: null,
  },
  {
    id: 12,
    parentId: 11,
    menuType: 'BUTTON',
    menuName: '新增菜单',
    routePath: '',
    routeName: '',
    component: '',
    icon: null,
    sort: 1,
    visible: 1,
    keepAlive: 0,
    perms: 'system:menu:add',
    status: 1,
    createTime: null,
  },
];

test('buildMenuTree keeps directory, menu and button hierarchy', () => {
  const tree = buildMenuTree(menus);

  assert.equal(tree.length, 1);
  assert.equal(tree[0]?.id, 10);
  assert.equal(tree[0]?.children[0]?.id, 11);
  assert.equal(tree[0]?.children[0]?.children[0]?.id, 12);
});

test('findMenuSelectionAfterFilter keeps ancestor chain for matched nodes', () => {
  const { tree, selectedId } = findMenuSelectionAfterFilter(menus, '新增', 10);

  assert.equal(selectedId, 12);
  assert.equal(tree.length, 1);
  assert.equal(tree[0]?.children.length, 1);
  assert.equal(tree[0]?.children[0]?.children[0]?.menuName, '新增菜单');
});

test('mapMenuDetail returns readable detail groups for the selected node', () => {
  assert.deepEqual(mapMenuDetail(menus[1]), {
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
```

- [ ] **步骤 2：运行测试验证失败**

运行：`cd ui-admin && pnpm test:unit -- --test-name-pattern="Menu"`

预期：FAIL，报错 `Cannot find module '../src/views/system/menus-support.ts'`

- [ ] **步骤 3：编写最少支持实现代码**

```ts
import type { MenuVO } from '@/types/system';

export interface MenuTreeNode extends MenuVO {
  children: MenuTreeNode[];
}

export interface MenuDetailView {
  title: string;
  tag: string;
  groups: Array<[string, string]>;
}

export function buildMenuTree(menus: MenuVO[]): MenuTreeNode[] {
  const nodeMap = new Map<number, MenuTreeNode>();
  const roots: MenuTreeNode[] = [];

  for (const menu of menus) {
    nodeMap.set(menu.id, { ...menu, children: [] });
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

export function findMenuSelectionAfterFilter(
  menus: MenuVO[],
  keyword: string,
  currentSelectedId?: number,
) {
  const tree = keyword.trim() ? filterMenuTree(buildMenuTree(menus), keyword.trim()) : buildMenuTree(menus);
  const selectedId = containsMenu(tree, currentSelectedId) ? currentSelectedId : findFirstMenuId(tree);
  return { tree, selectedId };
}

export function mapMenuDetail(menu?: MenuVO, parentName = '根菜单'): MenuDetailView | null {
  if (!menu) {
    return null;
  }
  return {
    title: menu.menuName,
    tag: menu.menuType,
    groups: [
      ['上级菜单', parentName],
      ['路由地址', menu.routePath || '--'],
      ['组件路径', menu.component || '--'],
      ['权限标识', menu.perms || '--'],
      ['显示状态', menu.visible === 1 ? '显示' : '隐藏'],
      ['缓存策略', menu.keepAlive === 1 ? '缓存' : '不缓存'],
      ['状态', menu.status === 1 ? '启用' : '停用'],
    ],
  };
}
```

- [ ] **步骤 4：运行测试验证通过**

运行：`cd ui-admin && pnpm test:unit -- menus-support.test.ts`

预期：PASS，`menus-support.test.ts` 中的全部用例通过

- [ ] **步骤 5：Commit**

```bash
git add ui-admin/src/views/system/menus-support.ts ui-admin/tests/menus-support.test.ts
git commit -m "test(ui): 补充菜单树支持逻辑用例"
```

---

### 任务 2：将菜单管理页改造成左树右详情双栏

**文件：**
- 修改：`ui-admin/src/views/system/menus.vue`

- [ ] **步骤 1：先让页面依赖新支持逻辑并制造失败场景**

把 `menus.vue` 中原来的表格数据入口替换为对 `buildMenuTree` / `findMenuSelectionAfterFilter` / `mapMenuDetail` 的调用，然后立即执行构建验证。

```ts
import {
  buildMenuTree,
  findMenuSelectionAfterFilter,
  mapMenuDetail,
} from './menus-support';
```

- [ ] **步骤 2：运行构建验证失败**

运行：`cd ui-admin && pnpm build`

预期：FAIL，报错来自 `menus.vue` 中仍引用旧的 `rows`、`columns`、`mapMenuRow` 或未完成的双栏模板结构

- [ ] **步骤 3：编写最少页面实现代码**

将页面改造成如下结构：

```vue
<div class="content-grid">
  <div class="page-card menu-tree-card">
    <div class="menu-tree-header">
      <div>
        <div class="section-title">菜单结构</div>
        <div class="menu-tree-tip">点击节点查看详情与操作</div>
      </div>
    </div>
    <a-tree
      v-if="treeData.length"
      block-node
      :data="treeData"
      :default-expand-all="true"
      :selected-keys="selectedKeys"
      @select="handleSelect"
    >
      <template #title="nodeData">
        <div class="menu-tree-node">
          <div class="menu-tree-main">
            <span>{{ nodeData?.menuName }}</span>
            <a-tag size="small">{{ nodeData?.menuType }}</a-tag>
          </div>
          <a-space size="mini">
            <a-button size="mini" type="text" @click.stop="openCreate(nodeData?.id)">新增子菜单</a-button>
            <a-button size="mini" type="text" @click.stop="openEdit(nodeData?.id)">编辑</a-button>
          </a-space>
        </div>
      </template>
    </a-tree>
    <a-empty v-else description="暂无匹配菜单" />
  </div>

  <div class="page-card menu-detail-card">
    <template v-if="detailView">
      <div class="detail-header">
        <div class="section-title">{{ detailView.title }}</div>
        <a-tag>{{ detailView.tag }}</a-tag>
      </div>
      <a-descriptions :column="1" bordered size="large">
        <a-descriptions-item
          v-for="[label, value] in detailView.groups"
          :key="label"
          :label="label"
        >
          {{ value }}
        </a-descriptions-item>
      </a-descriptions>
    </template>
    <a-empty v-else description="请选择左侧菜单节点" />
  </div>
</div>
```

脚本层至少完成以下状态收敛：

```ts
const allMenus = ref<MenuVO[]>([]);
const treeData = ref<MenuTreeNode[]>([]);
const selectedKeys = ref<Array<string | number>>([]);

function applyMenuTree(nextMenus = allMenus.value) {
  const { tree, selectedId } = findMenuSelectionAfterFilter(
    nextMenus,
    keyword.value,
    selectedKeys.value[0] == null ? undefined : Number(selectedKeys.value[0]),
  );
  treeData.value = tree;
  selectedKeys.value = selectedId == null ? [] : [selectedId];
}

const selectedMenu = computed(() => findMenuById(allMenus.value, Number(selectedKeys.value[0])));
const detailView = computed(() => mapMenuDetail(selectedMenu.value, findParentName(allMenus.value, selectedMenu.value)));
```

同时保留：

- 顶部搜索与新增根菜单按钮
- 原有弹窗表单
- 删除后刷新树并回退到可用节点

- [ ] **步骤 4：运行测试与构建验证通过**

运行：`cd ui-admin && pnpm test:unit -- menus-support.test.ts`

预期：PASS，菜单支持逻辑测试仍然通过

运行：`cd ui-admin && pnpm build`

预期：PASS，类型检查与 Vite 构建通过

- [ ] **步骤 5：Commit**

```bash
git add ui-admin/src/views/system/menus.vue
git commit -m "feat(ui): 改造菜单管理双栏结构"
```

---

## 自检

- 规格覆盖度：左树右详情、搜索过滤、子菜单操作入口、默认选中和空状态都已覆盖到任务。
- 占位符扫描：计划中没有 `TODO`、`后续实现`、`类似任务 N` 一类占位符。
- 类型一致性：纯函数集中在 `menus-support.ts`，页面通过统一导出类型和函数消费，避免在组件里再次定义树结构。

## 执行方式

用户已确认采用 **子代理驱动** 执行本计划。实施顺序：

1. 先执行任务 1，并完成规格审查与代码质量审查。
2. 任务 1 通过后，再执行任务 2，并完成规格审查与代码质量审查。
3. 最后由主线程整合结果并执行 `pnpm test:unit` 与 `pnpm build` 验证。
