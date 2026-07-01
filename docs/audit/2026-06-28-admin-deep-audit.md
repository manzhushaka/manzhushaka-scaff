# 后台管理系统深度审计报告

审计时间：2026-06-28 22:45（Asia/Shanghai）

## 结论摘要

本次已启动前后端并使用 `admin / admin123` 登录验证。开发环境验证码已关闭，`/dev-api/captchaImage` 返回 `captchaEnabled:false`。

整体结论：

- 登录、基础后台框架、系统管理、系统监控、隐藏路由、常规弹窗、导入模板、导出下载和主要系统 API 基本可用。
- 当前最大风险不是后端基础能力，而是菜单数据、前端视图文件、初始化 SQL、权限闭环之间不同步。
- 有 17 个菜单路由指向不存在的 Vue 组件，用户点击后主内容区空白。
- `数据监控` 和 `系统接口` iframe 页面当前不可用，后端资源未挂载或文档 UI 未启用。
- 首页快捷入口中 `操作日志`、`登录日志` 指向旧路由，点击后进入 404。
- 移动端表格页面可访问，但 `用户管理` 在 390 px 宽度下表格区域被压缩到约 136 px，属于明显布局问题。
- `sql/manzhushaka_db_init.sql` 与当前数据库菜单不一致；多个前端按钮权限和后端接口权限没有进入初始化菜单权限数据。
- 多个接口没有显式 `@PreAuthorize` 或 `@Anonymous`，虽然会被 `SecurityConfig.anyRequest().authenticated()` 保护，但不符合仓库“裸接口”约束。

## 审计范围

### 已执行的自动化检查

| 类型 | 产物 | 结果 |
| --- | --- | --- |
| 登录和基础接口 | `.codex-run/deep-api-audit.json` | 75 / 75 通过 |
| 菜单路由巡检 | `.codex-run/ui-audit-results.json` | 发现 17 个空白组件页 |
| 按钮交互巡检 | `.codex-run/button-audit-playwright-summary.json` | 18 页巡检，16 页可交互，2 页 iframe 空白 |
| 深度 UI 巡检 | `.codex-run/deep-ui-audit.json` | 隐藏路由可用；确认快捷入口、iframe、移动端问题 |
| 静态一致性审计 | `.codex-run/static-audit.json` | 发现菜单、SQL、权限闭环不一致 |
| 截图 | `.codex-run/screenshots/`、`.codex-run/screenshots/deep-ui/` | 保留复现截图 |

### 已确认可用的能力

- 登录链路：`/dev-api/login`、`/dev-api/getInfo`、`/dev-api/getRouters`。
- 系统管理 API：用户、角色、岗位、字典、参数、公告、部门临时 CRUD 和清理。
- 监控 API：在线用户、定时任务、调度日志、服务监控、缓存监控。
- 导出和模板下载：用户导出、用户导入模板、角色/岗位/字典/参数/日志/调度日志导出。
- 隐藏路由：
  - `/user/profile`
  - `/user/profile/resetPwd`
  - `/system/user-auth/role/1`
  - `/system/role-auth/user/2`
  - `/system/dict-data/index/1`
  - `/monitor/job-log/index/1`

## 问题 1：17 个菜单路由指向不存在的 Vue 组件

优先级：P0

### 现象

这些菜单在当前数据库返回的动态路由中存在，浏览器可以看到菜单和面包屑，但主内容区为空白。

缺失组件清单：

