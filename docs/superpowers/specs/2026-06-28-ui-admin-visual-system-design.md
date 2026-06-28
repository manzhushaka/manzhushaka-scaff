# ui-admin 全站视觉系统升级规格

## 1. 背景

`ui-admin` 当前基于 RuoYi-Vue3、Vue 3、Element Plus 和 Vite 构建。现有界面仍保留较多若依默认后台特征：布局能力完整，但品牌识别弱；首页偏项目介绍，不像日常工作台；主题能力主要集中在主色、侧边栏深浅、暗黑模式，缺少完整的品牌主题系统。

本次目标不是做单页美化，而是重塑后台产品的视觉系统和主布局框架。后续实现应以本文档为依据，分阶段落地。

## 2. 设计结论

### 2.1 总体方向

采用「统一主布局 + 三套品牌主题」方案。

- 默认主题使用 `B 冷感控制塔`。
- `A 琥珀指挥舱` 和 `C 米金账本台` 作为可切换主题。
- 主题切换入口放在登录后页面的右上角。
- 登录页固定使用 `B 冷感控制塔`，不跟随登录后主题切换。
- 后台登录后页面统一为一套主布局，不再强化当前多种导航模式并行的产品形态。

### 2.2 主布局框架

统一后的后台骨架为：

- 固定左侧导航：承载 Logo、主菜单、折叠态和当前模块状态。
- 顶部操作区：承载面包屑或全局搜索、消息、全屏、用户入口、主题切换。
- 标签页区：保留多页签能力，但视觉上融入新主题体系。
- 内容画布：承载首页工作台、列表页、表单页、详情页、监控页等业务页面。

当前 `navType` 的多导航分支不再作为主要产品能力。实现时可以分阶段收敛，优先让默认布局稳定运行，再逐步弱化混合菜单和顶部菜单配置。

## 3. 设计系统与主题参数规格

### 3.1 主题定义

| 主题 | 用途 | 气质 | 默认状态 |
| --- | --- | --- | --- |
| `B 冷感控制塔` | 默认主题 | 现代 SaaS、雾蓝玻璃感、清爽专业 | 默认启用 |
| `A 琥珀指挥舱` | 可选主题 | 深色控制台、运行态、监控感 | 用户切换启用 |
| `C 米金账本台` | 可选主题 | 暖白纸感、铜金点缀、商务稳重 | 用户切换启用 |

### 3.2 主题影响范围

主题切换应影响：

- 侧边栏背景、菜单文字、激活态、悬停态。
- 顶栏背景、图标按钮、主题切换器、用户入口。
- 标签页背景、激活态、关闭按钮、右键菜单。
- 主按钮、链接、状态标签、开关激活色。
- 卡片、表格、查询区、抽屉、弹窗的背景、边框、阴影和重点色。
- KPI、图表、空状态、提示块和业务高亮色。

主题切换不应影响：

- 导航位置和信息架构。
- 表格列结构、表单字段顺序、交互路径。
- 用户权限、路由结构、API 调用方式。
- 登录页视觉主题。

### 3.3 状态持久化

主题选择需要持久化到本地配置，优先复用现有 `layout-setting` 存储方式。建议新增 `brandTheme` 字段，取值为：

- `cool-tower`
- `amber-command`
- `gold-ledger`

`theme` 主色字段可保留兼容，但后续不再作为品牌主题的唯一来源。

### 3.4 根节点与 CSS 变量契约

实现时应在应用根节点挂载主题属性：

```html
<div id="app" data-ui-theme="cool-tower">
  ...
</div>
```

建议在 `App.vue` 中从 `settings` store 读取 `brandTheme`，并将其绑定到根容器。主题样式统一使用 `--ui-*` 变量，不建议继续把业务样式直接写死为某个色值。

```vue
<template>
  <div :data-ui-theme="settingsStore.brandTheme">
    <router-view />
  </div>
</template>
```

全局 token 文件建议新增为：

```text
ui-admin/src/assets/styles/theme-tokens.scss
```

并在 `ui-admin/src/assets/styles/index.scss` 中引入：

```scss
@use './theme-tokens.scss';
```

基础变量命名约定：

| 变量 | 用途 |
| --- | --- |
| `--ui-bg-page` | 页面底色 |
| `--ui-bg-panel` | 面板、卡片、表格容器背景 |
| `--ui-bg-panel-soft` | 查询区、弱强调区域背景 |
| `--ui-bg-sidebar` | 侧边栏背景 |
| `--ui-bg-sidebar-deep` | 侧边栏深层背景、子菜单背景 |
| `--ui-bg-topbar` | 顶栏背景 |
| `--ui-text-primary` | 主文本 |
| `--ui-text-regular` | 正文文本 |
| `--ui-text-secondary` | 辅助文本 |
| `--ui-text-inverse` | 深色背景上的文本 |
| `--ui-border` | 常规边框 |
| `--ui-border-strong` | 强边框 |
| `--ui-primary` | 主品牌色 |
| `--ui-primary-hover` | 主品牌色 hover |
| `--ui-primary-active` | 主品牌色 active |
| `--ui-primary-soft` | 主品牌弱底色 |
| `--ui-accent` | 装饰和图表强调色 |
| `--ui-success` | 成功语义色 |
| `--ui-warning` | 警告语义色 |
| `--ui-danger` | 危险语义色 |
| `--ui-info` | 信息语义色 |
| `--ui-shadow-panel` | 面板阴影 |
| `--ui-shadow-popover` | 弹层阴影 |
| `--ui-radius-control` | 输入框、按钮、选择器圆角 |
| `--ui-radius-panel` | 卡片、表格容器、查询区圆角 |
| `--ui-radius-popover` | 弹窗、抽屉、下拉菜单圆角 |
| `--ui-layout-sidebar-width` | 展开侧边栏宽度 |
| `--ui-layout-sidebar-collapse-width` | 折叠侧边栏宽度 |
| `--ui-layout-topbar-height` | 顶栏高度 |
| `--ui-layout-tags-height` | 标签页高度 |
| `--ui-layout-content-padding` | 内容区外边距 |

