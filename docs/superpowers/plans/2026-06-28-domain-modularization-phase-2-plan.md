# manzhushaka-scaff 业务域模块化迁移第二阶段实现计划

> **面向 AI 代理的工作者：** 必需子技能：使用 `superpowers:subagent-driven-development`（推荐）或 `superpowers:executing-plans` 逐任务实现此计划。步骤使用复选框（`- [ ]`）语法来跟踪进度。

**目标：** 承接第一阶段已完成的 `auth/user/role` 改造，完成剩余系统管理入口的 HTTP 边界收口，移除 `framework/common` 中的双模型桥接，并为第三阶段彻底清空 `common` 中的业务实体依赖打下基础。

**架构：** `admin` 继续作为唯一 HTTP 入口层，补齐 `menu/dept/dict/profile` 的 `DTO/VO/Converter + AppService` 调用链；`framework` 和 `common` 不再围绕 `common` 版 `SysUser/SysRole/...` 建立桥接转换；`system` 内部继续巩固 `application + persistence entity + repository` 的路径，逐步把“旧 service + 新 app service 混用”收敛为清晰边界。

**技术栈：** Java 17、Spring Boot 4、Spring Security、MyBatis、Redis、Maven 多模块工程

---

## 1. 计划定位

本计划是对以下文档的续篇：

- [2026-06-28-domain-modularization-migration-plan.md](/Users/manzhushaka/CodexProject/manzhushaka-scaff/docs/superpowers/plans/2026-06-28-domain-modularization-migration-plan.md)

适用前提：

1. `admin` 中已经建立了 `dto/vo/converter` 目录。
2. `system` 中已经建立了 `application/command/query/service` 目录。
3. `auth/user/role` 已经完成了第一轮试点改造。

本计划不重复第一阶段中已经完成的部分，而是直接面向当前仓库的剩余问题收尾。

## 2. 当前实际状态与第二阶段范围

根据当前仓库现状，以下工作已经完成或部分完成：

- `admin` 已新增：
  - `com.manzhushaka.web.dto.system`
  - `com.manzhushaka.web.dto.system.user`
  - `com.manzhushaka.web.dto.system.role`
  - `com.manzhushaka.web.vo.system.user`
  - `com.manzhushaka.web.vo.system.role`
  - `com.manzhushaka.web.converter.system.user`
  - `com.manzhushaka.web.converter.system.role`
- `system` 已新增：
  - `application.command`
  - `application.query`
  - `application.service`
  - `application.service.impl`
  - `infrastructure.persistence.entity`
  - `infrastructure.persistence.repository`
- `SystemUserAppService`、`SystemRoleAppService`、`SystemAuditAppService` 已经存在。

但仍然有以下残留问题，正是第二阶段的核心范围：

1. `menu/dept/dict/profile` 仍在 Controller 层直接使用持久化实体。
2. `SysProfileController` 仍通过 [SysUserConverter.java](/Users/manzhushaka/CodexProject/manzhushaka-scaff/manzhushaka-framework/src/main/java/com/manzhushaka/framework/web/service/SysUserConverter.java) 在 `common` 版和 `system` 版 `SysUser` 之间来回转换。
3. `framework` 中仍大量依赖 `common.core.domain.entity.SysUser/SysRole/...`，例如：
   - [UserDetailsServiceImpl.java](/Users/manzhushaka/CodexProject/manzhushaka-scaff/manzhushaka-framework/src/main/java/com/manzhushaka/framework/web/service/UserDetailsServiceImpl.java)
   - [SysPermissionService.java](/Users/manzhushaka/CodexProject/manzhushaka-scaff/manzhushaka-framework/src/main/java/com/manzhushaka/framework/web/service/SysPermissionService.java)
   - [SysPasswordService.java](/Users/manzhushaka/CodexProject/manzhushaka-scaff/manzhushaka-framework/src/main/java/com/manzhushaka/framework/web/service/SysPasswordService.java)
   - [DataScopeAspect.java](/Users/manzhushaka/CodexProject/manzhushaka-scaff/manzhushaka-framework/src/main/java/com/manzhushaka/framework/aspectj/DataScopeAspect.java)
4. `common` 中仍保留系统业务实体与登录请求模型：
   - `SysUser`、`SysRole`、`SysMenu`、`SysDept`、`SysDictType`、`SysDictData`
   - `LoginBody`、`RegisterBody`
