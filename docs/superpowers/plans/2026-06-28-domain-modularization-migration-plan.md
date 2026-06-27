# manzhushaka-ry 业务域模块化迁移实现计划

> **面向 AI 代理的工作者：** 必需子技能：使用 `superpowers:subagent-driven-development`（推荐）或 `superpowers:executing-plans` 逐任务实现此计划。步骤使用复选框（`- [ ]`）语法来跟踪进度。

**目标：** 将当前若依改造仓库从“`common/system/framework` 交叉持有业务对象”的结构，迁移为“`admin` 统一承接 HTTP，业务域各自持久化，各模块边界清晰”的单体多模块架构，并为后续新增 `member/order/content` 等业务域建立稳定模板。

**架构：** `manzhushaka-ry-admin` 统一承接 `Controller + HTTP DTO/VO + Converter`；`manzhushaka-ry-system` 等业务域模块各自拥有 `application/domain/infrastructure`；`manzhushaka-ry-common` 只保留真正通用能力；`manzhushaka-ry-framework` 聚焦安全、AOP、Redis、日志等跨领域基础设施，不再长期直接耦合业务实现细节。

**技术栈：** Java 17、Spring Boot 4、Spring Security、MyBatis、Druid、Redis、Quartz、Maven 多模块工程

---

## 1. 背景与当前问题

当前仓库已经具备多模块形态，但模块边界仍然偏“若依默认分层”，存在以下结构性问题：

1. `admin` 中的 Controller 直接使用业务实体作为 HTTP 入参和出参，例如 [SysUserController.java](/Users/manzhushaka/CodexProject/manzhushaka-ry-scaff/manzhushaka-ry-admin/src/main/java/com/manzhushaka/web/controller/system/SysUserController.java) 和 [SysRoleController.java](/Users/manzhushaka/CodexProject/manzhushaka-ry-scaff/manzhushaka-ry-admin/src/main/java/com/manzhushaka/web/controller/system/SysRoleController.java)。
2. `common` 中混入了系统业务实体，例如 [SysUser.java](/Users/manzhushaka/CodexProject/manzhushaka-ry-scaff/manzhushaka-ry-common/src/main/java/com/manzhushaka/common/core/domain/entity/SysUser.java)、[SysRole.java](/Users/manzhushaka/CodexProject/manzhushaka-ry-scaff/manzhushaka-ry-common/src/main/java/com/manzhushaka/common/core/domain/entity/SysRole.java)、[SysMenu.java](/Users/manzhushaka/CodexProject/manzhushaka-ry-scaff/manzhushaka-ry-common/src/main/java/com/manzhushaka/common/core/domain/entity/SysMenu.java)。
3. `system` 模块同时持有部分业务对象、Mapper、Service，但对象分布不统一。`SysPost`、`SysNotice` 等在 [manzhushaka-ry-system/src/main/java/com/manzhushaka/system/domain](/Users/manzhushaka/CodexProject/manzhushaka-ry-scaff/manzhushaka-ry-system/src/main/java/com/manzhushaka/system/domain)，而 `SysUser`、`SysRole` 等却在 `common`。
4. `framework` 目前直接依赖 `system` 模块，见 [manzhushaka-ry-framework/pom.xml](/Users/manzhushaka/CodexProject/manzhushaka-ry-scaff/manzhushaka-ry-framework/pom.xml)。例如 [AsyncFactory.java](/Users/manzhushaka/CodexProject/manzhushaka-ry-scaff/manzhushaka-ry-framework/src/main/java/com/manzhushaka/framework/manager/factory/AsyncFactory.java) 和 [LogAspect.java](/Users/manzhushaka/CodexProject/manzhushaka-ry-scaff/manzhushaka-ry-framework/src/main/java/com/manzhushaka/framework/aspectj/LogAspect.java) 直接引用 `system` 的日志领域对象。
5. 登录和注册请求体仍然放在 `common`，例如 [LoginBody.java](/Users/manzhushaka/CodexProject/manzhushaka-ry-scaff/manzhushaka-ry-common/src/main/java/com/manzhushaka/common/core/domain/model/LoginBody.java) 和 [RegisterBody.java](/Users/manzhushaka/CodexProject/manzhushaka-ry-scaff/manzhushaka-ry-common/src/main/java/com/manzhushaka/common/core/domain/model/RegisterBody.java)。这与“HTTP DTO/VO 统一归 `admin`”的目标不一致。
6. MyBatis 配置默认扫描 `com.manzhushaka.**.domain` 作为别名包，见 [application.yml](/Users/manzhushaka/CodexProject/manzhushaka-ry-scaff/manzhushaka-ry-admin/src/main/resources/application.yml)。后续若持久化实体迁移至 `infrastructure/persistence/entity`，必须同步调整配置。

这些问题决定了本次迁移不能简单通过新增一个 `manzhushaka-ry-db` 模块来“收口”。正确方向是：按业务域各自持久化，按入口层统一收口 HTTP。

## 2. 迁移完成后的目标结构

### 2.1 Maven 模块结构

```text
manzhushaka-ry-admin
manzhushaka-ry-common
manzhushaka-ry-framework
manzhushaka-ry-system
manzhushaka-ry-quartz

# 后续新增业务域时按相同模板扩展
manzhushaka-ry-member
manzhushaka-ry-order
manzhushaka-ry-content
```

### 2.2 `admin` 模块职责

`manzhushaka-ry-admin` 只做以下事情：