工程参数统一如下。视觉原型中的大圆角只用于方向演示，正式实现以此表为准。

| 参数 | 值 | 说明 |
| --- | --- | --- |
| `--ui-layout-sidebar-width` | `224px` | 比当前 `200px` 略宽，容纳中文菜单和品牌区 |
| `--ui-layout-sidebar-collapse-width` | `64px` | 折叠菜单宽度 |
| `--ui-layout-topbar-height` | `60px` | 顶栏高度 |
| `--ui-layout-tags-height` | `42px` | 标签页高度 |
| `--ui-layout-content-padding` | `18px` | 默认内容外边距，窄屏降为 `12px` |
| `--ui-radius-control` | `6px` | 表单控件、按钮 |
| `--ui-radius-panel` | `8px` | 卡片、查询区、表格容器 |
| `--ui-radius-popover` | `8px` | 弹窗、抽屉、下拉 |
| `--ui-table-row-height` | `48px` | 常规表格行高 |
| `--ui-form-control-height` | `36px` | 常规输入控件高度 |
| `--ui-transition-fast` | `160ms ease` | hover、激活态 |
| `--ui-transition-normal` | `240ms ease` | 侧边栏、抽屉、页签 |

### 3.5 三套主题精确参数

#### 3.5.1 `B 冷感控制塔`（默认）

```scss
[data-ui-theme='cool-tower'] {
  --ui-bg-page: #f4f8fb;
  --ui-bg-panel: #ffffff;
  --ui-bg-panel-soft: #edf6fb;
  --ui-bg-sidebar: #0f3b60;
  --ui-bg-sidebar-deep: #12202f;
  --ui-bg-topbar: rgba(255, 255, 255, 0.86);

  --ui-text-primary: #0f172a;
  --ui-text-regular: #334155;
  --ui-text-secondary: #64748b;
  --ui-text-inverse: #eaf8ff;

  --ui-border: #d8e6ef;
  --ui-border-strong: #bfd7e6;

  --ui-primary: #0ea5e9;
  --ui-primary-hover: #0284c7;
  --ui-primary-active: #0369a1;
  --ui-primary-soft: #e0f2fe;
  --ui-accent: #7dd3fc;

  --ui-success: #10b981;
  --ui-warning: #f59e0b;
  --ui-danger: #ef4444;
  --ui-info: #64748b;

  --ui-shadow-panel: 0 12px 28px rgba(15, 59, 96, 0.10);
  --ui-shadow-popover: 0 18px 40px rgba(15, 59, 96, 0.14);
}
```

适用规则：

- 侧边栏使用深蓝，内容区保持浅色。
- 主按钮、激活页签、菜单选中态使用 `--ui-primary`。
- 图表默认使用 `#0ea5e9`、`#7dd3fc`、`#10b981`、`#f59e0b`、`#ef4444`。
- 登录页固定使用本主题，不读取用户切换后的 `brandTheme`。

#### 3.5.2 `A 琥珀指挥舱`

```scss
[data-ui-theme='amber-command'] {
  --ui-bg-page: #0e1218;
  --ui-bg-panel: #151b24;
  --ui-bg-panel-soft: #1e2633;
  --ui-bg-sidebar: #0a0d12;
  --ui-bg-sidebar-deep: #111720;
  --ui-bg-topbar: rgba(21, 27, 36, 0.90);

  --ui-text-primary: #f8fafc;
  --ui-text-regular: #cbd5e1;
  --ui-text-secondary: #94a3b8;
  --ui-text-inverse: #101418;

  --ui-border: rgba(255, 255, 255, 0.10);
  --ui-border-strong: rgba(255, 255, 255, 0.18);

  --ui-primary: #ffb74d;
  --ui-primary-hover: #f59e0b;
  --ui-primary-active: #d97706;
  --ui-primary-soft: rgba(255, 183, 77, 0.16);
  --ui-accent: #ffd271;

  --ui-success: #22c55e;
  --ui-warning: #f59e0b;
  --ui-danger: #f87171;
  --ui-info: #94a3b8;

  --ui-shadow-panel: 0 16px 36px rgba(0, 0, 0, 0.30);
  --ui-shadow-popover: 0 22px 48px rgba(0, 0, 0, 0.42);
}
```

适用规则：

- A 是完整深色主题，不叠加现有 `html.dark` 的变量。
- 表格底色必须使用 `--ui-bg-panel`，表格边框使用 `--ui-border`，避免黑底灰字不可读。
- 危险、成功、警告等语义色保留辨识度，不能全部琥珀化。

#### 3.5.3 `C 米金账本台`

```scss
[data-ui-theme='gold-ledger'] {
  --ui-bg-page: #f7f1e6;
  --ui-bg-panel: #fffdf8;
  --ui-bg-panel-soft: #f2e6d4;
  --ui-bg-sidebar: #f4e9d8;
  --ui-bg-sidebar-deep: #ead8bd;
  --ui-bg-topbar: rgba(255, 253, 248, 0.90);

  --ui-text-primary: #2f2418;
  --ui-text-regular: #5c4630;
  --ui-text-secondary: #80664b;
  --ui-text-inverse: #fffaf0;

  --ui-border: #e7d6bd;
  --ui-border-strong: #d5bc93;

  --ui-primary: #c88b3a;
  --ui-primary-hover: #a96f24;
  --ui-primary-active: #875519;
  --ui-primary-soft: #f7e4c3;
  --ui-accent: #f3c67b;

  --ui-success: #15803d;
  --ui-warning: #b7791f;
  --ui-danger: #dc2626;
  --ui-info: #80664b;

  --ui-shadow-panel: 0 12px 28px rgba(108, 78, 38, 0.12);
  --ui-shadow-popover: 0 18px 40px rgba(108, 78, 38, 0.18);
}
```