5. `common` 中的 [LoginUser.java](/Users/manzhushaka/CodexProject/manzhushaka-scaff/manzhushaka-common/src/main/java/com/manzhushaka/common/core/domain/model/LoginUser.java)、[SecurityUtils.java](/Users/manzhushaka/CodexProject/manzhushaka-scaff/manzhushaka-common/src/main/java/com/manzhushaka/common/utils/SecurityUtils.java)、[TreeSelect.java](/Users/manzhushaka/CodexProject/manzhushaka-scaff/manzhushaka-common/src/main/java/com/manzhushaka/common/core/domain/TreeSelect.java)、[DictUtils.java](/Users/manzhushaka/CodexProject/manzhushaka-scaff/manzhushaka-common/src/main/java/com/manzhushaka/common/utils/DictUtils.java) 仍然建立在 `common` 版业务实体之上。

因此，第二阶段的范围明确为三大块：

1. 完成剩余 `admin` HTTP 边界收口。
2. 移除 `framework/common` 中的双模型桥接。
3. 为后续彻底删除 `common` 中业务实体做兼容性收敛。

## 3. 第二阶段完成后的目标

完成本计划后，仓库应达到以下状态：

1. `SysMenuController`、`SysDeptController`、`SysDictTypeController`、`SysDictDataController`、`SysProfileController` 不再直接使用持久化实体作为 HTTP 入参。
2. `admin` 中系统管理相关 Controller 均通过 DTO/VO/Converter 与 `application service` 交互。
3. `framework` 中不再需要 `SysUserConverter` 这类 `common <-> system` 桥接器。
4. `UserDetailsServiceImpl`、`SysPermissionService`、`SysPasswordService`、`DataScopeAspect`、`LogAspect` 直接围绕统一的系统用户模型工作。
5. `common` 中的 `LoginBody`、`RegisterBody` 不再被任何生产代码引用。
6. `common` 中保留的业务实体引用面被压缩到可控范围，为第三阶段删除这些类做准备。

## 4. 第二阶段的总原则

1. 不重复改 `auth/user/role` 已经稳定的路径，除非为了消除桥接器必须做联动修正。
2. 优先先处理“直接影响新增功能继续走歪路”的入口层问题。
3. 先收口 `admin`，再收口 `framework/common`，最后做统一回归。
4. 允许短期内 `system.service` 与 `system.application.service` 并存，但 `admin` 不再新增对旧 `ISys*Service` 的直接依赖。
5. 第二阶段结束时，`common` 中即使还残留实体类，也只能作为待删除遗留物，不能继续被新代码依赖。

## 5. 关键文件清单

### 5.1 需要重点改造的 Controller

- [SysMenuController.java](/Users/manzhushaka/CodexProject/manzhushaka-scaff/manzhushaka-admin/src/main/java/com/manzhushaka/web/controller/system/SysMenuController.java)
- [SysDeptController.java](/Users/manzhushaka/CodexProject/manzhushaka-scaff/manzhushaka-admin/src/main/java/com/manzhushaka/web/controller/system/SysDeptController.java)
- [SysDictTypeController.java](/Users/manzhushaka/CodexProject/manzhushaka-scaff/manzhushaka-admin/src/main/java/com/manzhushaka/web/controller/system/SysDictTypeController.java)
- [SysDictDataController.java](/Users/manzhushaka/CodexProject/manzhushaka-scaff/manzhushaka-admin/src/main/java/com/manzhushaka/web/controller/system/SysDictDataController.java)
- [SysProfileController.java](/Users/manzhushaka/CodexProject/manzhushaka-scaff/manzhushaka-admin/src/main/java/com/manzhushaka/web/controller/system/SysProfileController.java)

### 5.2 需要重点改造的 `framework` 文件

- [UserDetailsServiceImpl.java](/Users/manzhushaka/CodexProject/manzhushaka-scaff/manzhushaka-framework/src/main/java/com/manzhushaka/framework/web/service/UserDetailsServiceImpl.java)
- [SysPermissionService.java](/Users/manzhushaka/CodexProject/manzhushaka-scaff/manzhushaka-framework/src/main/java/com/manzhushaka/framework/web/service/SysPermissionService.java)
- [SysPasswordService.java](/Users/manzhushaka/CodexProject/manzhushaka-scaff/manzhushaka-framework/src/main/java/com/manzhushaka/framework/web/service/SysPasswordService.java)
- [SysRegisterService.java](/Users/manzhushaka/CodexProject/manzhushaka-scaff/manzhushaka-framework/src/main/java/com/manzhushaka/framework/web/service/SysRegisterService.java)
- [PermissionService.java](/Users/manzhushaka/CodexProject/manzhushaka-scaff/manzhushaka-framework/src/main/java/com/manzhushaka/framework/web/service/PermissionService.java)
- [SysUserConverter.java](/Users/manzhushaka/CodexProject/manzhushaka-scaff/manzhushaka-framework/src/main/java/com/manzhushaka/framework/web/service/SysUserConverter.java)
- [DataScopeAspect.java](/Users/manzhushaka/CodexProject/manzhushaka-scaff/manzhushaka-framework/src/main/java/com/manzhushaka/framework/aspectj/DataScopeAspect.java)
- [LogAspect.java](/Users/manzhushaka/CodexProject/manzhushaka-scaff/manzhushaka-framework/src/main/java/com/manzhushaka/framework/aspectj/LogAspect.java)

