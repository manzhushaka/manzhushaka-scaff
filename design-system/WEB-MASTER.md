# Web Design System Master

> Status: verified
> Last updated: 2026-08-15
> Owner: manzhushaka

## Contents

1. Product Context
2. Stage Record
3. Visual Direction
4. Semantic Tokens
5. Layout And Components
6. Implementation
7. Browser Verification
8. Decisions And Change Log

## 1. Product Context

- Product: Manzhushaka Scaff 后台管理系统
- Primary users: 需要管理系统配置、用户、权限、监控和日志的运营与开发人员
- Primary job: 快速扫描数据、筛选记录并执行低误差的后台操作
- Critical user flow: 进入业务列表页 -> 使用筛选条件查询 -> 扫描表格 -> 执行新增、编辑、删除、导出等操作
- Deliverable: 统一现有 UI 的颜色、字体、间距、边框、标签和筛选控件视觉语言
- Mode: existing UI optimization
- Optimization scope: `ui-admin` 全局主题 token、Element Plus 全局覆盖、页面壳层和共享控件；不改业务页面数据流
- Framework and package manager: Vue 3 + Vite + npm
- Component library: Element Plus 2.13.1
- Supported browsers and viewport range: Chromium/现代浏览器；1440px、991px、390px
- Accessibility target: WCAG 2.1 AA 级关键文本对比度、可见键盘焦点、尊重 reduced motion
- Affected routes and components: 全局布局、PageHeader、列表页筛选区/操作条/表格/分页、Element Plus 表单控件、标签和弹窗
- Adjacent regression surface: 登录/错误页、树列表页、详情抽屉、设置面板、标签页导航
- Behavior that must remain unchanged: 路由、接口、权限指令、查询参数、表格操作、弹窗交互和响应式断点语义
- Existing UI strengths to retain: 深色侧栏 + 暖白内容画布、橙色主操作信号、PageHeader 统一说明条、列表页结构类
- UI debt and inconsistencies observed: Element Plus 默认变量与自定义 token 并存；局部硬编码颜色；表单边框和焦点态重复覆盖；标签背景与边框混用；按钮、字体和间距缺少单一来源

## 2. Stage Record

| Stage | Skill | Status | Key output | Gate result |
|---|---|---|---|---|
| Visual direction | `frontend-design` | completed | 保留现有品牌，形成安静、紧凑、可扫描的后台视觉方向 | 方向与产品、现有壳层和优化范围一致 |
| Theme system | `theme-factory` | completed | 自定义 `VibeHub Admin / Quiet Workbench` 语义 token，统一控件边框、状态色、字体和间距 | 保留现有品牌；跳过预设主题展示，因为本任务是现有 UI 优化且当前主题已在产品中生效 |
| Implementation | `web-artifacts-builder` | completed | 共享 token、Element Plus 控件覆盖和相关共享组件已原地更新 | `npm run build:prod` 通过；未改业务脚本、接口或路由 |
| Verification | `webapp-testing` | completed | 桌面/手机视口、控件焦点、标签状态、控制台和溢出检查完成 | 认证 mock 流程无新增错误；真实登录 API 受后端未启动影响 |

## 3. Visual Direction

- Visual thesis: 让后台像一套可靠的工作台，而不是营销页面；以稳定的暖白表面、深色导航、橙色行动信号和清晰的 1px 边界提升扫描速度。
- Typography roles: `IBM Plex Sans` 作为中英文 UI 正文和控件字体；`IBM Plex Mono` 仅用于 PageHeader 英文分组和数据/技术标识；不引入新的字体依赖。
- Palette direction: 暖白画布 `#f6f5f2`、白色工作面 `#ffffff`、炭黑侧栏 `#191612`、橙色主信号 `#ff6a2a`，状态色分别使用绿色、琥珀、红色和蓝色；避免将所有状态染成橙色。
- Layout concept: 统一页面说明条 + 工作区；列表页以筛选、操作、表格、分页四段垂直节奏组成单一工作面板；树列表维持左右分栏但共享同一边界语言。
- Content voice: 中文短句、动作导向、信息优先；页面说明描述用途，不加入营销文案或操作教程。
- Motion approach: 仅保留按钮 hover、焦点、抽屉/弹窗等反馈级动效；持续动画和装饰动画关闭，`prefers-reduced-motion` 下进一步压缩。
- Signature element: 页面说明条中的“工程标记”视觉，使用橙色细线、单色工具图标和小型几何环作为稳定识别，不扩散到每个卡片。
- Generic defaults rejected: 不使用紫色渐变、玻璃拟态、巨大 hero、重复浮层卡片或多套圆角；不把旧版 Element Plus 蓝色默认值留在局部组件中。
- Existing patterns retained: 深色侧栏、暖白内容画布、橙色主操作、PageHeader、`ui-filter-card`/`ui-action-bar`/`ui-table-card` 结构类。
- Inconsistent patterns consolidated: 颜色、边框、圆角、控件高度、表格行高、按钮尺寸、标签状态变量和焦点环。
- Weak patterns replaced: 局部硬编码的蓝灰边框、旧主题残留色、标签背景/边框不匹配、筛选控件不同边界实现。