适用规则：

- C 是浅色暖调主题，不能做成全站米黄色糊成一片。
- 表格、查询区、抽屉仍要以 `--ui-bg-panel` 为主，`--ui-bg-panel-soft` 只用于弱分区。
- 告警和危险色不能改成金色，必须保留红色语义。

### 3.6 Element Plus 变量映射

在 `theme-tokens.scss` 中统一覆盖 Element Plus CSS 变量：

```scss
[data-ui-theme] {
  --el-color-primary: var(--ui-primary);
  --el-color-primary-light-3: color-mix(in srgb, var(--ui-primary) 70%, #ffffff 30%);
  --el-color-primary-light-5: color-mix(in srgb, var(--ui-primary) 45%, #ffffff 55%);
  --el-color-primary-light-7: color-mix(in srgb, var(--ui-primary) 24%, #ffffff 76%);
  --el-color-primary-light-9: var(--ui-primary-soft);
  --el-color-primary-dark-2: var(--ui-primary-active);

  --el-color-success: var(--ui-success);
  --el-color-warning: var(--ui-warning);
  --el-color-danger: var(--ui-danger);
  --el-color-info: var(--ui-info);

  --el-bg-color: var(--ui-bg-panel);
  --el-bg-color-page: var(--ui-bg-page);
  --el-bg-color-overlay: var(--ui-bg-panel);

  --el-text-color-primary: var(--ui-text-primary);
  --el-text-color-regular: var(--ui-text-regular);
  --el-text-color-secondary: var(--ui-text-secondary);

  --el-border-color: var(--ui-border);
  --el-border-color-light: var(--ui-border);
  --el-border-color-lighter: color-mix(in srgb, var(--ui-border) 68%, transparent);

  --el-border-radius-base: var(--ui-radius-control);
  --el-fill-color-light: var(--ui-bg-panel-soft);
  --el-fill-color-blank: var(--ui-bg-panel);
}
```

如果浏览器兼容性要求不允许使用 `color-mix()`，实现时需要为每个主题补充静态 light 变量：

- `--ui-primary-light-3`
- `--ui-primary-light-5`
- `--ui-primary-light-7`
- `--ui-primary-light-9`

### 3.7 最小可执行 SCSS 示例

以下代码可以作为 `theme-tokens.scss` 的初始骨架：

```scss
:root {
  --ui-layout-sidebar-width: 224px;
  --ui-layout-sidebar-collapse-width: 64px;
  --ui-layout-topbar-height: 60px;
  --ui-layout-tags-height: 42px;
  --ui-layout-content-padding: 18px;

  --ui-radius-control: 6px;
  --ui-radius-panel: 8px;
  --ui-radius-popover: 8px;
  --ui-table-row-height: 48px;
  --ui-form-control-height: 36px;
  --ui-transition-fast: 160ms ease;
  --ui-transition-normal: 240ms ease;
}

[data-ui-theme] {
  color: var(--ui-text-primary);
  background: var(--ui-bg-page);
}

.app-container {
  min-height: 100%;
  padding: var(--ui-layout-content-padding);
  background: var(--ui-bg-page);
}

.ui-filter-card,
.ui-table-card,
.ui-panel-card {
  border: 1px solid var(--ui-border);
  border-radius: var(--ui-radius-panel);
  background: var(--ui-bg-panel);
  box-shadow: var(--ui-shadow-panel);
}

.ui-filter-card {
  padding: 16px;
  background: var(--ui-bg-panel-soft);
}

.el-button--primary {
  --el-button-bg-color: var(--ui-primary);
  --el-button-border-color: var(--ui-primary);
  --el-button-hover-bg-color: var(--ui-primary-hover);
  --el-button-hover-border-color: var(--ui-primary-hover);
  --el-button-active-bg-color: var(--ui-primary-active);
  --el-button-active-border-color: var(--ui-primary-active);
}

@media (max-width: 991px) {
  :root {
    --ui-layout-content-padding: 12px;
  }
}
```

## 4. 关键页面规格

### 4.1 登录页

登录页固定使用 `B 冷感控制塔`。

设计要求：

- 使用冷感雾蓝背景，避免传统大图铺满造成的旧后台感。
- 登录卡片采用轻玻璃质感，但保证输入区域清晰可读。
- 保留账号、密码、验证码、记住密码、注册入口和版权信息。
- 登录行为、验证码刷新、Cookie 记忆逻辑不变。
- 移动端居中展示，表单宽度和按钮文字不能溢出。

### 4.2 首页工作台

首页从默认介绍页升级为真正的工作台。

建议内容结构：

- 欢迎区：展示系统名、当前用户、关键动作入口。
- 核心指标：展示用户数、角色数、任务数、在线用户或系统运行状态。
- 待办或提醒：展示通知公告、待处理任务、异常日志摘要。
- 最近动态：展示登录日志、操作日志或系统更新。
- 快捷入口：链接到用户管理、角色管理、菜单管理、服务监控等高频功能。

首页不再作为 RuoYi 项目介绍页，也不承载大量开源说明、QQ群、版本长日志等内容。

## 5. 业务页面公共体系

### 5.1 列表页

适用于用户管理、角色管理、字典管理、参数管理、通知公告、日志列表等页面。

统一结构：

- 过滤卡：承载查询表单，支持展开、收起、重置和搜索。
- 工具条：承载新增、修改、删除、导入、导出、刷新、列显隐等操作。
- 数据区：承载表格、分页、加载态、空状态、批量选择反馈。

要求：

