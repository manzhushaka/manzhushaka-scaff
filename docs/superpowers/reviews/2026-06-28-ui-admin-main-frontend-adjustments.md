# ui-admin main 分支前端调整清单

## 1. 审查范围

本次基于当前 `main` 分支审查 `ui-admin` 前端实现，重点关注视觉系统升级后的剩余问题：

- 三套品牌主题是否仍被旧主题链路覆盖。
- 典型后台页面是否符合「过滤卡 + 工具条 + 数据区」结构。
- 首页、服务监控、详情页、弹窗、通知等非列表页面是否跟随主题。
- 移动端、构建结果和可维护性是否还有明显风险。

对照文档：

- `docs/superpowers/specs/2026-06-28-ui-admin-visual-system-design.md`
- `docs/superpowers/plans/2026-06-28-ui-admin-visual-system-gap-fix-plan.md`

## 2. 当前已改善项

以下问题在当前 `main` 分支已经有明显进展，本次不再作为主要问题重复列入：

- `brandTheme` 已有合法值校验，非法值会回落到 `cool-tower`。
- `Settings/index.vue` 保存配置时已写入 `brandTheme`。
- 侧边栏主背景已改为 `--ui-bg-sidebar`。
- 主题切换器已经移出桌面端专属分支，移动端仍可显示。
- `ThemeSwitcher` 已补充 `type="button"`、键盘事件和 `aria-pressed`。
- 首页 KPI、快捷入口已从硬编码渐变改为 `tone-*` 体系。
- `theme-tokens.scss` 中全局 `.ui-table-card` 已不再使用 `:deep()`。
- `npm run build:prod` 构建通过，且不再出现 `%VITE_APP_TITLE% is not defined` 警告。

## 3. 仍需调整的问题

### 3.1 [必须修复] 旧主色变量仍可能覆盖品牌主题

**现象：**

`layout/index.vue` 仍从 `settingsStore.theme` 注入 `--current-color`、`--current-color-light`、`--current-color-dark-bg`。`variables.module.scss` 中仍用 `--current-color` 覆盖 TagsView 激活态颜色。

**影响：**

即使当前设置抽屉隐藏了旧主色选择器，只要本地 `layout-setting.theme` 还存在旧值，某些样式路径仍可能使用旧蓝色或旧主色，导致 A/B/C 品牌主题出现混色。

**证据：**

- `ui-admin/src/layout/index.vue:2`
- `ui-admin/src/layout/index.vue:23-24`
- `ui-admin/src/assets/styles/variables.module.scss:227-235`

**建议调整：**

- 从 `layout/index.vue` 移除 `--current-color*` 的 inline style。
- 将 `variables.module.scss` 中 TagsView 激活态改为 `--ui-primary`、`--ui-primary-soft` 和 `--ui-border`。
- 保留 `settingsStore.theme` 字段仅作兼容读取，不再参与主布局视觉。

**验收方式：**

1. 手动向 `localStorage.layout-setting.theme` 写入 `#ff4500`。
2. 切换 `brandTheme` 为 `gold-ledger`。
3. 刷新后检查 TagsView 激活态仍为铜金色，而不是橙红色。

### 3.2 [必须修复] 设置抽屉隐藏旧配置但仍保留可执行逻辑

**现象：**

`Settings/index.vue` 用 `v-show="false"` 隐藏了多导航、旧侧栏风格、旧主色 UI，但模板、状态、watch 和保存字段仍存在。

**影响：**

这属于“视觉隐藏但业务逻辑仍活着”的状态。旧 `navType`、`sideTheme`、`theme` 仍会被保存回 `layout-setting`，也会被 watch 读取并影响侧栏开合、路由菜单和布局状态。后续维护时容易误以为这些能力已经下线。

**证据：**

- `ui-admin/src/layout/components/Settings/index.vue:3-54`
- `ui-admin/src/layout/components/Settings/index.vue:144`
- `ui-admin/src/layout/components/Settings/index.vue:158-180`
- `ui-admin/src/layout/components/Settings/index.vue:187-199`

**建议调整：**

- 删除隐藏模板，而不是使用 `v-show="false"`。
- 删除 `handleNavType()` 和对应 `watch()`，或将 `navType` 在 store 层强制固定为 `1`。
- `saveSetting()` 不再写入 `sideTheme` 和 `theme`，只保留真实可配置项。
- 如需兼容老缓存，在 `settings` store 初始化时读取后立即归一化，不把旧配置继续扩散。

