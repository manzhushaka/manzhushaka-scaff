# 日志采集机制收敛与统一日志入口实现计划

> **面向 AI 代理的工作者：** 必需子技能：使用 superpowers:subagent-driven-development（推荐）或 superpowers:executing-plans 逐任务实现此计划。步骤使用复选框（`- [ ]`）语法来跟踪进度。

**目标：** 删除请求日志采集与展示链路，仅保留 `@Log` 操作日志和现有登录日志，并将前端日志入口收敛为一个统一日志页面。

**架构：** 后端移除 `RequestLogInterceptor`、请求日志实体、Service、Controller 与初始化 SQL，保留 `@Log` 审计日志和登录日志链路。前端新增统一日志页，页面内用标签页承载操作日志与登录日志，侧边栏和首页快捷入口只保留一个日志入口。

**技术栈：** Spring Boot、Spring MVC、AOP、MyBatis XML、Vue 3、Element Plus、Vite、现有权限插件与动态路由体系。

---

## 文件结构与职责

### 删除或停用的后端文件

- 删除：`manzhushaka-ry-framework/src/main/java/com/manzhushaka/framework/interceptor/RequestLogInterceptor.java`
- 删除：`manzhushaka-ry-framework/src/main/java/com/manzhushaka/framework/web/command/RequestLogRecord.java`
- 修改：`manzhushaka-ry-framework/src/main/java/com/manzhushaka/framework/config/ResourcesConfig.java`
- 修改：`manzhushaka-ry-framework/src/main/java/com/manzhushaka/framework/manager/factory/AsyncFactory.java`
- 修改：`manzhushaka-ry-system/src/main/java/com/manzhushaka/system/application/service/SystemAuditAppService.java`
- 修改：`manzhushaka-ry-system/src/main/java/com/manzhushaka/system/application/service/impl/SystemAuditAppServiceImpl.java`
- 删除：`manzhushaka-ry-system/src/main/java/com/manzhushaka/system/domain/SysRequestLog.java`
- 删除：`manzhushaka-ry-system/src/main/java/com/manzhushaka/system/mapper/SysRequestLogMapper.java`
- 删除：`manzhushaka-ry-system/src/main/java/com/manzhushaka/system/service/ISysRequestLogService.java`
- 删除：`manzhushaka-ry-system/src/main/java/com/manzhushaka/system/service/impl/SysRequestLogServiceImpl.java`
- 删除：`manzhushaka-ry-system/src/main/resources/mapper/system/SysRequestLogMapper.xml`
- 删除：`manzhushaka-ry-admin/src/main/java/com/manzhushaka/web/controller/monitor/SysRequestLogController.java`
- 删除：`manzhushaka-ry-framework/src/test/java/com/manzhushaka/framework/interceptor/RequestLogInterceptorTest.java`

### 保留并复用的后端文件

- 保留：`manzhushaka-ry-framework/src/main/java/com/manzhushaka/framework/aspectj/LogAspect.java`
- 保留：`manzhushaka-ry-admin/src/main/java/com/manzhushaka/web/controller/monitor/SysOperlogController.java`
- 保留：`manzhushaka-ry-admin/src/main/java/com/manzhushaka/web/controller/monitor/SysLogininforController.java`

### 前端相关文件

- 删除：`ui-admin/src/api/monitor/requestLog.js`
- 删除：`ui-admin/src/views/monitor/requestLog/index.vue`
- 修改：`ui-admin/src/views/monitor/operlog/index.vue`
- 修改：`ui-admin/src/views/monitor/operlog/detail.vue`
- 修改：`ui-admin/src/views/monitor/logininfor/index.vue`
- 新增：`ui-admin/src/views/monitor/logCenter/index.vue`
- 修改：`ui-admin/src/views/index.vue`

### SQL 文件

- 修改：`sql/ry_20260417.sql`

## 任务 1：删除后端请求日志采集入口

**文件：**

- 删除：`manzhushaka-ry-framework/src/main/java/com/manzhushaka/framework/interceptor/RequestLogInterceptor.java`
- 删除：`manzhushaka-ry-framework/src/main/java/com/manzhushaka/framework/web/command/RequestLogRecord.java`
- 修改：`manzhushaka-ry-framework/src/main/java/com/manzhushaka/framework/config/ResourcesConfig.java`

- [ ] **步骤 1：确认请求日志拦截器仅由 `ResourcesConfig` 注册**

运行：

```bash
rg -n "RequestLogInterceptor|addInterceptor\\(requestLogInterceptor\\)" manzhushaka-ry-framework/src/main/java
```