- 查询区不再以松散表单直接铺在页面顶部，应统一成视觉完整的过滤卡。
- 工具按钮保持权限指令和业务行为不变。
- 表格密度应服务后台效率，不能因为视觉升级牺牲可扫描性。
- `RightToolbar` 应纳入新工具条视觉体系。

### 5.2 表单、弹窗和抽屉

统一原则：

- 简单新增、编辑可以保留弹窗语义，但视觉风格要与主题一致。
- 复杂编辑、详情查看、权限配置、角色分配优先使用抽屉或双栏结构。
- 表单字段顺序、校验规则、提交 API 不变。
- 详情页应强化信息分组，减少纯字段堆叠。

### 5.3 树侧栏复合页

适用于用户管理、部门管理、菜单管理等带树结构的页面。

要求：

- 保留树侧栏能力和拖拽宽度能力。
- 树侧栏视觉跟随主题 token。
- 主内容区继续使用列表页体系。
- 折叠、刷新、搜索行为不变。

## 6. 实施边界

### 6.1 本次应纳入

- 统一主布局框架。
- 三套品牌主题 token。
- 组件级参数规范。
- 右上角主题切换入口。
- 登录页 `B` 主题重做。
- 首页工作台重做。
- 全局样式、Element Plus 覆盖、侧边栏、顶栏、标签页。
- 列表页、查询区、工具条、表格、分页、弹窗、抽屉、树侧栏的公共视觉体系。
- 典型页面验证：用户管理、角色管理、菜单管理、通知公告、服务监控、登录页、首页。

### 6.2 本次不纳入

- 后端接口改造。
- 权限模型、菜单数据结构、路由协议改造。
- 业务字段、表格列、表单校验规则的功能性调整。
- 暗黑模式的完整重构。A 主题已经承担深色控制台气质，现有暗黑模式可先保持兼容。
- 大规模组件库替换。
- TypeScript 迁移。
- 国际化重构。

### 6.3 兼容约束

- 不破坏现有登录流程、验证码、Token、权限指令。
- 不破坏现有 `layout-setting` 的读取。旧配置缺少 `brandTheme` 时，默认落到 `cool-tower`。
- 不破坏现有 `tags-view-visited` 持久化数据。
- 保持移动端可用，至少保证登录页、主布局、典型列表页不出现横向溢出。

## 7. 迁移策略

### 7.1 阶段一：主题 token 与主布局基础

目标：先建立新视觉系统的地基。

工作内容：

- 新增品牌主题 token 文件或模块。
- 新增组件级公共样式和参数变量。
- 在 `settings` store 中新增 `brandTheme` 状态。
- 在应用根节点挂载主题 class 或 data attribute。
- 将默认布局固定到统一主布局。
- 实现右上角主题切换器。
- 保留旧 `theme` 主色逻辑的兼容路径。

验收重点：

- 默认进入后台时使用 `B 冷感控制塔`。
- 切换 `A / B / C` 后刷新页面仍能保持选择。
- 现有权限菜单、用户入口、标签页、锁屏、全屏、消息入口可用。

### 7.2 阶段二：登录页与首页

目标：先让入口和第一屏完成品牌升级。

工作内容：

- 重做 `login.vue` 视觉结构。
- 重做 `views/index.vue` 为工作台。
- 首页数据优先使用已有可获得的信息；若接口不足，先做静态占位和高频入口，不新增后端依赖。

验收重点：

- 登录页功能不回归。
- 首页不再展示默认若依介绍内容。
- 桌面端和移动端首屏布局稳定。

### 7.3 阶段三：全局框架与公共组件

目标：让大多数业务页面自动吃到新视觉体系。

工作内容：

- 升级 `Navbar`、`Sidebar`、`TagsView`、`AppMain`。
- 升级全局 `index.scss`、`element-ui.scss`、`sidebar.scss`。
- 升级 `RightToolbar`、分页、弹窗、抽屉、表格、表单样式覆盖。
- 按第 10 章逐项落实组件参数，不允许只做局部色值替换。
- 统一 `.app-container`、查询表单、操作条和树侧栏容器样式。

验收重点：

- 用户管理、角色管理、字典管理等列表页整体风格一致。
- 查询区、操作条、表格、分页之间的间距稳定。
- 弹窗、抽屉、树侧栏在三套主题下都可读。

### 7.4 阶段四：典型页面微调与回归

目标：处理全局样式无法完全覆盖的页面。

工作内容：

- 针对用户管理树侧栏页微调。
- 针对角色权限树、菜单管理、监控页、服务监控页微调。
- 检查长文本、宽表格、小屏幕、空状态、加载态。

验收重点：

- 不出现明显的新旧视觉混搭。
- 不出现文字溢出、按钮错位、表格操作区拥挤。
- 三套主题下典型页面都可正常操作。

## 8. 验收方式

### 8.1 功能验收

必须验证：

- 登录成功、登录失败、验证码刷新。
- 主题切换、刷新保持、重新登录保持。
- 侧边栏折叠和展开。
- 标签页新增、关闭、刷新、关闭其他、全屏显示。
- 用户管理列表查询、重置、新增弹窗、编辑弹窗、删除确认。
- 角色管理列表查询、数据权限弹窗、分配用户入口。
- 树侧栏页面的搜索、节点点击、刷新。

### 8.2 视觉验收

必须检查：

- 默认主题为 `B 冷感控制塔`。
- `A / B / C` 主题在后台内部切换后，侧边栏、顶栏、标签页、按钮、表格、弹窗、抽屉都跟随变化。
- 登录页始终保持 `B` 主题。
- 首页为工作台形态，不再展示若依默认介绍内容。
- 典型列表页符合「过滤卡 + 工具条 + 数据区」结构。
- 按第 10 章抽查按钮、输入框、表格、弹窗、抽屉、分页、标签、树侧栏等组件参数。
- 业务页面没有明显新旧视觉混搭。

### 8.3 响应式验收

