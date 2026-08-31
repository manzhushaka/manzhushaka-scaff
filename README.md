# manzhushaka-scaff

`manzhushaka-scaff` 是基于 RuoYi / RuoYi-Vue 体系二次开发的前后端分离后台脚手架。后端采用 `Spring Boot 4 + Spring Security + MyBatis`，前端采用 `Vue 3 + Arco Design Vue + Vite`。当前仓库已按 `com.manzhushaka` 包名和 `manzhushaka-*` Maven 多模块结构继续演进。

本仓库不是若依官方原版。README 以当前代码状态为准，用于本地开发、模块理解和后续二次开发。

## 当前状态

- 保留后台基础能力：登录认证、验证码、用户、角色、菜单、部门、字典、参数、操作日志、登录日志、运行日志、慢 SQL 日志、在线用户、服务监控、缓存监控、Druid 监控和 Quartz 定时任务。
- 已接入 Redis Stream 消息发布、消费、重试、死信和消息台账管理。
- 前端入口在 `ui-admin`，使用 Vue 3、Arco Design Vue、Pinia、Vue Router 4 和 Axios；页面和公共组件已全部迁移到 Arco Design Vue，Element Plus 依赖已移除。
- 后端入口在 `manzhushaka-admin`，当前启动类为 `com.manzhushaka.ManzhushakaScaffApplication`。
- 系统业务正在从若依传统分层向更清晰的应用层、领域层、基础设施层拆分；新增代码应优先遵守 `AGENTS.md` 中的模块边界。
- 当前仓库已移除或正在清理若依原版中的岗位管理、通知公告、在线构建器等旧入口，文档和菜单不再把它们作为内置能力描述。

## 技术栈

### 后端

| 分类 | 技术 |
| --- | --- |
| 运行环境 | Java 17 |
| Web 框架 | Spring Boot 4.0.6、Spring MVC |
| 安全认证 | Spring Security、JWT |
| 持久化 | MyBatis、PageHelper |
| 数据源 | MySQL、Druid |
| 缓存 | Redis、Spring Cache |
| 定时任务 | Quartz |
| 接口文档 | SpringDoc OpenAPI |
| 测试 | JUnit 5、Spring Boot Test、ArchUnit |

### 前端

| 分类 | 技术 |
| --- | --- |
| 框架 | Vue 3.5 |
| 构建工具 | Vite 6 |
| UI 组件 | Arco Design Vue 2.58 |
| 状态管理 | Pinia |
| 路由 | Vue Router 4 |
| HTTP | Axios |
| 图表与富文本 | ECharts、Vue Quill |

## 项目结构

```text
.
├── manzhushaka-admin        # Web 启动层，Controller、HTTP DTO/VO、全局入口
├── manzhushaka-framework    # 框架能力，安全、配置、AOP、拦截器、验证码等
├── manzhushaka-system       # 系统业务，用户、角色、菜单、部门、字典、参数等
├── manzhushaka-quartz       # Quartz 定时任务
├── manzhushaka-common       # 通用工具、常量、注解、异常和基础能力
├── ui-admin                 # Vue 3 管理端
├── sql                      # 主初始化脚本
├── docs                     # 安全与使用文档
├── scripts                  # 架构检查等辅助脚本
├── bin                      # Windows 构建与运行脚本
└── pom.xml                  # Maven 聚合工程
```

## 环境要求

- JDK 17
- Maven 3.9+
- Node.js 18+（建议使用 LTS 版本）
- MySQL 8.0+
- Redis 6+

## 快速开始

### 1. 初始化数据库

创建数据库，默认配置使用：

```sql
CREATE DATABASE `manzhushaka-scaff` DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
```

导入基础脚本：

```bash
mysql --default-character-set=utf8mb4 -uroot -p manzhushaka-scaff < sql/manzhushaka_db_init.sql
```

以上导入命令中的 `--default-character-set=utf8mb4` 不要省略，否则初始化中文数据时可能出现乱码。

当前仓库仅保留 `sql/manzhushaka_db_init.sql`，用于从空库一次性完成系统表、Redis Stream MQ 台账表和 Quartz 调度表初始化。

### 2. 修改后端配置

重点检查：

- `manzhushaka-admin/src/main/resources/application.yml`
- `manzhushaka-admin/src/main/resources/application-dev.yml`

常用环境变量：