- Controller
- HTTP 请求 DTO
- HTTP 响应 VO
- `DTO/VO <-> Command/Query/Application Result` 转换
- Swagger / SpringDoc 分组声明
- 与接口协议相关的参数校验

`admin` 不允许新增以下内容：

- Mapper
- 持久化实体
- 跨表查询实现
- 业务规则编排
- 直接调用其他模块的 Mapper

### 2.3 业务域模块职责

每个业务域模块，例如 `manzhushaka-ry-system`、未来的 `manzhushaka-ry-order`，统一采用以下内部结构：

```text
com.manzhushaka.<domain>
├── application
│   ├── service
│   ├── command
│   ├── query
│   └── assembler
├── domain
│   ├── model
│   ├── service
│   └── repository
└── infrastructure
    ├── persistence
    │   ├── entity
    │   ├── mapper
    │   ├── repository
    │   └── converter
    └── config
```

职责约束：

- `application` 负责用例编排，供 `admin` 调用。
- `domain` 负责业务语义与规则。
- `infrastructure` 负责数据库交互、MyBatis Mapper、持久化实体。
- 一个业务域不得直接依赖另一个业务域的 `entity/mapper/repository impl`。

### 2.4 `common` 模块职责

`manzhushaka-ry-common` 只保留真正的通用能力：

- `BaseEntity`、`TreeEntity`
- 通用异常
- 通用注解
- 通用工具类
- 通用常量
- 通用返回体基础类型（后续如需继续保留 `AjaxResult` / `TableDataInfo`）
- 安全无业务归属的基础模型

不得再放入：

- `SysUser`
- `SysRole`
- `SysMenu`
- `SysDept`
- `SysDictType`
- `SysDictData`
- 登录/注册 HTTP 请求体

### 2.5 `framework` 模块职责

`manzhushaka-ry-framework` 聚焦跨领域基础设施：

- Security
- AOP
- Redis
- MyBatis 基础配置
- 日志切面
- 线程池
- 过滤器与拦截器

短期内允许它调用 `system` 暴露的应用服务或稳定接口；长期目标是不直接依赖 `system` 的持久化结构与内部模型。

## 3. 全局依赖规则

本计划执行后，必须遵守以下依赖规则：

1. `admin -> system/member/order/...`
2. `framework -> common`
3. `framework` 不得直接使用某业务域的 Mapper
4. 业务域 A -> 业务域 B 时，只能依赖 B 暴露的应用服务、查询能力或未来抽取的稳定契约
5. 业务域之间共享 `ID`、轻量视图对象、命令结果对象，不共享持久化实体
6. `admin` 不得把 MyBatis `entity` 直接作为 HTTP 出参
7. `admin` 不得直接把数据库实体作为 `@RequestBody`
8. 所有新业务域都必须“各自持久化”，不新增“大一统 `db` 模块”

## 4. 当前仓库的关键受影响文件

### 4.1 父工程与模块依赖

- [pom.xml](/Users/manzhushaka/CodexProject/manzhushaka-ry-scaff/pom.xml)
- [manzhushaka-ry-admin/pom.xml](/Users/manzhushaka/CodexProject/manzhushaka-ry-scaff/manzhushaka-ry-admin/pom.xml)
- [manzhushaka-ry-framework/pom.xml](/Users/manzhushaka/CodexProject/manzhushaka-ry-scaff/manzhushaka-ry-framework/pom.xml)
- [manzhushaka-ry-system/pom.xml](/Users/manzhushaka/CodexProject/manzhushaka-ry-scaff/manzhushaka-ry-system/pom.xml)
- [manzhushaka-ry-common/pom.xml](/Users/manzhushaka/CodexProject/manzhushaka-ry-scaff/manzhushaka-ry-common/pom.xml)

### 4.2 HTTP 入口层

- [manzhushaka-ry-admin/src/main/java/com/manzhushaka/web/controller/system](/Users/manzhushaka/CodexProject/manzhushaka-ry-scaff/manzhushaka-ry-admin/src/main/java/com/manzhushaka/web/controller/system)
- [manzhushaka-ry-admin/src/main/java/com/manzhushaka/web/controller/monitor](/Users/manzhushaka/CodexProject/manzhushaka-ry-scaff/manzhushaka-ry-admin/src/main/java/com/manzhushaka/web/controller/monitor)
- [manzhushaka-ry-admin/src/main/java/com/manzhushaka/web/controller/common](/Users/manzhushaka/CodexProject/manzhushaka-ry-scaff/manzhushaka-ry-admin/src/main/java/com/manzhushaka/web/controller/common)

重点 Controller：

- [SysUserController.java](/Users/manzhushaka/CodexProject/manzhushaka-ry-scaff/manzhushaka-ry-admin/src/main/java/com/manzhushaka/web/controller/system/SysUserController.java)
- [SysRoleController.java](/Users/manzhushaka/CodexProject/manzhushaka-ry-scaff/manzhushaka-ry-admin/src/main/java/com/manzhushaka/web/controller/system/SysRoleController.java)
- [SysLoginController.java](/Users/manzhushaka/CodexProject/manzhushaka-ry-scaff/manzhushaka-ry-admin/src/main/java/com/manzhushaka/web/controller/system/SysLoginController.java)
- [SysRegisterController.java](/Users/manzhushaka/CodexProject/manzhushaka-ry-scaff/manzhushaka-ry-admin/src/main/java/com/manzhushaka/web/controller/system/SysRegisterController.java)

### 4.3 `system` 业务实现与持久化

