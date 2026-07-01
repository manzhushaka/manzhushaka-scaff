# 后台管理系统问题修复指南

本文对应 `docs/audit/2026-06-28-admin-deep-audit.md`，按建议落地顺序给出修复方式。

## 修复顺序

建议按以下顺序处理：

1. 先处理菜单空白页和初始化 SQL 不一致。
2. 再处理权限闭环和裸接口。
3. 再处理 `数据监控`、`系统接口` iframe。
4. 最后处理首页快捷入口、移动端布局和本地脏数据。

这样做的原因是：菜单、SQL 和权限是底座；底座不稳定时，后续 UI 验证会反复出现环境差异。

## 1. 修复菜单空白页

### 推荐方案

对每个当前数据库已显示的菜单做取舍：

- 已经确定要交付：补齐 Vue 页面、API、权限、SQL。
- 暂不交付：从当前数据库和初始化 SQL 中隐藏或删除菜单。
- 需要保留占位：页面必须显示清晰的「功能建设中」或「暂无数据」状态，不能空白。

### 需要新增的页面文件

```text
ui-admin/src/views/biz/tradeFlow/index.vue
ui-admin/src/views/biz/order/index.vue
ui-admin/src/views/biz/review/index.vue
ui-admin/src/views/biz/order/material.vue
ui-admin/src/views/biz/installInfo/index.vue
ui-admin/src/views/biz/settlement/index.vue
ui-admin/src/views/biz/refund/index.vue
ui-admin/src/views/biz/order/rule.vue
ui-admin/src/views/biz/access/index.vue
ui-admin/src/views/biz/brand/index.vue
ui-admin/src/views/biz/order/transferTask.vue
ui-admin/src/views/biz/sku/index.vue
ui-admin/src/views/biz/sn/index.vue
ui-admin/src/views/biz/price/index.vue
ui-admin/src/views/biz/asyncImport/index.vue
ui-admin/src/views/tool/build/index.vue
ui-admin/src/views/tool/gen/index.vue
```

注意：`/goods/sku`、`/goods/sn`、`/goods/price` 当前后端路由组件分别指向 `biz/sku/index`、`biz/sn/index`、`biz/price/index`，不要误建到 `views/goods/**`，除非同步修改菜单组件路径。

### 页面最小实现标准

即使先做占位页，也建议满足：

- 使用 `app-container`，和现有后台页一致。
- 显示页面标题、查询区、操作区和空状态。
- 如果有按钮，必须带 `v-hasPermi`。
- 不要提交只有空 `template` 的页面。

示例结构：

```vue
<template>
  <div class="app-container">
    <el-empty description="功能建设中" />
  </div>
</template>

<script setup name="TradeFlow">
</script>
```

### 验证方式

```bash
cd ui-admin && npm run build:prod
node .codex-run/static-audit.cjs
```

预期：

- `missingViewComponents` 降为 0。
- 浏览器逐个打开上述菜单不再出现空白主内容区。

## 2. 同步初始化 SQL

### 需要维护的表

- `sys_menu`
- `sys_role_menu`

### 菜单 SQL 修复要点

在 `sql/manzhushaka_db_init.sql` 中补齐当前 live 路由。至少包括：

- 业务目录：以旧换新、商品管理、导入与导出。
- 业务菜单：交易流水、订单包管理、审核管理、订单资料、安装信息管理、补贴结算、退货退款台账、资料规则、接入方管理、品牌管理、资料转存任务。
- 商品菜单：SKU 管理、SN 码管理、价格管理。
- 导入导出菜单：异步导入任务。
- 日志子菜单：操作日志、登录日志。
- 系统工具：代码生成、系统接口。

### 权限 SQL 修复要点

补齐缺失权限对应的 `F` 类型按钮菜单，示例：

```sql
insert into sys_menu values('149', '用户导入', '100', '4', '', null, '', '', 1, 0, 'F', '0', '0', 'system:user:import', '#', 'admin', sysdate(), '', null, '用户导入按钮');
insert into sys_menu values('150', '用户导出', '100', '5', '', null, '', '', 1, 0, 'F', '0', '0', 'system:user:export', '#', 'admin', sysdate(), '', null, '用户导出按钮');
insert into sys_menu values('151', '重置密码', '100', '6', '', null, '', '', 1, 0, 'F', '0', '0', 'system:user:resetPwd', '#', 'admin', sysdate(), '', null, '重置密码按钮');
```

实际落地时不要直接照抄 ID，需要先确定当前 SQL 中最大 `menu_id`，按模块连续编号。

### 默认角色授权

如果普通角色也需要看到这些菜单或按钮，需要同步插入：

```sql
insert into sys_role_menu values ('2', '149');
insert into sys_role_menu values ('2', '150');
insert into sys_role_menu values ('2', '151');
```

如果只允许超级管理员访问，可以不插普通角色授权，但需要确保超级管理员逻辑确实拥有全部权限。

### 验证方式

```bash
node .codex-run/static-audit.cjs
```

预期：