| 菜单 | 路由 | 组件路径 |
| --- | --- | --- |
| 交易流水 | `/biz/tradeFlow` | `ui-admin/src/views/biz/tradeFlow/index.vue` |
| 订单包管理 | `/biz/order` | `ui-admin/src/views/biz/order/index.vue` |
| 审核管理 | `/biz/review` | `ui-admin/src/views/biz/review/index.vue` |
| 订单资料 | `/biz/order/material` | `ui-admin/src/views/biz/order/material.vue` |
| 安装信息管理 | `/biz/installInfo` | `ui-admin/src/views/biz/installInfo/index.vue` |
| 补贴结算 | `/biz/settlement` | `ui-admin/src/views/biz/settlement/index.vue` |
| 退货退款台账 | `/biz/refund` | `ui-admin/src/views/biz/refund/index.vue` |
| 资料规则 | `/biz/order/rule` | `ui-admin/src/views/biz/order/rule.vue` |
| 接入方管理 | `/biz/access` | `ui-admin/src/views/biz/access/index.vue` |
| 品牌管理 | `/biz/brand` | `ui-admin/src/views/biz/brand/index.vue` |
| 资料转存任务 | `/biz/order/transferTask` | `ui-admin/src/views/biz/order/transferTask.vue` |
| SKU 管理 | `/goods/sku` | `ui-admin/src/views/biz/sku/index.vue` |
| SN 码管理 | `/goods/sn` | `ui-admin/src/views/biz/sn/index.vue` |
| 价格管理 | `/goods/price` | `ui-admin/src/views/biz/price/index.vue` |
| 异步导入任务 | `/importExport/asyncImport` | `ui-admin/src/views/biz/asyncImport/index.vue` |
| 表单构建 | `/tool/build` | `ui-admin/src/views/tool/build/index.vue` |
| 代码生成 | `/tool/gen` | `ui-admin/src/views/tool/gen/index.vue` |

### 复现步骤

1. 打开 `http://localhost/`。
2. 使用 `admin / admin123` 登录。
3. 点击侧边栏中的任意上述菜单，例如「交易流水」。
4. 页面只显示面包屑和刷新按钮，主内容区为空。

### 证据

- `.codex-run/static-audit.json`：`missingViewComponents` 为 17。
- `.codex-run/ui-audit-results.json`：上述路由 `blankMain:true`。
- 截图示例：
  - `.codex-run/screenshots/03-交易流水.png`
  - `.codex-run/screenshots/33-表单构建.png`
  - `.codex-run/screenshots/34-代码生成.png`

### 根因

前端动态路由加载逻辑位于 `ui-admin/src/store/modules/permission.js`，会把后端返回的 `component` 字段映射到 `ui-admin/src/views/**/*.vue`。当 `component` 指向的文件不存在时，路由可以注册，但页面组件无法渲染。

### 影响

- 用户点击菜单后看到空白页，会误以为系统卡死或权限异常。
- 这些菜单如果是未完成业务，当前状态会把未交付功能暴露给用户。
- 如果这些菜单是应交付业务，说明前端页面和后端菜单数据没有一起提交。

## 问题 2：当前数据库菜单与初始化 SQL 不一致

优先级：P0

### 现象

当前登录后 `/getRouters` 返回 42 个菜单/路由节点，但 `sql/manzhushaka_db_init.sql` 只初始化到部分系统菜单和按钮权限。静态审计发现 19 个当前 live 路由不在初始化 SQL 中。

典型缺失：

- `/biz/**`、`/goods/**`、`/importExport/**` 业务菜单。
- `/system/log/operlog`、`/system/log/logininfor` 日志子菜单。
- `/tool/gen`、`/tool/swagger`。

### 证据

- `sql/manzhushaka_db_init.sql:167-182` 只初始化系统管理、系统监控、表单构建等菜单。
- `sql/manzhushaka_db_init.sql:215-249` 只初始化到部分系统管理和监控按钮权限。
- `.codex-run/static-audit.json`：`liveNotInSql` 为 19。

### 根因

当前数据库经过后续手工维护或迁移后，已经包含更多菜单；但主初始化脚本没有同步更新。仓库规范要求新增菜单、按钮权限和默认授权必须同步维护 `sys_menu` 和 `sys_role_menu`。

### 影响

