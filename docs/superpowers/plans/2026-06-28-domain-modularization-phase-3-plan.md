# manzhushaka-ry 业务域模块化迁移第三阶段实现计划

> **面向 AI 代理的工作者：** 必需子技能：使用 `superpowers:subagent-driven-development`（推荐）或 `superpowers:executing-plans` 逐任务实现此计划。步骤使用复选框（`- [ ]`）语法来跟踪进度。

**目标：** 在第二阶段完成 HTTP 边界收口之后，彻底删除 `common` 中的系统业务遗留，固化 `framework / system / admin` 的最终边界，并补上可持续防回退的架构守护。

**架构：** `framework` 负责安全上下文与登录态模型，`system` 通过稳定的 `application service / query result` 对外暴露认证、权限、树结构、字典等系统能力，`admin` 继续承接全部 HTTP DTO/VO，`common` 回归“无业务归属的基础库”。第三阶段不再新增过渡桥接层，而是以“替换 + 删除”完成最终收口。

**技术栈：** Java 17、Spring Boot 4、Spring Security、MyBatis、Redis、Maven 多模块工程、JUnit 5、ArchUnit

---

## 1. 计划定位

本计划是以下两份文档的续篇：

- [2026-06-28-domain-modularization-migration-plan.md](/Users/manzhushaka/CodexProject/manzhushaka-ry-scaff/docs/superpowers/plans/2026-06-28-domain-modularization-migration-plan.md)
- [2026-06-28-domain-modularization-phase-2-plan.md](/Users/manzhushaka/CodexProject/manzhushaka-ry-scaff/docs/superpowers/plans/2026-06-28-domain-modularization-phase-2-plan.md)

适用前提：

1. 第二阶段中的 `menu / dept / dict / profile` 已完成 HTTP 边界收口。
2. `SysUserConverter` 及 `common <-> system` 双模型桥接已经停止新增使用点。
3. `admin` 已以 `DTO / VO / Converter + AppService` 作为默认新入口模式。

如果第二阶段仍未完成，本计划中的“删除型步骤”暂时不能直接执行，应先回到第二阶段把桥接和遗留入口补齐。

## 2. 当前残留问题与第三阶段范围

结合当前仓库现状，第三阶段要解决的不是“再迁几个 Controller”，而是下面 4 类真正决定架构是否能稳定下来的问题：

1. 安全会话模型仍然挂在 `common`：
   - [LoginUser.java](/Users/manzhushaka/CodexProject/manzhushaka-ry-scaff/manzhushaka-ry-common/src/main/java/com/manzhushaka/common/core/domain/model/LoginUser.java)
   - [SecurityUtils.java](/Users/manzhushaka/CodexProject/manzhushaka-ry-scaff/manzhushaka-ry-common/src/main/java/com/manzhushaka/common/utils/SecurityUtils.java)
2. `common` 中仍有系统专属的树结构与字典工具：
   - [TreeSelect.java](/Users/manzhushaka/CodexProject/manzhushaka-ry-scaff/manzhushaka-ry-common/src/main/java/com/manzhushaka/common/core/domain/TreeSelect.java)
   - [DictUtils.java](/Users/manzhushaka/CodexProject/manzhushaka-ry-scaff/manzhushaka-ry-common/src/main/java/com/manzhushaka/common/utils/DictUtils.java)
3. `common` 仍保留系统业务实体与旧 HTTP 模型：
   - `SysUser`、`SysRole`、`SysMenu`、`SysDept`、`SysDictType`、`SysDictData`
   - `LoginBody`、`RegisterBody`
4. 架构虽然开始分层，但还没有“防回退护栏”：
   - `framework` 仍可能重新依赖系统实体或旧 `service`
   - `admin` 未来仍可能绕过应用层直连持久化对象
   - `common` 未来仍可能再次被塞入业务对象

因此，第三阶段的范围明确为 4 大块：

1. 把登录态模型与安全上下文从 `common` 收回到 `framework`。
2. 把 `TreeSelect / DictUtils` 一类系统专属能力收回到 `system + admin` 的正确边界。
3. 删除 `common` 中剩余的系统业务实体与旧请求模型。
4. 为模块边界增加自动化守护，避免之后再回退。

## 3. 第三阶段完成后的目标状态

完成本计划后，仓库应达到以下状态：

1. `common` 中不再存在：
   - `SysUser`、`SysRole`、`SysMenu`、`SysDept`、`SysDictType`、`SysDictData`
   - `LoginBody`、`RegisterBody`
   - `LoginUser`
   - `SecurityUtils`
   - `TreeSelect`
   - `DictUtils`
