# 日志采集机制收敛与统一日志入口设计规格

## 背景

当前仓库在日志相关能力上同时存在两条采集链路：

- 操作日志：基于 `@Log` 注解和 `LogAspect` 记录业务审计信息。
- 请求日志：基于 `RequestLogInterceptor` 统一拦截请求并记录请求级信息。

这两条链路在前端又分别暴露为「操作日志」「请求日志」页面，另外还存在独立的「登录日志」页面。对于后台使用者来说，入口偏多，字段又有明显重叠；对于代码实现来说，日志采集机制也被拆成了多条链路，维护成本偏高。

经确认，本次需求的目标不是保留全量 HTTP 请求流水，而是收敛到更明确的后台审计能力：

- 代码实现层面，只继续保留一种请求审计采集机制。
- 菜单层面，只保留一个日志入口。
- 登录日志链路本轮暂不重构为 `@Log`，继续保留现有实现。

## 范围

本规格覆盖以下内容：

- 移除 `RequestLogInterceptor` 及其请求日志写库链路。
- 下线请求日志后端接口、前端 API、前端页面和初始化菜单权限。
- 保留 `@Log` 注解采集的操作日志链路。
- 保留当前登录日志链路和登录日志相关接口。
- 将前端日志入口收敛为一个统一日志页面，在页面内展示「操作日志 / 登录日志」两个标签页。
- 同步维护 `sql/manzhushaka_db_init.sql` 中的菜单、按钮权限和请求日志表定义。

本规格不包含：

- 将登录日志采集机制改造为 `@Log`。
- 将操作日志和登录日志合并为同一张数据库表。
- 为所有查询接口补充 `@Log`，以恢复请求日志的全量覆盖能力。
- 对 `@Log` 注解模型做字段级扩展，例如增加 `statusCode`、`userAgent` 等请求日志字段。

## 已确认设计决策

- 代码采集机制收敛为：操作审计使用 `@Log`，登录日志仍保留现有登录链路；请求日志机制移除。
- 菜单入口收敛为一个统一日志页面。
- 统一日志页面内使用两个标签页：`操作日志`、`登录日志`。
- 登录日志接口、权限和表结构本轮不改，只调整前端入口承载方式。
- 请求日志相关表、接口、页面和菜单初始化 SQL 一并移除。

## 当前实现与问题

### 操作日志

当前操作日志由 `com.manzhushaka.common.annotation.Log` 注解声明，在 `LogAspect` 中记录：

- 模块标题
- 业务类型
- 操作人
- 请求地址
- 请求参数
- 返回参数
- 执行状态
- 错误信息
- 执行耗时

该链路对应现有操作日志页面和 `sys_oper_log` 表，属于明确的业务审计能力。

### 请求日志

当前请求日志由 `RequestLogInterceptor` 在 `ResourcesConfig` 中注册，对所有请求执行统一拦截，记录：

- 请求路径
- 请求方式
- 控制器方法
- Query String
- 请求参数
- IP
- 用户账号
- HTTP 状态码
- User-Agent
- 请求时间
- 执行耗时

该链路是 2026-06-29 的后续新增能力，不属于仓库初始化时的基础能力。它带来了请求级排障信息，但也引入了与操作日志高度重叠的页面和数据模型。

### 登录日志

登录日志不走 `@Log`，而是通过登录/退出链路中的异步记录逻辑写入 `sys_logininfor`。该链路本轮不改，因为它与账户安全、登录失败、账号解锁等行为绑定更紧，贸然改为 `@Log` 反而会扩大范围。

## 总体方案

本轮采用「删除请求日志链路 + 保留审计日志和登录日志 + 统一前端入口」方案。

### 后端

后端只保留两类日志能力：

- 操作日志：继续使用 `@Log` + `LogAspect` + `sys_oper_log`。
- 登录日志：继续使用现有登录审计链路 + `sys_logininfor`。

请求日志相关实现全部删除：

- `RequestLogInterceptor`
- `RequestLogRecord`
- `AsyncFactory.recordRequest(...)`
- `SystemAuditAppService.recordRequestLog(...)`
- `SystemAuditAppServiceImpl.recordRequestLog(...)`
- `SysRequestLogController`
- `ISysRequestLogService`、`SysRequestLogServiceImpl`
- `SysRequestLog`、Mapper、XML
- 请求日志相关单元测试

