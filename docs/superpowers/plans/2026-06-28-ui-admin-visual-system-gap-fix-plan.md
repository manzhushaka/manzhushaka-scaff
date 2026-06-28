# ui-admin 视觉系统缺口修复实现计划

> **面向 AI 代理的工作者：** 必需子技能：使用 superpowers:subagent-driven-development（推荐）或 superpowers:executing-plans 逐任务实现此计划。步骤使用复选框（`- [ ]`）语法来跟踪进度。

**目标：** 修复 `main` 分支中 `ui-admin` 视觉系统升级未完全落地的问题，让实现与 `docs/superpowers/specs/2026-06-28-ui-admin-visual-system-design.md` 保持一致。

**架构：** 以 `brandTheme` 和 `[data-ui-theme]` 作为唯一品牌主题入口，保留旧 `theme` 主色、`sideTheme` 和暗黑模式作为兼容能力，但不能覆盖 A/B/C 品牌主题。全局样式通过 `--ui-*` token 统一驱动，业务页面逐步迁移到「过滤卡 + 工具条 + 数据区」结构，并通过典型页面和构建命令验收。

**技术栈：** Vue 3、Pinia、Element Plus、Vite、SCSS、localStorage `layout-setting`。

---

## 1. 背景与审查结论

当前 `main` 分支已经完成了视觉系统的第一层地基：

- `App.vue` 已挂载 `data-ui-theme`，登录页固定 `cool-tower`。
- `settings` store 已新增 `brandTheme`，默认值为 `cool-tower`。
- `theme-tokens.scss` 已包含 `cool-tower`、`amber-command`、`gold-ledger` 三套 token。
- `ThemeSwitcher` 已放入登录后顶栏。
- 登录页、首页、用户管理、角色管理、菜单管理、通知公告有初步改造。
- `npm run build:prod` 当前可构建通过。

主要缺口不是「没有做」，而是「主题来源未收敛、组件参数未全站化、部分样式仍绕过 token」。本计划按风险从高到低拆分修复任务。

## 2. 问题清单

| 优先级 | 问题 | 影响 | 证据文件 |
| --- | --- | --- | --- |
| P0 | `brandTheme` 会被布局设置保存覆盖丢失 | 用户切换 A/C 后，保存布局配置会重置主题持久化 | `ui-admin/src/layout/components/Settings/index.vue`、`ui-admin/src/store/modules/settings.js` |
| P0 | 旧 `theme` 主色、`sideTheme`、`html.dark` 仍参与主视觉链路 | A/B/C 主题可能被旧变量覆盖，形成新旧混搭 | `ui-admin/src/layout/components/Sidebar/index.vue`、`ui-admin/src/layout/components/TagsView/index.vue`、`ui-admin/src/layout/components/Navbar.vue` |
| P0 | 移动端隐藏主题切换器 | 不符合「移动端保留菜单、主题、用户入口」规格 | `ui-admin/src/layout/components/Navbar.vue` |
| P0 | 全局 SCSS 中使用 `:deep()` | 构建通过但选择器可能无法按预期匹配，表格覆盖不可靠 | `ui-admin/src/assets/styles/theme-tokens.scss` |
| P1 | `.ui-filter-card/.ui-action-bar/.ui-table-card` 只覆盖 4 个页面 | 大量后台页面仍是旧若依视觉结构 | `ui-admin/src/views/**` |
| P1 | 首页硬编码渐变和状态色 | A/C 主题下首页仍带 B 主题或额外紫粉色系 | `ui-admin/src/views/index.vue` |
| P1 | 第 10 章组件参数未系统落地 | 按钮、表单、分页、弹窗、抽屉、树侧栏、空态等仍是局部覆盖 | `ui-admin/src/assets/styles/theme-tokens.scss`、`ui-admin/src/assets/styles/element-ui.scss` |
| P1 | `Settings` 抽屉仍暴露多导航模式、侧栏深浅、主色选择作为强入口 | 与「统一主布局 + 三套品牌主题」目标冲突 | `ui-admin/src/layout/components/Settings/index.vue` |
| P1 | `manzhushaka.scss` 和部分组件存在大量硬编码色值 | A 主题深色和 C 主题暖色下容易出现对比度与混搭问题 | `ui-admin/src/assets/styles/manzhushaka.scss`、`ui-admin/src/components/**` |
| P2 | `ThemeSwitcher` 缺少按钮类型和更明确的可访问标签 | 在复杂容器或表单上下文中有默认 submit 风险，可访问性也不够完整 | `ui-admin/src/components/ThemeSwitcher/index.vue` |
| P2 | 构建提示 `%VITE_APP_TITLE% is not defined` | 不阻塞 UI，但会污染构建输出和页面标题 | `ui-admin/index.html`、`.env*` |
| P2 | `color-mix()` 浏览器兼容策略未收口 | 低版本浏览器可能丢失主题浅色派生 | `ui-admin/src/assets/styles/theme-tokens.scss` |