预期输出包含：

```text
ResourcesConfig.java
RequestLogInterceptor.java
registry.addInterceptor(requestLogInterceptor).addPathPatterns("/**");
```

- [ ] **步骤 2：删除 `RequestLogInterceptor` 字段注入与拦截器注册**

将 `ResourcesConfig.java` 中：

```java
import com.manzhushaka.framework.interceptor.RequestLogInterceptor;
...
@Autowired
private RequestLogInterceptor requestLogInterceptor;
...
registry.addInterceptor(requestLogInterceptor).addPathPatterns("/**");
registry.addInterceptor(repeatSubmitInterceptor).addPathPatterns("/**");
```

改为：

```java
import com.manzhushaka.framework.interceptor.RepeatSubmitInterceptor;
...
@Autowired
private RepeatSubmitInterceptor repeatSubmitInterceptor;
...
registry.addInterceptor(repeatSubmitInterceptor).addPathPatterns("/**");
```

- [ ] **步骤 3：删除请求日志拦截器与命令对象文件**

删除：

```text
manzhushaka-ry-framework/src/main/java/com/manzhushaka/framework/interceptor/RequestLogInterceptor.java
manzhushaka-ry-framework/src/main/java/com/manzhushaka/framework/web/command/RequestLogRecord.java
```

- [ ] **步骤 4：运行编译范围检查**

运行：

```bash
rg -n "RequestLogInterceptor|RequestLogRecord" manzhushaka-ry-framework/src/main/java manzhushaka-ry-admin/src/main/java manzhushaka-ry-system/src/main/java
```

预期：只剩下待后续任务清理的少量引用，不再出现拦截器注册代码。

## 任务 2：删除请求日志异步写库与应用服务接口

**文件：**

- 修改：`manzhushaka-ry-framework/src/main/java/com/manzhushaka/framework/manager/factory/AsyncFactory.java`
- 修改：`manzhushaka-ry-system/src/main/java/com/manzhushaka/system/application/service/SystemAuditAppService.java`
- 修改：`manzhushaka-ry-system/src/main/java/com/manzhushaka/system/application/service/impl/SystemAuditAppServiceImpl.java`

- [ ] **步骤 1：删除 `AsyncFactory.recordRequest(...)`**

从 `AsyncFactory.java` 中删除：

```java
public static TimerTask recordRequest(final RequestLogRecord requestLog)
{
    return new TimerTask()
    {
        @Override
        public void run()
        {
            SpringUtils.getBean(SystemAuditAppService.class).recordRequestLog(
                    requestLog.requestUri(), requestLog.requestMethod(), requestLog.controllerMethod(),
                    requestLog.queryString(), requestLog.requestParams(), requestLog.ipaddr(),
                    requestLog.userName(), requestLog.statusCode(), requestLog.status(),
                    requestLog.errorMsg(), requestLog.userAgent(), requestLog.costTime(),
                    requestLog.requestTime());
        }
    };
}
```

- [ ] **步骤 2：删除 `SystemAuditAppService.recordRequestLog(...)` 接口**

从接口中删除：

```java
void recordRequestLog(String requestUri, String requestMethod, String controllerMethod, String queryString,
                      String requestParams, String ipaddr, String userName, Integer statusCode,
                      Integer status, String errorMsg, String userAgent, Long costTime,
                      java.util.Date requestTime);
```

- [ ] **步骤 3：删除 `SystemAuditAppServiceImpl.recordRequestLog(...)` 实现**

从实现类中删除：

```java
@Override
public void recordRequestLog(String requestUri, String requestMethod, String controllerMethod, String queryString,
                              String requestParams, String ipaddr, String userName, Integer statusCode,
                              Integer status, String errorMsg, String userAgent, Long costTime,
                              java.util.Date requestTime)
{
    SysRequestLog requestLog = new SysRequestLog();
    ...
    requestLogService.insertRequestLog(requestLog);
}
```

同时删除：

```java
import com.manzhushaka.system.domain.SysRequestLog;
import com.manzhushaka.system.service.ISysRequestLogService;
...
@Autowired
private ISysRequestLogService requestLogService;
```

- [ ] **步骤 4：运行引用检查**

运行：

```bash
rg -n "recordRequestLog|recordRequest\\(" manzhushaka-ry-framework/src/main/java manzhushaka-ry-system/src/main/java
```

预期：无结果。

## 任务 3：删除请求日志领域模型、服务、Mapper 与控制器

**文件：**