建议检查视口：

- 桌面：`1440 x 900`
- 小桌面：`1280 x 720`
- 平板宽度：`1024 x 768`
- 手机宽度：`390 x 844`

必须确认：

- 登录页不溢出。
- 主布局在移动端侧边栏可收起或以抽屉形式出现。
- 表格页面在小屏幕下不破坏操作入口。
- 右上角主题切换器不会遮挡用户菜单和消息入口。

### 8.4 技术验收

必须运行：

```bash
cd ui-admin
npm run build:prod
```

建议运行：

```bash
cd ui-admin
npm run dev
```

并使用浏览器手动验证：

- 登录页。
- 首页。
- 用户管理。
- 角色管理。
- 菜单管理。
- 服务监控。

### 8.5 回归风险清单

实现和验收时重点关注：

- Element Plus 组件样式覆盖过宽，影响不可预期页面。
- 旧 `layout-setting` 与新 `brandTheme` 兼容失败。
- 顶栏右侧操作区在小屏幕拥挤。
- 深色 A 主题下表格、弹窗、Tooltip 对比度不足。
- 暖色 C 主题下告警、危险、成功等语义色辨识度下降。
- 标签页全屏功能隐藏元素后恢复异常。
- 树侧栏拖拽宽度与新布局容器冲突。

## 9. 后续实现建议

建议后续按以下顺序创建实现计划：

1. 主题 token、组件参数和设置状态。
2. 主布局、顶栏、侧边栏、标签页。
3. 登录页和首页。
4. 列表页、表格、查询区、工具条公共样式。
5. 弹窗、抽屉、树侧栏和典型页面微调。
6. 全量构建、浏览器验收和回归修复。

每一阶段都应保持可运行、可回退，避免一次性改完整站后才验证。

## 10. 组件级参数规范

本章是后续实现的硬约束。除非页面有明确业务原因，否则所有后台页面都应优先使用本章参数，不允许每个页面自行定义圆角、阴影、按钮尺寸、表格间距和状态颜色。

### 10.1 布局容器

| 组件 | 选择器建议 | 参数 |
| --- | --- | --- |
| 应用根容器 | `[data-ui-theme]` | `min-height: 100vh`；背景 `--ui-bg-page`；文本 `--ui-text-primary` |
| 主外壳 | `.app-wrapper` | 高度 `100%`；背景 `--ui-bg-page`；不额外加卡片阴影 |
| 主内容容器 | `.main-container` | 左边距 `--ui-layout-sidebar-width`；折叠时 `--ui-layout-sidebar-collapse-width`；过渡 `--ui-transition-normal` |
| 页面内容区 | `.app-main` | 背景 `--ui-bg-page`；固定头部时内部滚动；不设置额外横向滚动 |
| 标准页面容器 | `.app-container` | padding `--ui-layout-content-padding`；背景 `transparent`；最小高度 `100%` |
| 业务内容内层 | `.content-inner` | 宽度 `100%`；间距由内部组件控制，不额外包卡片 |

实现规则：

- 页面级结构不使用大卡片套小卡片。
- `.app-container` 只负责页面留白，不负责视觉分组。
- 视觉分组交给 `.ui-filter-card`、`.ui-table-card`、`.ui-panel-card`。
- 移动端 `max-width: 991px` 时，内容 padding 改为 `12px`。

### 10.2 左侧导航

| 部位 | 参数 |
| --- | --- |
| 宽度 | 展开 `224px`；折叠 `64px` |
| 背景 | `--ui-bg-sidebar` |
| 子菜单背景 | `--ui-bg-sidebar-deep` |
| 边框 | 右侧 `1px solid color-mix(in srgb, var(--ui-border) 72%, transparent)` |
| 阴影 | 默认无阴影；A 主题可使用 `--ui-shadow-panel` |
| Logo 区高度 | `60px` |
| Logo 图标 | `32px x 32px`；圆角 `8px` |
| 品牌文字 | 14px / 20px，字重 600；折叠时隐藏 |
| 菜单项高度 | `44px` |
| 菜单项左右 padding | 展开 `14px`；折叠居中 |
| 菜单项圆角 | `6px` |
| 菜单项间距 | `4px` |
| 菜单图标 | `16px x 16px`；右间距 `10px` |
| 菜单文字 | 14px / 20px；单行省略 |
| 激活态背景 | `--ui-primary-soft`；A 主题使用 `rgba(255, 183, 77, 0.16)` |
| 激活态文字 | `--ui-primary`；深色侧栏下使用 `--ui-text-inverse` 加左侧强调线 |
| 激活强调线 | 宽 `3px`；高 `20px`；圆角 `999px`；颜色 `--ui-primary` |
| hover 背景 | `color-mix(in srgb, var(--ui-primary-soft) 70%, transparent)` |

落地规则：

- 保留现有 `Sidebar/index.vue`、`SidebarItem.vue` 结构，优先通过 SCSS 覆盖。
- `theme-dark` / `theme-light` 后续只作为兼容 class，不作为新视觉主入口。
- 折叠态只显示图标，不显示 Tooltip 以外的文本。
- 菜单项不得使用超过 `8px` 的圆角。

### 10.3 顶部导航栏

| 部位 | 参数 |
| --- | --- |
| 高度 | `60px` |
| 背景 | `--ui-bg-topbar` |
| backdrop | `blur(14px)`；不支持时退化为 `--ui-bg-panel` |
| 下边框 | `1px solid var(--ui-border)` |
| 左侧折叠按钮 | `40px x 40px`；圆角 `6px` |
| 面包屑 | 13px / 20px；当前项 `--ui-text-primary`；历史项 `--ui-text-secondary` |
| 顶栏图标按钮 | `36px x 36px`；圆角 `6px`；图标 `18px` |
| 图标按钮 hover | 背景 `--ui-primary-soft`；图标色 `--ui-primary` |
| 用户头像 | `32px x 32px`；圆形 |
| 用户昵称 | 13px / 20px；最大宽度 `96px`；单行省略 |
| 右侧操作间距 | `6px` |