### 5.3 `common` 中需要收口的遗留文件

- [LoginUser.java](/Users/manzhushaka/CodexProject/manzhushaka-scaff/manzhushaka-common/src/main/java/com/manzhushaka/common/core/domain/model/LoginUser.java)
- [SecurityUtils.java](/Users/manzhushaka/CodexProject/manzhushaka-scaff/manzhushaka-common/src/main/java/com/manzhushaka/common/utils/SecurityUtils.java)
- [TreeSelect.java](/Users/manzhushaka/CodexProject/manzhushaka-scaff/manzhushaka-common/src/main/java/com/manzhushaka/common/core/domain/TreeSelect.java)
- [DictUtils.java](/Users/manzhushaka/CodexProject/manzhushaka-scaff/manzhushaka-common/src/main/java/com/manzhushaka/common/utils/DictUtils.java)
- [LoginBody.java](/Users/manzhushaka/CodexProject/manzhushaka-scaff/manzhushaka-common/src/main/java/com/manzhushaka/common/core/domain/model/LoginBody.java)
- [RegisterBody.java](/Users/manzhushaka/CodexProject/manzhushaka-scaff/manzhushaka-common/src/main/java/com/manzhushaka/common/core/domain/model/RegisterBody.java)

## 6. 分任务实施蓝图

### 任务 1：补齐 `menu/dept/dict/profile` 的 HTTP 边界

**文件：**

- 修改：`manzhushaka-admin/src/main/java/com/manzhushaka/web/controller/system/SysMenuController.java`
- 修改：`manzhushaka-admin/src/main/java/com/manzhushaka/web/controller/system/SysDeptController.java`
- 修改：`manzhushaka-admin/src/main/java/com/manzhushaka/web/controller/system/SysDictTypeController.java`
- 修改：`manzhushaka-admin/src/main/java/com/manzhushaka/web/controller/system/SysDictDataController.java`
- 修改：`manzhushaka-admin/src/main/java/com/manzhushaka/web/controller/system/SysProfileController.java`
- 创建：`manzhushaka-admin/src/main/java/com/manzhushaka/web/dto/system/menu/*`
- 创建：`manzhushaka-admin/src/main/java/com/manzhushaka/web/dto/system/dept/*`
- 创建：`manzhushaka-admin/src/main/java/com/manzhushaka/web/dto/system/dict/*`
- 创建：`manzhushaka-admin/src/main/java/com/manzhushaka/web/dto/system/profile/*`
- 创建：`manzhushaka-admin/src/main/java/com/manzhushaka/web/vo/system/menu/*`
- 创建：`manzhushaka-admin/src/main/java/com/manzhushaka/web/vo/system/dept/*`
- 创建：`manzhushaka-admin/src/main/java/com/manzhushaka/web/vo/system/dict/*`
- 创建：`manzhushaka-admin/src/main/java/com/manzhushaka/web/vo/system/profile/*`
- 创建：`manzhushaka-admin/src/main/java/com/manzhushaka/web/converter/system/menu/*`
- 创建：`manzhushaka-admin/src/main/java/com/manzhushaka/web/converter/system/dept/*`
- 创建：`manzhushaka-admin/src/main/java/com/manzhushaka/web/converter/system/dict/*`
- 创建：`manzhushaka-admin/src/main/java/com/manzhushaka/web/converter/system/profile/*`

- [ ] **步骤 1：先为 `menu`、`dept`、`dict`、`profile` 定义请求 DTO**

至少新增以下 DTO：

```java
public class MenuListRequest { ... }
public class CreateMenuRequest { ... }
public class UpdateMenuRequest { ... }
public class UpdateMenuSortRequest { ... }
```