- 删除：`manzhushaka-ry-system/src/main/java/com/manzhushaka/system/domain/SysRequestLog.java`
- 删除：`manzhushaka-ry-system/src/main/java/com/manzhushaka/system/mapper/SysRequestLogMapper.java`
- 删除：`manzhushaka-ry-system/src/main/java/com/manzhushaka/system/service/ISysRequestLogService.java`
- 删除：`manzhushaka-ry-system/src/main/java/com/manzhushaka/system/service/impl/SysRequestLogServiceImpl.java`
- 删除：`manzhushaka-ry-system/src/main/resources/mapper/system/SysRequestLogMapper.xml`
- 删除：`manzhushaka-ry-admin/src/main/java/com/manzhushaka/web/controller/monitor/SysRequestLogController.java`

- [ ] **步骤 1：确认请求日志领域文件完整列表**

运行：

```bash
rg --files manzhushaka-ry-system/src/main/java manzhushaka-ry-system/src/main/resources manzhushaka-ry-admin/src/main/java | rg "SysRequestLog|requestLog"
```

预期输出包含上述 6 个文件。

- [ ] **步骤 2：删除请求日志领域与控制器文件**

删除：

```text
manzhushaka-ry-system/src/main/java/com/manzhushaka/system/domain/SysRequestLog.java
manzhushaka-ry-system/src/main/java/com/manzhushaka/system/mapper/SysRequestLogMapper.java
manzhushaka-ry-system/src/main/java/com/manzhushaka/system/service/ISysRequestLogService.java
manzhushaka-ry-system/src/main/java/com/manzhushaka/system/service/impl/SysRequestLogServiceImpl.java
manzhushaka-ry-system/src/main/resources/mapper/system/SysRequestLogMapper.xml
manzhushaka-ry-admin/src/main/java/com/manzhushaka/web/controller/monitor/SysRequestLogController.java
```

- [ ] **步骤 3：删除请求日志相关测试文件**

若存在请求日志专属测试文件，删除它们。对当前已定位文件，先删除：

```text
manzhushaka-ry-framework/src/test/java/com/manzhushaka/framework/interceptor/RequestLogInterceptorTest.java
```

- [ ] **步骤 4：运行全仓残余引用扫描**

运行：

```bash
rg -n "SysRequestLog|requestLog|RequestLogInterceptor" manzhushaka-ry-admin manzhushaka-ry-framework manzhushaka-ry-system ui-admin sql
```

预期：只剩前端请求日志页、请求日志 API、SQL 和文档中的引用，供后续任务继续清理。

## 任务 4：清理初始化 SQL 中的请求日志表与菜单权限

**文件：**

- 修改：`sql/ry_20260417.sql`

- [ ] **步骤 1：删除请求日志页面菜单与按钮权限**

从菜单区域删除：

```sql
insert into sys_menu values('161',  '请求日志', '108', '1', 'requestLog', 'monitor/requestLog/index', '', '', 1, 0, 'C', '0', '0', 'monitor:requestlog:list', 'form',          'admin', sysdate(), '', null, '请求日志菜单');
```

从按钮区域删除：

```sql
insert into sys_menu values('164', '请求日志查询', '161', '1', '', null, '', '', 1, 0, 'F', '0', '0', 'monitor:requestlog:list',  '#',                'admin', sysdate(), '', null, '请求日志查询按钮');
insert into sys_menu values('165', '请求日志详情', '161', '2', '', null, '', '', 1, 0, 'F', '0', '0', 'monitor:requestlog:query', '#',                'admin', sysdate(), '', null, '请求日志详情按钮');
insert into sys_menu values('166', '请求日志删除', '161', '3', '', null, '', '', 1, 0, 'F', '0', '0', 'monitor:requestlog:remove','#',                'admin', sysdate(), '', null, '请求日志删除按钮');
insert into sys_menu values('167', '请求日志导出', '161', '4', '', null, '', '', 1, 0, 'F', '0', '0', 'monitor:requestlog:export','#',                'admin', sysdate(), '', null, '请求日志导出按钮');
```

- [ ] **步骤 2：新增统一日志菜单并保留操作日志、登录日志按钮权限**

将日志中心下页面级菜单收敛为一个。例如新增或替换为：

```sql
insert into sys_menu values('161',  '统一日志', '108', '1', 'logCenter', 'monitor/logCenter/index', '', '', 1, 0, 'C', '0', '0', 'monitor:operlog:list', 'log', 'admin', sysdate(), '', null, '统一日志菜单');
```

并调整原操作日志、登录日志按钮权限的 `parent_id` 到 `161`，确保统一页仍能收到按钮权限集合。

- [ ] **步骤 3：删除请求日志建表 SQL**