## 3. 文件结构与职责

### 3.1 配置与状态

- 修改：`ui-admin/src/store/modules/settings.js`
- 修改：`ui-admin/src/layout/components/Settings/index.vue`
- 修改：`ui-admin/src/settings.js`

职责：收敛 `brandTheme` 的默认值、合法值校验、持久化和设置抽屉保存行为。

### 3.2 主布局与导航

- 修改：`ui-admin/src/layout/index.vue`
- 修改：`ui-admin/src/layout/components/Navbar.vue`
- 修改：`ui-admin/src/layout/components/Sidebar/index.vue`
- 修改：`ui-admin/src/layout/components/Sidebar/Logo.vue`
- 修改：`ui-admin/src/layout/components/TagsView/index.vue`
- 修改：`ui-admin/src/layout/components/TopNav/index.vue`
- 修改：`ui-admin/src/layout/components/TopBar/index.vue`

职责：让登录后默认布局以新 token 为准，弱化旧导航模式和旧主色入口，确保桌面端与移动端都能切换主题。

### 3.3 全局样式与组件参数

- 修改：`ui-admin/src/assets/styles/theme-tokens.scss`
- 修改：`ui-admin/src/assets/styles/element-ui.scss`
- 修改：`ui-admin/src/assets/styles/sidebar.scss`
- 修改：`ui-admin/src/assets/styles/manzhushaka.scss`
- 修改：`ui-admin/src/assets/styles/index.scss`
- 修改：`ui-admin/src/components/ThemeSwitcher/index.vue`
- 修改：`ui-admin/src/components/RightToolbar/index.vue`
- 修改：`ui-admin/src/components/Pagination/index.vue`
- 修改：`ui-admin/src/components/TreePanel/index.vue`

职责：把第 10 章组件参数固化为可复用样式，避免每个页面重复写视觉细节。

### 3.4 典型页面迁移

- 修改：`ui-admin/src/views/index.vue`
- 修改：`ui-admin/src/views/system/user/index.vue`
- 修改：`ui-admin/src/views/system/role/index.vue`
- 修改：`ui-admin/src/views/system/menu/index.vue`
- 修改：`ui-admin/src/views/system/notice/index.vue`
- 修改：`ui-admin/src/views/system/dept/index.vue`
- 修改：`ui-admin/src/views/system/post/index.vue`
- 修改：`ui-admin/src/views/system/dict/type.vue`
- 修改：`ui-admin/src/views/system/dict/data.vue`
- 修改：`ui-admin/src/views/system/config/index.vue`
- 修改：`ui-admin/src/views/monitor/operlog/index.vue`
- 修改：`ui-admin/src/views/monitor/logininfor/index.vue`
- 修改：`ui-admin/src/views/monitor/online/index.vue`
- 修改：`ui-admin/src/views/monitor/server/index.vue`

职责：覆盖规格要求的典型页面，并把高频列表页迁移到统一结构。

### 3.5 构建与环境

- 修改：`ui-admin/index.html`
- 修改：`ui-admin/.env.development`
- 修改：`ui-admin/.env.production`
- 修改：`ui-admin/.env.staging`

职责：清理构建提示，保证生产构建输出稳定。

## 4. 修复任务

### 任务 1：修复 `brandTheme` 持久化被覆盖

**问题：** `ThemeSwitcher` 会写入 `layout-setting.brandTheme`，但 `Settings/index.vue` 的 `saveSetting()` 未保存 `brandTheme`，会把配置重写成不含 `brandTheme` 的对象。

**文件：**

- 修改：`ui-admin/src/store/modules/settings.js`
- 修改：`ui-admin/src/layout/components/Settings/index.vue`

- [ ] **步骤 1：在 `settings.js` 增加主题合法值和安全读取**

将 `storageSetting` 从字符串 fallback 改为对象 fallback，避免后续访问属性依赖隐式行为。

```js
const BRAND_THEMES = ['cool-tower', 'amber-command', 'gold-ledger']

function normalizeBrandTheme(theme) {
  return BRAND_THEMES.includes(theme) ? theme : 'cool-tower'
}

function getStorageSetting() {
  try {
    return JSON.parse(localStorage.getItem('layout-setting')) || {}
  } catch (error) {
    return {}
  }
}

const storageSetting = getStorageSetting()
```

- [ ] **步骤 2：让 `state.brandTheme` 使用合法值**

```js
brandTheme: normalizeBrandTheme(storageSetting.brandTheme)
```

- [ ] **步骤 3：让 `setBrandTheme()` 只写合法主题**

```js
setBrandTheme(theme) {
  const nextTheme = normalizeBrandTheme(theme)
  this.brandTheme = nextTheme

  const storageSetting = getStorageSetting()
  storageSetting.brandTheme = nextTheme
  localStorage.setItem('layout-setting', JSON.stringify(storageSetting))
}
```

- [ ] **步骤 4：在 `Settings/index.vue` 保存配置时写入 `brandTheme`**