```java
public class DeptListRequest { ... }
public class CreateDeptRequest { ... }
public class UpdateDeptRequest { ... }
public class UpdateDeptSortRequest { ... }
```

```java
public class DictTypeListRequest { ... }
public class CreateDictTypeRequest { ... }
public class UpdateDictTypeRequest { ... }
public class DictDataListRequest { ... }
public class CreateDictDataRequest { ... }
public class UpdateDictDataRequest { ... }
```

```java
public class UpdateProfileRequest { ... }
public class UpdatePasswordRequest { ... }
```

预期：Controller 不再用 `SysMenu`、`SysDept`、`SysDictType`、`SysDictData` 作为 `@RequestBody`。

- [ ] **步骤 2：为 `menu/dept/dict/profile` 定义转换器**

在各自的 `converter` 包中新增：

```java
public final class MenuAdminConverter {
    public static CreateMenuCommand toCreateCommand(CreateMenuRequest request) { ... }
    public static UpdateMenuCommand toUpdateCommand(UpdateMenuRequest request) { ... }
}
```

同理为 `dept/dict/profile` 定义 `DTO -> Command/Query` 与 `AppResult -> VO` 的转换方法。

预期：Controller 中不再手写大量字段拷贝。

- [ ] **步骤 3：为 `menu/dept/dict/profile` 在 `system.application` 中补齐命令与查询对象**

新增类似如下对象：

```java
public record MenuListQuery(...)
public record CreateMenuCommand(...)
public record UpdateMenuCommand(...)
public record UpdateMenuSortCommand(...)
```

```java
public record DeptListQuery(...)
public record CreateDeptCommand(...)
public record UpdateDeptCommand(...)
public record UpdateDeptSortCommand(...)
```

```java
public record DictTypeListQuery(...)
public record CreateDictTypeCommand(...)
public record UpdateDictTypeCommand(...)
public record DictDataListQuery(...)
public record CreateDictDataCommand(...)
public record UpdateDictDataCommand(...)
```

```java
public record UpdateProfileCommand(...)
public record UpdatePasswordCommand(...)
public record UpdateAvatarCommand(...)
```

预期：第二批系统管理能力也走统一的应用层入参。

- [ ] **步骤 4：新增对应的应用服务接口与实现**

至少新增：

```java
public interface SystemMenuAppService { ... }
public interface SystemDeptAppService { ... }
public interface SystemDictAppService { ... }
public interface SystemProfileAppService { ... }
```

其中 `SystemProfileAppService` 需要吸收 [SysProfileController.java](/Users/manzhushaka/CodexProject/manzhushaka-scaff/manzhushaka-admin/src/main/java/com/manzhushaka/web/controller/system/SysProfileController.java) 当前的资料修改、密码修改、头像上传编排逻辑。

预期：`admin` 不再直接调 `ISysMenuService`、`ISysDeptService`、`ISysDictTypeService`、`ISysDictDataService`、`ISysUserService` 组合编排个人信息逻辑。

- [ ] **步骤 5：改造 5 个 Controller，只保留 HTTP 职责**

重点将以下签名改掉：

```java
public AjaxResult add(@Validated @RequestBody SysMenu menu)
public AjaxResult edit(@Validated @RequestBody SysMenu menu)
```

```java
public AjaxResult add(@Validated @RequestBody SysDept dept)
public AjaxResult edit(@Validated @RequestBody SysDept dept)
```

```java
public AjaxResult add(@Validated @RequestBody SysDictType dict)
public AjaxResult edit(@Validated @RequestBody SysDictType dict)
```

```java
public AjaxResult add(@Validated @RequestBody SysDictData dict)
public AjaxResult edit(@Validated @RequestBody SysDictData dict)
```

```java
public AjaxResult updateProfile(@RequestBody com.manzhushaka.system.infrastructure.persistence.entity.SysUser user)
```

改造后 Controller 只做：

- 接受 DTO
- 调用 Converter
- 调用 AppService
- 返回 `AjaxResult` 或 `TableDataInfo`

- [ ] **步骤 6：为 `menu/dept/dict/profile` 新增 Controller 级测试**

创建测试：

- `manzhushaka-admin/src/test/java/com/manzhushaka/web/controller/system/SysMenuControllerTest.java`
- `manzhushaka-admin/src/test/java/com/manzhushaka/web/controller/system/SysDeptControllerTest.java`
- `manzhushaka-admin/src/test/java/com/manzhushaka/web/controller/system/SysDictTypeControllerTest.java`
- `manzhushaka-admin/src/test/java/com/manzhushaka/web/controller/system/SysDictDataControllerTest.java`
- `manzhushaka-admin/src/test/java/com/manzhushaka/web/controller/system/SysProfileControllerTest.java`