2. `framework` 统一拥有登录态模型与安全上下文帮助类，不再依赖 `common` 版系统用户对象。
3. `system` 只通过稳定的 `application service / query result` 对外提供认证、权限、树结构、字典能力。
4. `admin` 不再直接返回或接收任何 `system.infrastructure.persistence.*` 类型作为 HTTP 协议对象。
5. 生产代码中不再存在对 `common.core.domain.entity.*` 的引用。
6. 增加静态或测试级边界守护，确保后续代码提交无法轻易把旧依赖加回来。

## 4. 关键设计决策

第三阶段需要先统一几个最终归属，否则会一边删一边乱：

### 4.1 登录态模型归 `framework`

`LoginUser` 不是通用基础模型，而是 Spring Security 登录态对象，应该由 `framework` 管理。最终建议：

- 新建 `framework.security.model.LoginPrincipal`
- 新建 `framework.security.context.SecurityContextHelper`
- 原 `LoginUser` 删除
- 原 `SecurityUtils` 拆分：
  - 登录上下文相关逻辑迁到 `framework`
  - 纯密码工具沉淀为 `common` 中无业务依赖的 `PasswordUtils`

### 4.2 树结构不是 `common` 对象

`TreeSelect` 本质上是“面向前端树控件的输出模型”，不应属于 `common`。最终建议：

- `system` 只返回应用层树节点结果，例如 `TreeNodeResult`
- `admin` 把 `TreeNodeResult` 转换为 HTTP 响应 `TreeSelectVo`
- 删除 `common` 与 `system.infrastructure.persistence` 中两个版本的 `TreeSelect`

### 4.3 字典能力用 SPI 反转依赖

`ExcelUtil` 在 `common` 中确实需要“字典标签解析能力”，但不该直接依赖 `SysDictData`。最终建议：

- `common` 新增字典解析 SPI，例如 `DictResolver`
- `ExcelUtil` 依赖 `DictResolver`
- `system` 提供 `SystemDictResolver` 实现
- 删除 `common.utils.DictUtils`

### 4.4 `framework` 访问 `system` 只能走稳定契约

第三阶段之后，`framework` 不应再直接围绕旧 `ISys*Service + persistence entity` 工作。最终建议：

- `system` 提供稳定的 `SystemSecurityQueryService`
- 认证、权限、在线用户等跨模块读取能力返回应用层结果对象
- `framework` 仅消费这些结果对象，不再拼装或转换持久化实体

## 5. 关键文件清单

### 5.1 需要重点处理的安全上下文文件

- [TokenService.java](/Users/manzhushaka/CodexProject/manzhushaka-ry-scaff/manzhushaka-ry-framework/src/main/java/com/manzhushaka/framework/web/service/TokenService.java)
- [UserDetailsServiceImpl.java](/Users/manzhushaka/CodexProject/manzhushaka-ry-scaff/manzhushaka-ry-framework/src/main/java/com/manzhushaka/framework/web/service/UserDetailsServiceImpl.java)
- [JwtAuthenticationTokenFilter.java](/Users/manzhushaka/CodexProject/manzhushaka-ry-scaff/manzhushaka-ry-framework/src/main/java/com/manzhushaka/framework/security/filter/JwtAuthenticationTokenFilter.java)
- [LogoutSuccessHandlerImpl.java](/Users/manzhushaka/CodexProject/manzhushaka-ry-scaff/manzhushaka-ry-framework/src/main/java/com/manzhushaka/framework/security/handle/LogoutSuccessHandlerImpl.java)
- [DataScopeAspect.java](/Users/manzhushaka/CodexProject/manzhushaka-ry-scaff/manzhushaka-ry-framework/src/main/java/com/manzhushaka/framework/aspectj/DataScopeAspect.java)
- [LogAspect.java](/Users/manzhushaka/CodexProject/manzhushaka-ry-scaff/manzhushaka-ry-framework/src/main/java/com/manzhushaka/framework/aspectj/LogAspect.java)
- [BaseController.java](/Users/manzhushaka/CodexProject/manzhushaka-ry-scaff/manzhushaka-ry-common/src/main/java/com/manzhushaka/common/core/controller/BaseController.java)

### 5.2 需要重点处理的系统专属共享对象