- [manzhushaka-ry-system/src/main/java/com/manzhushaka/system/service](/Users/manzhushaka/CodexProject/manzhushaka-ry-scaff/manzhushaka-ry-system/src/main/java/com/manzhushaka/system/service)
- [manzhushaka-ry-system/src/main/java/com/manzhushaka/system/mapper](/Users/manzhushaka/CodexProject/manzhushaka-ry-scaff/manzhushaka-ry-system/src/main/java/com/manzhushaka/system/mapper)
- [manzhushaka-ry-system/src/main/resources/mapper/system](/Users/manzhushaka/CodexProject/manzhushaka-ry-scaff/manzhushaka-ry-system/src/main/resources/mapper/system)
- [manzhushaka-ry-system/src/main/java/com/manzhushaka/system/domain](/Users/manzhushaka/CodexProject/manzhushaka-ry-scaff/manzhushaka-ry-system/src/main/java/com/manzhushaka/system/domain)

### 4.4 `common` 中待回收的业务对象

- [SysUser.java](/Users/manzhushaka/CodexProject/manzhushaka-ry-scaff/manzhushaka-ry-common/src/main/java/com/manzhushaka/common/core/domain/entity/SysUser.java)
- [SysRole.java](/Users/manzhushaka/CodexProject/manzhushaka-ry-scaff/manzhushaka-ry-common/src/main/java/com/manzhushaka/common/core/domain/entity/SysRole.java)
- [SysMenu.java](/Users/manzhushaka/CodexProject/manzhushaka-ry-scaff/manzhushaka-ry-common/src/main/java/com/manzhushaka/common/core/domain/entity/SysMenu.java)
- [SysDept.java](/Users/manzhushaka/CodexProject/manzhushaka-ry-scaff/manzhushaka-ry-common/src/main/java/com/manzhushaka/common/core/domain/entity/SysDept.java)
- [SysDictType.java](/Users/manzhushaka/CodexProject/manzhushaka-ry-scaff/manzhushaka-ry-common/src/main/java/com/manzhushaka/common/core/domain/entity/SysDictType.java)
- [SysDictData.java](/Users/manzhushaka/CodexProject/manzhushaka-ry-scaff/manzhushaka-ry-common/src/main/java/com/manzhushaka/common/core/domain/entity/SysDictData.java)
- [LoginBody.java](/Users/manzhushaka/CodexProject/manzhushaka-ry-scaff/manzhushaka-ry-common/src/main/java/com/manzhushaka/common/core/domain/model/LoginBody.java)
- [RegisterBody.java](/Users/manzhushaka/CodexProject/manzhushaka-ry-scaff/manzhushaka-ry-common/src/main/java/com/manzhushaka/common/core/domain/model/RegisterBody.java)

### 4.5 基础设施耦合点

- [AsyncFactory.java](/Users/manzhushaka/CodexProject/manzhushaka-ry-scaff/manzhushaka-ry-framework/src/main/java/com/manzhushaka/framework/manager/factory/AsyncFactory.java)
- [LogAspect.java](/Users/manzhushaka/CodexProject/manzhushaka-ry-scaff/manzhushaka-ry-framework/src/main/java/com/manzhushaka/framework/aspectj/LogAspect.java)
- [MyBatisConfig.java](/Users/manzhushaka/CodexProject/manzhushaka-ry-scaff/manzhushaka-ry-framework/src/main/java/com/manzhushaka/framework/config/MyBatisConfig.java)
- [ApplicationConfig.java](/Users/manzhushaka/CodexProject/manzhushaka-ry-scaff/manzhushaka-ry-framework/src/main/java/com/manzhushaka/framework/config/ApplicationConfig.java)
- [application.yml](/Users/manzhushaka/CodexProject/manzhushaka-ry-scaff/manzhushaka-ry-admin/src/main/resources/application.yml)

## 5. 迁移原则

1. 先建立边界，再迁移代码，不先大规模重命名。
2. 先试点，后批量。优先以 `login/register` 和 `user/role` 作为迁移试点。
3. 每阶段必须可编译、可启动、可回滚。
4. 新结构优先增量引入，不一次性推倒旧结构。
5. 迁移期间允许少量“旧服务接口 + 新应用服务”并存，但必须有明确去除计划。
6. 在新增 `member/order/content` 前，先把 `system` 域的模板打磨稳定。

## 6. 分阶段实施蓝图

### 阶段 0：建立迁移护栏与目录骨架

**目标：** 在不改业务行为的前提下，先把新的包结构、测试依赖、验证方式和文档规则准备好。

**涉及文件：**

- 修改：`pom.xml`
- 修改：`manzhushaka-ry-admin/pom.xml`
- 修改：`manzhushaka-ry-framework/pom.xml`
- 修改：`manzhushaka-ry-system/pom.xml`
- 修改：`manzhushaka-ry-common/pom.xml`
- 创建：`manzhushaka-ry-admin/src/main/java/com/manzhushaka/web/dto/`
- 创建：`manzhushaka-ry-admin/src/main/java/com/manzhushaka/web/vo/`
- 创建：`manzhushaka-ry-admin/src/main/java/com/manzhushaka/web/converter/`
- 创建：`manzhushaka-ry-system/src/main/java/com/manzhushaka/system/application/`
- 创建：`manzhushaka-ry-system/src/main/java/com/manzhushaka/system/domain/model/`
- 创建：`manzhushaka-ry-system/src/main/java/com/manzhushaka/system/domain/repository/`
- 创建：`manzhushaka-ry-system/src/main/java/com/manzhushaka/system/infrastructure/persistence/`