至少验证：

- 请求 DTO 校验
- Controller 调用 AppService
- 不再把持久化实体作为请求体使用

运行：

```bash
mvn -pl manzhushaka-admin -am test
```

预期：剩余系统管理入口全部并入新边界。

- [ ] **步骤 7：Commit**

```bash
git add manzhushaka-admin/src/main/java/com/manzhushaka/web/controller/system manzhushaka-admin/src/main/java/com/manzhushaka/web/dto/system manzhushaka-admin/src/main/java/com/manzhushaka/web/vo/system manzhushaka-admin/src/main/java/com/manzhushaka/web/converter/system manzhushaka-system/src/main/java/com/manzhushaka/system/application manzhushaka-admin/src/test/java
git commit -m "refactor: complete http boundary for menu dept dict and profile"
```

### 任务 2：去掉 `framework` 中的 `common <-> system` 双模型桥接

**文件：**

- 修改：`manzhushaka-framework/src/main/java/com/manzhushaka/framework/web/service/UserDetailsServiceImpl.java`
- 修改：`manzhushaka-framework/src/main/java/com/manzhushaka/framework/web/service/SysPermissionService.java`
- 修改：`manzhushaka-framework/src/main/java/com/manzhushaka/framework/web/service/SysPasswordService.java`
- 修改：`manzhushaka-framework/src/main/java/com/manzhushaka/framework/web/service/SysRegisterService.java`
- 修改：`manzhushaka-framework/src/main/java/com/manzhushaka/framework/web/service/PermissionService.java`
- 删除：`manzhushaka-framework/src/main/java/com/manzhushaka/framework/web/service/SysUserConverter.java`

- [ ] **步骤 1：统一 `framework` 使用 `system.infrastructure.persistence.entity` 版用户模型**

把 `UserDetailsServiceImpl`、`SysPermissionService`、`SysPasswordService`、`SysRegisterService` 中对 `common.core.domain.entity.SysUser`、`SysRole` 的依赖改为 `system.infrastructure.persistence.entity` 对应类型。

预期：`framework` 内部只围绕一套用户模型工作。

- [ ] **步骤 2：重写 `UserDetailsServiceImpl#createLoginUser`，不再构造 `common` 版 `SysUser`**

当前实现会：

```java
SysUserConverter.toCommon(user)
return new LoginUser(user.getUserId(), user.getDeptId(), commonUser, ...)
```

第二阶段应改为直接使用统一模型，必要时同步调整 `LoginUser` 类型参数。

预期：删除 `SysUserConverter` 的根本依赖源头。

- [ ] **步骤 3：改造 `SysPermissionService`**

将：

```java
public Set<String> getRolePermission(SysUser user)
public Set<String> getMenuPermission(SysUser user)
```

中的 `SysUser/SysRole` 替换为统一模型类型。

预期：权限计算逻辑不再建立在 `common` 版角色上。

- [ ] **步骤 4：改造 `SysPasswordService` 与 `SysRegisterService`**

重点移除：

```java
SysUserConverter.toCommon(user)
SysUserConverter.toSystem(sysUser)
```

改为直接使用统一模型，避免“注册 -> common SysUser -> 再转 system SysUser”的来回折返。

- [ ] **步骤 5：删除 `SysUserConverter` 并清空所有引用**

运行：

```bash
rg -n "SysUserConverter|toCommon\\(|toSystem\\(" manzhushaka-admin/src/main/java manzhushaka-framework/src/main/java manzhushaka-system/src/main/java
```

预期：无任何生产代码引用。

- [ ] **步骤 6：为 `framework` 补充最小集成测试**

至少补充：

- `UserDetailsServiceImplTest`
- `SysPermissionServiceTest`

验证：

- 登录用户装配成功
- 权限集计算正确
- 不再依赖桥接转换器

运行：

```bash
mvn -pl manzhushaka-framework -am test
```

- [ ] **步骤 7：Commit**

```bash
git add manzhushaka-framework/src/main/java/com/manzhushaka/framework/web/service manzhushaka-framework/src/test/java
git commit -m "refactor: remove common-system user model bridge in framework"
```

### 任务 3：统一 `LoginUser`、`SecurityUtils`、`DataScope` 链路的用户模型

**文件：**

