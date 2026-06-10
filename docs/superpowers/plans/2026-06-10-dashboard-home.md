# 仪表盘首页改版实现计划

> **面向 AI 代理的工作者：** 必需子技能：使用 superpowers:subagent-driven-development（推荐）或 superpowers:executing-plans 逐任务实现此计划。步骤使用复选框（`- [ ]`）语法来跟踪进度。

**目标：** 将 `ui-admin` 的仪表盘首页改造成用户已确认的 `B1：均衡总览型` 首页，提升信息密度、总览感和快捷入口可用性，同时保持现有数据来源与整体视觉语言一致。

**架构：** 前端将首页的静态配置和纯计算逻辑抽到 `dashboard-support.ts`，用单元测试锁定指标卡、角色文案和快捷入口权限显隐；`dashboard/index.vue` 只负责页面布局、路由跳转和响应式样式，实现“顶区 4 卡 + 中区双栏 + 底区入口卡”的三段式结构。

**技术栈：** Vue 3、TypeScript、Pinia、Vue Router、Arco Design Vue、Node test runner、Vite

---

## 文件结构与职责

- 创建：`ui-admin/src/views/dashboard/dashboard-support.ts`
  负责首页静态配置、指标卡构建、角色编码文案和快捷入口权限过滤等纯函数。
- 创建：`ui-admin/tests/dashboard-support.test.ts`
  负责首页支持逻辑的失败测试与回归测试。
- 修改：`ui-admin/src/views/dashboard/index.vue`
  负责首页模板、路由跳转、图标接线、卡片布局和响应式样式。

---

### 任务 1：先用单元测试钉住首页支持逻辑

**文件：**
- 创建：`ui-admin/tests/dashboard-support.test.ts`
- 创建：`ui-admin/src/views/dashboard/dashboard-support.ts`

- [ ] **步骤 1：编写失败的首页支持逻辑测试**

```ts
import test from 'node:test';
import assert from 'node:assert/strict';
import {
  buildDashboardMetrics,
  dashboardQuickEntries,
  filterQuickEntriesByPermission,
  formatDashboardRoleCodes,
} from '../src/views/dashboard/dashboard-support.ts';

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
  ]);

  assert.deepEqual(
    visible.map((item) => item.title),
    ['用户管理', '菜单管理'],
  );
});

test('formatDashboardRoleCodes joins role codes and falls back to placeholder', () => {
  assert.equal(formatDashboardRoleCodes(['SUPER_ADMIN', 'OPS_ADMIN']), 'SUPER_ADMIN, OPS_ADMIN');
  assert.equal(formatDashboardRoleCodes([]), '--');
  assert.equal(formatDashboardRoleCodes(undefined), '--');
});
```

- [ ] **步骤 2：运行测试验证失败**

运行：`cd ui-admin && pnpm test:unit -- tests/dashboard-support.test.ts`

预期：FAIL，报错 `Cannot find module '../src/views/dashboard/dashboard-support.ts'`

- [ ] **步骤 3：编写最少支持实现代码**

```ts
export interface DashboardMetric {
  title: string;
  value: number;
  note: string;
}

export interface DashboardQuickEntry {
  title: string;
  description: string;
  category: string;
  path: string;
  permission: string;
  badge: string;
  tone: 'blue' | 'teal' | 'amber' | 'slate';
}

export const dashboardQuickEntries: DashboardQuickEntry[] = [
  {
    title: '用户管理',
    description: '维护账号、昵称、状态和所属部门。',
    category: '账号',
    path: '/system/users',
    permission: 'system:user:list',
    badge: 'USR',
    tone: 'blue',
  },
  {
    title: '角色管理',
    description: '梳理角色编码与数据权限范围。',
    category: '权限',
    path: '/system/roles',
    permission: 'system:role:list',
    badge: 'RLS',
    tone: 'teal',
  },
  {
    title: '菜单管理',
    description: '维护路由结构、组件映射和权限标识。',
    category: '导航',
    path: '/system/access/menus',
    permission: 'system:menu:list',
    badge: 'MNU',
    tone: 'amber',
  },
  {
    title: '日志管理',
    description: '查看关键操作留痕和审计结果。',
    category: '审计',
    path: '/logs/op',
    permission: 'system:log:view',
    badge: 'LOG',
    tone: 'slate',
  },
];

export function buildDashboardMetrics(input: {
  menuCount: number;
  permissionCount: number;
  roleCount: number;
  quickEntryCount: number;
}): DashboardMetric[] {
  return [
    { title: '菜单总数', value: input.menuCount, note: '已挂载的功能入口' },
    { title: '权限点', value: input.permissionCount, note: '当前前端识别权限编码' },
    { title: '角色数', value: input.roleCount, note: '当前身份关联角色数量' },
    { title: '常用入口', value: input.quickEntryCount, note: '当前会话可见快捷入口' },
  ];
}

export function filterQuickEntriesByPermission(entries: DashboardQuickEntry[], permissions: string[]) {
  return entries.filter((item) => permissions.includes(item.permission));
}

export function formatDashboardRoleCodes(roleCodes?: string[]) {
  return roleCodes?.length ? roleCodes.join(', ') : '--';
}
```