在 `layoutSetting` 中加入：

```js
"brandTheme": storeSettings.value.brandTheme
```

- [ ] **步骤 5：手工验证持久化**

运行：`npm run dev`

浏览器验证：

1. 登录后切换到 `amber-command`。
2. 打开「布局设置」，点击「保存配置」。
3. 刷新页面。
4. 预期：`document.querySelector('[data-ui-theme]').dataset.uiTheme` 仍为 `amber-command`。

- [ ] **步骤 6：运行构建**

运行：

```bash
cd ui-admin
npm run build:prod
```

预期：exit 0。

### 任务 2：收敛旧主题入口，避免覆盖品牌主题

**问题：** 侧边栏、页签和顶栏仍读取旧 `settingsStore.theme`、`sideTheme`、`isDark`。这些兼容项可以保留，但不能覆盖 A/B/C 品牌主题。

**文件：**

- 修改：`ui-admin/src/layout/components/Sidebar/index.vue`
- 修改：`ui-admin/src/layout/components/Sidebar/Logo.vue`
- 修改：`ui-admin/src/layout/components/TagsView/index.vue`
- 修改：`ui-admin/src/layout/components/Navbar.vue`
- 修改：`ui-admin/src/layout/components/TopNav/index.vue`
- 修改：`ui-admin/src/layout/components/TopBar/index.vue`
- 修改：`ui-admin/src/assets/styles/sidebar.scss`
- 修改：`ui-admin/src/assets/styles/variables.module.scss`

- [ ] **步骤 1：侧边栏菜单背景改为 token 驱动**

在 `Sidebar/index.vue` 中移除 `variables` 对背景和文字的主导逻辑，让 `el-menu` 使用 token。

```vue
<el-menu
  :default-active="activeMenu"
  :collapse="isCollapse"
  background-color="var(--ui-bg-sidebar)"
  text-color="var(--ui-text-inverse)"
  :unique-opened="true"
  active-text-color="var(--ui-text-inverse)"
  :collapse-transition="false"
  mode="vertical"
>
```

- [ ] **步骤 2：删除 `tagActiveStyle()` 对旧主色的 inline 覆盖**

在 `TagsView/index.vue` 中删除 `theme` computed 和 `tagActiveStyle()`，同时移除模板上的 `:style="tagActiveStyle(tag)"`。

```vue
class="tags-view-item"
```

激活样式统一使用现有 SCSS：

```scss
.tags-view-item.active {
  background-color: var(--ui-primary-soft);
  color: var(--ui-primary);
  border-color: color-mix(in srgb, var(--ui-primary) 28%, var(--ui-border));
}
```

- [ ] **步骤 3：暗黑模式入口降级为兼容项**

在 `Navbar.vue` 中保留暗黑模式按钮时增加说明 tooltip，例如「兼容暗黑模式」。如果产品决定不展示，则从右侧常用操作中移出，仅保留设置抽屉或隐藏入口。

推荐实现：默认不在顶栏显示暗黑模式按钮，避免与 `amber-command` 深色品牌主题混淆。

```vue
<!-- 暗黑模式兼容入口不作为品牌主题主入口展示 -->
```

- [ ] **步骤 4：弱化 `sideTheme` 和 `theme` 在设置抽屉中的入口**

在 `Settings/index.vue` 中把「主题风格设置」和「主题颜色」移动到「兼容设置」分组，或在本轮直接隐藏。

推荐实现：保留状态字段读取，但 UI 不展示 `sideTheme` 与旧主色选择器。

- [ ] **步骤 5：验证 A/B/C 主题不被旧主色覆盖**

浏览器验证：

1. 切换到 `gold-ledger`。
2. 打开用户管理、角色管理、菜单管理。
3. 检查侧边栏、顶栏、TagsView、按钮、表格表头均使用金色主色和暖色边框。
4. 修改旧主色 `settingsStore.theme = '#ff4500'` 后刷新。
5. 预期：品牌主题仍以 `gold-ledger` 的 `--ui-primary` 为准。

### 任务 3：恢复移动端主题切换入口

**问题：** `ThemeSwitcher` 位于 `appStore.device !== 'mobile'` 分支内，手机宽度下被隐藏。

**文件：**

- 修改：`ui-admin/src/layout/components/Navbar.vue`
- 修改：`ui-admin/src/components/ThemeSwitcher/index.vue`

- [ ] **步骤 1：把 `ThemeSwitcher` 移出桌面端专属分支**

将主题切换器放在桌面条件外，但保留在用户入口之前。

```vue
<template v-if="appStore.device !== 'mobile'">
  <header-search id="header-search" class="right-menu-item" />
  <screenfull id="screenfull" class="right-menu-item hover-effect" />
  <size-select id="size-select" class="right-menu-item hover-effect" />
  <header-notice id="header-notice" class="right-menu-item hover-effect" />
</template>

<theme-switcher class="right-menu-item theme-switcher-entry" />
```

- [ ] **步骤 2：移动端压缩主题切换器尺寸**