| 变量 | 默认值 | 说明 |
| --- | --- | --- |
| `SPRING_PROFILES_ACTIVE` | `dev` | Spring Profile |
| `JDBC_MASTER_URL` | `jdbc:mysql://localhost:3306/manzhushaka-scaff...characterEncoding=UTF-8...` | 主库连接 |
| `JDBC_MASTER_USERNAME` | `root` | 主库用户名 |
| `JDBC_MASTER_PASSWORD` | `1a2s3d4f` | 主库密码 |
| `JDBC_SLAVE_ENABLED` | `false` | 是否启用从库 |
| `SPRINGDOC_SWAGGER_UI_ENABLED` | `false` | 是否启用 Swagger UI |

默认服务配置：

- 后端端口：`8080`
- 应用路径：`/`
- Redis：`localhost:6379`
- 接口文档：`/v3/api-docs`
- Swagger UI：`/swagger-ui.html`（需设置 `SPRINGDOC_SWAGGER_UI_ENABLED=true`）

### 3. 启动后端

在仓库根目录执行：

```bash
mvn -pl manzhushaka-admin -am package -DskipTests
java -jar manzhushaka-admin/target/manzhushaka-admin.jar
```

### 4. 启动前端

```bash
cd ui-admin
npm install
npm run dev
```

前端开发服务默认监听 `80` 端口，访问地址为 `http://localhost`。开发环境接口前缀为 `/dev-api`，Vite 会代理到 `http://localhost:8080`。

### 5. 默认账号与初始密码

初始化数据包含超级管理员账号：

| 用户名 | 密码 |
| --- | --- |
| `admin` | `admin@123` |

系统参数 `sys.user.initPassword` 的默认值同样为 `admin@123`，用于新建或导入用户时设置初始密码。

如果初始化脚本或本地数据库已调整，以本地数据库为准。

## 常用命令

### 后端

```bash
# 构建全部 Maven 模块
mvn clean package

# 运行后端测试和架构测试
mvn test

# 仅启动 Web 模块及其依赖
mvn -pl manzhushaka-admin -am spring-boot:run

# 检查模块边界
bash scripts/architecture/check-module-boundaries.sh
```

### 前端

```bash
cd ui-admin

# 安装依赖
npm install

# 启动开发服务
npm run dev

# 构建生产包
npm run build:prod

# 构建预发布包
npm run build:stage
```

## 开发约定

新增或修改代码前，请先阅读 `AGENTS.md`。其中包含本仓库当前的模块边界、Java 编码规范、权限闭环、SQL 维护、日志注解、安全和测试要求。

几个高频约定：

- Controller、HTTP DTO/VO 和全局 HTTP 入口放在 `manzhushaka-admin`。
- 框架能力放在 `manzhushaka-framework`。
- 系统业务放在 `manzhushaka-system`，新增持久化实体优先放到 `infrastructure/persistence/entity`。
- `manzhushaka-common` 只放通用基础能力，不承载业务实体。
- 所有页面和公共组件统一使用 Arco Design Vue；新增或修改前端代码时不得重新引入 Element Plus 依赖或组件 API。
- 新增页面、按钮、接口权限时，需要同步维护前端 `v-hasPermi`、后端 `@PreAuthorize`、`sql/manzhushaka_db_init.sql` 中的 `sys_menu` 和必要的 `sys_role_menu`。
- 新增或调整数据库结构、基础数据、默认菜单时，需要同步维护 `sql` 目录下的初始化或增量脚本。
- `manzhushaka-admin/src/main/java/com/manzhushaka/web/controller` 下的 HTTP 接口方法需要同时满足权限注解和 `@Log` 操作日志规范。

## 关键入口

- 后端启动类：`manzhushaka-admin/src/main/java/com/manzhushaka/ManzhushakaScaffApplication.java`
- 后端主配置：`manzhushaka-admin/src/main/resources/application.yml`
- 开发数据源配置：`manzhushaka-admin/src/main/resources/application-dev.yml`
- MyBatis 映射：`manzhushaka-system/src/main/resources/mapper`
- 前端环境配置：`ui-admin/.env.development`、`ui-admin/.env.production`
- 前端代理配置：`ui-admin/vite.config.js`
- 前端路由：`ui-admin/src/router/index.js`
- 前端页面：`ui-admin/src/views`
- 初始化 SQL：`sql/manzhushaka_db_init.sql`
- 协作规范：`AGENTS.md`

## 文档

- 敏感字段加密说明：`docs/security/sensitive-encryption.md`
- 若依原始生态参考：[RuoYi](https://gitee.com/y_project/RuoYi)、[RuoYi-Vue](https://gitee.com/y_project/RuoYi-Vue)

## 许可证

本项目基于若依开源生态二次开发，沿用 MIT License。详见 `LICENSE`。