- [TreeSelect.java](/Users/manzhushaka/CodexProject/manzhushaka-ry-scaff/manzhushaka-ry-common/src/main/java/com/manzhushaka/common/core/domain/TreeSelect.java)
- [TreeSelect.java](/Users/manzhushaka/CodexProject/manzhushaka-ry-scaff/manzhushaka-ry-system/src/main/java/com/manzhushaka/system/infrastructure/persistence/TreeSelect.java)
- [DictUtils.java](/Users/manzhushaka/CodexProject/manzhushaka-ry-scaff/manzhushaka-ry-common/src/main/java/com/manzhushaka/common/utils/DictUtils.java)
- [DictUtils.java](/Users/manzhushaka/CodexProject/manzhushaka-ry-scaff/manzhushaka-ry-system/src/main/java/com/manzhushaka/system/infrastructure/persistence/DictUtils.java)
- [ExcelUtil.java](/Users/manzhushaka/CodexProject/manzhushaka-ry-scaff/manzhushaka-ry-common/src/main/java/com/manzhushaka/common/utils/poi/ExcelUtil.java)

### 5.3 需要最终删除的 `common` 遗留文件

- [SysUser.java](/Users/manzhushaka/CodexProject/manzhushaka-ry-scaff/manzhushaka-ry-common/src/main/java/com/manzhushaka/common/core/domain/entity/SysUser.java)
- [SysRole.java](/Users/manzhushaka/CodexProject/manzhushaka-ry-scaff/manzhushaka-ry-common/src/main/java/com/manzhushaka/common/core/domain/entity/SysRole.java)
- [SysMenu.java](/Users/manzhushaka/CodexProject/manzhushaka-ry-scaff/manzhushaka-ry-common/src/main/java/com/manzhushaka/common/core/domain/entity/SysMenu.java)
- [SysDept.java](/Users/manzhushaka/CodexProject/manzhushaka-ry-scaff/manzhushaka-ry-common/src/main/java/com/manzhushaka/common/core/domain/entity/SysDept.java)
- [SysDictType.java](/Users/manzhushaka/CodexProject/manzhushaka-ry-scaff/manzhushaka-ry-common/src/main/java/com/manzhushaka/common/core/domain/entity/SysDictType.java)
- [SysDictData.java](/Users/manzhushaka/CodexProject/manzhushaka-ry-scaff/manzhushaka-ry-common/src/main/java/com/manzhushaka/common/core/domain/entity/SysDictData.java)
- [LoginBody.java](/Users/manzhushaka/CodexProject/manzhushaka-ry-scaff/manzhushaka-ry-common/src/main/java/com/manzhushaka/common/core/domain/model/LoginBody.java)
- [RegisterBody.java](/Users/manzhushaka/CodexProject/manzhushaka-ry-scaff/manzhushaka-ry-common/src/main/java/com/manzhushaka/common/core/domain/model/RegisterBody.java)

## 6. 分任务实施蓝图

### 任务 1：重建登录态模型与安全上下文归属

**文件：**

- 创建：`manzhushaka-ry-common/src/main/java/com/manzhushaka/common/utils/security/PasswordUtils.java`
- 创建：`manzhushaka-ry-framework/src/main/java/com/manzhushaka/framework/security/model/LoginPrincipal.java`
- 创建：`manzhushaka-ry-framework/src/main/java/com/manzhushaka/framework/security/context/SecurityContextHelper.java`
- 修改：`manzhushaka-ry-framework/src/main/java/com/manzhushaka/framework/web/service/TokenService.java`
- 修改：`manzhushaka-ry-framework/src/main/java/com/manzhushaka/framework/web/service/UserDetailsServiceImpl.java`
- 修改：`manzhushaka-ry-framework/src/main/java/com/manzhushaka/framework/security/filter/JwtAuthenticationTokenFilter.java`
- 修改：`manzhushaka-ry-framework/src/main/java/com/manzhushaka/framework/security/handle/LogoutSuccessHandlerImpl.java`
- 修改：`manzhushaka-ry-framework/src/main/java/com/manzhushaka/framework/aspectj/DataScopeAspect.java`
- 修改：`manzhushaka-ry-framework/src/main/java/com/manzhushaka/framework/aspectj/LogAspect.java`
- 修改：`manzhushaka-ry-common/src/main/java/com/manzhushaka/common/core/controller/BaseController.java`
- 删除：`manzhushaka-ry-common/src/main/java/com/manzhushaka/common/core/domain/model/LoginUser.java`
- 删除：`manzhushaka-ry-common/src/main/java/com/manzhushaka/common/utils/SecurityUtils.java`
- 测试：`manzhushaka-ry-framework/src/test/java/com/manzhushaka/framework/security/context/SecurityContextHelperTest.java`
- 测试：`manzhushaka-ry-framework/src/test/java/com/manzhushaka/framework/security/model/LoginPrincipalTest.java`

- [ ] **步骤 1：先写失败测试，锁定新登录态模型行为**

新增测试至少覆盖：