- 修改：`manzhushaka-common/src/main/java/com/manzhushaka/common/core/domain/model/LoginUser.java`
- 修改：`manzhushaka-common/src/main/java/com/manzhushaka/common/utils/SecurityUtils.java`
- 修改：`manzhushaka-framework/src/main/java/com/manzhushaka/framework/aspectj/DataScopeAspect.java`
- 修改：`manzhushaka-framework/src/main/java/com/manzhushaka/framework/aspectj/LogAspect.java`
- 修改：`manzhushaka-admin/src/main/java/com/manzhushaka/web/controller/system/SysLoginController.java`
- 修改：`manzhushaka-admin/src/main/java/com/manzhushaka/web/controller/system/SysProfileController.java`

- [ ] **步骤 1：让 `LoginUser` 持有统一用户类型**

当前 [LoginUser.java](/Users/manzhushaka/CodexProject/manzhushaka-scaff/manzhushaka-common/src/main/java/com/manzhushaka/common/core/domain/model/LoginUser.java) 仍然持有 `common.core.domain.entity.SysUser`。

将其改为统一用户模型，例如：

```java
private com.manzhushaka.system.infrastructure.persistence.entity.SysUser user;
```

并同步修改构造器、getter、setter。

预期：安全上下文中不再保存“遗留 `common` 版用户”。

- [ ] **步骤 2：调整 `SecurityUtils` 对角色与用户的访问**

当前 `SecurityUtils.hasRole()` 直接读取 `List<SysRole>`（`common` 版）。

同步切换到统一用户模型与统一角色模型。

预期：公共安全工具不再依赖遗留 `common` 业务实体。

- [ ] **步骤 3：改造 `DataScopeAspect` 与 `LogAspect`**

将其中的：

- `SysUser`
- `SysRole`

切换到统一模型，确认数据权限和操作日志逻辑保持不变。

重点验证：

- `role.getPermissions()`
- `user.getDeptId()`
- `user.getRoles()`
- `currentUser.getDept()`

预期：AOP 层不再被 `common` 业务实体绑定。

- [ ] **步骤 4：更新 `SysLoginController` 与 `SysProfileController` 读取登录态的代码**

当前两个 Controller 仍把 `LoginUser.getUser()` 视为 `common` 版 `SysUser`。

同步替换 import 与字段访问，确保：

- `getInfo`
- `profile`
- `updateProfile`
- `updatePwd`
- `avatar`

都能直接基于统一模型工作。

- [ ] **步骤 5：运行登录态与权限链路回归**

手工验证：

- `/login`
- `getInfo`
- `getRouters`
- 修改个人信息
- 修改密码
- 带数据权限的列表查询

运行：

```bash
mvn clean package -DskipTests
```

- [ ] **步骤 6：Commit**

```bash
git add manzhushaka-common/src/main/java/com/manzhushaka/common/core/domain/model/LoginUser.java manzhushaka-common/src/main/java/com/manzhushaka/common/utils/SecurityUtils.java manzhushaka-framework/src/main/java/com/manzhushaka/framework/aspectj manzhushaka-admin/src/main/java/com/manzhushaka/web/controller/system
git commit -m "refactor: unify security user model across common and framework"
```

### 任务 4：把 `TreeSelect`、`DictUtils` 等公共辅助对象迁移到统一模型

**文件：**

- 修改：`manzhushaka-common/src/main/java/com/manzhushaka/common/core/domain/TreeSelect.java`
- 修改：`manzhushaka-common/src/main/java/com/manzhushaka/common/utils/DictUtils.java`
- 修改：`manzhushaka-system/src/main/java/com/manzhushaka/system/service/ISysDeptService.java`
- 修改：`manzhushaka-system/src/main/java/com/manzhushaka/system/service/ISysMenuService.java`
- 修改：`manzhushaka-system/src/main/java/com/manzhushaka/system/service/impl/SysDeptServiceImpl.java`
- 修改：`manzhushaka-system/src/main/java/com/manzhushaka/system/service/impl/SysMenuServiceImpl.java`
- 修改：`manzhushaka-system/src/main/java/com/manzhushaka/system/service/impl/SysDictTypeServiceImpl.java`
- 修改：`manzhushaka-system/src/main/java/com/manzhushaka/system/service/impl/SysDictDataServiceImpl.java`

- [ ] **步骤 1：确认保留哪一个 `TreeSelect`**

当前仓库同时存在：

- [common/core/domain/TreeSelect.java](/Users/manzhushaka/CodexProject/manzhushaka-scaff/manzhushaka-common/src/main/java/com/manzhushaka/common/core/domain/TreeSelect.java)
- `system/infrastructure/persistence/TreeSelect.java`