在 `ThemeSwitcher/index.vue` 中增加移动端样式：

```scss
@media (max-width: 480px) {
  .theme-switcher {
    height: 32px;
    padding: 3px;
  }

  .theme-swatch {
    width: 22px;
    height: 22px;
  }
}
```

- [ ] **步骤 3：验证移动端顶栏**

浏览器验证宽度：`390 x 844`。

预期：

- 左侧菜单按钮可见。
- 主题切换器可见。
- 用户头像可见。
- 搜索、源码、文档、全屏、布局大小等非关键按钮隐藏。
- 主题切换器不遮挡用户菜单。

### 任务 4：修复全局 SCSS 中的 `:deep()` 选择器

**问题：** `theme-tokens.scss` 是全局样式，不应使用 Vue SFC scoped 下的 `:deep()`。当前写法构建通过，但实际匹配不可靠。

**文件：**

- 修改：`ui-admin/src/assets/styles/theme-tokens.scss`

- [ ] **步骤 1：替换 `.ui-table-card` 内部 `:deep()` 选择器**

把：

```scss
.ui-table-card {
  overflow: hidden;
  :deep(.el-table) {
    border: none;
  }
  :deep(.el-table th.el-table__cell) {
    background-color: var(--ui-bg-panel-soft) !important;
    color: var(--ui-text-regular);
  }
  :deep(.el-table__inner-wrapper::before) {
    display: none;
  }
}
```

替换为：

```scss
.ui-table-card {
  overflow: hidden;

  .el-table {
    border: none;
    color: var(--ui-text-regular);
    background: var(--ui-bg-panel);
  }

  .el-table th.el-table__cell {
    height: 44px;
    background-color: var(--ui-bg-panel-soft) !important;
    color: var(--ui-text-regular);
    font-size: 13px;
    font-weight: 600;
  }

  .el-table td.el-table__cell {
    height: var(--ui-table-row-height);
    color: var(--ui-text-regular);
    border-bottom: 1px solid var(--ui-border);
  }

  .el-table__inner-wrapper::before {
    display: none;
  }
}
```

- [ ] **步骤 2：全局搜索确认无误用**

运行：

```bash
rg -n ":deep\\(" ui-admin/src/assets/styles
```

预期：无输出。

- [ ] **步骤 3：验证表格覆盖生效**

浏览器检查用户管理表格：

- 表头背景为 `--ui-bg-panel-soft`。
- 行高接近 `48px`。
- A 主题下表格底色为深色面板，不出现白底黑字割裂。

### 任务 5：完善组件级参数覆盖

**问题：** 第 10 章的按钮、表单、过滤卡、工具条、表格、分页、弹窗、抽屉、树侧栏、标签、弹层、空态、滚动条和移动端规则仍是局部覆盖。

**文件：**

- 修改：`ui-admin/src/assets/styles/theme-tokens.scss`
- 修改：`ui-admin/src/assets/styles/element-ui.scss`
- 修改：`ui-admin/src/components/RightToolbar/index.vue`
- 修改：`ui-admin/src/components/Pagination/index.vue`
- 修改：`ui-admin/src/components/TreePanel/index.vue`

- [ ] **步骤 1：在 `theme-tokens.scss` 补齐组件基础类**

新增或补齐以下样式块：

```scss
.ui-action-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  min-height: 36px;
  margin: 12px 0;
  gap: 8px;
  flex-wrap: wrap;
}

.ui-filter-card {
  margin-bottom: 12px;
  padding: 16px;
  background: var(--ui-bg-panel);
  border: 1px solid var(--ui-border);
  border-radius: var(--ui-radius-panel);
  box-shadow: var(--ui-shadow-panel);
}

.ui-filter-card .el-form-item {
  margin-right: 12px;
  margin-bottom: 10px;
}

.ui-table-card {
  border: 1px solid var(--ui-border);
  border-radius: var(--ui-radius-panel);
  background: var(--ui-bg-panel);
  box-shadow: var(--ui-shadow-panel);
}
```

- [ ] **步骤 2：补齐 Element Plus 控件参数**

在 `element-ui.scss` 中补齐：

```scss
.el-button {
  min-height: 32px;
  padding: 0 12px;
  font-size: 13px;
  border-radius: var(--ui-radius-control);
}

.el-button--primary {
  --el-button-bg-color: var(--ui-primary);
  --el-button-border-color: var(--ui-primary);
  --el-button-hover-bg-color: var(--ui-primary-hover);
  --el-button-hover-border-color: var(--ui-primary-hover);
  --el-button-active-bg-color: var(--ui-primary-active);
  --el-button-active-border-color: var(--ui-primary-active);
}

.el-input__wrapper,
.el-select__wrapper {
  min-height: var(--ui-form-control-height);
  border-radius: var(--ui-radius-control);
}

.el-dialog {
  max-height: calc(100vh - 96px);
  border-radius: var(--ui-radius-popover);
  box-shadow: var(--ui-shadow-popover);
}

.el-overlay {
  background-color: rgba(15, 23, 42, 0.45);
}

[data-ui-theme='amber-command'] .el-overlay {
  background-color: rgba(0, 0, 0, 0.62);
}
```