```java
@Test
void should_read_user_id_from_security_context() {
    LoginPrincipal principal = LoginPrincipal.builder()
            .userId(100L)
            .deptId(200L)
            .username("admin")
            .password("{bcrypt}xxx")
            .permissions(Set.of("system:user:list"))
            .roleKeys(Set.of("admin"))
            .build();
    TestingAuthenticationToken authentication =
            new TestingAuthenticationToken(principal, null, principal.getAuthorities());
    SecurityContextHolder.getContext().setAuthentication(authentication);

    assertThat(SecurityContextHelper.getUserId()).isEqualTo(100L);
    assertThat(SecurityContextHelper.hasRole("admin")).isTrue();
}
```

```java
@Test
void should_match_password_with_password_utils() {
    String encoded = PasswordUtils.encrypt("123456");
    assertThat(PasswordUtils.matches("123456", encoded)).isTrue();
}
```

- [ ] **步骤 2：实现 `LoginPrincipal`、`SecurityContextHelper`、`PasswordUtils`**

核心结构建议如下：

```java
public final class LoginPrincipal implements UserDetails, Serializable {
    private Long userId;
    private Long deptId;
    private String username;
    private String password;
    private Set<String> permissions;
    private Set<String> roleKeys;
    private String token;
    private Long loginTime;
    private Long expireTime;
    private String ipaddr;
    private String loginLocation;
    private String browser;
    private String os;
}
```

```java
public final class SecurityContextHelper {
    public static LoginPrincipal getPrincipal() { ... }
    public static Long getUserId() { ... }
    public static Long getDeptId() { ... }
    public static String getUsername() { ... }
    public static boolean hasPermi(String permission) { ... }
    public static boolean hasRole(String roleKey) { ... }
}
```

```java
public final class PasswordUtils {
    public static String encrypt(String rawPassword) { ... }
    public static boolean matches(String rawPassword, String encodedPassword) { ... }
}
```

- [ ] **步骤 3：批量替换调用点，不再引用 `LoginUser / SecurityUtils`**

重点替换方向：

- `BaseController#getLoginUser()` 改为返回 `LoginPrincipal`
- `TokenService` 缓存对象改为 `LoginPrincipal`
- `DataScopeAspect`、`LogAspect` 直接从 `SecurityContextHelper` 读取用户信息
- `SysPasswordService`、`SysRegisterService`、`SysProfileController` 改用 `PasswordUtils`

典型替换示例：

```java
LoginPrincipal principal = SecurityContextHelper.getPrincipal();
Long userId = principal.getUserId();
```

```java
if (!PasswordUtils.matches(oldPassword, encodedPassword)) {
    throw new ServiceException("旧密码错误");
}
```

- [ ] **步骤 4：运行测试并确认旧类型已经不再被使用**

运行：

```bash
mvn -pl manzhushaka-ry-framework,manzhushaka-ry-common -am test
rg -n "common\\.core\\.domain\\.model\\.LoginUser|common\\.utils\\.SecurityUtils" manzhushaka-ry-admin/src/main/java manzhushaka-ry-framework/src/main/java manzhushaka-ry-system/src/main/java manzhushaka-ry-common/src/main/java
```

预期：

- 单元测试通过
- `rg` 无生产代码命中

- [ ] **步骤 5：Commit**

```bash
git add manzhushaka-ry-common/src/main/java/com/manzhushaka/common/utils/security manzhushaka-ry-framework/src/main/java/com/manzhushaka/framework/security manzhushaka-ry-framework/src/main/java/com/manzhushaka/framework/web/service manzhushaka-ry-framework/src/main/java/com/manzhushaka/framework/aspectj manzhushaka-ry-common/src/main/java/com/manzhushaka/common/core/controller manzhushaka-ry-framework/src/test/java
git commit -m "refactor: move security context model out of common"
```

### 任务 2：为 `framework` 与 `admin` 提供稳定的系统认证查询契约

**文件：**