落地规则：

- 主题切换器放在消息、全屏、布局大小、用户菜单之前，属于常用操作。
- 顶栏不承载大段说明文字。
- `navType == 3` 的纯顶部菜单后续不作为默认能力展示。

### 10.4 主题切换器

| 部位 | 参数 |
| --- | --- |
| 容器尺寸 | 高 `36px`；padding `4px` |
| 容器背景 | `--ui-bg-panel` |
| 容器边框 | `1px solid var(--ui-border)` |
| 容器圆角 | `8px` |
| 色块尺寸 | `26px x 26px` |
| 色块圆角 | `6px` |
| 色块间距 | `4px` |
| 激活态 | 外描边 `2px solid var(--ui-primary)`；内阴影 `inset 0 0 0 1px rgba(255,255,255,0.5)` |
| hover | `transform: translateY(-1px)`；过渡 `--ui-transition-fast` |

色块参数：

| 主题 | 色块背景 |
| --- | --- |
| `cool-tower` | `linear-gradient(135deg, #0f3b60 0%, #0ea5e9 62%, #e0f2fe 100%)` |
| `amber-command` | `linear-gradient(135deg, #0e1218 0%, #ffb74d 100%)` |
| `gold-ledger` | `linear-gradient(135deg, #fffdf8 0%, #c88b3a 100%)` |

交互规则：

- 鼠标悬停显示 Tooltip，文案为主题中文名。
- 点击后立即切换 `brandTheme`，并写入 `layout-setting`。
- 当前主题按钮需要 `aria-pressed="true"`。
- 登录页不展示主题切换器。

### 10.5 标签页 `TagsView`

| 部位 | 参数 |
| --- | --- |
| 容器高度 | `42px` |
| 背景 | `--ui-bg-panel` |
| 下边框 | `1px solid var(--ui-border)` |
| 页签高度 | `30px` |
| 页签 padding | `0 10px` |
| 页签圆角 | `6px` |
| 页签间距 | `6px` |
| 页签文字 | 13px / 30px |
| 普通页签背景 | `transparent` |
| 普通页签边框 | `1px solid transparent` |
| hover 背景 | `--ui-primary-soft` |
| 激活页签背景 | `--ui-primary-soft` |
| 激活页签文字 | `--ui-primary` |
| 激活页签边框 | `1px solid color-mix(in srgb, var(--ui-primary) 28%, var(--ui-border))` |
| 关闭按钮 | `16px x 16px`；圆形；hover 背景 `--ui-border` |
| 左右滚动按钮 | `32px x 32px`；圆角 `6px` |
| 下拉操作按钮 | `32px x 32px`；圆角 `6px` |

落地规则：

- 保留现有右键菜单和全屏逻辑。
- `chrome` 页签风格可兼容，但新默认应统一为 `card`。
- 页签区不使用大阴影，避免压缩主内容空间。

### 10.6 按钮

| 类型 | 高度 | padding | 圆角 | 字号 | 图标 |
| --- | --- | --- | --- | --- | --- |
| 默认按钮 | `32px` | `0 12px` | `6px` | 13px | `16px` |
| 主要按钮 | `32px` | `0 14px` | `6px` | 13px | `16px` |
| 大按钮 | `36px` | `0 16px` | `6px` | 14px | `16px` |
| 小按钮 | `28px` | `0 10px` | `6px` | 12px | `14px` |
| 圆形图标按钮 | `32px x 32px` | `0` | `6px` | 不显示文字 | `16px` |
| 表格 link 按钮 | `28px` | `0 4px` | `4px` | 13px | `15px` |

状态参数：

| 状态 | 参数 |
| --- | --- |
| 默认 | 背景 `--ui-bg-panel`；边框 `--ui-border`；文字 `--ui-text-regular` |
| hover | 边框 `--ui-primary`；文字 `--ui-primary`；背景 `--ui-primary-soft` |
| primary | 背景和边框 `--ui-primary`；文字白色或 `--ui-text-inverse` |
| primary hover | 背景 `--ui-primary-hover` |
| primary active | 背景 `--ui-primary-active` |
| disabled | opacity `0.48`；禁止阴影和 transform |
| danger | 背景 `--ui-danger`；hover 使用加深色或 Element Plus danger hover |

落地规则：

- 工具栏按钮优先使用 `plain` 视觉，但颜色仍跟随语义。
- 单图标按钮必须有 Tooltip。
- 不新增无图标的圆形按钮。

### 10.7 表单控件

| 组件 | 参数 |
| --- | --- |
| `el-input` | 高 `36px`；圆角 `6px`；背景 `--ui-bg-panel`；边框 `--ui-border` |
| `el-select` | 高 `36px`；圆角 `6px`；宽度跟随业务，常用 `220px` |
| `el-date-picker` | 高 `36px`；日期范围宽 `300px`；圆角 `6px` |
| `el-input-number` | 高 `36px`；宽 `140px` |
| `el-textarea` | 最小高 `88px`；圆角 `6px`；行高 `1.6` |
| `el-switch` | 使用 Element Plus 默认尺寸；激活色 `--ui-primary` |
| `el-radio` / `el-checkbox` | 激活色 `--ui-primary`；文字 13px |
| 表单 label | 13px；颜色 `--ui-text-regular`；字重 500 |
| 表单 item 间距 | 横向 `16px`；纵向 `14px` |
| 错误提示 | 12px；颜色 `--ui-danger`；上间距 `4px` |

状态参数：