### 前端

前端新增一个统一日志页面，位置仍在 `ui-admin/src/views/monitor` 目录下。该页面本身只负责：

- 渲染日志类型切换
- 根据当前标签页切换操作日志和登录日志的查询区域、表格区域、详情弹层或操作按钮
- 承接原有两个日志页的能力

页面内部分为两个面板：

- 操作日志面板：复用现有 `operlog` 页面和详情组件的主体逻辑。
- 登录日志面板：复用现有 `logininfor` 页面的主体逻辑。

为降低改动风险，本轮优先将原有两个页面拆成可内嵌组件或在统一页中直接复制经验证的局部逻辑，不强制先做高度抽象。目标是入口统一，而不是在第一轮把所有查询表格逻辑抽成一个复杂组件。

### 菜单与权限

菜单层面保留「日志中心」目录，但只保留一个页面级 `C` 菜单作为统一入口，例如：

- 菜单名称：统一日志
- 路由：`monitor/logCenter/index`
- 页面级权限：建议使用 `monitor:operlog:list`

页面内标签页是否显示由权限决定：

- 有 `monitor:operlog:list` 时显示「操作日志」标签页
- 有 `monitor:logininfor:list` 时显示「登录日志」标签页

这样可以保证：

- 菜单上只有一个入口
- 页面内仍可按原有权限区分可见能力
- 不需要新增复杂的聚合后端权限模型

请求日志菜单、按钮权限和默认角色授权全部删除。

## 数据模型与持久化方案

### 保留

- `sys_oper_log` 表及其全链路保留
- `sys_logininfor` 表及其全链路保留

### 删除

- `sys_request_log` 表定义从 `sql/manzhushaka_db_init.sql` 中移除
- 请求日志对应的 Mapper XML、实体、Service、Controller 全部移除

本轮不做数据迁移，也不尝试把历史 `sys_request_log` 数据导入其他表。已有数据库实例如果已经建过该表，允许表物理存在但不再被应用使用；初始化 SQL 不再继续创建它。

## 页面结构设计

统一日志页建议结构如下：

```text
日志中心
  └─ 统一日志页
       ├─ 顶部标签页：操作日志 | 登录日志
       ├─ 当前标签页对应的查询表单
       ├─ 当前标签页对应的工具栏
       ├─ 当前标签页对应的表格
       └─ 当前标签页对应的详情或附加操作
```

### 操作日志标签页

保留现有能力：

- 搜索
- 删除
- 清空
- 导出
- 详情查看

详情弹窗继续使用当前操作日志详情展示方式。

### 登录日志标签页

保留现有能力：

- 搜索
- 删除
- 清空
- 导出
- 解锁账号

登录日志当前没有详情弹窗，本轮保持现状。

### 标签页权限策略

统一日志页加载时按前端权限判断：

- 仅有操作日志权限：默认打开操作日志标签页，隐藏登录日志标签页
- 仅有登录日志权限：默认打开登录日志标签页，隐藏操作日志标签页
- 两者都有：默认打开操作日志标签页
- 两者都没有：理论上不会进入该页面；若因为缓存或手输 URL 进入，显示无权限提示或空状态

## 路由与侧边栏行为

当前侧边栏在目录只有一个可见子菜单时会直接渲染该子菜单。为了让「日志中心」在视觉上真正只有一个入口，本轮建议：

- 保留「日志中心」目录
- 将其子页面收敛为单一 `C` 菜单

这样侧边栏自然只显示一个日志入口，不需要新增额外的路由适配逻辑。

首页快捷入口中原有「操作日志」「登录日志」两个入口，也同步收敛成一个「日志中心」或「统一日志」入口。

## 安全与审计考量

移除请求日志后，需要接受一个明确变化：系统不再保留所有 HTTP 请求的全量访问流水。这是本轮主动接受的产品与技术取舍，而不是实现遗漏。

本轮仍保留以下安全边界：