- 创建：`manzhushaka-ry-system/src/main/java/com/manzhushaka/system/application/result/auth/AuthUserProfileResult.java`
- 创建：`manzhushaka-ry-system/src/main/java/com/manzhushaka/system/application/service/SystemSecurityQueryService.java`
- 创建：`manzhushaka-ry-system/src/main/java/com/manzhushaka/system/application/service/impl/SystemSecurityQueryServiceImpl.java`
- 修改：`manzhushaka-ry-framework/src/main/java/com/manzhushaka/framework/web/service/UserDetailsServiceImpl.java`
- 修改：`manzhushaka-ry-framework/src/main/java/com/manzhushaka/framework/web/service/SysPermissionService.java`
- 修改：`manzhushaka-ry-framework/src/main/java/com/manzhushaka/framework/web/service/TokenService.java`
- 修改：`manzhushaka-ry-admin/src/main/java/com/manzhushaka/web/controller/system/SysLoginController.java`
- 修改：`manzhushaka-ry-system/src/main/java/com/manzhushaka/system/service/ISysUserOnlineService.java`
- 修改：`manzhushaka-ry-system/src/main/java/com/manzhushaka/system/service/impl/SysUserOnlineServiceImpl.java`
- 测试：`manzhushaka-ry-system/src/test/java/com/manzhushaka/system/application/service/SystemSecurityQueryServiceTest.java`

- [ ] **步骤 1：先写失败测试，锁定系统认证查询契约**

新增测试至少覆盖：

```java
@Test
void should_load_auth_profile_by_username() {
    AuthUserProfileResult result = service.loadAuthProfileByUsername("admin");
    assertThat(result.userId()).isEqualTo(1L);
    assertThat(result.username()).isEqualTo("admin");
    assertThat(result.permissions()).isNotEmpty();
}
```

- [ ] **步骤 2：实现统一的系统认证结果对象与查询服务**

建议结果对象如下：

```java
public record AuthUserProfileResult(
        Long userId,
        Long deptId,
        String username,
        String nickName,
        String password,
        String status,
        boolean admin,
        Set<String> roleKeys,
        Set<String> permissions) {
}
```

建议服务接口如下：

```java
public interface SystemSecurityQueryService {
    AuthUserProfileResult loadAuthProfileByUsername(String username);
    AuthUserProfileResult loadAuthProfileByUserId(Long userId);
    Set<String> loadRoleKeys(Long userId);
    Set<String> loadPermissions(Long userId);
}
```

- [ ] **步骤 3：让 `framework` 和 `admin` 只依赖认证查询契约**

`UserDetailsServiceImpl` 改造方向：

```java
AuthUserProfileResult profile = systemSecurityQueryService.loadAuthProfileByUsername(username);
LoginPrincipal principal = LoginPrincipalAssembler.from(profile);
return principal;
```

`SysLoginController#getInfo()` 改造方向：

```java
LoginPrincipal principal = SecurityContextHelper.getPrincipal();
AuthUserProfileResult profile = systemSecurityQueryService.loadAuthProfileByUserId(principal.getUserId());
ajax.put("user", profile);
ajax.put("roles", profile.roleKeys());
ajax.put("permissions", profile.permissions());
```

预期：`framework` 和 `admin` 不再为了获取角色、权限、用户信息去碰持久化实体。

- [ ] **步骤 4：运行测试并验证旧桥接链路彻底消失**

运行：

```bash
mvn -pl manzhushaka-ry-system,manzhushaka-ry-framework,manzhushaka-ry-admin -am test
rg -n "SysUserConverter|toCommon\\(|toSystem\\(" manzhushaka-ry-admin/src/main/java manzhushaka-ry-framework/src/main/java manzhushaka-ry-system/src/main/java
```

预期：

- `SystemSecurityQueryService` 相关测试通过
- `SysUserConverter` 及其调用点为 0

- [ ] **步骤 5：Commit**

```bash
git add manzhushaka-ry-system/src/main/java/com/manzhushaka/system/application manzhushaka-ry-framework/src/main/java/com/manzhushaka/framework/web/service manzhushaka-ry-admin/src/main/java/com/manzhushaka/web/controller/system manzhushaka-ry-system/src/test/java
git commit -m "refactor: expose stable system security query contract"
```

### 任务 3：收口树结构与字典能力，删除系统专属 `common` 工具

**文件：**

