# AGENTS.md

## 项目定位

`manzhushaka-scaff` 是一个管理系统脚手架仓库，采用后端 Maven 多模块单体 + 前端独立 `ui-admin` 的组织方式。

- 后端技术栈：Spring Boot 3、Sa-Token、MyBatis-Plus、Redis Stream
- 前端技术栈：Vue 3、Vite、TypeScript、Pinia、Vue Router、Arco Design Vue
- 当前目标：围绕认证、权限、系统管理、日志、消息链路完成第一版交付

当前仓库仍处于脚手架建设阶段。不要假设所有模块都已经完整实现；改动前先读取实际代码路径。

## 目录职责

- `manzhushaka-boot/`：启动模块、`application*.yml` 配置、运行装配
- `manzhushaka-auth/`：登录、验证码、当前用户信息、菜单装载等认证能力
- `manzhushaka-common/`：通用模型、枚举、注解、上下文、工具类
- `manzhushaka-framework/`：Web 基础设施、权限、日志、数据权限等框架能力
- `manzhushaka-db/`：数据库基础设施、实体元数据、持久层支撑
- `manzhushaka-system/`：用户、角色、部门、菜单、字典、参数、日志等系统管理域能力
- `manzhushaka-mq/`：Redis Stream 消息链路
- `sql/`：数据库初始化脚本
- `ui-admin/`：前端管理台

## 工作方式

- 优先做满足需求的最小改动，不要顺手做无关重构。
- 后端改动保持模块边界清晰，不要把领域逻辑塞进 `boot`、`common` 这类基础模块。
- 前端优先复用已有组件和模式，尤其是：
  - `ui-admin/src/components/CrudPage.vue`
  - `ui-admin/src/components/LogTablePage.vue`
  - `ui-admin/src/components/PageHeaderCard.vue`
  - `ui-admin/src/router/`
  - `ui-admin/src/store/`
- 不要额外引入新的 UI 框架、状态管理方案或请求库；现有前端已使用 Arco Design、Pinia、Axios。
- 仓库可能处于脏工作区；除非用户明确要求，不要回滚、覆盖或格式化与你任务无关的文件。
- `target/` 下内容属于构建产物，默认不要手工编辑。

## 常用命令

### 后端

```bash
mvn clean package
mvn -pl manzhushaka-boot spring-boot:run
mvn -pl manzhushaka-auth test
mvn -pl manzhushaka-framework test
```

### 前端

```bash
cd ui-admin
pnpm install
pnpm dev
pnpm build
```

说明：当前 `ui-admin/package.json` 里没有内置 `test` 脚本；如果补测试，请同时补可执行命令或在文档中说明运行方式。

## 环境与联调

- JDK 17
- Maven 3.9+
- MySQL 8.0
- Redis 7.0+
- Node.js 20+
- pnpm 9+

数据库初始化脚本位于 `sql/manzhushaka_init.sql`。

后端运行参数优先参考根目录 `README.md` 中的环境变量约定：

- `SPRING_PROFILES_ACTIVE`
- `DB_URL`
- `DB_USERNAME`
- `DB_PASSWORD`
- `REDIS_HOST`
- `REDIS_PORT`

前端本地开发默认通过 Vite 代理将 `/api` 转发到 `http://127.0.0.1:8080`。

## 改动联动提示

- 修改登录流程时，优先检查这些位置是否需要一起更新：
  - `manzhushaka-auth/src/main/java/com/manzhushaka/auth/`
  - `ui-admin/src/api/auth.ts`
  - `ui-admin/src/store/auth.ts`
  - `ui-admin/src/views/auth/LoginView.vue`
- 修改菜单、权限或当前用户可见范围时，注意联动检查：
  - `ui-admin/src/router/dynamic.ts`
  - `ui-admin/src/router/routes.ts`
  - `ui-admin/src/directives/permission.ts`
- 修改系统管理页字段结构时，注意同步：
  - `ui-admin/src/types/system.ts`
  - `ui-admin/src/views/system/`
  - 相关 `src/api/*.ts`

## 验证要求

- 文档改动：至少复核命令、路径、模块名和端口信息是否与仓库现状一致。
- 后端改动：优先执行受影响模块测试；如果涉及启动配置或装配，至少补一次 `mvn clean package` 或启动验证。
- 前端改动：至少执行一次 `cd ui-admin && pnpm build`。
- 如果某项验证因环境缺失无法执行，在结论里明确写出阻塞条件，不要直接宣称“已完成”。