- [ ] **步骤 4：运行测试验证通过**

运行：`cd ui-admin && pnpm test:unit -- tests/dashboard-support.test.ts`

预期：PASS，`dashboard-support.test.ts` 中的全部用例通过

- [ ] **步骤 5：Commit**

```bash
git add ui-admin/src/views/dashboard/dashboard-support.ts ui-admin/tests/dashboard-support.test.ts
git commit -m "test(ui): 补充仪表盘首页支持逻辑用例"
```

---

### 任务 2：按 B1 方案重构仪表盘首页结构与交互

**文件：**
- 修改：`ui-admin/src/views/dashboard/index.vue`

- [ ] **步骤 1：接入支持逻辑、图标和路由能力**

将 `dashboard/index.vue` 的脚本替换为基于计算属性的数据拼装方式，避免把静态配置和权限过滤揉进模板。

```ts
<script setup lang="ts">
import { computed } from 'vue';
import { useRouter } from 'vue-router';
import {
  IconApps,
  IconBook,
  IconMenu,
  IconSafe,
  IconSettings,
} from '@arco-design/web-vue/es/icon';
import { useAuthStore } from '@/store/auth';
import {
  buildDashboardMetrics,
  dashboardQuickEntries,
  dashboardReminders,
  filterQuickEntriesByPermission,
  formatDashboardRoleCodes,
} from './dashboard-support';

const authStore = useAuthStore();
const router = useRouter();

const quickEntryCards = computed(() =>
  filterQuickEntriesByPermission(dashboardQuickEntries, authStore.permissions),
);

const overviewMetrics = computed(() =>
  buildDashboardMetrics({
    menuCount: authStore.menus.length,
    permissionCount: authStore.permissions.length,
    roleCount: authStore.profile?.roleCodes?.length ?? 0,
    quickEntryCount: quickEntryCards.value.length,
  }),
);

const roleCodeText = computed(() => formatDashboardRoleCodes(authStore.profile?.roleCodes));

const quickEntryIcons = {
  '用户管理': IconApps,
  '角色管理': IconSafe,
  '菜单管理': IconMenu,
  '日志管理': IconBook,
} as const;

const goToEntry = (path: string) => {
  router.push(path);
};
</script>
```

- [ ] **步骤 2：把模板改成“顶区 4 卡 + 中区双栏 + 底区入口卡”**

首页模板替换为三段式结构，重点保留现有欢迎语义，但把身份、提醒和入口重新排布。

```vue
<template>
  <div class="dashboard-page">
    <section class="overview-grid">
      <a-card
        v-for="item in overviewMetrics"
        :key="item.title"
        class="page-card overview-card"
        :bordered="false"
      >
        <div class="overview-card__label">{{ item.title }}</div>
        <div class="overview-card__value">{{ item.value }}</div>
        <div class="overview-card__note">{{ item.note }}</div>
      </a-card>
    </section>

    <section class="summary-grid">
      <a-card class="page-card identity-card" :bordered="false">
        <div class="identity-card__header">
          <div>
            <div class="dashboard-kicker">当前身份</div>
            <div class="identity-card__title">欢迎回来</div>
            <div class="dashboard-intro">
              {{ authStore.profile?.nickname ?? '管理员' }}，当前会话已经接入菜单、权限、日志和系统配置能力。
            </div>
          </div>
          <a-tag color="arcoblue" bordered>{{ authStore.profile?.username ?? 'SYSTEM' }}</a-tag>
        </div>

        <div class="identity-grid">
          <div class="identity-pill">
            <div class="identity-pill__label">当前用户</div>
            <div class="identity-pill__value">{{ authStore.profile?.nickname ?? '--' }}</div>
          </div>
          <div class="identity-pill">
            <div class="identity-pill__label">所属部门</div>
            <div class="identity-pill__value">{{ authStore.profile?.deptName ?? '--' }}</div>
          </div>
          <div class="identity-pill">
            <div class="identity-pill__label">角色编码</div>
            <div class="identity-pill__value code-text">{{ roleCodeText }}</div>
          </div>
        </div>

        <div class="identity-band">
          <span>权限点 {{ authStore.permissions.length }}</span>
          <span>菜单挂载 {{ authStore.menus.length }}</span>
          <span>角色数 {{ authStore.profile?.roleCodes?.length ?? 0 }}</span>
        </div>
      </a-card>

      <a-card class="page-card reminder-card" :bordered="false">
        <template #title>系统提醒</template>
        <div class="reminder-list">
          <div v-for="item in dashboardReminders" :key="item.title" class="reminder-item">
            <div class="reminder-item__title">{{ item.title }}</div>
            <div class="reminder-item__desc">{{ item.description }}</div>
          </div>
        </div>
      </a-card>
    </section>

    <a-card class="page-card quick-entry-panel" :bordered="false" title="常用入口">
      <div class="quick-entry-description">把高频系统维护页集中展示，减少来回寻找导航的成本。</div>
      <div class="quick-entry-grid">
        <div
          v-for="item in quickEntryCards"
          :key="item.title"
          class="quick-entry-card"
          :class="`quick-entry-card--${item.tone}`"
          @click="goToEntry(item.path)"
        >
          <div class="quick-entry-card__icon">
            <component :is="quickEntryIcons[item.title]" />
          </div>
          <div class="quick-entry-card__title">{{ item.title }}</div>
          <div class="quick-entry-card__desc">{{ item.description }}</div>
          <div class="quick-entry-card__footer">
            <span class="quick-entry-card__badge">{{ item.badge }}</span>
            <a-tag bordered size="small">{{ item.category }}</a-tag>
          </div>
        </div>
      </div>
    </a-card>
  </div>
</template>
```