- [ ] **步骤 3：RightToolbar 纳入工具条视觉体系**

调整 `RightToolbar` 的 `.top-right-btn`，确保它可以作为 `.ui-action-bar` 的右侧工具区。

```scss
.top-right-btn {
  margin-left: auto;
  display: flex;
  align-items: center;
  gap: 6px;
}

:deep(.el-button.is-circle) {
  width: 32px;
  height: 32px;
  border-radius: var(--ui-radius-control);
}
```

- [ ] **步骤 4：分页组件补齐容器参数**

在 `Pagination/index.vue` 或全局样式中保证：

```scss
.pagination-container {
  min-height: 48px;
  margin-top: 12px;
  display: flex;
  justify-content: flex-end;
  align-items: center;
}

@media (max-width: 768px) {
  .pagination-container {
    justify-content: flex-start;
    overflow-x: auto;
  }
}
```

- [ ] **步骤 5：树侧栏参数固化**

在 `TreePanel` 对应样式中确认：

```scss
.tree-sidebar {
  width: var(--tree-sidebar-width, 260px);
  min-width: 220px;
  max-width: 360px;
  border: 1px solid var(--ui-border);
  border-radius: var(--ui-radius-panel);
  background: var(--ui-bg-panel);
}

.tree-sidebar-content {
  min-width: 0;
}
```

- [ ] **步骤 6：组件参数抽查验收**

在 `cool-tower`、`amber-command`、`gold-ledger` 下分别检查：

- 按钮高度约 `32px`。
- 输入框高度约 `36px`。
- 表格行高约 `48px`。
- 弹窗圆角 `8px`。
- 抽屉背景跟随主题。
- 分页当前页使用 `--ui-primary`。
- 树节点 hover 使用 `--ui-primary-soft`。

### 任务 6：迁移高频列表页到统一结构

**问题：** 只有用户、角色、菜单、通知 4 个页面使用 `.ui-*` 结构。规格验收要求用户管理、角色管理、菜单管理、通知公告、服务监控，同时列表体系应覆盖字典、参数、日志等页面。

**文件：**

- 修改：`ui-admin/src/views/system/dept/index.vue`
- 修改：`ui-admin/src/views/system/post/index.vue`
- 修改：`ui-admin/src/views/system/dict/type.vue`
- 修改：`ui-admin/src/views/system/dict/data.vue`
- 修改：`ui-admin/src/views/system/config/index.vue`
- 修改：`ui-admin/src/views/monitor/operlog/index.vue`
- 修改：`ui-admin/src/views/monitor/logininfor/index.vue`
- 修改：`ui-admin/src/views/monitor/online/index.vue`
- 修改：`ui-admin/src/views/monitor/job/index.vue`

- [ ] **步骤 1：统一查询表单 class**

每个列表页的查询表单增加：

```vue
class="ui-filter-card"
```

示例：

```vue
<el-form
  :model="queryParams"
  ref="queryRef"
  :inline="true"
  v-show="showSearch"
  class="ui-filter-card"
>
```

- [ ] **步骤 2：统一操作条 class**

每个列表页操作按钮行增加：

```vue
class="mb8 ui-action-bar"
```

示例：

```vue
<el-row :gutter="10" class="mb8 ui-action-bar">
```

- [ ] **步骤 3：统一表格容器**

用 `.ui-table-card` 包裹表格和分页。

```vue
<div class="ui-table-card">
  <el-table v-loading="loading" :data="list">
    ...
  </el-table>
  <pagination ... />
</div>
```

- [ ] **步骤 4：修正用户管理页面的大卡片嵌套**

`user/index.vue` 当前把过滤卡、工具条、表格都放进 `content-inner ui-table-card`。调整为：

```vue
<div class="tree-sidebar-content">
  <div class="content-inner">
    <el-form class="ui-filter-card">...</el-form>
    <el-row class="mb8 ui-action-bar">...</el-row>
    <div class="ui-table-card">
      <el-table>...</el-table>
      <pagination ... />
    </div>
  </div>
</div>
```

- [ ] **步骤 5：保留权限指令和业务行为**

迁移时不得修改：

- `v-hasPermi`
- 查询字段
- 表格列
- `handleQuery`
- `resetQuery`
- `handleAdd`
- `handleUpdate`
- `handleDelete`
- `handleExport`

- [ ] **步骤 6：覆盖率验证**

运行：

```bash
rg -l "ui-filter-card|ui-action-bar|ui-table-card" ui-admin/src/views -g "*.vue" | sort
```

预期至少包含：