从表结构区域删除：

```sql
drop table if exists sys_request_log;
create table sys_request_log (
  ...
) engine=innodb auto_increment=100 comment = '请求日志记录';
```

- [ ] **步骤 4：运行 SQL 关键字检查**

运行：

```bash
rg -n "requestlog|请求日志|sys_request_log|monitor/requestLog" sql/ry_20260417.sql
```

预期：无结果。

## 任务 5：删除前端请求日志 API 与页面

**文件：**

- 删除：`ui-admin/src/api/monitor/requestLog.js`
- 删除：`ui-admin/src/views/monitor/requestLog/index.vue`

- [ ] **步骤 1：确认请求日志前端文件**

运行：

```bash
rg --files ui-admin/src | rg "requestLog"
```

预期输出包含：

```text
ui-admin/src/api/monitor/requestLog.js
ui-admin/src/views/monitor/requestLog/index.vue
```

- [ ] **步骤 2：删除请求日志 API 与页面**

删除：

```text
ui-admin/src/api/monitor/requestLog.js
ui-admin/src/views/monitor/requestLog/index.vue
```

- [ ] **步骤 3：检查前端残余引用**

运行：

```bash
rg -n "requestLog|monitor/requestLog" ui-admin/src
```

预期：无结果。

## 任务 6：新增统一日志页面并承接操作日志与登录日志

**文件：**

- 新增：`ui-admin/src/views/monitor/logCenter/index.vue`
- 读取并复用：`ui-admin/src/views/monitor/operlog/index.vue`
- 读取并复用：`ui-admin/src/views/monitor/logininfor/index.vue`
- 读取并复用：`ui-admin/src/views/monitor/operlog/detail.vue`
- 读取并复用：`ui-admin/src/plugins/auth.js`

- [ ] **步骤 1：创建统一日志页基础结构**

创建文件：

```vue
<template>
  <div class="app-container">
    <div class="ui-page-head">
      <div>
        <h2 class="ui-page-title">统一日志</h2>
        <p class="ui-page-desc">集中查看后台操作审计与登录访问记录。</p>
      </div>
    </div>

    <el-tabs v-model="activeTab" class="log-center-tabs">
      <el-tab-pane
        v-if="canViewOperlog"
        label="操作日志"
        name="operlog"
      />
      <el-tab-pane
        v-if="canViewLogininfor"
        label="登录日志"
        name="logininfor"
      />
    </el-tabs>

    <div v-if="activeTab === 'operlog' && canViewOperlog">
      <!-- 操作日志区域 -->
    </div>

    <div v-if="activeTab === 'logininfor' && canViewLogininfor">
      <!-- 登录日志区域 -->
    </div>
  </div>
</template>

<script setup name="LogCenter">
import auth from '@/plugins/auth'

const canViewOperlog = auth.hasPermi('monitor:operlog:list')
const canViewLogininfor = auth.hasPermi('monitor:logininfor:list')

const activeTab = ref(canViewOperlog ? 'operlog' : 'logininfor')
</script>
```

- [ ] **步骤 2：将操作日志查询区域与表格逻辑迁入统一页**

把 `ui-admin/src/views/monitor/operlog/index.vue` 中以下逻辑迁入统一页的操作日志分支：

```vue
<el-form ...>...</el-form>
<el-row ...>...</el-row>
<div class="ui-table-card">...</div>
<pagination ... />
<operlog-detail ... />
```

并迁入对应 script 状态：

```js
const operlogList = ref([])
const detailVisible = ref(false)
const detailRow = ref({})
const operlogDateRange = ref([])
const operlogQueryParams = ref({...})
function getOperlogList() { ... }
function handleOperlogQuery() { ... }
```

要求：

- 避免与登录日志状态变量重名
- 继续复用 `useDict("sys_oper_type", "sys_common_status")`
- 继续复用 `@/api/monitor/operlog`

- [ ] **步骤 3：将登录日志查询区域与表格逻辑迁入统一页**

把 `ui-admin/src/views/monitor/logininfor/index.vue` 中以下逻辑迁入统一页的登录日志分支：

```vue
<el-form ...>...</el-form>
<el-row ...>...</el-row>
<div class="ui-table-card">...</div>
<pagination ... />
```

并迁入对应 script 状态：

```js
const logininforList = ref([])
const logininforDateRange = ref([])
const logininforQueryParams = ref({...})
function getLogininforList() { ... }
function handleLogininforQuery() { ... }
```

要求：

- 保留登录日志的解锁操作
- 继续复用 `@/api/monitor/logininfor`
- 不新增登录日志详情弹窗