- 新环境执行 `sql/manzhushaka_db_init.sql` 后菜单会缺失。
- 初始化库与开发库表现不同，测试和交付不可复现。
- 权限按钮可能在某些环境不可见，或者后端接口权限存在但角色没有授权数据。

## 问题 3：权限闭环不完整

优先级：P1

### 现象

静态审计发现后端 Controller 和前端按钮使用了权限字符串，但 `sql/manzhushaka_db_init.sql` 中缺少对应 `sys_menu.perms`。

后端权限存在但 SQL 缺失的权限：

```text
monitor:job:add
monitor:job:changeStatus
monitor:job:edit
monitor:job:export
monitor:job:query
monitor:job:remove
monitor:logininfor:unlock
monitor:online:forceLogout
system:config:export
system:config:query
system:dept:query
system:dict:export
system:dict:query
system:menu:query
system:post:export
system:post:query
system:role:export
system:role:query
system:user:export
system:user:import
system:user:query
system:user:resetPwd
tool:test:list
```

前端按钮存在但 SQL 缺失的权限：

```text
monitor:job:add
monitor:job:changeStatus
monitor:job:edit
monitor:job:export
monitor:job:query
monitor:job:remove
monitor:logininfor:unlock
monitor:online:forceLogout
system:config:export
system:dict:export
system:post:export
system:role:export
system:user:export
system:user:import
system:user:resetPwd
```

另外，前端引用了 `monitor:operlog:query`，但静态扫描未找到对应后端 `@PreAuthorize`。

### 证据

- `.codex-run/static-audit.json`：
  - `controllerPermsMissingSql` 为 23。
  - `frontendPermsMissingSql` 为 15。
  - `frontendPermsMissingController` 为 1。
- 前端用户管理按钮示例：
  - `ui-admin/src/views/system/user/index.vue:38` 使用 `system:user:import`。
  - `ui-admin/src/views/system/user/index.vue:41` 使用 `system:user:export`。
  - `ui-admin/src/views/system/user/index.vue:82` 使用 `system:user:resetPwd`。

### 根因

新增接口权限、按钮权限和 SQL 初始化没有同步维护，导致权限来源分散。

### 影响

- 非超级管理员角色初始化后可能看不到按钮，或者点击后后端 403。
- 权限评审无法通过 SQL 一眼确认闭环。
- 新环境权限表现不可复现。

## 问题 4：多个接口缺少显式鉴权注解

优先级：P1

### 现象

以下接口没有 `@PreAuthorize` 或 `@Anonymous`。它们仍会被 `SecurityConfig.anyRequest().authenticated()` 要求登录，但不符合仓库「不允许裸接口」的显式约束。

重点接口：

| 接口 | 文件和行号 | 建议分类 |
| --- | --- | --- |
| `GET /system/config/configKey/{configKey}` | `SysConfigController.java:72` | 建议补 `system:config:query` |
| `GET /system/dict/data/type/{dictType}` | `SysDictDataController.java:75` | 如果仅登录用户可用，补显式登录注解；若后台专用，补 `system:dict:query` |
| `GET /system/dict/type/optionselect` | `SysDictTypeController.java:125` | 补显式登录注解或 `system:dict:list` |
| `GET /system/menu/treeselect` | `SysMenuController.java:64` | 补 `system:menu:list` 或 `system:menu:query` |
| `GET /system/menu/roleMenuTreeselect/{roleId}` | `SysMenuController.java:75` | 补 `system:role:query` 或 `system:menu:query` |
| `GET /system/notice/published/{noticeId}` | `SysNoticeController.java:66` | 补显式登录注解 |
| `GET /system/notice/listTop` | `SysNoticeController.java:99` | 补显式登录注解 |
| `POST /system/notice/markRead` | `SysNoticeController.java:114` | 补显式登录注解并确保只能标记当前用户 |
| `POST /system/notice/markReadAll` | `SysNoticeController.java:126` | 补显式登录注解并校验当前用户 |
| `POST /system/user/importTemplate` | `SysUserController.java:102` | 补 `system:user:import` 或 `system:user:export` |
| `/test/user/*` 写接口 | `TestController.java:51-96` | 禁止生产暴露，建议 dev profile 或补完整权限 |