```text
ui-admin/src/views/system/config/index.vue
ui-admin/src/views/system/dept/index.vue
ui-admin/src/views/system/dict/data.vue
ui-admin/src/views/system/dict/type.vue
ui-admin/src/views/system/menu/index.vue
ui-admin/src/views/system/notice/index.vue
ui-admin/src/views/system/post/index.vue
ui-admin/src/views/system/role/index.vue
ui-admin/src/views/system/user/index.vue
ui-admin/src/views/monitor/job/index.vue
ui-admin/src/views/monitor/logininfor/index.vue
ui-admin/src/views/monitor/online/index.vue
ui-admin/src/views/monitor/operlog/index.vue
```

### 任务 7：首页硬编码色值 token 化

**问题：** 首页 KPI、快捷入口、欢迎面板直接使用固定渐变和紫色、粉色等硬编码色值，切换 A/C 主题后仍带 B 主题视觉。

**文件：**

- 修改：`ui-admin/src/views/index.vue`
- 修改：`ui-admin/src/assets/styles/theme-tokens.scss`

- [ ] **步骤 1：为图表和 KPI 增加主题色序列 token**

在每个主题下增加：

```scss
--ui-chart-1: var(--ui-primary);
--ui-chart-2: var(--ui-accent);
--ui-chart-3: var(--ui-success);
--ui-chart-4: var(--ui-warning);
--ui-chart-5: var(--ui-danger);
```

- [ ] **步骤 2：首页数据改为语义 key**

把 `iconBg` 和 `trendColor` 从固定色值改为 class 或 token key。

```js
const kpiList = ref([
  { label: '用户总数', value: '—', icon: 'user', tone: 'primary', trend: '' },
  { label: '角色总数', value: '—', icon: 'peoples', tone: 'success', trend: '' },
  { label: '通知公告', value: '—', icon: 'message', tone: 'warning', trend: '' },
  { label: '服务状态', value: '正常', icon: 'server', tone: 'accent', trend: '运行中' },
])
```

- [ ] **步骤 3：用 CSS class 生成不同视觉**

```scss
.tone-primary {
  --tone-color: var(--ui-primary);
  --tone-soft: var(--ui-primary-soft);
}

.tone-success {
  --tone-color: var(--ui-success);
  --tone-soft: color-mix(in srgb, var(--ui-success) 14%, transparent);
}

.tone-warning {
  --tone-color: var(--ui-warning);
  --tone-soft: color-mix(in srgb, var(--ui-warning) 14%, transparent);
}

.tone-accent {
  --tone-color: var(--ui-accent);
  --tone-soft: color-mix(in srgb, var(--ui-accent) 18%, transparent);
}

.kpi-icon-wrap,
.quick-icon {
  background: linear-gradient(135deg, var(--tone-color), color-mix(in srgb, var(--tone-color) 72%, var(--ui-bg-panel)));
}
```

- [ ] **步骤 4：欢迎面板改为主题渐变**

```scss
.welcome-panel {
  background:
    radial-gradient(circle at 82% 20%, color-mix(in srgb, var(--ui-accent) 24%, transparent), transparent 30%),
    linear-gradient(135deg, var(--ui-bg-sidebar) 0%, var(--ui-primary) 100%);
}
```

- [ ] **步骤 5：验证首页三主题**

浏览器验证：

- `cool-tower`：保持冷感蓝色。
- `amber-command`：首页面板和 KPI 适配深色控制台，不出现大面积亮紫。
- `gold-ledger`：首页主色转为铜金，不出现冷蓝主导。

### 任务 8：设置抽屉按新规格收口

**问题：** 规格要求默认统一主布局，不再强化多导航模式并行。但设置抽屉仍把「左侧菜单 / 混合菜单 / 顶部菜单」作为第一组配置展示，还展示旧主色和侧栏深浅。

**文件：**

- 修改：`ui-admin/src/layout/components/Settings/index.vue`
- 修改：`ui-admin/src/settings.js`

- [ ] **步骤 1：默认布局固定为 `navType: 1`**

保持 `settings.js`：

```js
navType: 1
```

并在读取旧缓存时，如果值不合法或不希望继续暴露，强制回落：

```js
navType: 1
```

- [ ] **步骤 2：隐藏多导航模式 UI**

从设置抽屉界面隐藏 `nav-wrap`，保留底层兼容代码以降低风险。

```vue
<!-- 统一主布局阶段不展示多导航模式切换 -->
```

- [ ] **步骤 3：隐藏旧主色和侧栏风格 UI**

隐藏「主题风格设置」和「主题颜色」，避免用户以为旧主色等价于品牌主题。

- [ ] **步骤 4：在设置抽屉展示品牌主题当前值**

新增只读说明或复用 `ThemeSwitcher`：

```vue
<div class="drawer-item">
  <span>品牌主题</span>
  <theme-switcher />
</div>
```

- [ ] **步骤 5：验证设置抽屉**

浏览器验证：

- 设置抽屉不再展示「混合菜单」「顶部菜单」作为主要能力。
- 设置抽屉可以切换 A/B/C 或清晰提示「请使用右上角主题切换」。
- 保存配置后 `brandTheme` 不丢失。

