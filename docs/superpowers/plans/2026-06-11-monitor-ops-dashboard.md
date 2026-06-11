# 运行监控运维总览实现计划

> **面向 AI 代理的工作者：** 必需子技能：使用 superpowers:subagent-driven-development（推荐）或 superpowers:executing-plans 逐任务实现此计划。步骤使用复选框（`- [ ]`）语法来跟踪进度。

**目标：** 在现有“运行监控”页上扩展缓存监控、任务健康、消息积压、慢 SQL 和在线日志能力，并保持现有管理台 UI 风格一致。

**架构：** 后端继续沿用 `/system/monitor` 这条接口边界，在 `manzhushaka-system` 中聚合任务、消息和运行态数据；慢 SQL 采集器放在 `manzhushaka-db`，在线日志缓冲放在 `manzhushaka-framework`。前端复用现有 `monitor.vue` 路由入口，改为“总览 + 分区标签”布局，并提供到缓存、任务、消息页的钻取入口。

**技术栈：** Spring Boot 3、MyBatis-Plus、Redis、Vue 3、TypeScript、Arco Design Vue

---

### 任务 1：定义监控聚合数据结构与接口边界

**文件：**
- 修改：`manzhushaka-system/src/main/java/com/manzhushaka/system/vo/monitor/ServerMonitorVO.java`
- 修改：`manzhushaka-system/src/main/java/com/manzhushaka/system/controller/ServerMonitorController.java`
- 修改：`ui-admin/src/types/system.ts`
- 修改：`ui-admin/src/api/system.ts`
- 测试：`manzhushaka-system/src/test/java/com/manzhushaka/system/controller/ServerMonitorControllerTest.java`

- [ ] 扩展 `ServerMonitorVO`，增加缓存指标、任务健康、消息积压、慢 SQL 摘要与在线日志摘要字段
- [ ] 在 `ServerMonitorController` 中补充慢 SQL 列表与在线日志 tail 查询接口
- [ ] 在前端类型和 API 层补齐新增返回结构与查询方法
- [ ] 运行 `mvn -pl manzhushaka-system test -Dtest=ServerMonitorControllerTest`

### 任务 2：补慢 SQL 采集器与在线日志缓冲

**文件：**
- 创建：`manzhushaka-db/src/main/java/com/manzhushaka/db/monitor/SlowSqlRecord.java`
- 创建：`manzhushaka-db/src/main/java/com/manzhushaka/db/monitor/SlowSqlMonitorStore.java`
- 创建：`manzhushaka-db/src/main/java/com/manzhushaka/db/monitor/SlowSqlMonitorInterceptor.java`
- 修改：`manzhushaka-db/src/main/java/com/manzhushaka/db/config/MybatisPlusConfig.java`
- 创建：`manzhushaka-db/src/test/java/com/manzhushaka/db/monitor/SlowSqlMonitorInterceptorTest.java`
- 创建：`manzhushaka-framework/src/main/java/com/manzhushaka/framework/monitor/AppLogEntry.java`
- 创建：`manzhushaka-framework/src/main/java/com/manzhushaka/framework/monitor/ApplicationLogBuffer.java`
- 创建：`manzhushaka-framework/src/main/java/com/manzhushaka/framework/monitor/ApplicationLogAppenderBinder.java`
- 创建：`manzhushaka-framework/src/test/java/com/manzhushaka/framework/monitor/ApplicationLogBufferTest.java`

- [ ] 先写慢 SQL 采集器失败测试，验证超阈值 SQL 会写入环形缓冲、普通 SQL 不会写入
- [ ] 实现 `SlowSqlMonitorStore` 与 `SlowSqlMonitorInterceptor`，注册到 MyBatis 插件链
- [ ] 先写在线日志缓冲失败测试，验证日志会按顺序进入固定长度缓冲
- [ ] 实现 `ApplicationLogBuffer` 与启动时绑定到 root logger 的 binder
- [ ] 运行 `mvn -pl manzhushaka-db test -Dtest=SlowSqlMonitorInterceptorTest`
- [ ] 运行 `mvn -pl manzhushaka-framework test -Dtest=ApplicationLogBufferTest`

### 任务 3：扩展监控聚合服务

**文件：**
- 修改：`manzhushaka-system/src/main/java/com/manzhushaka/system/service/impl/ServerMonitorService.java`
- 创建：`manzhushaka-system/src/test/java/com/manzhushaka/system/service/impl/ServerMonitorServiceMonitorOpsTest.java`

- [ ] 先写失败测试，覆盖缓存统计、任务健康、消息积压、慢 SQL 摘要和日志摘要聚合结果
- [ ] 在 `ServerMonitorService` 中注入 `SysJobMapper`、`SysJobLogMapper`、`SysMqMessageMapper`、`SlowSqlMonitorStore`、`ApplicationLogBuffer`
- [ ] 实现缓存 stats 扩展、任务统计汇总、消息状态聚合、慢 SQL 摘要和日志 tail 查询
- [ ] 运行 `mvn -pl manzhushaka-system test -Dtest=ServerMonitorServiceTest,ServerMonitorServiceMonitorOpsTest`

### 任务 4：重构运行监控页 UI

**文件：**
- 修改：`ui-admin/src/views/system/monitor.vue`
- 修改：`ui-admin/src/types/system.ts`
- 修改：`ui-admin/src/api/system.ts`

- [ ] 将页面改为“总览 + 分区标签”结构，保留现有运行态视觉风格
- [ ] 在总览区展示核心健康卡片、异常列表与钻取入口
- [ ] 在缓存、任务、消息、SQL、日志标签下接入对应数据与交互
- [ ] 为所有新增按钮补 `v-permission`
- [ ] 运行 `cd ui-admin && pnpm build`

### 任务 5：补权限与初始化 SQL 并完成回归验证

**文件：**
- 修改：`sql/manzhushaka_init.sql`
- 视需要修改：`ui-admin/src/api/mock.ts`

- [ ] 为慢 SQL 查看、在线日志刷新等新增按钮权限补初始化 SQL
- [ ] 同步补 mock 或静态数据，避免 `VITE_USE_MOCK=true` 页面崩溃
- [ ] 运行 `mvn -pl manzhushaka-system -am test`
- [ ] 运行 `cd ui-admin && pnpm build`