- 创建：`manzhushaka-ry-common/src/main/java/com/manzhushaka/common/spi/DictResolver.java`
- 创建：`manzhushaka-ry-system/src/main/java/com/manzhushaka/system/infrastructure/dict/SystemDictResolver.java`
- 创建：`manzhushaka-ry-system/src/main/java/com/manzhushaka/system/infrastructure/dict/SystemDictCacheSupport.java`
- 创建：`manzhushaka-ry-system/src/main/java/com/manzhushaka/system/application/result/shared/TreeNodeResult.java`
- 创建：`manzhushaka-ry-admin/src/main/java/com/manzhushaka/web/vo/system/shared/TreeSelectVo.java`
- 创建：`manzhushaka-ry-admin/src/main/java/com/manzhushaka/web/converter/system/shared/TreeSelectAdminConverter.java`
- 修改：`manzhushaka-ry-common/src/main/java/com/manzhushaka/common/utils/poi/ExcelUtil.java`
- 修改：`manzhushaka-ry-system/src/main/java/com/manzhushaka/system/service/impl/SysDictTypeServiceImpl.java`
- 修改：`manzhushaka-ry-system/src/main/java/com/manzhushaka/system/service/impl/SysDictDataServiceImpl.java`
- 修改：`manzhushaka-ry-system/src/main/java/com/manzhushaka/system/service/ISysMenuService.java`
- 修改：`manzhushaka-ry-system/src/main/java/com/manzhushaka/system/service/ISysDeptService.java`
- 修改：`manzhushaka-ry-admin/src/main/java/com/manzhushaka/web/controller/system/SysMenuController.java`
- 修改：`manzhushaka-ry-admin/src/main/java/com/manzhushaka/web/controller/system/SysDeptController.java`
- 删除：`manzhushaka-ry-common/src/main/java/com/manzhushaka/common/core/domain/TreeSelect.java`
- 删除：`manzhushaka-ry-common/src/main/java/com/manzhushaka/common/utils/DictUtils.java`
- 删除：`manzhushaka-ry-system/src/main/java/com/manzhushaka/system/infrastructure/persistence/TreeSelect.java`
- 删除：`manzhushaka-ry-system/src/main/java/com/manzhushaka/system/infrastructure/persistence/DictUtils.java`
- 测试：`manzhushaka-ry-common/src/test/java/com/manzhushaka/common/utils/poi/ExcelUtilDictResolverTest.java`
- 测试：`manzhushaka-ry-admin/src/test/java/com/manzhushaka/web/converter/system/shared/TreeSelectAdminConverterTest.java`

- [ ] **步骤 1：先写失败测试，锁定字典解析与树节点转换**

新增测试至少覆盖：

```java
@Test
void should_resolve_dict_label_via_spi() {
    DictResolver resolver = mock(DictResolver.class);
    when(resolver.getDictLabel("sys_yes_no", "Y", ",")).thenReturn("是");
    assertThat(resolver.getDictLabel("sys_yes_no", "Y", ",")).isEqualTo("是");
}
```

```java
@Test
void should_convert_tree_node_result_to_tree_select_vo() {
    TreeNodeResult child = new TreeNodeResult(2L, "子节点", false, List.of());
    TreeNodeResult root = new TreeNodeResult(1L, "根节点", false, List.of(child));

    TreeSelectVo vo = TreeSelectAdminConverter.toVo(root);

    assertThat(vo.getId()).isEqualTo(1L);
    assertThat(vo.getChildren()).hasSize(1);
}
```

- [ ] **步骤 2：实现 `DictResolver` SPI 与系统实现**

建议 SPI：

```java
public interface DictResolver {
    String getDictLabel(String dictType, String dictValue, String separator);
    String getDictValue(String dictType, String dictLabel, String separator);
    String getDictLabels(String dictType);
    String getDictValues(String dictType);
}
```

`ExcelUtil` 改造方向：

```java
DictResolver dictResolver = SpringUtils.getBean(DictResolver.class);
return dictResolver.getDictLabel(dictType, dictValue, separator);
```

`system` 中的字典缓存建议改为：

```java
public final class SystemDictCacheSupport {
    public static void set(String key, List<SysDictData> dictDatas) { ... }
    public static List<SysDictData> get(String key) { ... }
    public static void clear() { ... }
}
```

- [ ] **步骤 3：实现树节点结果对象与 `admin` 响应 VO**

建议结构：

```java
public record TreeNodeResult(
        Long id,
        String label,
        boolean disabled,
        List<TreeNodeResult> children) {
}
```

```java
public class TreeSelectVo {
    private Long id;
    private String label;
    private boolean disabled;
    private List<TreeSelectVo> children;
}
```

预期：

- `system` 输出应用层结果
- `admin` 输出 HTTP VO
- 两个 `TreeSelect` 旧类都能删除

- [ ] **步骤 4：运行测试并确认旧工具已无引用**

运行：

```bash
mvn -pl manzhushaka-ry-common,manzhushaka-ry-system,manzhushaka-ry-admin -am test
rg -n "common\\.core\\.domain\\.TreeSelect|common\\.utils\\.DictUtils|system\\.infrastructure\\.persistence\\.TreeSelect|system\\.infrastructure\\.persistence\\.DictUtils" manzhushaka-ry-admin/src/main/java manzhushaka-ry-framework/src/main/java manzhushaka-ry-system/src/main/java manzhushaka-ry-common/src/main/java
```

预期：上述 4 类旧对象在生产代码中均不再被引用。