| 状态 | 参数 |
| --- | --- |
| focus | 边框 `--ui-primary`；阴影 `0 0 0 2px var(--ui-primary-soft)` |
| hover | 边框 `--ui-border-strong` |
| disabled | 背景 `color-mix(in srgb, var(--ui-bg-panel-soft) 72%, var(--ui-bg-panel))`；文字 `--ui-text-secondary` |
| readonly | 背景 `--ui-bg-panel-soft`；边框 `--ui-border` |

落地规则：

- 查询表单里的控件默认宽度 `220px`。
- 弹窗或抽屉表单按 `el-row` 两列布局时，列间距 `16px`。
- 移动端表单全部转单列。

### 10.8 查询过滤卡

| 部位 | 参数 |
| --- | --- |
| 选择器建议 | `.ui-filter-card` 或 `.app-container > .el-form:first-child` 的兼容覆盖 |
| 背景 | `--ui-bg-panel`，内部弱区可用 `--ui-bg-panel-soft` |
| 边框 | `1px solid var(--ui-border)` |
| 圆角 | `8px` |
| padding | `16px` |
| 阴影 | `--ui-shadow-panel`，A 主题可降低透明度 |
| 底部间距 | `12px` |
| 表单项横向间距 | `12px` |
| 表单项纵向间距 | `10px` |
| 操作按钮区 | 与最后一个条件同排；空间不足换行到右侧 |

交互规则：

- `RightToolbar` 控制搜索区显示隐藏时，动画时长 `240ms`。
- 收起查询区后，工具条仍保持可见。
- 搜索和重置按钮固定在查询区末尾，不漂到页面右上角。

### 10.9 操作工具条

| 部位 | 参数 |
| --- | --- |
| 选择器建议 | `.ui-action-bar`，兼容 `.mb8` |
| 高度 | 最小 `36px` |
| 上下间距 | 查询区后 `12px`；表格前 `12px` |
| 左侧按钮组间距 | `8px` |
| 右侧工具按钮间距 | `6px` |
| 背景 | 默认透明；复杂页可使用 `--ui-bg-panel` |
| 圆形工具按钮 | `32px x 32px`；圆角 `6px` |

布局规则：

- 左侧放业务操作：新增、修改、删除、导入、导出。
- 右侧放视图操作：显示搜索、刷新、显隐列。
- 批量选择后，可在工具条左侧显示已选数量，字号 13px，颜色 `--ui-text-secondary`。
- 小屏幕下按钮允许换行，但行距保持 `8px`。

### 10.10 表格

| 部位 | 参数 |
| --- | --- |
| 表格容器 | 背景 `--ui-bg-panel`；边框 `--ui-border`；圆角 `8px`；阴影可选 `--ui-shadow-panel` |
| 表头高度 | `44px` |
| 表头背景 | `--ui-bg-panel-soft` |
| 表头文字 | 13px；字重 600；颜色 `--ui-text-regular` |
| 行高 | `48px` |
| 单元格文字 | 13px；颜色 `--ui-text-regular` |
| 单元格 padding | `8px 10px` |
| 行边框 | `1px solid var(--ui-border)` |
| hover 行背景 | `--ui-primary-soft`，A 主题透明度控制在 `0.12` 左右 |
| 选中行背景 | `color-mix(in srgb, var(--ui-primary-soft) 72%, var(--ui-bg-panel))` |
| 固定列阴影 | `0 0 10px rgba(15, 23, 42, 0.08)`，A 主题使用黑色透明 |
| 操作列宽 | 纯图标 `120px` 起；图标 + 文字 `180px` 起 |
| 空表格高度 | 最小 `240px` |

落地规则：

- 默认表格不做斑马纹，除非数据密度很高。
- 操作列图标按钮必须带 Tooltip。
- 状态列优先使用标签或开关，不能只靠颜色区分状态。
- 宽表格保留横向滚动，不强行压缩到不可读。

### 10.11 分页

| 部位 | 参数 |
| --- | --- |
| 容器高度 | `48px` |
| 上边距 | `12px` |
| 对齐 | 默认右对齐；移动端左对齐或换行 |
| 背景 | 透明 |
| 页码按钮 | `30px x 30px`；圆角 `6px` |
| 当前页 | 背景 `--ui-primary`；文字 `--ui-text-inverse` |
| 每页条数选择器 | 高 `32px`；宽 `110px` |
| 总数文字 | 13px；颜色 `--ui-text-secondary` |

### 10.12 弹窗 `Dialog`

| 部位 | 参数 |
| --- | --- |
| 宽度 | 小 `480px`；中 `600px`；大 `760px` |
| 最大高度 | `calc(100vh - 96px)` |
| 圆角 | `8px` |
| 背景 | `--ui-bg-panel` |
| 阴影 | `--ui-shadow-popover` |
| Header 高度 | 最小 `52px` |
| Header padding | `16px 20px` |
| 标题 | 15px / 22px；字重 600 |
| Body padding | `18px 20px` |
| Footer padding | `12px 20px 16px` |
| Footer 对齐 | 右对齐 |
| 遮罩 | `rgba(15, 23, 42, 0.45)`；A 主题 `rgba(0, 0, 0, 0.62)` |

落地规则：

- 简单新增、编辑可继续使用 `el-dialog`。
- 表单超过 8 个字段时，优先考虑抽屉或分组。
- 弹窗内不再套完整页面卡片。

### 10.13 抽屉 `Drawer`

| 部位 | 参数 |
| --- | --- |
| 宽度 | 详情 `520px`；表单 `640px`；复杂配置 `720px` |
| 移动端宽度 | `100vw` |
| 背景 | `--ui-bg-panel` |
| 圆角 | 右侧抽屉左上、左下 `8px`；移动端 `0` |
| 阴影 | `--ui-shadow-popover` |
| Header 高度 | `56px` |
| Header padding | `16px 20px` |
| Body padding | `18px 20px` |
| Footer 高度 | 最小 `56px`；padding `12px 20px` |
| 分割线 | `1px solid var(--ui-border)` |