### 任务 9：硬编码色值清理

**问题：** `manzhushaka.scss`、错误页、部分组件仍存在大量硬编码色值，会在 A/C 主题下产生旧视觉混搭。

**文件：**

- 修改：`ui-admin/src/assets/styles/manzhushaka.scss`
- 修改：`ui-admin/src/components/Breadcrumb/index.vue`
- 修改：`ui-admin/src/layout/components/TopNav/index.vue`
- 修改：`ui-admin/src/layout/components/TopBar/index.vue`
- 修改：`ui-admin/src/views/error/401.vue`
- 修改：`ui-admin/src/views/error/404.vue`

- [ ] **步骤 1：统计待清理硬编码色值**

运行：

```bash
rg -n "#[0-9a-fA-F]{3,8}|rgba\\(|rgb\\(|linear-gradient\\(" ui-admin/src/assets/styles ui-admin/src/components ui-admin/src/layout ui-admin/src/views -g "*.scss" -g "*.vue"
```

将结果分类：

- 主题 token 定义允许保留。
- 登录页固定 B 主题允许保留。
- 错误页可保留少量品牌色，但应映射到 `--ui-primary`。
- 业务详情样式应改为 `--ui-*`。

- [ ] **步骤 2：替换详情页和通用样式色值**

示例替换：

```scss
color: #303133;
```

改为：

```scss
color: var(--ui-text-primary);
```

示例替换：

```scss
border: 1px solid #ebeef5;
```

改为：

```scss
border: 1px solid var(--ui-border);
```

- [ ] **步骤 3：错误页接入主题 token**

错误页主按钮：

```scss
background: var(--ui-primary);
color: var(--ui-text-inverse);
```

- [ ] **步骤 4：验证深色主题可读性**

在 `amber-command` 下检查：

- 面包屑文字对比清晰。
- 详情页卡片不是白底。
- 错误页按钮仍可见。
- 顶部菜单不出现黑字深底。

### 任务 10：增强 `ThemeSwitcher` 可访问性和交互稳定性

**问题：** 主题色块按钮缺少 `type="button"` 和独立 `aria-label`，嵌套 tooltip 结构也偏复杂。

**文件：**

- 修改：`ui-admin/src/components/ThemeSwitcher/index.vue`

- [ ] **步骤 1：按钮补齐类型和可访问标签**

```vue
<button
  v-for="theme in themes"
  :key="theme.id"
  type="button"
  class="theme-swatch"
  :class="{ active: settingsStore.brandTheme === theme.id }"
  :aria-label="`切换到${theme.label}`"
  :aria-pressed="settingsStore.brandTheme === theme.id"
  :style="{ background: theme.gradient }"
  @click="switchTheme(theme.id)"
>
  <span class="theme-swatch-inner"></span>
</button>
```

- [ ] **步骤 2：Tooltip 改为单按钮包裹**

避免外层「主题切换」tooltip 和内层主题名 tooltip 同时触发。

```vue
<el-tooltip
  v-for="theme in themes"
  :key="theme.id"
  :content="theme.label"
  effect="dark"
  placement="bottom"
>
  <button ... />
</el-tooltip>
```

- [ ] **步骤 3：键盘操作验证**

浏览器验证：

- Tab 可以聚焦到每个主题色块。
- Enter 或 Space 可以触发主题切换。
- 当前主题 `aria-pressed` 为 `true`。

### 任务 11：处理构建环境变量提示

**问题：** `npm run build:prod` 提示 `%VITE_APP_TITLE% is not defined in /index.html`。

**文件：**

- 修改：`ui-admin/index.html`
- 修改：`ui-admin/.env.development`
- 修改：`ui-admin/.env.production`
- 修改：`ui-admin/.env.staging`

- [ ] **步骤 1：确认 Vite HTML 变量写法**

当前：

```html
<title>%VITE_APP_TITLE%</title>
```

如果项目环境文件已有 `VITE_APP_TITLE`，保持该写法即可；如果环境文件缺失，需要补充。

- [ ] **步骤 2：补齐环境变量**

在每个环境文件加入：

```env
VITE_APP_TITLE=满招科技后台管理系统
```

- [ ] **步骤 3：构建验证**

运行：

```bash
cd ui-admin
npm run build:prod
```

预期：

- exit 0。
- 不再出现 `%VITE_APP_TITLE% is not defined`。

### 任务 12：制定 `color-mix()` 兼容策略

**问题：** 当前主题大量使用 `color-mix()`。如果目标浏览器包含低版本内核，需要静态 fallback。

**文件：**

- 修改：`ui-admin/src/assets/styles/theme-tokens.scss`

- [ ] **步骤 1：增加静态 light token**

每个主题补齐：

```scss
--ui-primary-light-3: #38bdf8;
--ui-primary-light-5: #7dd3fc;
--ui-primary-light-7: #bae6fd;
--ui-primary-light-9: var(--ui-primary-soft);
```

不同主题使用对应静态值：