第二阶段要做出明确决策：

- 要么保留 `common` 版并改为依赖统一模型
- 要么彻底迁到 `system` 并修改所有返回类型

推荐：保留 `common` 版 `TreeSelect` 作为通用树返回体，但让它依赖统一模型。

- [ ] **步骤 2：改造 `TreeSelect` 的构造函数参数类型**

把：

```java
public TreeSelect(SysDept dept)
public TreeSelect(SysMenu menu)
```

中的 `SysDept/SysMenu` 改为统一模型类型。

同步搜索：

```bash
rg -n "new TreeSelect|buildDeptTreeSelect|buildMenuTreeSelect" manzhushaka-system/src/main/java manzhushaka-admin/src/main/java
```

确认调用链可编译。

- [ ] **步骤 3：改造 `DictUtils`**

当前 [DictUtils.java](/Users/manzhushaka/CodexProject/manzhushaka-scaff/manzhushaka-common/src/main/java/com/manzhushaka/common/utils/DictUtils.java) 仍使用 `common` 版 `SysDictData`。

将其统一为 `system.infrastructure.persistence.entity.SysDictData`，并同步调整：

- `getDictCache`
- `setDictCache`
- `getDictLabel`
- `getDictValue`

预期：字典工具链不再依赖 `common` 版字典实体。

- [ ] **步骤 4：回归字典与树结构接口**

手工验证：

- 菜单树
- 部门树
- 字典类型列表
- 字典数据列表
- 字典缓存刷新

运行：

```bash
mvn -pl manzhushaka-system -am test
```

- [ ] **步骤 5：Commit**

```bash
git add manzhushaka-common/src/main/java/com/manzhushaka/common/core/domain/TreeSelect.java manzhushaka-common/src/main/java/com/manzhushaka/common/utils/DictUtils.java manzhushaka-system/src/main/java/com/manzhushaka/system/service manzhushaka-system/src/main/java/com/manzhushaka/system/infrastructure
git commit -m "refactor: align tree and dict utilities with unified system model"
```

### 任务 5：压缩 `common` 中遗留业务实体的生产代码引用面

**文件：**

- 修改：`manzhushaka-common/src/main/java/com/manzhushaka/common/core/domain/model/LoginBody.java`
- 修改：`manzhushaka-common/src/main/java/com/manzhushaka/common/core/domain/model/RegisterBody.java`
- 修改：所有仍引用 `common.core.domain.entity` 生产代码的文件

- [ ] **步骤 1：清零 `LoginBody`、`RegisterBody` 的生产代码引用**

运行：

```bash
rg -n "LoginBody|RegisterBody" manzhushaka-admin/src/main/java manzhushaka-framework/src/main/java manzhushaka-system/src/main/java
```

预期：无任何生产代码命中。

- [ ] **步骤 2：清零剩余 `common` 业务实体在生产代码中的直接引用**

运行：

```bash
rg -n "common\\.core\\.domain\\.entity\\.(SysUser|SysRole|SysMenu|SysDept|SysDictType|SysDictData)" manzhushaka-admin/src/main/java manzhushaka-framework/src/main/java manzhushaka-system/src/main/java
```

目标不是立刻删除 `common` 中这些类，而是把生产代码引用收敛到 `0`。

- [ ] **步骤 3：补充一份迁移检查说明到 README 或开发文档**

明确写入：

```markdown
- `common.core.domain.entity.*` 为遗留兼容目录，不得新增引用
- 所有新代码统一使用 `system.infrastructure.persistence.entity.*`
- `admin` 不允许把实体类型直接作为 HTTP 请求体
```

- [ ] **步骤 4：执行全量回归检查**

运行：

```bash
rg -n "@RequestBody\\s+Sys(Menu|Dept|DictType|DictData|User)" manzhushaka-admin/src/main/java
```

```bash
rg -n "SysUserConverter|toCommon\\(|toSystem\\(" manzhushaka-admin/src/main/java manzhushaka-framework/src/main/java manzhushaka-system/src/main/java
```

```bash
rg -n "common\\.core\\.domain\\.entity\\.(SysUser|SysRole|SysMenu|SysDept|SysDictType|SysDictData)" manzhushaka-admin/src/main/java manzhushaka-framework/src/main/java manzhushaka-system/src/main/java
```

预期：

- 无 Controller 继续直接接收系统持久化实体
- 无桥接转换器残留
- 无生产代码继续依赖 `common` 版系统业务实体

- [ ] **步骤 5：Commit**

