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

- 每次回复开头先输出固定问候语：「你好， 我是manzhushaka」。
- 优先做满足需求的最小改动，不要顺手做无关重构。
- 后端改动保持模块边界清晰，不要把领域逻辑塞进 `boot`、`common` 这类基础模块。
- 如果接口需要匿名访问，优先直接在接口方法上添加 `@SaIgnore`，不要只在路径白名单里零散放行。
- 前端优先复用已有组件和模式，尤其是：
  - `ui-admin/src/components/CrudPage.vue`
  - `ui-admin/src/components/LogTablePage.vue`
  - `ui-admin/src/components/PageHeaderCard.vue`
  - `ui-admin/src/router/`
  - `ui-admin/src/store/`
- 前端页面中的每一个按钮都需要加上权限注解；如果当前登录用户没有对应的权限字符串，则按钮不显示。
- 后端对外返回给前端展示的敏感信息必须先脱敏；如果字段需要在接口响应中展示但不能明文返回，优先在对应 `VO` 字段上使用 `@SensitiveField` 注解，而不是在前端临时裁剪或拼接。
- 不要额外引入新的 UI 框架、状态管理方案或请求库；现有前端已使用 Arco Design、Pinia、Axios。
- 仓库可能处于脏工作区；除非用户明确要求，不要回滚、覆盖或格式化与你任务无关的文件。
- `target/` 下内容属于构建产物，默认不要手工编辑。

## 编码执行约定

- 改代码前先明确目标、约束和关键假设，并优先读取真实调用链路；不要只根据目录名或文件名猜实现。
- 需求存在歧义时，先说明可能解释，默认选择风险最低、改动最小的方案。
- 优先复用现有局部模式；不要为了“以后可能会用”提前引入新抽象、新依赖、全局配置或大范围改写。
- 如果需要在配置文件中新增敏感配置（如密码、密钥、Token），建议使用 `${XXXX:XXXX}` 这类占位写法通过环境变量注入，不要把敏感信息直接硬编码到配置文件中。
- 新增或调整接口出参字段时，如果字段属于手机号、证件号、银行卡号、密码、Token 等敏感信息，必须同时评估是否需要脱敏；需要展示时，默认在后端 `VO` 字段使用 `@SensitiveField`，不要把明文直接返回给前端。
- 只修改与当前任务直接相关的文件；命名、日志、异常处理和分层方式保持与周边代码一致。
- 每个方法都必须编写方法注释，至少说明方法用途、入参和出参；注释必须使用对应语言支持的标准注释语法，不允许使用 HTML 标签的注释。
- 新增数据库表时遵循统一命名规范：系统功能表使用 `sys_` 前缀，业务功能表使用 `biz_` 前缀，并确保表名能准确表达所属业务域。
- 后端开发如果涉及新增表或新增字段，必须同步更新 `sql/manzhushaka_init.sql` 中的项目初始化 SQL 语句。
- 功能开发先定义预期行为和落点，再实现并验证；缺陷修复先复现或定位根因，优先修根因而不是只补表面现象。
- 重构默认以“行为不变”为前提，控制 diff 范围，并补充能覆盖改动路径的检查。
- 评审或自查时，优先指出具体风险、证据和缺少的验证，不做笼统结论。
- 出现以下信号时先停下来复查：准备新增框架/依赖/全局配置、diff 触及与目标无关的文件、修复依赖尚未验证的假设、还没验证就准备宣称完成。

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
