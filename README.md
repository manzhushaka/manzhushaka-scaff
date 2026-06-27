# manzhushaka-ry-scaff

一个基于 `RuoYi` 体系二次开发的前后端分离后台脚手架，整合了 `Spring Boot 4` 后端与 `Vue 3 + Vite` 前端，并按当前仓库需求做了包名、模块名、配置项和默认文案层面的魔改。

## 项目说明

本项目不是若依官方原版仓库，而是基于 `RuoYi / RuoYi-Vue` 生态改造而来，当前仓库主要做了这些调整：

- 后端包名与模块名改为 `com.manzhushaka` 及 `manzhushaka-ry-*` 体系。
- 前端切换为 `Vue 3 + Element Plus + Vite` 技术栈。
- 保留了若依常用的权限、用户、角色、菜单、字典、日志、代码生成、定时任务等后台基础能力。
- 接入了 `SpringDoc OpenAPI`，默认提供 `Swagger UI` 接口文档页面。

如果你熟悉若依，这个项目可以理解为一个面向当前业务继续演进的若依魔改脚手架。

## 技术栈

### 后端

- `Java 17`
- `Spring Boot 4.0.6`
- `Spring Security`
- `MyBatis`
- `Druid`
- `Redis`
- `Quartz`
- `SpringDoc OpenAPI`

### 前端

- `Vue 3.5`
- `Element Plus`
- `Vite 6`
- `Pinia`
- `Vue Router 4`
- `Axios`

## 项目结构

```text
.
├── manzhushaka-ry-admin        # Web 启动层，接口入口与应用启动类
├── manzhushaka-ry-framework    # 框架层，安全、配置、AOP、拦截器等
├── manzhushaka-ry-system       # 系统业务层，用户、角色、菜单、部门等
├── manzhushaka-ry-quartz       # 定时任务模块
├── manzhushaka-ry-common       # 通用工具、常量、注解与基础能力
├── sql                         # 数据库初始化脚本
├── ui-admin                    # 前端管理端
├── doc                         # 项目附带文档
├── ry.sh / ry.bat              # 常用启动脚本
└── pom.xml                     # Maven 聚合工程
```

## 内置能力

- 权限认证与动态菜单
- 用户、角色、部门、岗位管理
- 字典、参数、通知公告管理
- 操作日志与登录日志
- 在线用户与服务监控
- 缓存监控
- Quartz 定时任务
- 代码生成与接口文档

## 快速开始

### 环境要求

- `JDK 17`
- `Maven 3.9+`
- `Node.js 18+`
- `MySQL 8.0`（推荐）
- `Redis 6+`

### 1. 初始化数据库

先创建数据库，例如 `ry-vue`，然后依次导入以下脚本：

- [sql/ry_20260417.sql](/Users/manzhushaka/CodexProject/manzhushaka-ry-scaff/sql/ry_20260417.sql)
- [sql/quartz.sql](/Users/manzhushaka/CodexProject/manzhushaka-ry-scaff/sql/quartz.sql)

### 2. 修改后端配置

重点检查以下配置文件：

- [manzhushaka-ry-admin/src/main/resources/application-druid.yml](/Users/manzhushaka/CodexProject/manzhushaka-ry-scaff/manzhushaka-ry-admin/src/main/resources/application-druid.yml)
  更新 MySQL 地址、用户名和密码。
- [manzhushaka-ry-admin/src/main/resources/application.yml](/Users/manzhushaka/CodexProject/manzhushaka-ry-scaff/manzhushaka-ry-admin/src/main/resources/application.yml)
  更新 Redis 地址、端口，以及文件上传目录 `manzhushaka.profile`。

默认配置中：

- 后端端口为 `8080`
- Redis 地址为 `localhost:6379`
- Swagger UI 地址为 `/swagger-ui.html`

### 3. 启动后端

在仓库根目录执行：

```bash
mvn -pl manzhushaka-ry-admin -am spring-boot:run
```

也可以先打包再运行：

```bash
mvn clean package -DskipTests
java -jar manzhushaka-ry-admin/target/manzhushaka-ry-admin.jar
```

后端默认访问地址：

- `http://localhost:8080`
- `http://localhost:8080/swagger-ui.html`

### 4. 启动前端

进入前端目录执行：

```bash
cd ui-admin
npm install
npm run dev
```

前端开发环境默认：

- 启动端口为 `80`
- 访问地址为 `http://localhost`
- `/dev-api` 会代理到 `http://localhost:8080`

### 5. 默认账号

当前仓库前端登录页预置了默认开发账号：

- 用户名：`admin`
- 密码：`admin123`

如果你修改过初始化数据，请以本地数据库中的账号信息为准。

## 二次开发说明

- 启动类位于 [manzhushaka-ry-admin/src/main/java/com/manzhushaka/ManzhushakaRyApplication.java](/Users/manzhushaka/CodexProject/manzhushaka-ry-scaff/manzhushaka-ry-admin/src/main/java/com/manzhushaka/ManzhushakaRyApplication.java)。
- 前端代理配置位于 [ui-admin/vite.config.js](/Users/manzhushaka/CodexProject/manzhushaka-ry-scaff/ui-admin/vite.config.js)。
- 如果你准备继续做品牌化改造，建议优先检查登录页、系统名称、上传目录、默认域名和接口前缀等配置。
- 仓库内仍保留若依体系的很多命名和默认数据，这是后续继续清理与业务化改造的重点区域。

## 文档与参考

- 项目附带文档： [doc/若依环境使用手册.docx](/Users/manzhushaka/CodexProject/manzhushaka-ry-scaff/doc/若依环境使用手册.docx)
- 若依原始生态可参考：
  - [RuoYi](https://gitee.com/y_project/RuoYi)
  - [RuoYi-Vue](https://gitee.com/y_project/RuoYi-Vue)

## 致谢与许可证

本项目基于若依开源生态进行二次开发，感谢 `RuoYi` 项目提供的基础能力与开源许可证支持。

当前仓库沿用 `MIT` 许可证，详见 [LICENSE](/Users/manzhushaka/CodexProject/manzhushaka-ry-scaff/LICENSE)。