- 对关键后台操作，通过 `@Log` 保留业务审计记录。
- 对登录成功、失败、退出、解锁等账户安全行为，继续保留登录日志。
- `@Log` 现有的敏感字段排除策略保持不变。

本轮不尝试用 `@Log` 替代请求日志的 `statusCode`、`userAgent`、`queryString` 等信息，因为这会把注解职责扩展为请求层访问日志，反而偏离「只保留一种清晰采集机制」的目标。

## 受影响文件范围

后端预计涉及：

- `manzhushaka-framework/src/main/java/com/manzhushaka/framework/config/ResourcesConfig.java`
- `manzhushaka-framework/src/main/java/com/manzhushaka/framework/interceptor/RequestLogInterceptor.java`
- `manzhushaka-framework/src/main/java/com/manzhushaka/framework/manager/factory/AsyncFactory.java`
- `manzhushaka-framework/src/main/java/com/manzhushaka/framework/web/command/RequestLogRecord.java`
- `manzhushaka-system/src/main/java/com/manzhushaka/system/application/service/SystemAuditAppService.java`
- `manzhushaka-system/src/main/java/com/manzhushaka/system/application/service/impl/SystemAuditAppServiceImpl.java`
- `manzhushaka-system/src/main/java/com/manzhushaka/system/domain/SysRequestLog.java`
- `manzhushaka-system/src/main/java/com/manzhushaka/system/mapper/SysRequestLogMapper.java`
- `manzhushaka-system/src/main/java/com/manzhushaka/system/service/ISysRequestLogService.java`
- `manzhushaka-system/src/main/java/com/manzhushaka/system/service/impl/SysRequestLogServiceImpl.java`
- `manzhushaka-system/src/main/resources/mapper/system/SysRequestLogMapper.xml`
- `manzhushaka-admin/src/main/java/com/manzhushaka/web/controller/monitor/SysRequestLogController.java`
- 请求日志相关测试文件

前端预计涉及：

- `ui-admin/src/views/monitor/requestLog/index.vue`
- `ui-admin/src/api/monitor/requestLog.js`
- `ui-admin/src/views/monitor/operlog/index.vue`
- `ui-admin/src/views/monitor/operlog/detail.vue`
- `ui-admin/src/views/monitor/logininfor/index.vue`
- 新增统一日志页
- 首页快捷入口

SQL 预计涉及：

- `sql/manzhushaka_db_init.sql`

## 验证标准

后端验证：

- 启动后不再注册 `RequestLogInterceptor`
- 请求日志相关类全部从编译链路中移除
- 操作日志查询、导出、删除、清空不受影响
- 登录日志查询、导出、删除、清空、解锁不受影响

前端验证：

- 侧边栏和菜单中只保留一个日志入口
- 统一日志页可在标签页中切换操作日志和登录日志
- 两类日志原有能力仍正常可用
- 请求日志页面和请求日志入口消失
- 首页快捷入口收敛为一个日志入口

构建验证：

- 运行 `mvn test` 或至少运行受影响模块测试
- 运行 `mvn clean package`
- 运行 `cd ui-admin && npm run build:prod`

## 风险与缓解

### 风险 1：用户习惯了请求日志页面

缓解：

- 在统一日志页中保留清晰的操作日志与登录日志标签页
- 在变更说明中明确请求日志已下线，不再保留全量访问流水

### 风险 2：菜单权限收敛后，部分角色只能看到空页面

缓解：

- 前端根据 `monitor:operlog:list` 与 `monitor:logininfor:list` 动态决定显示哪个标签页
- 统一日志入口建议授予至少一种日志查看权限的角色

### 风险 3：删除请求日志链路后，存在未清理的残余引用

缓解：

- 通过 `rg` 全仓搜索 `requestLog`、`SysRequestLog`、`RequestLogInterceptor`
- 编译与前端构建双重验证

## 实施顺序建议

1. 先删除后端请求日志采集与持久化链路。
2. 再删除请求日志前端 API、页面和菜单 SQL。
3. 新增统一日志页并承接操作日志与登录日志视图。
4. 收敛首页快捷入口和日志中心菜单。
5. 完成构建与回归验证。
