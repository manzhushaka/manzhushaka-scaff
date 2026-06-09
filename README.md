# manzhushaka-scaff

基于 `Spring Boot 3`、`Sa-Token`、`MyBatis-Plus`、`Redis Stream` 和 `Vue 3` 的管理系统脚手架。

当前仓库面向第一版交付，目标范围包括：

- 后端 Maven 多模块单体
- 前端 `ui-admin` 独立项目
- 认证、权限、菜单、数据权限、日志、消息总线
- 用户、角色、部门、菜单、字典、参数、日志管理
- 同仓库维护数据库初始化脚本与交付说明

## 项目结构

```text
.
├── manzhushaka-auth/        认证与登录能力
├── manzhushaka-boot/        Spring Boot 启动装配模块
├── manzhushaka-common/      公共模型、枚举、注解、上下文
├── manzhushaka-db/          数据库基础设施与实体元数据
├── manzhushaka-framework/   Web、权限、日志、数据权限等基础框架
├── manzhushaka-mq/          Redis Stream 消息链路
├── manzhushaka-system/      系统管理领域能力
├── sql/                     MySQL 初始化脚本
└── ui-admin/                Vue 3 管理台（按 spec 独立维护）
```

## 环境依赖

- JDK 17
- Maven 3.9+
- MySQL 8.0
- Redis 7.0+
- Node.js 20+
- pnpm 9+

## 数据库初始化

初始化脚本位于 [sql/manzhushaka_init.sql](/Users/manzhushaka/CodexProject/manzhushaka-scaff/sql/manzhushaka_init.sql)。

推荐先创建数据库，再执行脚本：

```sql
CREATE DATABASE IF NOT EXISTS manzhushaka
  DEFAULT CHARACTER SET utf8mb4
  COLLATE utf8mb4_0900_ai_ci;
```

```bash
mysql -uroot -p manzhushaka < sql/manzhushaka_init.sql
```

脚本覆盖以下第一版表与基础数据：

- `sys_user`
- `sys_role`
- `sys_user_role`
- `sys_dept`
- `sys_menu`
- `sys_role_menu`
- `sys_dict_type`
- `sys_dict_item`
- `sys_config`
- `sys_op_log`
- `sys_login_log`

## 环境变量说明

第一版建议通过环境变量注入后端运行参数，推荐键名如下：

| 变量名 | 说明 | 示例 |
| --- | --- | --- |
| `SPRING_PROFILES_ACTIVE` | Spring Boot 运行环境 | `dev` |
| `DB_URL` | MySQL 连接串 | `jdbc:mysql://127.0.0.1:3306/manzhushaka?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai` |
| `DB_USERNAME` | MySQL 用户名 | `root` |
| `DB_PASSWORD` | MySQL 密码 | `root` |
| `REDIS_HOST` | Redis 主机 | `127.0.0.1` |
| `REDIS_PORT` | Redis 端口 | `6379` |

当前仓库的 [application-prod.yml](/Users/manzhushaka/CodexProject/manzhushaka-scaff/manzhushaka-boot/src/main/resources/application-prod.yml) 已接入以上数据库和 Redis 配置键。默认运行 profile 按当前仓库口径使用 `dev`。

## 后端启动

后端采用 Maven 多模块单体，启动入口为 `manzhushaka-boot`。

```bash
mvn clean package
```

```bash
mvn -pl manzhushaka-boot spring-boot:run
```

如果使用环境变量启动，可参考：

```bash
export SPRING_PROFILES_ACTIVE=dev
export DB_URL='jdbc:mysql://127.0.0.1:3306/manzhushaka?useUnicode=true&characterEncoding=utf8&serverTimezone=Asia/Shanghai'
export DB_USERNAME=root
export DB_PASSWORD=root
export REDIS_HOST=127.0.0.1
export REDIS_PORT=6379
mvn -pl manzhushaka-boot spring-boot:run
```

## 前端启动

按 spec，前端管理台位于 `ui-admin/`，与后端分仓目录、同仓库维护。

常用启动命令如下：

```bash
cd ui-admin
pnpm install
pnpm dev
```

生产构建命令：

```bash
cd ui-admin
pnpm build
```

说明：当前分支如果尚未同步任务 5 的前端产物，`ui-admin/` 目录可能暂未出现；届时请先合入对应前端脚手架提交，再执行上述命令。

## 默认账号

初始化脚本会写入一个系统管理员账号：

- 用户名：`admin`
- 密码：`Admin@123456`
- 角色：`SUPER_ADMIN`
- 部门：`manzhushaka`

说明：当前脚本中的初始密码以第一版演示可读性为先写入数据库，正式接入登录校验时应尽快切换为安全哈希存储，并在首登后强制修改默认密码。

登录与操作日志字段约定：

- `sys_role.data_scope` 使用字符串枚举：`ALL`、`DEPT_AND_CHILD`、`DEPT`、`SELF`
- `sys_menu.menu_type` 使用字符串枚举：`DIR`、`MENU`、`BUTTON`
- `sys_login_log.login_status` 使用字符串枚举：`SUCCESS`、`FAIL`

## 基础数据说明

第一版初始化数据包含：

- 1 个根部门和 1 个默认管理员角色
- 管理台核心菜单：仪表盘、系统管理、用户管理、角色管理、部门管理、菜单管理、字典管理、参数管理、登录日志、操作日志
- 基础字典：用户状态、通用启停状态、是否枚举
- 基础参数：系统名称、默认密码、前端标题

## 当前交付边界

- 本次 README 与 SQL 以 spec 第一版为准，重点覆盖初始化与启动说明。
- 当前仓库内部分模块仍处于脚手架建设阶段，README 中的前端与环境变量章节同时承担交付说明作用。
- 若后续认证实现确定了密码加密方案、配置文件命名或前端端口约定，应同步更新本 README 与初始化脚本。