落地规则：

- 详情查看、用户资料、复杂权限配置优先使用抽屉。
- 抽屉 footer 固定在底部，长表单 body 内滚动。
- 双栏抽屉左侧编辑、右侧说明或摘要，比例建议 `58% / 42%`。

### 10.14 树侧栏

| 部位 | 参数 |
| --- | --- |
| 默认宽度 | `260px` |
| 最小宽度 | `220px` |
| 最大宽度 | `360px` |
| 背景 | `--ui-bg-panel` |
| 边框 | `1px solid var(--ui-border)` |
| 圆角 | `8px` |
| Header 高度 | `48px` |
| Header padding | `12px` |
| 搜索框高度 | `32px` |
| 树节点高度 | `34px` |
| 树节点圆角 | `6px` |
| 树节点 padding | `0 8px` |
| hover 背景 | `--ui-primary-soft` |
| 当前节点背景 | `--ui-primary-soft` |
| 当前节点文字 | `--ui-primary` |
| 拖拽分割条宽度 | `6px` |
| 拖拽分割条 hover | `--ui-primary` |

落地规则：

- 保留现有宽度持久化能力。
- 树侧栏和主内容之间间距 `12px`。
- 移动端树侧栏改为抽屉或顶部筛选入口。

### 10.15 卡片、指标和工作台面板

| 组件 | 参数 |
| --- | --- |
| 普通卡片 | 圆角 `8px`；padding `16px`；背景 `--ui-bg-panel`；边框 `--ui-border` |
| KPI 卡片 | 高度 `96px` 起；padding `16px`；标题 13px；数值 24px / 32px，字重 700 |
| 首页欢迎面板 | 最小高 `160px`；padding `20px`；圆角 `8px` |
| 快捷入口卡片 | 高 `72px`；图标 `24px`；标题 14px；说明 12px |
| 图表面板 | 最小高 `280px`；Header `44px`；Body padding `12px` |

落地规则：

- 卡片不能再嵌套卡片。
- 工作台可以使用轻微渐变，但必须来自主题 token。
- KPI 颜色不超过 5 组，且必须保留语义色。

### 10.16 状态标签与徽标

| 类型 | 参数 |
| --- | --- |
| 普通标签 | 高 `24px`；padding `0 8px`；圆角 `6px`；字号 12px |
| 状态圆点 | `6px x 6px`；圆形；右间距 `6px` |
| 数字徽标 | 最小宽 `18px`；高 `18px`；圆角 `999px`；字号 11px |
| 成功 | 背景 `color-mix(in srgb, var(--ui-success) 14%, transparent)`；文字 `--ui-success` |
| 警告 | 背景 `color-mix(in srgb, var(--ui-warning) 14%, transparent)`；文字 `--ui-warning` |
| 危险 | 背景 `color-mix(in srgb, var(--ui-danger) 14%, transparent)`；文字 `--ui-danger` |
| 信息 | 背景 `color-mix(in srgb, var(--ui-info) 14%, transparent)`；文字 `--ui-info` |

落地规则：

- 关键状态不能只靠颜色，要保留文字。
- A 主题下标签背景透明度最高不超过 `0.22`，避免视觉过重。

### 10.17 下拉菜单、Tooltip 和 Popover

| 组件 | 参数 |
| --- | --- |
| Dropdown 菜单 | 圆角 `8px`；padding `6px`；阴影 `--ui-shadow-popover` |
| Dropdown item | 高 `34px`；padding `0 10px`；圆角 `6px`；字号 13px |
| Dropdown hover | 背景 `--ui-primary-soft`；文字 `--ui-primary` |
| Tooltip | 圆角 `6px`；字号 12px；最大宽 `240px` |
| Popover | 圆角 `8px`；padding `12px`；阴影 `--ui-shadow-popover` |

### 10.18 空状态、加载态和骨架屏

| 状态 | 参数 |
| --- | --- |
| 空状态容器 | 最小高 `240px`；居中；padding `32px` |
| 空状态图标 | `64px x 64px`；颜色 `--ui-text-secondary`；透明度 `0.55` |
| 空状态标题 | 14px；颜色 `--ui-text-regular` |
| 空状态说明 | 13px；颜色 `--ui-text-secondary` |
| Loading 遮罩 | 背景 `color-mix(in srgb, var(--ui-bg-panel) 78%, transparent)` |
| Loading 主色 | `--ui-primary` |
| Skeleton 背景 | `--ui-bg-panel-soft` |
| Skeleton 高亮 | `color-mix(in srgb, var(--ui-bg-panel) 70%, #ffffff 30%)` |

落地规则：

- 表格加载时优先使用 Element Plus loading，不额外自定义动画。
- 首页工作台可以使用骨架屏，但不要影响真实数据渲染。

### 10.19 滚动条

| 部位 | 参数 |
| --- | --- |
| 宽度 | `6px` |
| 轨道 | `transparent` |
| 滑块 | `color-mix(in srgb, var(--ui-text-secondary) 32%, transparent)` |
| hover 滑块 | `color-mix(in srgb, var(--ui-text-secondary) 50%, transparent)` |
| 圆角 | `999px` |

### 10.20 移动端规则

| 断点 | 规则 |
| --- | --- |
| `<= 991px` | 侧边栏走抽屉或 transform 收起；内容 padding `12px` |
| `<= 768px` | 查询表单转单列；操作条允许换行；弹窗宽度 `calc(100vw - 24px)` |
| `<= 480px` | 顶栏隐藏非关键图标，只保留菜单、主题、用户入口；表格保留横向滚动 |

移动端必须保证：

- 登录表单不超过视口宽度。
- 主题切换器不遮挡用户菜单。
- 表格操作列可横向滚动访问。
- 抽屉宽度为 `100vw`，不出现左右空隙。