### 根因

若依原有代码里部分「下拉、当前用户、公共上传」接口依赖全局登录认证；但本仓库 AGENTS 规范进一步要求每个接口显式声明权限或匿名原因。

### 影响

- 权限策略不自解释。
- 后续如果安全配置变更，裸接口容易被误放开。
- 接口评审无法明确区分「登录用户可访问」和「后台权限访问」。

## 问题 5：数据监控 iframe 空白

优先级：P1

### 现象

`/monitor/druid` 页面只显示导航和面包屑，iframe 主体为空白。

### 复现步骤

1. 登录后台。
2. 打开「系统监控 / 数据监控」。
3. 页面主内容区为空白。
4. 带 token 直接访问 `http://localhost/dev-api/druid/login.html`，后端返回：

```json
{"msg":"No static resource druid/login.html for request '/druid/login.html'.","code":500}
```

### 证据

- `ui-admin/src/views/monitor/druid/index.vue:12` iframe 指向 `/dev-api/druid/login.html`。
- `manzhushaka-ry-admin/src/main/resources/application-dev.yml:45` 默认 `DRUID_STAT_VIEW_SERVLET_ENABLED:false`。
- `.codex-run/screenshots/deep-ui/route-数据监控菜单页.png`。

### 根因

Druid 监控 Servlet 在开发配置中默认关闭，导致 `/druid/*` 下没有真实资源。

### 影响

用户看到一个存在菜单但无法使用的监控页面。

## 问题 6：系统接口 iframe 空白

优先级：P1

### 现象

`/tool/swagger` 页面只显示导航和面包屑，iframe 主体为空白。

带 token 直接访问：

```text
/dev-api/swagger-ui/index.html -> No static resource index.html
/dev-api/swagger-ui.html       -> No static resource swagger-ui.html
/dev-api/v3/api-docs           -> OpenAPI JSON 正常返回
```

### 证据

- `ui-admin/src/views/tool/swagger/index.vue:8` iframe 指向 `/dev-api/swagger-ui/index.html`。
- `manzhushaka-ry-admin/src/main/resources/application.yml:123-125` 中 `springdoc.swagger-ui.enabled` 默认是 `false`，路径配置为 `/swagger-ui.html`。
- `manzhushaka-ry-framework/src/main/java/com/manzhushaka/framework/config/ResourcesConfig.java:36-39` 仍把 `/swagger-ui/**` 映射到旧的 `springfox-swagger-ui` webjar。
- `.codex-run/screenshots/deep-ui/route-系统接口菜单页.png`。

### 根因

项目已经使用 Springdoc OpenAPI，但资源映射和前端 iframe 仍混用了旧 Springfox 路径；同时 Swagger UI 默认未启用。

### 影响

接口文档入口不可用。虽然 `/v3/api-docs` 能返回 OpenAPI JSON，但普通后台用户无法从菜单查看接口文档。

## 问题 7：首页快捷入口日志路由错误

优先级：P1

### 现象

首页「快捷入口」中的 `操作日志`、`登录日志` 点击后进入 404。

### 证据

- `ui-admin/src/views/index.vue:200`：`操作日志` 指向 `/monitor/operlog`。
- `ui-admin/src/views/index.vue:201`：`登录日志` 指向 `/monitor/logininfor`。
- 当前动态菜单实际路由是：
  - `/system/log/operlog`
  - `/system/log/logininfor`
- `.codex-run/deep-ui-audit.json` 中 dashboard quick links 显示这两个入口 `has404:true`。

### 根因

首页快捷入口使用了旧路由，未跟随菜单结构变更同步更新。

### 影响

首页核心入口直接 404，影响日常运维入口可信度。

## 问题 8：移动端用户管理布局明显挤压

优先级：P2