- `liveNotInSql` 为 0。
- `frontendPermsMissingSql` 为 0。
- `controllerPermsMissingSql` 中不再包含系统管理、系统监控常规权限。

## 3. 补齐后端接口显式鉴权

### 管理后台接口

后台管理接口建议使用业务权限：

```java
@PreAuthorize("@ss.hasPermi('system:config:query')")
@GetMapping(value = "/configKey/{configKey}")
public AjaxResult getConfigKey(@PathVariable String configKey)
```

适合这样处理的接口：

- `/system/config/configKey/{configKey}`
- `/system/menu/treeselect`
- `/system/menu/roleMenuTreeselect/{roleId}`
- `/system/user/importTemplate`
- `/test/user/*` 如果保留在后台。

### 登录态公共接口

当前用户自服务接口、顶部公告接口不一定适合绑定某个菜单权限。建议明确为“已登录即可访问”。

如果项目接受 Spring Security 表达式，可以使用：

```java
@PreAuthorize("isAuthenticated()")
```

适合这样处理的接口：

- `/getInfo`
- `/getRouters`
- `/system/user/profile`
- `/system/user/profile/updatePwd`
- `/system/user/profile/avatar`
- `/system/notice/listTop`
- `/system/notice/markRead`
- `/system/notice/markReadAll`
- `/system/notice/published/{noticeId}`
- `/system/dict/data/type/{dictType}`
- `/system/dict/type/optionselect`
- `/system/post/optionselect`

如果团队不希望使用 `isAuthenticated()`，可以新增一个本项目统一注解或统一权限服务方法，例如 `@ss.isLogin()`，再统一应用。

### 测试接口处理

`TestController` 建议二选一：

- 只在 dev profile 启用：

```java
@Profile("dev")
@RestController
@RequestMapping("/test/user")
public class TestController {
}
```

- 或补完整权限：

```java
@PreAuthorize("@ss.hasPermi('tool:test:query')")
@GetMapping("/{userId}")

@PreAuthorize("@ss.hasPermi('tool:test:add')")
@PostMapping("/save")

@PreAuthorize("@ss.hasPermi('tool:test:edit')")
@PutMapping("/update")

@PreAuthorize("@ss.hasPermi('tool:test:remove')")
@DeleteMapping("/{userId}")
```

同时在 `sql/manzhushaka_db_init.sql` 增加 `tool:test:*` 菜单权限。

### 验证方式

```bash
node .codex-run/static-audit.cjs
mvn test
```

预期：

- `endpointsWithoutMethodPermission` 只保留明确允许的例外，最好为 0。
- 无新增 403 或接口文档扫描异常。

## 4. 修复数据监控

### 开发环境启用 Druid 控制台

在本地或 dev 环境启用：

```bash
DRUID_STAT_VIEW_SERVLET_ENABLED=true
DRUID_STAT_VIEW_ALLOW=127.0.0.1
DRUID_STAT_VIEW_USERNAME=admin
DRUID_STAT_VIEW_PASSWORD=change-me
```

也可以写入开发专用启动脚本，但不要提交真实密码。

### 前端页面

`ui-admin/src/views/monitor/druid/index.vue` 当前 URL 是：

```js
const url = ref(import.meta.env.VITE_APP_BASE_API + '/druid/login.html')
```

如果继续用 iframe，确认 `/dev-api/druid/login.html` 能直接加载页面，而不是返回 JSON 错误。

### 安全注意

- 生产环境建议保持关闭。
- 如果生产必须开启，应限制内网 IP、单独账号密码，并避免把 Druid 控制台暴露在公网。

### 验证方式

```bash
curl -I http://localhost/dev-api/druid/login.html
```

预期：

- 返回 HTML，而不是 `No static resource druid/login.html`。
- 后台「数据监控」页面 iframe 可见。

## 5. 修复系统接口

### 推荐统一到 Springdoc

后端配置建议：

```yaml
springdoc:
  api-docs:
    path: /v3/api-docs
  swagger-ui:
    enabled: ${SPRINGDOC_SWAGGER_UI_ENABLED:true}
    path: /swagger-ui.html
```

开发启动时可以设置：

```bash
SPRINGDOC_SWAGGER_UI_ENABLED=true
```

### 移除旧 Springfox 静态映射

`ResourcesConfig.java` 中这段是旧路径：

```java
registry.addResourceHandler("/swagger-ui/**")
    .addResourceLocations("classpath:/META-INF/resources/webjars/springfox-swagger-ui/")
```

使用 Springdoc 后建议删除，避免把 `/swagger-ui/**` 抢到不存在的 webjar 目录。

### 前端 iframe 路径

`ui-admin/src/views/tool/swagger/index.vue` 建议改成：

```js
import { ref } from 'vue'

const url = ref(import.meta.env.VITE_APP_BASE_API + '/swagger-ui.html')
```

如果 Springdoc 最终实际跳转到 `/swagger-ui/index.html`，可以保留 `/swagger-ui.html` 作为稳定入口。

### API 文档范围

当前 `application.yml` 只扫描：

```yaml
packages-to-scan: com.manzhushaka.web.controller.tool
```