## 4. Semantic Tokens

### Color

| Token | Value | Meaning |
|---|---|---|
| `color.bg.canvas` | `#f6f5f2` | 页面画布 |
| `color.bg.surface` | `#ffffff` | 工作面板 |
| `color.bg.muted` | `#faf9f7` | 表头、抽屉标题等弱强调表面 |
| `color.text.primary` | `#16130f` | 主文本 |
| `color.text.secondary` | `#5c564c` | 标签、说明和表头文本 |
| `color.border.subtle` | `#ebe8e2` | 弱分隔线 |
| `color.border.default` | `#dfdcd5` | 卡片、表格和布局边界 |
| `color.border.control` | `#c7c2b9` | 输入、选择器、日期控件默认边界 |
| `color.border.control-hover` | `#9e9588` | 控件 hover 边界 |
| `color.action.primary` | `#ff6a2a` | 主要操作和当前导航 |
| `color.state.success` | `#1f8a5b` | 成功、启用 |
| `color.state.warning` | `#b76e00` | 警告、待处理 |
| `color.state.danger` | `#c73515` | 错误、破坏性操作 |
| `color.state.info` | `#2a6fdb` | 信息和链接 |

### Typography

| Role | Family | Size | Weight | Line height | Use |
|---|---|---:|---:|---:|---|
| `type.display` | `IBM Plex Sans`, system fallback | 22px | 700 | 1.3 | PageHeader 标题 |
| `type.heading` | `IBM Plex Sans`, system fallback | 15-18px | 600-700 | 1.35 | 面板和区块标题 |
| `type.body` | `IBM Plex Sans`, `Noto Sans SC`, system fallback | 14px | 400 | 1.6 | 页面正文 |
| `type.ui` | `IBM Plex Sans`, `Noto Sans SC`, system fallback | 13px | 500-600 | 1.45 | 控件、导航、表格 |
| `type.data` | `IBM Plex Mono`, system monospace | 12-13px | 400-600 | 1.5 | 英文分组、日志、技术数据 |

### Foundations

- Spacing scale: `4 / 8 / 12 / 16 / 20 / 24 / 32px`。
- Radius scale: 控件 `6px`、面板 `8px`、浮层 `8px`、轻量标签 `4px`。
- Border rules: 统一 `1px`；控件使用 `color.border.control`，hover 使用 `color.border.control-hover`，focus 使用主色边界 + focus ring；标签背景和边框由同一状态色生成。
- Elevation scale: 普通面板无浮夸阴影；面板使用 `0 2px 8px rgba(22,19,15,.06)`，浮层使用更深但仍克制的阴影。
- Icon family and sizing: 复用 Element Plus 和现有 SVG；表格操作图标保持 16px，PageHeader 工具图标 42px，图标按钮保留 tooltip/aria-label。
- Motion durations and easing: 快速反馈 `160ms ease`，普通过渡 `240ms ease`。
- Reduced-motion behavior: `prefers-reduced-motion: reduce` 下将动画和过渡压缩到近乎即时。

## 5. Layout And Components

- Page regions and content width: 侧栏由布局 token 控制；内容使用 `--ui-layout-content-padding`；页面说明条与工作区保持统一外边距。
- Responsive breakpoints and behavior: 1440px 桌面保持密度；991px 以下侧栏进入移动模式，筛选项可换行；640px 以下弹窗表单单列、表格横向滚动。
- Navigation model: 现有顶栏、标签页和侧栏导航不变，仅统一颜色、边界和焦点态。
- Shared primitives: `theme-tokens.scss`、`element-ui.scss`、`PageHeader`、列表页结构类、树面板和 Element Plus 原子控件。
- Forms and validation: 保留现有 Element Plus 校验和交互；统一输入、选择器、日期范围、文本域的高度、边框和焦点态。
- Loading, empty, error, success, and disabled states: 使用 Element Plus 现有状态语义，统一文字、底色、边框和禁用对比度。
- Destructive-action behavior: 删除和危险状态仍使用 danger 语义，不改变确认弹窗和权限行为。
- Keyboard and focus behavior: 共享可见焦点环，不移除现有键盘交互；图标按钮继续保留 tooltip/aria 语义。