```scss
[data-ui-theme='amber-command'] {
  --ui-primary-light-3: #ffc878;
  --ui-primary-light-5: #ffd99e;
  --ui-primary-light-7: #ffe9c7;
  --ui-primary-light-9: var(--ui-primary-soft);
}

[data-ui-theme='gold-ledger'] {
  --ui-primary-light-3: #d9a85d;
  --ui-primary-light-5: #e8c68e;
  --ui-primary-light-7: #f3ddb9;
  --ui-primary-light-9: var(--ui-primary-soft);
}
```

- [ ] **步骤 2：Element Plus light 变量优先使用静态 token**

```scss
[data-ui-theme] {
  --el-color-primary-light-3: var(--ui-primary-light-3);
  --el-color-primary-light-5: var(--ui-primary-light-5);
  --el-color-primary-light-7: var(--ui-primary-light-7);
  --el-color-primary-light-9: var(--ui-primary-light-9);
}
```

- [ ] **步骤 3：保留 `color-mix()` 作为增强效果**

对非关键装饰效果可继续使用 `color-mix()`，关键可读性颜色必须有静态 token。

- [ ] **步骤 4：浏览器验证**

使用目标浏览器检查：

- 主按钮 hover 可见。
- 表格 hover 可见。
- 标签背景可见。
- A 主题下弹层边框和文字对比清晰。

## 5. 验收清单

### 5.1 命令验收

- [ ] 运行构建：

```bash
cd ui-admin
npm run build:prod
```

预期：exit 0，且不出现 `%VITE_APP_TITLE% is not defined`。

- [ ] 统计页面迁移覆盖：

```bash
rg -l "ui-filter-card|ui-action-bar|ui-table-card" ui-admin/src/views -g "*.vue" | sort
```

预期：至少覆盖用户、角色、菜单、通知、部门、岗位、字典、参数、操作日志、登录日志、在线用户、定时任务。

- [ ] 检查全局样式误用：

```bash
rg -n ":deep\\(" ui-admin/src/assets/styles
```

预期：无输出。

### 5.2 主题验收

- [ ] 默认登录后主题为 `cool-tower`。
- [ ] 切换 `amber-command` 后刷新仍保持。
- [ ] 切换 `gold-ledger` 后打开设置抽屉保存，刷新仍保持。
- [ ] 登录页始终为 `cool-tower`。
- [ ] A 主题下表格、弹窗、抽屉、Tooltip 不出现白底黑字割裂。
- [ ] C 主题下危险、警告、成功语义色仍可辨识。

### 5.3 典型页面验收

- [ ] 首页为工作台，不展示若依介绍内容。
- [ ] 用户管理符合「树侧栏 + 过滤卡 + 工具条 + 表格卡」。
- [ ] 角色管理的数据权限抽屉跟随主题。
- [ ] 菜单管理抽屉跟随主题，树表格可读。
- [ ] 通知公告富文本弹窗不破坏主题背景。
- [ ] 服务监控卡片、表格和异常红色跟随 token。
- [ ] 操作日志和登录日志列表不出现旧视觉混搭。

### 5.4 响应式验收

检查视口：

- [ ] `1440 x 900`
- [ ] `1280 x 720`
- [ ] `1024 x 768`
- [ ] `390 x 844`

移动端必须确认：

- [ ] 登录页不横向溢出。
- [ ] 主布局侧边栏可收起或抽屉化。
- [ ] 顶栏保留菜单、主题切换、用户入口。
- [ ] 表格页面横向滚动可访问操作列。
- [ ] 弹窗宽度不超过 `calc(100vw - 24px)`。
- [ ] 抽屉宽度为 `100vw`。

## 6. 建议执行顺序

1. 任务 1：修复 `brandTheme` 持久化。
2. 任务 4：修复全局 `:deep()`。
3. 任务 2：收敛旧主题入口。
4. 任务 3：恢复移动端主题切换。
5. 任务 5：完善组件级参数覆盖。
6. 任务 6：迁移高频列表页。
7. 任务 7：首页硬编码 token 化。
8. 任务 8：设置抽屉收口。
9. 任务 9：硬编码色值清理。
10. 任务 10：增强 `ThemeSwitcher`。
11. 任务 11：处理构建环境变量提示。
12. 任务 12：制定 `color-mix()` 兼容策略。

## 7. 完成标准

当以下条件全部满足时，本修复计划可以视为完成：

- `npm run build:prod` 通过且无标题环境变量警告。
- `brandTheme` 在切换、刷新、保存布局配置、重新登录后保持。
- A/B/C 主题影响侧边栏、顶栏、TagsView、按钮、表格、分页、弹窗、抽屉、树侧栏、首页 KPI。
- 高频列表页统一为「过滤卡 + 工具条 + 数据区」。
- 登录页固定 B 主题。
- 移动端保留主题切换入口。
- 全局样式不再在非 scoped SCSS 中使用 `:deep()`。
- 典型页面不存在明显新旧视觉混搭。