**验收方式：**

1. 本地写入 `layout-setting.navType = 3`。
2. 刷新页面。
3. 预期仍进入统一左侧主布局。
4. 打开设置抽屉保存后，`layout-setting` 不再生成新的 `sideTheme` 和 `theme` 配置。

### 3.3 [建议修改] 用户管理页面仍存在大卡片嵌套

**现象：**

用户管理页使用 `content-inner ui-table-card` 包住过滤表单、工具条、表格和分页。规格要求 `.app-container` 只负责页面留白，视觉分组应分别交给 `.ui-filter-card`、`.ui-action-bar`、`.ui-table-card`。

**影响：**

该页会比其他列表页更重，形成「表格卡里套过滤卡」的层级混乱。A 主题下阴影和边框叠加更明显，C 主题下也容易出现纸面感过重。

**证据：**

- `ui-admin/src/views/system/user/index.vue:4-6`
- `ui-admin/src/views/system/user/index.vue:27-46`
- `ui-admin/src/views/system/user/index.vue:88-90`

**建议调整：**

结构改为：

```vue
<div class="tree-sidebar-content">
  <div class="content-inner">
    <el-form class="ui-filter-card">...</el-form>
    <el-row class="mb8 ui-action-bar">...</el-row>
    <div class="ui-table-card">
      <el-table>...</el-table>
      <pagination />
    </div>
  </div>
</div>
```

**验收方式：**

- 用户管理和角色管理页面的过滤区、操作条、表格卡层级一致。
- 用户管理主内容区不再出现整块外层卡片包裹所有内容。

### 3.4 [建议修改] 用户管理列显隐 `storageKey` 使用占位值

**现象：**

用户管理 `RightToolbar` 的列显隐持久化 key 是 `xxxxxxxx`。

**影响：**

该 key 没有业务语义，后续其他页面复制时容易冲突。用户列显隐配置也难以排查和迁移。

**证据：**

- `ui-admin/src/views/system/user/index.vue:43`

**建议调整：**

改为稳定业务 key，例如：

```vue
storageKey="system-user-columns"
```

**验收方式：**

1. 在用户管理隐藏一列。
2. 刷新页面后该列保持隐藏。
3. `localStorage` 中 key 为 `system-user-columns`，不存在新的 `xxxxxxxx`。

### 3.5 [建议修改] 服务监控页仍是旧表格结构，响应式风险较高

**现象：**

服务监控使用多个 `el-card` + 原生 `table` + 大量 inline style。虽然外层加了 `server-monitor-card` 做主题化，但内部仍不是统一的面板和表格体系。

**影响：**

在 A 主题下，原生 table 的边框、行 hover、文本层级不一定完整跟随 Element Plus token。在手机和平板宽度下，CPU、内存两个 `span=12` 卡片和宽表格容易横向拥挤。

**证据：**

- `ui-admin/src/views/monitor/server/index.vue:3-8`
- `ui-admin/src/views/monitor/server/index.vue:38-42`
- `ui-admin/src/views/monitor/server/index.vue:77-81`
- `ui-admin/src/views/monitor/server/index.vue:101-105`
- `ui-admin/src/views/monitor/server/index.vue:137-141`

**建议调整：**

- 把 `el-col :span="12"` 改成响应式写法：`:xs="24" :sm="24" :md="12"`。
- 为原生 table 增加统一 class，例如 `.server-info-table`，移除 inline `style="width: 100%;"`。
- 表格背景、边框、表头、单元格统一使用 `--ui-*` token。
- 长路径、运行参数字段增加换行或横向滚动容器。

**验收方式：**

- `390 x 844` 下 CPU、内存卡片单列显示。
- A/B/C 三主题下服务监控卡片、表格和危险状态色都清晰可读。
- JVM 参数、项目路径长文本不撑破布局。

### 3.6 [建议修改] 非列表页面主题化仍未覆盖完全

**现象：**

当前 38 个 `views/*.vue` 中有 13 个页面使用了 `.ui-filter-card/.ui-action-bar/.ui-table-card`。剩余页面中，部分是登录、错误页、详情页、个人中心、缓存监控、Swagger 等非列表页面，它们不一定需要列表结构，但仍需要主题一致性检查。

**重点页面：**

