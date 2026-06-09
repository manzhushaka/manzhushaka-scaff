# Manzhushaka Admin Scaffold 实现计划

> **面向 AI 代理的工作者：** 必需子技能：使用 superpowers:subagent-driven-development（推荐）或 superpowers:executing-plans 逐任务实现此计划。步骤使用复选框（`- [ ]`）语法来跟踪进度。

**目标：** 基于给定 spec 在单仓库中完成可运行的 Spring Boot 3 + Vue 3 管理系统脚手架第一版。

**架构：** 后端采用 Maven 多模块单体，`boot` 负责装配，`common/framework/db/auth/system/mq` 按职责拆分；前端 `ui-admin` 独立承接登录、布局、动态菜单、权限指令和系统管理页面；数据库初始化脚本与环境配置同仓库维护。

**技术栈：** Java 17、Spring Boot 3、Sa-Token、MyBatis-Plus、MySQL 8、Redis、Redis Stream、Vue 3、Vite、TypeScript、Pinia、Arco Design Vue、pnpm

---

### 任务 1：仓库与工程骨架

**文件：**
- 创建：`pom.xml`
- 创建：`manzhushaka-boot/pom.xml`
- 创建：`manzhushaka-common/pom.xml`
- 创建：`manzhushaka-framework/pom.xml`
- 创建：`manzhushaka-db/pom.xml`
- 创建：`manzhushaka-auth/pom.xml`
- 创建：`manzhushaka-system/pom.xml`
- 创建：`manzhushaka-mq/pom.xml`
- 创建：`ui-admin/package.json`
- 创建：`README.md`

- [ ] 建立 Git 仓库、根级忽略文件和目录约定。
- [ ] 写父 `pom.xml`，统一版本、依赖管理和多模块声明。
- [ ] 为 6 个后端模块创建最小可编译的 `pom.xml` 和源码目录。
- [ ] 为 `ui-admin` 创建最小可安装的 Vite + Vue 3 工程骨架。

### 任务 2：后端公共基础设施

**文件：**
- 创建：`manzhushaka-common/src/main/java/com/manzhushaka/common/**`
- 创建：`manzhushaka-framework/src/main/java/com/manzhushaka/framework/**`
- 创建：`manzhushaka-db/src/main/java/com/manzhushaka/db/**`
- 创建：`manzhushaka-boot/src/main/resources/application*.yml`

- [ ] 先写公共返回体、异常、枚举、注解和上下文对象。
- [ ] 再落地 MVC、Jackson、异常处理、Sa-Token 拦截、数据权限切面、操作日志切面。
- [ ] 建立 MyBatis-Plus、分页、审计字段和环境化 SQL 打印配置。
- [ ] 运行后端编译，确保基础模块能被 `boot` 成功装配。

### 任务 3：认证与系统能力

**文件：**
- 创建：`manzhushaka-auth/src/main/java/com/manzhushaka/auth/**`
- 创建：`manzhushaka-system/src/main/java/com/manzhushaka/system/**`
- 创建：`manzhushaka-db/src/main/resources/mapper/system/*.xml`
- 测试：`manzhushaka-system/src/test/java/com/manzhushaka/system/**`

- [ ] 先写关键单测，覆盖菜单树、数据权限合并、日志脱敏等核心行为。
- [ ] 实现登录、登出、当前用户信息与登录日志落库。
- [ ] 实现用户、角色、部门、菜单、字典、参数、登录日志、操作日志查询接口。
- [ ] 跑后端单测和 `mvn test`，确认关键行为与模块编译通过。

### 任务 4：Redis Stream 消息链路

**文件：**
- 创建：`manzhushaka-mq/src/main/java/com/manzhushaka/mq/**`
- 修改：`manzhushaka-framework/src/main/java/com/manzhushaka/framework/logging/**`

- [ ] 定义统一事件信封和 Stream 常量。
- [ ] 实现发布器、消费者注册、重试与死信策略。
- [ ] 把 `@OpLog` 切面接入 `manzhushaka:stream:oplog`，异步消费入库。
- [ ] 运行后端测试和编译，确认消息模块与操作日志链路可装配。

### 任务 5：前端管理台

**文件：**
- 创建：`ui-admin/src/**`
- 测试：`ui-admin` 构建输出

- [ ] 搭建登录页、主布局、请求封装、Pinia 和路由守卫。
- [ ] 实现动态菜单装配、组件映射和按钮权限指令。
- [ ] 实现用户、角色、部门、菜单、字典、参数、日志页面骨架与基本 CRUD 交互。
- [ ] 运行 `pnpm build`，确认前端可编译。

### 任务 6：数据库脚本与交付验证

**文件：**
- 创建：`sql/manzhushaka_init.sql`
- 修改：`README.md`

- [ ] 编写表结构与种子数据脚本，覆盖 admin、角色、菜单和基础字典。
- [ ] 补齐启动说明、环境变量说明和默认账号说明。
- [ ] 运行 `mvn test` 与 `pnpm build` 作为最终证据。