- [ ] **步骤 1：在父 POM 中补齐测试基线依赖规划**

在 `pom.xml` 的 `dependencyManagement` 中新增测试依赖版本规划，至少包括：

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <version>${spring-boot.version}</version>
    <scope>test</scope>
</dependency>
```

预期：后续各模块可按需引入统一测试能力。

- [ ] **步骤 2：在业务模块中引入最小测试依赖**

在 `manzhushaka-ry-system/pom.xml` 和 `manzhushaka-ry-admin/pom.xml` 中按需引入：

```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-test</artifactId>
    <scope>test</scope>
</dependency>
```

预期：至少为后续 Controller 转换测试和应用服务测试提供基础。

- [ ] **步骤 3：创建新的包骨架，但不迁移行为**

创建以下空目录并保留包注释文件或占位类：

```text
manzhushaka-ry-admin/src/main/java/com/manzhushaka/web/dto/system
manzhushaka-ry-admin/src/main/java/com/manzhushaka/web/vo/system
manzhushaka-ry-admin/src/main/java/com/manzhushaka/web/converter/system
manzhushaka-ry-system/src/main/java/com/manzhushaka/system/application/service
manzhushaka-ry-system/src/main/java/com/manzhushaka/system/application/command
manzhushaka-ry-system/src/main/java/com/manzhushaka/system/application/query
manzhushaka-ry-system/src/main/java/com/manzhushaka/system/domain/model
manzhushaka-ry-system/src/main/java/com/manzhushaka/system/domain/repository
manzhushaka-ry-system/src/main/java/com/manzhushaka/system/infrastructure/persistence/entity
manzhushaka-ry-system/src/main/java/com/manzhushaka/system/infrastructure/persistence/mapper
manzhushaka-ry-system/src/main/java/com/manzhushaka/system/infrastructure/persistence/repository
manzhushaka-ry-system/src/main/java/com/manzhushaka/system/infrastructure/persistence/converter
```

预期：为后续迁移提供确定落点，避免边改边想路径。

- [ ] **步骤 4：记录架构约束到 README 或开发规范附录**

在 [README.md](/Users/manzhushaka/CodexProject/manzhushaka-ry-scaff/README.md) 或新增开发文档中补充：

```markdown
- Controller 与 HTTP DTO/VO 统一放在 `manzhushaka-ry-admin`
- 业务域各自持久化，不新增统一 `db` 模块
- `common` 不承载业务实体
```

预期：后续新开发不会继续反向生长。

- [ ] **步骤 5：运行全量编译**

运行：

```bash
mvn clean test -DskipTests=false
```

如果当前仓库尚无测试，允许先运行：

```bash
mvn clean package -DskipTests
```

预期：阶段 0 仅新增骨架与依赖，不应破坏构建。

- [ ] **步骤 6：Commit**

```bash
git add pom.xml manzhushaka-ry-admin/pom.xml manzhushaka-ry-system/pom.xml manzhushaka-ry-common/pom.xml manzhushaka-ry-framework/pom.xml README.md manzhushaka-ry-admin/src/main/java/com/manzhushaka/web manzhushaka-ry-system/src/main/java/com/manzhushaka/system
git commit -m "chore: establish domain modularization skeleton"
```

### 阶段 1：先迁 HTTP DTO/VO，切断 `common` 持有 Web 请求体

**目标：** 把登录、注册以及第一批系统管理请求对象从 `common` 迁出，验证“`admin` 管 HTTP 对象”的规则。

**涉及文件：**

- 修改：`manzhushaka-ry-admin/src/main/java/com/manzhushaka/web/controller/system/SysLoginController.java`
- 修改：`manzhushaka-ry-admin/src/main/java/com/manzhushaka/web/controller/system/SysRegisterController.java`
- 修改：`manzhushaka-ry-framework/src/main/java/com/manzhushaka/framework/web/service/SysRegisterService.java`
- 创建：`manzhushaka-ry-admin/src/main/java/com/manzhushaka/web/dto/system/LoginRequest.java`
- 创建：`manzhushaka-ry-admin/src/main/java/com/manzhushaka/web/dto/system/RegisterRequest.java`
- 创建：`manzhushaka-ry-admin/src/main/java/com/manzhushaka/web/converter/system/AuthAdminConverter.java`
- 保留旧文件待兼容：`LoginBody.java`、`RegisterBody.java`

- [ ] **步骤 1：为登录和注册 DTO 创建新对象**

在 `admin` 中新增：

```java
public class LoginRequest {
    private String username;
    private String password;
    private String code;
    private String uuid;
}
```

```java
public class RegisterRequest {
    private String username;
    private String password;
    private String code;
    private String uuid;
}
```

预期：HTTP 入参从 `common` 中脱离。

- [ ] **步骤 2：补充转换器**

新增 `AuthAdminConverter`，负责：

```java
public record LoginCommand(String username, String password, String code, String uuid) {}
public record RegisterCommand(String username, String password, String code, String uuid) {}
```

以及：

```java
public LoginCommand toLoginCommand(LoginRequest request) { ... }
public RegisterCommand toRegisterCommand(RegisterRequest request) { ... }
```

预期：Controller 不再把 Web 请求对象直接透传到下层。

- [ ] **步骤 3：让 `SysLoginController` 和 `SysRegisterController` 改用新 DTO**

将：

```java
public AjaxResult login(@RequestBody LoginBody loginBody)
```

改为：

```java
public AjaxResult login(@RequestBody LoginRequest request)
```

将：

```java
public AjaxResult register(@RequestBody RegisterBody user)
```

改为：

```java
public AjaxResult register(@RequestBody RegisterRequest request)
```

预期：`admin` 成为 HTTP 请求对象唯一所有者。

- [ ] **步骤 4：修改 `SysRegisterService` 的入参**

将：

```java
public String register(RegisterBody registerBody)
```

调整为：

```java
public String register(RegisterCommand command)
```

预期：`framework` 不再直接感知 HTTP 请求体类型。

- [ ] **步骤 5：为登录和注册转换器补充单元测试**

创建测试：

`manzhushaka-ry-admin/src/test/java/com/manzhushaka/web/converter/system/AuthAdminConverterTest.java`

至少验证：

- DTO 字段完整映射到 Command
- `null` 输入的处理策略明确

运行：

```bash
mvn -pl manzhushaka-ry-admin -am test
```

预期：转换器行为被固定下来。

- [ ] **步骤 6：删除对旧 `LoginBody/RegisterBody` 的直接引用**

搜索：

```bash
rg -n "LoginBody|RegisterBody" manzhushaka-ry-admin manzhushaka-ry-framework manzhushaka-ry-system
```

预期：除兼容期保留文件本身外，不再有业务代码引用它们。

- [ ] **步骤 7：Commit**

```bash
git add manzhushaka-ry-admin/src/main/java/com/manzhushaka/web/controller/system/SysLoginController.java manzhushaka-ry-admin/src/main/java/com/manzhushaka/web/controller/system/SysRegisterController.java manzhushaka-ry-admin/src/main/java/com/manzhushaka/web/dto/system manzhushaka-ry-admin/src/main/java/com/manzhushaka/web/converter/system manzhushaka-ry-framework/src/main/java/com/manzhushaka/framework/web/service/SysRegisterService.java manzhushaka-ry-admin/src/test/java/com/manzhushaka/web/converter/system/AuthAdminConverterTest.java
git commit -m "refactor: move auth http dto into admin module"
```

### 阶段 2：为 `system` 建立应用层入口，试点 `user` 与 `role`

**目标：** 引入 `application service + command/query`，避免 Controller 直接操作 `system` 中的旧服务接口与业务实体。

**涉及文件：**

- 修改：`SysUserController.java`
- 修改：`SysRoleController.java`
- 创建：`manzhushaka-ry-system/src/main/java/com/manzhushaka/system/application/service/SystemUserAppService.java`
- 创建：`manzhushaka-ry-system/src/main/java/com/manzhushaka/system/application/service/SystemRoleAppService.java`
- 创建：`manzhushaka-ry-system/src/main/java/com/manzhushaka/system/application/command/...`
- 创建：`manzhushaka-ry-system/src/main/java/com/manzhushaka/system/application/query/...`
- 创建：`manzhushaka-ry-admin/src/main/java/com/manzhushaka/web/dto/system/user/...`
- 创建：`manzhushaka-ry-admin/src/main/java/com/manzhushaka/web/vo/system/user/...`
- 创建：`manzhushaka-ry-admin/src/main/java/com/manzhushaka/web/converter/system/user/...`

- [ ] **步骤 1：为用户和角色试点定义 Command/Query**

至少新增：

```java
public record UserListQuery(...)
public record CreateUserCommand(...)
public record UpdateUserCommand(...)
public record ResetUserPasswordCommand(...)
public record ChangeUserStatusCommand(...)
```

```java
public record RoleListQuery(...)
public record CreateRoleCommand(...)
public record UpdateRoleCommand(...)
public record ChangeRoleStatusCommand(...)
```

预期：Controller 依赖“应用层入参”，而不是 `SysUser/SysRole` 实体。

- [ ] **步骤 2：新增应用服务作为 Controller 唯一入口**

在 `SystemUserAppService` 中暴露方法，例如：

```java
PageResult<UserSummary> listUsers(UserListQuery query);
UserDetail getUserDetail(Long userId);
Long createUser(CreateUserCommand command);
void updateUser(UpdateUserCommand command);
```

在 `SystemRoleAppService` 中暴露角色用例方法。

预期：Controller 不再直接编排多个旧 `ISys*Service`。

- [ ] **步骤 3：在 `admin` 新增 DTO/VO/Converter**

为用户和角色最先落地以下对象：

- 用户列表查询 DTO
- 用户新增 DTO
- 用户编辑 DTO
- 用户详情 VO
- 角色列表查询 DTO
- 角色新增/编辑 DTO

预期：`SysUserController`、`SysRoleController` 不再直接接收 `SysUser/SysRole/SysUserRole`。

- [ ] **步骤 4：改造 `SysUserController`**

将如下签名：

```java
public TableDataInfo list(SysUser user)
public AjaxResult add(@Validated @RequestBody SysUser user)
public AjaxResult edit(@Validated @RequestBody SysUser user)
public AjaxResult resetPwd(@RequestBody SysUser user)
```

改造为 DTO + AppService 调用。

预期：Controller 只做：

- 参数接收
- DTO 转 Command/Query
- 调用 AppService
- 转 VO

- [ ] **步骤 5：改造 `SysRoleController`**

尤其要把：

- `allocatedList(SysUser user)`
- `unallocatedList(SysUser user)`
- `cancelAuthUser(@RequestBody SysUserRole userRole)`

从“直接吃业务实体”迁移为 DTO + 应用服务。

- [ ] **步骤 6：为 `user/role` 试点补充 Web 层测试**

建议至少新增：

- `SysUserControllerTest`
- `SysRoleControllerTest`

测试重点：

- DTO 校验生效
- Controller 只依赖 AppService
- 响应结构不回传持久化实体内部字段

运行：

```bash
mvn -pl manzhushaka-ry-admin -am test
```

- [ ] **步骤 7：Commit**

```bash
git add manzhushaka-ry-admin/src/main/java/com/manzhushaka/web/controller/system/SysUserController.java manzhushaka-ry-admin/src/main/java/com/manzhushaka/web/controller/system/SysRoleController.java manzhushaka-ry-admin/src/main/java/com/manzhushaka/web/dto manzhushaka-ry-admin/src/main/java/com/manzhushaka/web/vo manzhushaka-ry-admin/src/main/java/com/manzhushaka/web/converter manzhushaka-ry-system/src/main/java/com/manzhushaka/system/application manzhushaka-ry-admin/src/test/java
git commit -m "refactor: introduce system application layer for user and role"
```

### 阶段 3：回收 `common` 中的系统业务实体到 `system`

**目标：** 让 `SysUser`、`SysRole`、`SysMenu`、`SysDept`、`SysDictType`、`SysDictData` 回归 `system` 域所有。

**涉及文件：**

- 修改：`manzhushaka-ry-common/src/main/java/com/manzhushaka/common/core/domain/entity/*`
- 创建：`manzhushaka-ry-system/src/main/java/com/manzhushaka/system/domain/model/*` 或 `infrastructure/persistence/entity/*`
- 修改：所有 import 这些类的文件
- 修改：MyBatis Mapper 与 XML 引用

- [ ] **步骤 1：决定回收目标位置**

对每个对象做归属判断：

- 若对象承担数据库映射责任，迁入 `infrastructure/persistence/entity`
- 若对象承担业务语义责任，新增对应 `domain/model`

首批建议：

- `SysUser`、`SysRole`、`SysMenu`、`SysDept`、`SysDictType`、`SysDictData` 先迁入 `system.infrastructure.persistence.entity`

预期：先完成所有权回收，再做进一步领域建模。

- [ ] **步骤 2：修改 `system` 中的 Mapper 引用**

例如 [SysUserMapper.java](/Users/manzhushaka/CodexProject/manzhushaka-ry-scaff/manzhushaka-ry-system/src/main/java/com/manzhushaka/system/mapper/SysUserMapper.java) 当前引用 `common.core.domain.entity.SysUser`，应改为新的 `system` 包路径。

同理处理：

- `SysRoleMapper`
- `SysMenuMapper`
- `SysDeptMapper`
- `SysDictTypeMapper`
- `SysDictDataMapper`

- [ ] **步骤 3：修改 `framework` 与 `admin` 的 import**

使用：

```bash
rg -n "common\\.core\\.domain\\.entity\\.(SysUser|SysRole|SysMenu|SysDept|SysDictType|SysDictData)" manzhushaka-ry-admin/src/main/java manzhushaka-ry-framework/src/main/java manzhushaka-ry-system/src/main/java
```

逐一改为 `system` 中的新包路径。

- [ ] **步骤 4：调整 MyBatis `typeAliasesPackage`**

当前配置：

```yaml
mybatis:
  typeAliasesPackage: com.manzhushaka.**.domain
```

调整为覆盖新的持久化实体路径，例如：

```yaml
mybatis:
  typeAliasesPackage: com.manzhushaka.**.domain,com.manzhushaka.**.infrastructure.persistence.entity
```

必要时同步确认 [MyBatisConfig.java](/Users/manzhushaka/CodexProject/manzhushaka-ry-scaff/manzhushaka-ry-framework/src/main/java/com/manzhushaka/framework/config/MyBatisConfig.java) 扫描逻辑兼容。

- [ ] **步骤 5：删除或清空 `common` 中对应业务实体**

在所有引用迁完并验证通过后，删除：

- `SysUser.java`
- `SysRole.java`
- `SysMenu.java`
- `SysDept.java`
- `SysDictType.java`
- `SysDictData.java`

预期：`common` 不再持有系统业务实体。

- [ ] **步骤 6：运行编译与关键回归**

运行：

```bash
mvn clean package -DskipTests
```

然后手工验证：

- 登录
- 获取用户列表
- 获取角色列表
- 获取菜单与路由

- [ ] **步骤 7：Commit**

```bash
git add manzhushaka-ry-common/src/main/java/com/manzhushaka/common/core/domain/entity manzhushaka-ry-system/src/main/java/com/manzhushaka/system manzhushaka-ry-admin/src/main/java manzhushaka-ry-framework/src/main/java manzhushaka-ry-admin/src/main/resources/application.yml
git commit -m "refactor: move system business entities out of common"
```

### 阶段 4：把 `system` 的持久化能力收敛到 `infrastructure/persistence`

**目标：** 让 Mapper、Mapper XML、持久化实体、仓储实现统一进入 `system.infrastructure.persistence`，形成域内闭环。

**涉及文件：**

- 修改：`manzhushaka-ry-system/src/main/java/com/manzhushaka/system/mapper/*`
- 修改：`manzhushaka-ry-system/src/main/resources/mapper/system/*`
- 创建：`manzhushaka-ry-system/src/main/java/com/manzhushaka/system/infrastructure/persistence/mapper/*`
- 创建：`manzhushaka-ry-system/src/main/java/com/manzhushaka/system/domain/repository/*`
- 创建：`manzhushaka-ry-system/src/main/java/com/manzhushaka/system/infrastructure/persistence/repository/*`

- [ ] **步骤 1：为仓储定义接口**

例如：

```java
public interface UserRepository {
    List<UserEntity> findUsers(UserFilter filter);
    Optional<UserEntity> findById(Long userId);
    Long save(UserEntity user);
}
```

预期：`application` 和 `domain` 只依赖仓储接口，不依赖 MyBatis Mapper。

- [ ] **步骤 2：迁移 Mapper 到新包**

将 `com.manzhushaka.system.mapper` 下的接口逐步迁移至：

```text
com.manzhushaka.system.infrastructure.persistence.mapper
```

保持接口名不变，先只改包路径，避免一次性改名带来过多噪音。

- [ ] **步骤 3：实现 Repository Impl 封装 Mapper**

例如：

```java
@Repository
public class UserRepositoryImpl implements UserRepository {
    private final SysUserMapper userMapper;
}
```

预期：上层不再直接注入 `SysUserMapper`。

- [ ] **步骤 4：迁移 XML 路径并确认扫描规则**

可保留 `resources/mapper/system` 物理路径，也可迁移为：

```text
manzhushaka-ry-system/src/main/resources/mapper/system/user/
manzhushaka-ry-system/src/main/resources/mapper/system/role/
```

不建议此阶段大改命名，只要确保 `mapperLocations: classpath*:mapper/**/*Mapper.xml` 仍能扫描到。

- [ ] **步骤 5：让应用服务改为依赖 Repository**

例如 `SysUserServiceImpl` 中对 `SysUserMapper`、`SysRoleMapper`、`SysPostMapper` 的直接依赖，要在迁移路径上逐步过渡到：

- `UserRepository`
- `RoleRepository`
- `PostRepository`

预期：`service impl` 不再同时扮演“应用编排 + 数据访问协调器”。

- [ ] **步骤 6：补充仓储级测试或最小集成测试**

若仓库尚无数据库测试基线，至少补充：

- Mapper 上下文启动测试
- Repository Bean 装配测试

运行：

```bash
mvn -pl manzhushaka-ry-system -am test
```

- [ ] **步骤 7：Commit**

```bash
git add manzhushaka-ry-system/src/main/java/com/manzhushaka/system manzhushaka-ry-system/src/main/resources/mapper/system
git commit -m "refactor: isolate system persistence under infrastructure"
```

### 阶段 5：削弱 `framework -> system` 的直接耦合

**目标：** 让 `framework` 只依赖稳定业务能力，不直接了解 `system` 的内部持久化结构。

**涉及文件：**

- 修改：`manzhushaka-ry-framework/pom.xml`
- 修改：`AsyncFactory.java`
- 修改：`LogAspect.java`
- 修改：`SysLoginService.java`
- 修改：`SysRegisterService.java`
- 修改：`UserDetailsServiceImpl.java`
- 修改：`SysPermissionService.java`
- 修改：`PermissionService.java`

- [ ] **步骤 1：盘点 `framework` 中必须保留的业务能力依赖**

目前主要集中在：

- 用户认证
- 权限查询
- 登录日志
- 操作日志
- 配置项查询

将这些能力明确为“应用层依赖点”，而不是实体依赖点。

- [ ] **步骤 2：优先替换日志对象直接构造方式**

`AsyncFactory` 和 `LogAspect` 当前直接构造 `SysLogininfor` / `SysOperLog`。短期建议引入 `system.application.service.SystemAuditAppService`，由 `framework` 调它的方法：

```java
void recordLoginInfo(LoginAuditCommand command);
void recordOperation(OperationAuditCommand command);
```

预期：`framework` 不再直接 new `system` 内部日志实体。

- [ ] **步骤 3：为权限与用户查询建立稳定服务边界**

让 `UserDetailsServiceImpl`、`SysPermissionService`、`PermissionService` 优先通过应用层查询用户、角色、菜单能力。

预期：减少 `framework` 对 `system` 领域模型结构的敏感度。

- [ ] **步骤 4：评估是否需要新增 `manzhushaka-ry-system-api`**

只有当以下条件同时满足时才抽：

- `framework`、`admin`、未来的 `member/order` 都稳定依赖同一组 `system` 能力
- 这组能力已经足够稳定
- 继续直接依赖 `system` 实现会显著放大编译和结构耦合

若条件尚不满足，则先维持“依赖 `system` 的应用层类”即可，不强行抽 API 模块。

- [ ] **步骤 5：运行登录、鉴权、日志链路回归**

手工验证：

- `/login`
- `getInfo`
- `getRouters`
- 用户操作日志落库
- 登录日志落库

预期：用户主流程无回归。

- [ ] **步骤 6：Commit**

```bash
git add manzhushaka-ry-framework/pom.xml manzhushaka-ry-framework/src/main/java/com/manzhushaka/framework manzhushaka-ry-system/src/main/java/com/manzhushaka/system
git commit -m "refactor: decouple framework from system internals"
```

### 阶段 6：建立新业务域模板并停止向 `system` 堆新业务

**目标：** 确保未来新增业务时不再把代码继续塞进 `system`，而是按域新建模块。

**涉及文件：**

- 修改：`pom.xml`
- 创建：`manzhushaka-ry-member/pom.xml`
- 创建：`manzhushaka-ry-order/pom.xml`
- 创建：模块骨架目录与 README

- [ ] **步骤 1：新增业务域模块模板**

在父 `pom.xml` 中新增模块声明，例如：

```xml
<module>manzhushaka-ry-member</module>
<module>manzhushaka-ry-order</module>
```

并创建最小模块骨架。

- [ ] **步骤 2：为每个新域初始化统一目录结构**

至少创建：

```text
application/service
application/command
application/query
domain/model
domain/repository
infrastructure/persistence/entity
infrastructure/persistence/mapper
infrastructure/persistence/repository
```

- [ ] **步骤 3：在 `admin` 中按域建立 HTTP 目录**

例如：

```text
com.manzhushaka.web.controller.member
com.manzhushaka.web.dto.member
com.manzhushaka.web.vo.member
com.manzhushaka.web.converter.member
```

预期：新增业务自然走新结构，而不是继续进入 `system` 包。

- [ ] **步骤 4：在 README 中增加“新增业务域开发模板”**

明确说明：

- 不往 `system` 堆业务
- 不新增总 `db` 模块
- 各域各自持久化

- [ ] **步骤 5：Commit**

```bash
git add pom.xml manzhushaka-ry-member manzhushaka-ry-order README.md
git commit -m "feat: add domain module templates for future business areas"
```

## 7. 试点顺序建议

推荐严格按以下顺序推进：

1. `login/register`
2. `user`
3. `role`
4. `menu`
5. `dept`
6. `dict`
7. `notice/post/config`
8. `logininfor/operlog/userOnline/cache`

原因：

- `login/register` 范围最小，适合先验证 `admin DTO -> command` 路线。
- `user/role` 涉及最典型的实体、列表、详情、授权、状态切换，适合验证整条新调用链。
- `monitor` 相关对象还与 `framework` 紧耦合，放后面更稳。

## 8. 验证与验收标准

### 8.1 每阶段通用验收

每阶段结束都必须满足：

- `mvn clean package -DskipTests` 成功
- 若已建立测试，则对应模块测试成功
- 应用可启动
- 关键接口手工回归通过
- 无跨模块直接调用对方 Mapper 的新代码
- `common` 中业务对象数量只减不增

### 8.2 关键回归清单

- 登录
- 获取当前用户信息
- 获取动态路由
- 用户列表
- 用户新增/编辑/重置密码/状态修改
- 角色列表/授权
- 菜单树
- 部门树
- 字典列表
- 登录日志
- 操作日志

### 8.3 边界检查命令

每次提交前至少运行：

```bash
rg -n "common\\.core\\.domain\\.entity\\.(SysUser|SysRole|SysMenu|SysDept|SysDictType|SysDictData)" manzhushaka-ry-admin/src/main/java manzhushaka-ry-framework/src/main/java manzhushaka-ry-system/src/main/java
```

```bash
rg -n "@RequestBody\\s+Sys(User|Role|Menu|Dept|DictType|DictData)" manzhushaka-ry-admin/src/main/java
```

```bash
rg -n "new Sys(Logininfor|OperLog)|import com\\.manzhushaka\\.system\\.mapper" manzhushaka-ry-framework/src/main/java
```

预期：

- 不再从 `common` 引用系统业务实体
- Controller 不再直接吃业务实体
- `framework` 不再依赖业务域 Mapper

## 9. 风险与回滚策略

### 9.1 主要风险

1. 包迁移导致 import 爆炸式修改，编译失败。
2. MyBatis `typeAliasesPackage` 与 XML `resultType`/`parameterType` 路径失配。
3. Controller 从实体切换为 DTO 后，前端提交字段与后端参数不兼容。
4. `framework` 中认证与日志链路改造不当，导致登录或审计回归。
5. 旧服务接口与新应用服务并存时间过长，形成双轨复杂度。

### 9.2 回滚策略

- 每个阶段独立 commit，禁止跨阶段混合提交。
- 若阶段内回归复杂，优先回滚当前阶段而不是硬修后续阶段。
- 包迁移时优先“复制新类 + 改引用 + 删除旧类”，不要直接大范围 rename。
- MyBatis 迁移时优先保留 XML 文件名与 SQL ID 不变，只改 Java 包路径。

## 10. 非目标

本计划明确不做以下事情：

- 不引入统一 `manzhushaka-ry-db` 模块
- 不在本轮中拆分为微服务
- 不重写所有历史业务逻辑
- 不一次性替换全部 `ISys*Service` 接口
- 不在没有稳定跨模块契约前，强行给每个域都抽 `*-api` 模块
- 不在本轮迁移中全面替换返回体风格（如 `AjaxResult` / `TableDataInfo`）

## 11. 交付物

本计划完成后，应至少产出：

1. 新的 `admin` HTTP DTO/VO/Converter 目录结构
2. `system` 的 `application/domain/infrastructure` 目录结构
3. `common` 中不再持有系统业务实体
4. `framework` 对 `system` 的耦合下降
5. 一个可供后续 `member/order/content` 复制的业务域模板

## 12. 执行建议

建议采用“小步快跑 + 高频验证”的节奏：

- 每个阶段控制在 1 到 3 个自然日
- 每个阶段单独 PR 或单独提交组
- 优先做“能形成示范效应”的试点，例如 `login/register`、`user/role`
- 在 `member/order` 尚未开始前，把 `system` 迁移模板打磨稳定

计划已完成并保存到 `docs/superpowers/plans/2026-06-28-domain-modularization-migration-plan.md`。两种执行方式：

**1. 子代理驱动（推荐）** - 每个任务调度一个新的子代理，任务间进行审查，快速迭代

**2. 内联执行** - 在当前会话中使用 `executing-plans` 执行任务，批量执行并设有检查点

选哪种方式？