- [ ] **步骤 5：Commit**

```bash
git add manzhushaka-ry-common/src/main/java/com/manzhushaka/common/spi manzhushaka-ry-common/src/main/java/com/manzhushaka/common/utils/poi manzhushaka-ry-system/src/main/java/com/manzhushaka/system/infrastructure/dict manzhushaka-ry-system/src/main/java/com/manzhushaka/system/application/result manzhushaka-ry-admin/src/main/java/com/manzhushaka/web/vo/system/shared manzhushaka-ry-admin/src/main/java/com/manzhushaka/web/converter/system/shared manzhushaka-ry-admin/src/main/java/com/manzhushaka/web/controller/system manzhushaka-ry-common/src/test/java manzhushaka-ry-admin/src/test/java
git commit -m "refactor: remove common tree and dict coupling"
```

### 任务 4：删除 `common` 中的系统业务实体与旧请求模型

**文件：**

- 删除：`manzhushaka-ry-common/src/main/java/com/manzhushaka/common/core/domain/entity/SysUser.java`
- 删除：`manzhushaka-ry-common/src/main/java/com/manzhushaka/common/core/domain/entity/SysRole.java`
- 删除：`manzhushaka-ry-common/src/main/java/com/manzhushaka/common/core/domain/entity/SysMenu.java`
- 删除：`manzhushaka-ry-common/src/main/java/com/manzhushaka/common/core/domain/entity/SysDept.java`
- 删除：`manzhushaka-ry-common/src/main/java/com/manzhushaka/common/core/domain/entity/SysDictType.java`
- 删除：`manzhushaka-ry-common/src/main/java/com/manzhushaka/common/core/domain/entity/SysDictData.java`
- 删除：`manzhushaka-ry-common/src/main/java/com/manzhushaka/common/core/domain/model/LoginBody.java`
- 删除：`manzhushaka-ry-common/src/main/java/com/manzhushaka/common/core/domain/model/RegisterBody.java`
- 修改：所有仍有 `common.core.domain.entity.*` 或旧登录模型引用的生产代码
- 测试：`manzhushaka-ry-admin/src/test/java/com/manzhushaka/architecture/LegacyCommonReferenceTest.java`

- [ ] **步骤 1：先增加失败检查，禁止生产代码再引用旧 `common` 业务对象**

新增检查至少覆盖：

```java
@Test
void should_not_reference_legacy_common_entities() {
    JavaClasses classes = new ClassFileImporter().importPackages("com.manzhushaka");
    noClasses()
            .that().resideOutsideOfPackage("..common..")
            .should().dependOnClassesThat()
            .resideInAnyPackage("..common.core.domain.entity..", "..common.core.domain.model..")
            .check(classes);
}
```

- [ ] **步骤 2：清理最后的生产代码引用并删除旧类**

删除前先用命令确认：

```bash
rg -n "common\\.core\\.domain\\.entity\\.(SysUser|SysRole|SysMenu|SysDept|SysDictType|SysDictData)|common\\.core\\.domain\\.model\\.(LoginBody|RegisterBody)" manzhushaka-ry-admin/src/main/java manzhushaka-ry-framework/src/main/java manzhushaka-ry-system/src/main/java manzhushaka-ry-common/src/main/java
```

预期：只有待删除文件自身或测试代码命中。

确认后再删除上述 8 个文件。

- [ ] **步骤 3：跑全仓静态搜索，确保 `common` 已经回归基础库**

运行：

```bash
rg -n "common\\.core\\.domain\\.entity\\.|common\\.core\\.domain\\.model\\.(LoginBody|RegisterBody|LoginUser)" manzhushaka-ry-admin/src/main/java manzhushaka-ry-framework/src/main/java manzhushaka-ry-system/src/main/java manzhushaka-ry-common/src/main/java
```

预期：无生产代码命中。

- [ ] **步骤 4：Commit**

```bash
git add manzhushaka-ry-common/src/main/java/com/manzhushaka/common/core/domain manzhushaka-ry-admin/src/test/java
git commit -m "refactor: delete legacy system objects from common"
```

### 任务 5：收口旧服务入口并增加防回退架构守护

**文件：**

- 修改：`pom.xml`
- 修改：`manzhushaka-ry-admin/pom.xml`
- 修改：`manzhushaka-ry-framework/pom.xml`
- 修改：`manzhushaka-ry-system/pom.xml`
- 创建：`manzhushaka-ry-admin/src/test/java/com/manzhushaka/architecture/AdminBoundaryArchTest.java`
- 创建：`manzhushaka-ry-framework/src/test/java/com/manzhushaka/architecture/FrameworkBoundaryArchTest.java`
- 创建：`manzhushaka-ry-system/src/test/java/com/manzhushaka/architecture/SystemBoundaryArchTest.java`
- 创建：`scripts/architecture/check-module-boundaries.sh`