## 6. Implementation

- Routes and entry points: 现有路由不变；样式入口为 `ui-admin/src/main.js` -> `ui-admin/src/assets/styles/index.scss`。
- Component boundaries: 优先调整共享 token 和 Element Plus 覆盖，页面仅在确有局部问题时调整。
- State management: 不变。
- Assets and fonts: 复用现有字体栈和图标，不新增依赖。
- Build and static-check commands: `cd ui-admin && npm run build:prod`；`node --test ui-admin/tests/pageHeader.test.mjs`。
- Existing dependencies reused: Element Plus、现有 SVG 图标和布局组件。
- New dependencies and reasons: none planned。
- Business behavior preserved: yes。
- Functional changes required for working interactions: none。
- Unintended file or dependency churn check: implementation 后检查 `git diff`、构建产物和 lockfile。
- Intentional deviations: 旧版 `variables.module.scss` 中仍被历史组件引用的变量不做全量删除，以避免无关行为变化；只覆盖实际生效的共享样式。

## 7. Browser Verification

| Scenario | Viewport | Result | Evidence or issue |
|---|---|---|---|
| Critical user flow | 1440px | passed | 认证 mock 后访问 `/index` 和 `/userAuth/user`，页面说明条、筛选区、操作条和工作面板正常渲染 |
| Keyboard navigation | 390px | passed | 筛选 input 聚焦后显示橙色边界和 focus ring，未改变输入行为 |
| Responsive layout | 390px | passed | 用户列表页 body scrollWidth 等于 viewport，无横向溢出；筛选项和操作按钮按列换行 |
| Console and page errors | 1440px / 390px | passed with external blocker | mock 认证页面无新增 console/page error；无后端访问登录页会收到预期 500 API 错误 |
| Preserved behavior | 1440px | passed | 查询表单、路由、权限指令和 Element Plus 交互未改动 |
| Adjacent regression surface | 1440px | passed | 首页工作台、登录页视觉、侧栏/顶栏/标签页壳层完成截图核对 |

- Server command and URL: `cd ui-admin && npm run dev -- --host 127.0.0.1 --port 4173`; `http://127.0.0.1:4173`
- Browser-test command: `python3 /Users/manzhushaka/.codex/skills/webapp-testing/scripts/with_server.py --help`（环境没有 `python` 命令，实际浏览器检查使用 bundled Playwright Node runtime）
- Screenshots: `design-system/login-desktop.png`, `design-system/dashboard-desktop.png`, `design-system/user-list-desktop.png`, `design-system/user-list-mobile.png`
- Failed requests: 登录页在未启动后端时请求 `/dev-api/**` 返回 500；认证 mock 页面无失败请求
- Residual risks or blockers: 需要连接真实后端再次核对登录、验证码、动态菜单和真实列表数据；未在真实业务数据下做视觉回归

## 8. Decisions And Change Log

| Date | Decision or change | Reason | Confirmed by |
|---|---|---|---|
| 2026-08-15 | 选择 existing UI optimization，保留暖白/炭黑/橙色品牌基线 | 用户要求统一现有后台视觉，不要求重做产品方向 | manzhushaka |
| 2026-08-15 | 先调整共享 token 和 Element Plus 覆盖，再处理局部硬编码 | 颜色、字体、间距和边框问题具有全局重复性 | manzhushaka |
| 2026-08-15 | 用 `color.border.control`、`color.border.control-hover` 和统一 focus ring 收敛筛选框边界 | 修复同类控件边框深浅不一致、聚焦不可见问题 | manzhushaka |
| 2026-08-15 | 提高 `.el-tag.el-tag--*` 选择器具体度并统一背景/边框/文字来源 | 覆盖 Element Plus 默认标签颜色，解决底色和边框不匹配 | manzhushaka |
| 2026-08-15 | 通过 Vite + mock API 做桌面/手机浏览器核对 | 本地后端未启动，仍需验证真实认证和动态菜单链路 | manzhushaka |