- `ui-admin/src/views/monitor/cache/index.vue`
- `ui-admin/src/views/monitor/cache/list.vue`
- `ui-admin/src/views/system/user/profile/index.vue`
- `ui-admin/src/views/system/user/profile/userInfo.vue`
- `ui-admin/src/views/system/user/profile/userAvatar.vue`
- `ui-admin/src/views/system/user/authRole.vue`
- `ui-admin/src/views/system/role/authUser.vue`
- `ui-admin/src/views/system/role/selectUser.vue`
- `ui-admin/src/views/system/dict/detail.vue`
- `ui-admin/src/views/system/notice/ReadUsers.vue`
- `ui-admin/src/views/tool/swagger/index.vue`
- `ui-admin/src/views/error/401.vue`
- `ui-admin/src/views/error/404.vue`

**影响：**

这些页面在用户实际操作中会形成“主列表很新，详情页或工具页又回到旧后台”的割裂感。

**建议调整：**

- 详情页统一使用 `.ui-panel-card`、`.detail-drawer` 或专用详情 token。
- 缓存监控、Swagger 等工具页至少统一卡片背景、边框、标题和空态。
- 错误页按钮、标题、说明文字接入 `--ui-primary`、`--ui-text-*`。

**验收方式：**

- 在 A/B/C 主题下逐个打开上述页面。
- 不出现明显白底硬卡片、旧蓝色按钮、旧灰色边框。
- 详情页和列表页的间距、圆角、阴影一致。

### 3.7 [建议修改] 硬编码色值仍集中在通知、详情、上传、树侧栏等组件

**现象：**

全局搜索仍能发现较多硬编码色值，尤其集中在：

- `ui-admin/src/assets/styles/manzhushaka.scss`
- `ui-admin/src/layout/components/HeaderNotice/index.vue`
- `ui-admin/src/layout/components/HeaderNotice/DetailView.vue`
- `ui-admin/src/components/TreePanel/index.vue`
- `ui-admin/src/components/ImageUpload/index.vue`
- `ui-admin/src/components/FileUpload/index.vue`
- `ui-admin/src/views/system/dict/detail.vue`
- `ui-admin/src/views/system/notice/ReadUsers.vue`

**影响：**

这些区域在浅色 B 主题下不一定明显，但 A 深色主题和 C 暖色主题下会出现硬白、旧蓝、旧灰、固定红色等不协调细节。

**建议调整：**

- 固定语义色替换为 `--ui-danger`、`--ui-warning`、`--ui-success`。
- 固定背景替换为 `--ui-bg-panel`、`--ui-bg-panel-soft`。
- 固定边框替换为 `--ui-border`。
- 通知弹层和详情弹层建立局部 token，但默认值必须来自 `--ui-*`。

**验收方式：**

运行：

```bash
rg -n "#[0-9a-fA-F]{3,8}|rgba\\(|rgb\\(|linear-gradient\\(" ui-admin/src/assets/styles ui-admin/src/components ui-admin/src/layout ui-admin/src/views -g "*.scss" -g "*.vue"
```

人工确认剩余色值只属于以下类型：

- 主题 token 定义。
- 登录页固定 B 主题。
- 必要的透明遮罩。
- 图标或图片组件内部不可主题化的固定色。

### 3.8 [建议修改] `TreePanel` 自身样式仍有旧色值

**现象：**

`theme-tokens.scss` 已为 `.tree-sidebar` 写了 token 化覆盖，但 `TreePanel/index.vue` scoped 样式里仍有固定白底、固定边框、固定蓝色拖拽条和阴影。

**影响：**

用户管理这类树侧栏页面可能依赖 scoped 样式优先级，导致全局 token 覆盖不完整。A 主题下尤其容易出现树侧栏局部白底。

**证据：**

- `ui-admin/src/components/TreePanel/index.vue:554-555`
- `ui-admin/src/components/TreePanel/index.vue:592-596`
- `ui-admin/src/components/TreePanel/index.vue:611-618`

**建议调整：**

- 将 TreePanel 内部固定色改为 `--ui-bg-panel`、`--ui-border`、`--ui-primary`、`--ui-shadow-panel`。
- 拖拽条 hover 色使用 `--ui-primary`。
- 保留宽度持久化逻辑，不调整业务行为。

**验收方式：**