如果「系统接口」期望展示完整后台接口，应改为覆盖系统、监控和定时任务 Controller，例如：

```yaml
packages-to-scan:
  - com.manzhushaka.web.controller
  - com.manzhushaka.quartz.controller
```

具体语法需要按当前 Springdoc 版本验证。

### 验证方式

```bash
curl -sS -H "Authorization: Bearer $TOKEN" http://localhost/dev-api/v3/api-docs | head
curl -I http://localhost/dev-api/swagger-ui.html
```

预期：

- `/v3/api-docs` 返回 OpenAPI JSON。
- `/swagger-ui.html` 或跳转后的 `/swagger-ui/index.html` 返回 HTML。
- 后台「系统接口」页面不空白。

## 6. 修复首页快捷入口

修改 `ui-admin/src/views/index.vue`：

```diff
- { label: '操作日志', route: '/monitor/operlog', icon: 'log', tone: 'primary' },
- { label: '登录日志', route: '/monitor/logininfor', icon: 'logininfor', tone: 'success' },
+ { label: '操作日志', route: '/system/log/operlog', icon: 'log', tone: 'primary' },
+ { label: '登录日志', route: '/system/log/logininfor', icon: 'logininfor', tone: 'success' },
```

备选方案是在路由层增加兼容重定向，但当前日志菜单已经归在 `系统管理 / 日志管理` 下，直接改首页入口更简单。

### 验证方式

1. 打开首页。
2. 点击 `操作日志`。
3. 确认 URL 是 `/system/log/operlog` 且列表正常。
4. 返回首页，点击 `登录日志`。
5. 确认 URL 是 `/system/log/logininfor` 且列表正常。

## 7. 修复移动端用户管理布局

### 推荐改法

在 768 px 以下做三件事：

1. 组织机构树默认折叠为抽屉或置顶折叠面板。
2. 筛选表单改为单列，输入框宽度 `100%`。
3. 表格保留横向滚动，不把表格压缩到不可读。

### 样式示例

可以在用户管理页面或全局后台样式中增加：

```scss
@media (max-width: 768px) {
  .tree-sidebar-manage-wrap {
    display: block;
  }

  .tree-sidebar-content {
    width: 100%;
  }

  .ui-filter-card {
    .el-form-item {
      display: block;
      margin-right: 0;

      .el-input,
      .el-select,
      .el-date-editor {
        width: 100% !important;
      }
    }
  }

  .ui-table-card {
    overflow-x: auto;
  }

  .ui-table-card .el-table {
    min-width: 760px;
  }
}
```

### 更好的交互方案

对移动端用户管理，建议做“管理后台窄屏模式”：

- 顶部只显示关键筛选：用户名、手机号、状态。
- 其他筛选折叠到「更多筛选」。
- 表格默认显示用户名称、状态、操作，其他列通过列设置或详情页查看。
- 组织机构树放入按钮触发的侧边抽屉。

### 验证方式

```bash
node .codex-run/playwright-runner/deep-ui-audit.cjs
```

预期：

- 390 px 视口下用户管理不再出现 136 px 左右的窄表格。
- 表格可横向滚动或列已响应式简化。
- 操作按钮不重叠。

## 8. 清理或补齐无效定时任务

### 如果任务已废弃

在数据库迁移或初始化脚本中移除/暂停该任务：

```sql
update sys_job
set status = '1', update_by = 'admin', update_time = sysdate()
where invoke_target = 'bizReviewTask.processMaterialTransferTasks';
```

如果要删除，先查询确认：

```sql
select job_id, job_name, invoke_target, status
from sys_job
where invoke_target = 'bizReviewTask.processMaterialTransferTasks';
```

### 如果任务需要保留

新增对应 Spring Bean：

```java
@Component("bizReviewTask")
public class BizReviewTask {
    public void processMaterialTransferTasks() {
        // 实现资料转存任务
    }
}
```

同时补单元测试或集成测试，至少验证 Bean 名称和方法签名能被 `ScheduleUtils` 调用。

### 验证方式

1. 启动后端。
2. 查看 `sys-error.log` 或控制台。
3. 等待一个调度周期。
4. 不再出现 `No bean named 'bizReviewTask' available`。

## 9. 建议新增持续审计脚本到团队流程

当前 `.codex-run/static-audit.cjs` 已能检查：

- live 路由对应组件是否存在。
- live 路由是否写入初始化 SQL。
- 前端按钮权限是否写入 SQL。
- 后端接口权限是否写入 SQL。
- Controller 是否缺少显式鉴权注解。

建议后续把它整理为正式脚本，例如：

```text
scripts/audit/admin-static-audit.cjs
```

并在 CI 中执行：

```bash
node scripts/audit/admin-static-audit.cjs
```

CI 失败条件建议：

- 可见菜单组件缺失。
- 前端权限缺少 SQL。
- 后端业务权限缺少 SQL。
- 新增 Controller 方法没有 `@PreAuthorize` 或 `@Anonymous`。

这样可以避免这次的问题再次堆积到运行时才暴露。