### 现象

在 390 × 844 移动端视口下，首页、角色管理、操作日志、个人中心基本可用，但用户管理页面表格区域被压缩。

### 证据

- `.codex-run/deep-ui-audit.json`：用户管理移动端表格 rect 宽约 136 px。
- 截图：`.codex-run/screenshots/deep-ui/mobile-用户管理.png`。
- `ui-admin/src/views/system/user/index.vue:2-5` 使用树侧栏 + 内容区布局。
- `ui-admin/src/views/system/user/index.vue:6-20` 筛选项仍有固定宽度 `240px`、`308px`。

### 根因

`用户管理` 同时包含组织机构树、筛选表单、动作按钮和多列表格；移动端没有为树侧栏、表单和表格设置折叠或横向滚动策略。

### 影响

移动端和窄屏窗口下难以操作用户管理，尤其是筛选、表格列查看和行内操作。

## 问题 9：当前本地数据库存在无效定时任务

优先级：P2

### 现象

本地数据库中曾存在 `sys_job.job_id=100`，调用目标是 `bizReviewTask.processMaterialTransferTasks`，但代码中没有名为 `bizReviewTask` 的 Spring Bean。运行时日志出现过：

```text
No bean named 'bizReviewTask' available
```

该任务已在本地通过后台接口暂停，避免继续刷错误日志。

### 证据

- 代码中仅存在 `@Component("ryTask")`：`manzhushaka-ry-quartz/src/main/java/com/manzhushaka/quartz/task/ManzhushakaRyTask.java`。
- `sql/manzhushaka_db_init.sql:525-527` 初始化的 3 个任务均是 `ryTask.*`，不包含 `bizReviewTask`。

### 根因

当前本地数据库存在脚本外任务数据，业务 Bean 未提交或任务数据未清理。

### 影响

后端启动后定时任务可能持续报错，污染日志，并影响任务监控页面的可信度。

## 问题 10：Swagger 测试接口暴露策略不清晰

优先级：P2

### 现象

OpenAPI 配置只扫描 `com.manzhushaka.web.controller.tool`，并且 `TestController` 中只有列表接口带 `tool:test:list`，详情、新增、更新、删除接口没有显式权限。

### 证据

- `manzhushaka-ry-admin/src/main/resources/application.yml:127-131` 只扫描 tool 包。
- `manzhushaka-ry-admin/src/main/java/com/manzhushaka/web/controller/tool/TestController.java:41-43` 列表接口有权限。
- `TestController.java:51-96` 详情、新增、更新、删除接口无权限注解。
- `tool:test:list` 不在 `sql/manzhushaka_db_init.sql` 的 `sys_menu.perms` 中。

### 根因

测试接口和接口文档配置仍保留示例状态，没有与当前后台功能和权限体系统一。

### 影响

- 如果 Swagger UI 修好，文档默认只展示测试模块，不代表真实后台 API。
- 测试接口如果在生产可访问，会造成不必要的攻击面。

## 总体验证建议

修复后至少重新执行：

```bash
node .codex-run/static-audit.cjs
node .codex-run/deep-api-audit.cjs
node .codex-run/playwright-runner/deep-ui-audit.cjs
mvn test
cd ui-admin && npm run build:prod
```

验收口径：

- `missingViewComponents` 为 0，或未实现菜单不再出现在可见菜单中。
- `liveNotInSql` 为 0，或文档明确说明哪些菜单由单独迁移脚本维护。
- `frontendPermsMissingSql` 为 0。
- `controllerPermsMissingSql` 中不包含业务后台权限。
- `endpointsWithoutMethodPermission` 仅保留已明确标注为登录态公共能力或匿名能力的接口，并在代码中补注解或说明。
- 首页快捷入口不再 404。
- `数据监控`、`系统接口` 页面不再空白，或者菜单在对应环境隐藏。
- 390 px 移动端用户管理不出现内容被压缩到不可读的情况。