- [ ] **步骤 4：补齐标签页初始化和切换加载**

在统一页中增加：

```js
watch(activeTab, (tab) => {
  if (tab === 'operlog' && canViewOperlog) {
    getOperlogList()
  }
  if (tab === 'logininfor' && canViewLogininfor) {
    getLogininforList()
  }
}, { immediate: true })
```

并增加兜底：

```js
if (!canViewOperlog && !canViewLogininfor) {
  activeTab.value = ''
}
```

- [ ] **步骤 5：构建统一页样式**

在统一页 `style scoped` 中增加最少样式：

```scss
.log-center-tabs {
  margin-bottom: 12px;
}
```

只做必要补充，优先复用现有 `ui-filter-card`、`ui-action-bar`、`ui-table-card` 样式，不做过度美化。

## 任务 7：收敛原页面引用与首页快捷入口

**文件：**

- 修改：`ui-admin/src/views/index.vue`
- 可选删除或留空：`ui-admin/src/views/monitor/operlog/index.vue`
- 可选删除或留空：`ui-admin/src/views/monitor/logininfor/index.vue`

- [ ] **步骤 1：收敛首页快捷入口**

将：

```js
{ label: '操作日志', route: '/system/log/operlog', icon: 'log', tone: 'primary' },
{ label: '登录日志', route: '/system/log/logininfor', icon: 'logininfor', tone: 'success' },
```

改为：

```js
{ label: '日志中心', route: '/monitor/logCenter', icon: 'log', tone: 'primary' },
```

- [ ] **步骤 2：确认动态路由组件路径匹配**

统一日志菜单 SQL 使用组件路径：

```text
monitor/logCenter/index
```

因此需确保文件存在：

```text
ui-admin/src/views/monitor/logCenter/index.vue
```

- [ ] **步骤 3：检查旧页面是否仍被其他路由引用**

运行：

```bash
rg -n "monitor/operlog/index|monitor/logininfor/index|/system/log/operlog|/system/log/logininfor" ui-admin/src sql/ry_20260417.sql
```

预期：除计划中的兼容性引用外，页面级入口均已改为统一日志页。

## 任务 8：运行验证

**文件：**

- 验证：全仓

- [ ] **步骤 1：运行后端测试或至少编译**

运行：

```bash
mvn test
```

若全量测试耗时过长或存在与本次无关失败，再运行：

```bash
mvn clean package -DskipTests
```

预期：请求日志相关类删除后，Java 编译通过。

- [ ] **步骤 2：运行前端生产构建**

运行：

```bash
cd ui-admin && npm run build:prod
```

预期：退出码为 0。

- [ ] **步骤 3：运行残余引用检查**

运行：

```bash
rg -n "SysRequestLog|RequestLogInterceptor|monitor/requestLog|requestlog|sys_request_log" manzhushaka-ry-admin manzhushaka-ry-framework manzhushaka-ry-system ui-admin sql
```

预期：业务代码与初始化 SQL 中无残余实现引用；若只剩设计文档引用，属于可接受结果。

- [ ] **步骤 4：整理 git 变更并准备提交**

运行：

```bash
git status --short
```

预期：只包含本次日志收敛相关文件。

- [ ] **步骤 5：提交变更**

运行：

```bash
git add manzhushaka-ry-framework manzhushaka-ry-system manzhushaka-ry-admin ui-admin sql docs/superpowers/specs/2026-06-30-unify-log-collection-design.md docs/superpowers/plans/2026-06-30-unify-log-collection-plan.md
git commit -m "refactor(日志中心): 收敛请求日志采集并统一日志入口"
```

预期：生成一条只包含本次日志机制收敛与入口统一的提交。

## 自检

- 规格中要求删除请求日志的所有层次，计划均已覆盖：采集、异步工厂、应用服务、实体、Mapper、Controller、前端 API、前端页面、SQL、菜单。
- 规格中要求保留操作日志与登录日志，计划中没有触碰其核心采集链路。
- 规格中要求只保留一个日志入口，计划已覆盖统一日志页、菜单 SQL 与首页快捷入口。
- 计划没有保留“待定”“后续处理”占位符；每个关键改动都给出了精确文件路径和验证命令。

计划已完成并保存到 `docs/superpowers/plans/2026-06-30-unify-log-collection-plan.md`。两种执行方式：

**1. 子代理驱动（推荐）** - 每个任务调度一个新的子代理，任务间进行审查，快速迭代

**2. 内联执行** - 在当前会话中使用 executing-plans 执行任务，批量执行并设有检查点

选哪种方式？