- 用户管理切换 A 主题，树侧栏不出现白底。
- 拖拽分割条 hover 明确但不突兀。
- 树节点当前态和 hover 跟随品牌主题。

### 3.9 [建议修改] 环境变量标题仍是旧品牌名

**现象：**

构建警告已消失，但 `.env.*` 中 `VITE_APP_TITLE` 仍为「若依管理系统」。

**影响：**

登录页、浏览器标题、侧栏 Logo 标题等位置可能继续显示旧品牌。视觉系统已经改为「满招科技」后，这会造成品牌不一致。

**证据：**

- `ui-admin/.env.development`
- `ui-admin/.env.production`
- `ui-admin/.env.staging`
- `ui-admin/src/views/login.vue:102`
- `ui-admin/src/layout/components/Sidebar/Logo.vue:26`

**建议调整：**

统一改为项目实际名称，例如：

```env
VITE_APP_TITLE=满招科技后台管理系统
```

同时建议移除等号两侧空格，保持 Vite env 文件常见写法。

**验收方式：**

- 登录页标题显示「满招科技后台管理系统」。
- 侧栏 Logo 标题显示一致。
- 浏览器 `<title>` 一致。

### 3.10 [仅供参考] `ThemeSwitcher` 的 Tooltip 结构还可以更干净

**现象：**

当前是 `button` 内部包 `el-tooltip`，tooltip 触发元素是内部 `span`。

**影响：**

目前构建通过，功能风险不高。但从可访问性和交互结构看，更推荐让 tooltip 包裹整个按钮，减少 hover/focus 触发区域不一致的问题。

**证据：**

- `ui-admin/src/components/ThemeSwitcher/index.vue:3-21`

**建议调整：**

结构改为：

```vue
<el-tooltip v-for="theme in themes" :key="theme.id" :content="theme.label">
  <button type="button" class="theme-swatch">...</button>
</el-tooltip>
```

**验收方式：**

- 鼠标悬停整个色块都能显示主题名。
- Tab 聚焦到按钮后，读屏能读出「切换到冷感控制塔」这类动作语义。

## 4. 建议优先级

### 第一优先级

1. 移除旧 `--current-color` 和 TagsView 旧主色覆盖。
2. 清理设置抽屉中隐藏但仍活跃的旧配置逻辑。
3. 修正用户管理大卡片嵌套和 `storageKey="xxxxxxxx"`。

### 第二优先级

1. 服务监控页响应式和原生 table 主题化。
2. TreePanel scoped 固定色 token 化。
3. 非列表页面主题一致性补齐。

### 第三优先级

1. 通知、上传、详情页硬编码色值清理。
2. 环境变量标题改为新品牌名。
3. ThemeSwitcher Tooltip 结构优化。

## 5. 验证记录

本次审查运行了以下命令：

```bash
git branch --show-current
git status --short
rg -n "brandTheme|data-ui-theme|ThemeSwitcher|cool-tower|amber-command|gold-ledger|ui-filter-card|ui-action-bar|ui-table-card|:deep\\(|navType|sideTheme|settingsStore\\.theme|--current-color|VITE_APP_TITLE" ui-admin/src ui-admin/index.html ui-admin/.env*
find ui-admin/src/views -name '*.vue' | wc -l
rg -l "ui-filter-card|ui-action-bar|ui-table-card" ui-admin/src/views -g '*.vue' | sort | wc -l
npm run build:prod
```

验证结果：

- 当前分支：`main`。
- 审查开始前工作区：干净。
- `ui-admin/src/views` 下共 38 个 Vue 页面。
- 其中 13 个页面已接入 `.ui-filter-card/.ui-action-bar/.ui-table-card`。
- `npm run build:prod` 通过，exit 0。
- 构建输出未出现 `%VITE_APP_TITLE% is not defined` 警告。

## 6. 结论

当前 `main` 分支已经不再是「只有样板页改造」的状态，主题系统、典型列表页和构建稳定性都有明显进展。剩余调整主要集中在三个方向：

- 彻底切断旧主色和旧布局配置对品牌主题的影响。
- 补齐用户管理、服务监控、树侧栏、详情页、通知弹层等边角页面的主题一致性。
- 清理占位 key、旧品牌名、硬编码色值和隐藏但仍执行的配置逻辑。

建议下一轮先处理第一优先级问题，这些改动小，但能显著减少后续主题混搭和维护误判。