```bash
git add README.md manzhushaka-admin/src/main/java manzhushaka-framework/src/main/java manzhushaka-system/src/main/java manzhushaka-common/src/main/java
git commit -m "chore: eliminate remaining production references to legacy common entities"
```

## 7. 建议执行顺序

建议按以下顺序推进：

1. 任务 1：补齐 `menu/dept/dict/profile` 的 HTTP 边界
2. 任务 2：去掉 `framework` 中的双模型桥接
3. 任务 3：统一 `LoginUser/SecurityUtils/DataScope` 链路
4. 任务 4：改造 `TreeSelect/DictUtils`
5. 任务 5：执行遗留引用清零与全量回归

原因：

- 如果不先补 Controller 边界，后续仍然会继续往 `admin` 里写实体直传代码。
- 如果不尽早删除 `SysUserConverter`，安全链路和个人信息链路会一直两套模型并存。
- `TreeSelect/DictUtils` 属于公共辅助类型，适合在核心用户模型统一后再处理。

## 8. 验证与验收标准

第二阶段完成后，必须满足以下验收条件：

1. `SysMenuController`、`SysDeptController`、`SysDictTypeController`、`SysDictDataController`、`SysProfileController` 不再直接以系统实体作为 HTTP 请求体。
2. `framework` 中不存在 `SysUserConverter`。
3. `UserDetailsServiceImpl` 不再创建 `common` 版 `SysUser`。
4. `LoginUser` 与 `SecurityUtils` 建立在统一用户模型之上。
5. `TreeSelect` 与 `DictUtils` 不再依赖 `common` 版业务实体。
6. 下列命令应全部无结果：

```bash
rg -n "SysUserConverter|toCommon\\(|toSystem\\(" manzhushaka-admin/src/main/java manzhushaka-framework/src/main/java manzhushaka-system/src/main/java
```

```bash
rg -n "@RequestBody\\s+Sys(Menu|Dept|DictType|DictData|User)" manzhushaka-admin/src/main/java
```

```bash
rg -n "common\\.core\\.domain\\.entity\\.(SysUser|SysRole|SysMenu|SysDept|SysDictType|SysDictData)" manzhushaka-admin/src/main/java manzhushaka-framework/src/main/java manzhushaka-system/src/main/java
```

7. 手工回归通过以下功能：

- 登录
- 获取用户信息
- 获取路由
- 个人信息维护
- 个人密码修改
- 菜单树
- 部门树
- 字典类型/数据管理
- 数据权限相关列表

## 9. 风险与注意事项

1. `LoginUser` 一旦改为统一模型，会联动 `TokenService`、`JwtAuthenticationTokenFilter`、`LogoutSuccessHandlerImpl`、`SysUserOnlineService` 等认证链路，必须一次性回归登录态。
2. `TreeSelect` 与 `DictUtils` 属于“看似小、波及面广”的公共对象，改造时要先用 `rg` 扫出所有调用点，再分批提交。
3. `SystemUserAppService`、`SystemRoleAppService` 当前仍然返回实体列表，第二阶段可以暂时接受，但不要再在 `admin` 层暴露这些实体作为请求体。
4. 第二阶段结束后，`common` 中那些业务实体类可能还暂时存在，但它们应该已经没有生产代码引用，第三阶段才适合真正删除。

## 10. 非目标

本计划明确不做以下事情：

- 不在本阶段新增 `system-api` 独立模块
- 不重写 `user/role` 已经完成的第一阶段路径
- 不在本阶段全面把 `system.service` 逻辑全部替换成纯 `repository + domain service`
- 不在本阶段引入新业务域 `member/order/content`
- 不在本阶段统一替换 `AjaxResult` / `TableDataInfo` 返回风格

## 11. 交付物

第二阶段完成后，应至少产出：

1. 第二批系统管理 DTO/VO/Converter
2. `SystemMenuAppService`、`SystemDeptAppService`、`SystemDictAppService`、`SystemProfileAppService`
3. 被移除的 `SysUserConverter`
4. 统一用户模型驱动的 `LoginUser/SecurityUtils`
5. 被清零的 `common` 版系统业务实体生产引用

计划已完成并保存到 `docs/superpowers/plans/2026-06-28-domain-modularization-phase-2-plan.md`。两种执行方式：

**1. 子代理驱动（推荐）** - 每个任务调度一个新的子代理，任务间进行审查，快速迭代

**2. 内联执行** - 在当前会话中使用 `executing-plans` 执行任务，批量执行并设有检查点

选哪种方式？