- [ ] **步骤 1：在测试依赖中加入 ArchUnit**

在相关 POM 中补充：

```xml
<dependency>
    <groupId>com.tngtech.archunit</groupId>
    <artifactId>archunit-junit5</artifactId>
    <scope>test</scope>
</dependency>
```

- [ ] **步骤 2：编写 3 组边界规则**

`AdminBoundaryArchTest` 至少覆盖：

```java
noClasses()
    .that().resideInAPackage("..web.controller..")
    .should().dependOnClassesThat()
    .resideInAnyPackage("..infrastructure.persistence.entity..", "..mapper..");
```

`FrameworkBoundaryArchTest` 至少覆盖：

```java
noClasses()
    .that().resideInAPackage("..framework..")
    .should().dependOnClassesThat()
    .resideInAnyPackage("..common.core.domain.entity..");
```

`SystemBoundaryArchTest` 至少覆盖：

```java
noClasses()
    .that().resideInAPackage("..system.application..")
    .should().dependOnClassesThat()
    .resideInAnyPackage("..web.dto..", "..web.vo..");
```

- [ ] **步骤 3：增加一个脚本级快速检查，便于本地和 CI 复用**

脚本建议内容：

```bash
#!/usr/bin/env bash
set -euo pipefail

rg -n "common\\.core\\.domain\\.entity\\." manzhushaka-ry-admin/src/main/java manzhushaka-ry-framework/src/main/java manzhushaka-ry-system/src/main/java && exit 1 || true
rg -n "infrastructure\\.persistence\\.entity" manzhushaka-ry-admin/src/main/java && exit 1 || true
rg -n "web\\.dto|web\\.vo" manzhushaka-ry-system/src/main/java && exit 1 || true
```

- [ ] **步骤 4：执行全量验证**

运行：

```bash
bash scripts/architecture/check-module-boundaries.sh
mvn -pl manzhushaka-ry-common,manzhushaka-ry-framework,manzhushaka-ry-system,manzhushaka-ry-admin -am test
```

预期：

- 边界检查脚本通过
- ArchUnit 测试通过
- 四个核心模块测试通过

- [ ] **步骤 5：Commit**

```bash
git add pom.xml manzhushaka-ry-admin/pom.xml manzhushaka-ry-framework/pom.xml manzhushaka-ry-system/pom.xml manzhushaka-ry-admin/src/test/java manzhushaka-ry-framework/src/test/java manzhushaka-ry-system/src/test/java scripts/architecture
git commit -m "test: add architecture guardrails for module boundaries"
```

## 7. 第三阶段验收清单

满足以下条件，第三阶段才算真正完成：

1. `common` 中不再存在任何系统业务实体、系统登录态模型、系统树结构、系统字典工具。
2. `framework` 中不再出现 `common.core.domain.entity.*`、`common.core.domain.model.LoginUser`、`common.utils.SecurityUtils`。
3. `admin` 中不再依赖 `infrastructure.persistence.entity` 作为 HTTP 协议对象。
4. `system` 中对外暴露的跨模块读取能力统一通过 `application service / query result`。
5. `ArchUnit + 脚本检查 + 模块测试` 全部通过。

建议用以下命令做最终验收：

```bash
rg -n "common\\.core\\.domain\\.entity\\.|common\\.core\\.domain\\.model\\.LoginUser|common\\.utils\\.SecurityUtils|common\\.utils\\.DictUtils|common\\.core\\.domain\\.TreeSelect" manzhushaka-ry-admin/src/main/java manzhushaka-ry-framework/src/main/java manzhushaka-ry-system/src/main/java manzhushaka-ry-common/src/main/java
bash scripts/architecture/check-module-boundaries.sh
mvn -pl manzhushaka-ry-common,manzhushaka-ry-framework,manzhushaka-ry-system,manzhushaka-ry-admin -am test
```

## 8. 风险与执行顺序建议

第三阶段的风险不在“代码量”，而在“删得太早导致运行时断链”。建议严格按下面顺序执行：

1. 先完成任务 1，稳定登录态模型。
2. 再完成任务 2，稳定 `framework -> system` 读取契约。
3. 接着完成任务 3，收回树结构与字典能力。
4. 确认无生产引用后，再执行任务 4 的批量删除。
5. 最后执行任务 5，把边界守护补上。

不要一开始就直接删除 `common` 旧类，否则会让排错成本大幅上升。