- [ ] **步骤 3：用新的 scoped 样式完成首页层次和响应式**

样式替换为 B1 方案，重点控制卡片节奏、悬停反馈和移动端换列。

```css
.dashboard-page {
  display: grid;
  gap: 18px;
}

.overview-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 18px;
}

.summary-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.45fr) minmax(320px, 1fr);
  gap: 18px;
}

.identity-grid,
.quick-entry-grid {
  display: grid;
  gap: 14px;
}

.identity-grid {
  grid-template-columns: repeat(3, minmax(0, 1fr));
  margin-top: 18px;
}

.quick-entry-grid {
  grid-template-columns: repeat(4, minmax(0, 1fr));
}

.quick-entry-card {
  position: relative;
  display: grid;
  gap: 12px;
  padding: 18px;
  border: 1px solid rgba(15, 23, 42, 0.08);
  border-radius: 18px;
  background: linear-gradient(180deg, rgba(255, 255, 255, 0.98), rgba(245, 249, 255, 0.95));
  cursor: pointer;
  transition:
    transform 0.18s ease,
    box-shadow 0.18s ease,
    border-color 0.18s ease;
}

.quick-entry-card:hover {
  transform: translateY(-2px);
  border-color: rgba(36, 91, 219, 0.18);
  box-shadow: 0 18px 32px rgba(15, 23, 42, 0.1);
}

@media (max-width: 1200px) {
  .overview-grid,
  .quick-entry-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }

  .summary-grid {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .overview-grid,
  .identity-grid,
  .quick-entry-grid {
    grid-template-columns: 1fr;
  }
}
```

- [ ] **步骤 4：运行单测与构建验证**

运行：

- `cd ui-admin && pnpm test:unit -- tests/dashboard-support.test.ts`
- `cd ui-admin && pnpm build`

预期：

- `dashboard-support.test.ts` 全部通过
- `vue-tsc --noEmit` 和 `vite build` 通过

- [ ] **步骤 5：Commit**

```bash
git add ui-admin/src/views/dashboard/index.vue
git commit -m "feat(ui): 重构仪表盘首页总览布局"
```

---

### 任务 3：做本地页面验证并收尾

**文件：**
- 修改：`ui-admin/src/views/dashboard/index.vue`（仅当验证发现样式问题时）

- [ ] **步骤 1：启动本地开发服务用于人工验证**

运行：`cd ui-admin && pnpm dev -- --host 127.0.0.1 --port 5173`

预期：Vite 输出本地访问地址 `http://127.0.0.1:5173/`

- [ ] **步骤 2：在浏览器中检查桌面端首页表现**

检查点：

- 顶区必须是 4 张概览卡
- 中区左侧身份卡与右侧提醒卡形成明显双栏
- 底区快捷入口必须从纵向列表改成卡片网格
- 没有权限的入口卡不显示

- [ ] **步骤 3：切到窄屏检查响应式布局**

检查点：

- 中区双栏在窄屏下改为上下堆叠
- 快捷入口在中宽屏为 2 列，移动端为 1 列
- 角色编码和标签没有溢出卡片边界

- [ ] **步骤 4：如果验证出现问题，做最小样式修正并重新构建**

运行：`cd ui-admin && pnpm build`

预期：构建继续通过，且修正只落在 `dashboard/index.vue`

- [ ] **步骤 5：Commit**

```bash
git add ui-admin/src/views/dashboard/index.vue
git commit -m "fix(ui): 收敛仪表盘首页响应式细节"
```
