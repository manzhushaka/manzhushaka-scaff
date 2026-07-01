# 代码质量与安全修复实现计划

> **面向 AI 代理的工作者：** 必需子技能：使用 superpowers:subagent-driven-development（推荐）或 superpowers:executing-plans 逐任务实现此计划。步骤使用复选框（`- [ ]`）语法来跟踪进度。

**目标：** 收口当前代码基线中的安全暴露面、权限闭环缺口、测试失效问题和主要可维护性债务。

**架构：** 先修复可被外部触发的安全与权限问题，再修复初始化数据和测试工具链，最后按模块边界治理重复的领域模型、Mapper 与前端大组件。每个任务都保持小范围提交，避免把安全修复和大规模重构混在同一个变更里。

**技术栈：** Java 17、Spring Boot、Spring Security、MyBatis、JUnit 5、ArchUnit、Vue 3、Element Plus、Vite。

---

## 当前代码质量评估

整体评价：当前项目能完成后端测试命令和前端生产构建，但代码质量处于“可运行、可迭代，但需要尽快收口边界”的状态。最大风险不在编译层面，而在安全默认值、权限闭环、测试有效性和新旧架构并存。

### 主要质量问题

1. **安全默认值偏宽。** `SecurityConfig` 匿名放行 Druid 和 Swagger，`application-dev.yml` 默认启用 Druid 且默认密码为 `123456`，不适合作为可直接启动的默认基线。
2. **权限闭环不完整。** 多个接口仅依赖登录态，没有按仓库规范显式声明 `@PreAuthorize` 或 `@Anonymous`；初始化 SQL 里 `sys_role_menu` 引用了大量不存在的菜单 ID。
3. **新旧分层并存。** `manzhushaka-ry-system` 同时存在 `domain`、`service/mapper` 旧路径，以及 `application`、`domain/repository`、`infrastructure/persistence` 新路径；`MapperScan("com.manzhushaka.**.mapper")` 会扫到两套 Mapper，维护成本和误用风险偏高。
4. **大文件和大组件偏多。** 后端 `ExcelUtil`、`SysMenuServiceImpl`、`SysUserServiceImpl` 超过合理单文件复杂度；前端 `TreePanel`、`TagsView`、`system/role`、`system/menu` 等文件超过 500 行，状态、视图、交互混在一起。
5. **测试保护不足。** `mvn test` 能通过，但 ArchUnit 测试报告显示 `Tests run: 0`，架构规则没有真正执行。业务单元测试数量也偏少。
6. **前端错误处理不统一。** `request.js` 仍有 `console.log`，多处 `.catch(() => {})` 静默吞错，上传组件没有 `on-error` 处理，排障和用户反馈都偏弱。

---

## 文件结构

### 后端安全与权限

- 修改：`manzhushaka-ry-framework/src/main/java/com/manzhushaka/framework/config/SecurityConfig.java`
- 修改：`manzhushaka-ry-admin/src/main/resources/application.yml`
- 修改：`manzhushaka-ry-admin/src/main/resources/application-dev.yml`
- 修改：`manzhushaka-ry-admin/src/main/java/com/manzhushaka/web/controller/tool/TestController.java`
- 修改：`manzhushaka-ry-admin/src/main/java/com/manzhushaka/web/controller/system/SysNoticeController.java`
- 修改：`manzhushaka-ry-system/src/main/resources/mapper/system/SysNoticeMapper.xml`
- 修改：`sql/manzhushaka_db_init.sql`

### 测试工具链

- 修改：`pom.xml`
- 修改：`manzhushaka-ry-admin/src/test/java/com/manzhushaka/architecture/AdminBoundaryArchTest.java`
- 修改：`manzhushaka-ry-framework/src/test/java/com/manzhushaka/architecture/FrameworkBoundaryArchTest.java`
- 修改：`manzhushaka-ry-system/src/test/java/com/manzhushaka/architecture/SystemBoundaryArchTest.java`
- 创建：`manzhushaka-ry-admin/src/test/java/com/manzhushaka/web/controller/system/SysNoticeControllerTest.java`

### 架构边界治理

- 修改：`manzhushaka-ry-framework/src/main/java/com/manzhushaka/framework/config/ApplicationConfig.java`
- 修改：`manzhushaka-ry-admin/src/main/resources/application.yml`
- 修改：`manzhushaka-ry-system/src/main/java/com/manzhushaka/system/application/service/impl/SystemUserAppServiceImpl.java`
- 修改：`manzhushaka-ry-system/src/main/java/com/manzhushaka/system/service/impl/SysUserServiceImpl.java`
- 修改：`manzhushaka-ry-system/src/main/java/com/manzhushaka/system/infrastructure/persistence/repository/UserRepositoryImpl.java`

### 前端质量

- 修改：`ui-admin/src/utils/request.js`
- 修改：`ui-admin/src/components/ExcelImportDialog/index.vue`
- 修改：`ui-admin/src/layout/components/HeaderNotice/DetailView.vue`
- 修改：`ui-admin/src/components/HeaderSearch/index.vue`
- 后续拆分：`ui-admin/src/components/TreePanel/index.vue`
- 后续拆分：`ui-admin/src/views/system/role/index.vue`

---

## 任务 1：收紧 Druid 和 Swagger 暴露面

**文件：**
- 修改：`manzhushaka-ry-framework/src/main/java/com/manzhushaka/framework/config/SecurityConfig.java:103-108`
- 修改：`manzhushaka-ry-admin/src/main/resources/application.yml:120-131`
- 修改：`manzhushaka-ry-admin/src/main/resources/application-dev.yml:44-51`

- [ ] **步骤 1：调整安全白名单**

将 `SecurityConfig` 中的 Druid 和 Swagger 匿名放行从默认链路移除，只保留登录、注册、验证码和静态资源。示例方向：

```java
requests.requestMatchers("/login", "/register", "/captchaImage").permitAll()
    .requestMatchers(HttpMethod.GET, "/", "/*.html", "/**.html", "/**.css", "/**.js", "/profile/**").permitAll()
    .anyRequest().authenticated();
```

- [ ] **步骤 2：增加配置开关**

将 Swagger UI 和 Druid 监控默认改为通过环境变量显式开启：

```yaml
springdoc:
  swagger-ui:
    enabled: ${SPRINGDOC_SWAGGER_UI_ENABLED:false}

spring:
  datasource:
    druid:
      statViewServlet:
        enabled: ${DRUID_STAT_VIEW_SERVLET_ENABLED:false}
        allow: ${DRUID_STAT_VIEW_ALLOW:127.0.0.1}
        login-username: ${DRUID_STAT_VIEW_USERNAME:}
        login-password: ${DRUID_STAT_VIEW_PASSWORD:}
```

- [ ] **步骤 3：运行验证**

运行：`mvn test -DskipTests=false`

预期：构建成功；未设置环境变量时，Druid 和 Swagger 不再作为匿名入口暴露。

- [ ] **步骤 4：Commit**

```bash
git add manzhushaka-ry-framework/src/main/java/com/manzhushaka/framework/config/SecurityConfig.java manzhushaka-ry-admin/src/main/resources/application.yml manzhushaka-ry-admin/src/main/resources/application-dev.yml
git commit -m "fix(安全): 收紧 Druid 和 Swagger 默认暴露面"
```

---

## 任务 2：移除或隔离测试 Controller

**文件：**
- 修改：`manzhushaka-ry-admin/src/main/java/com/manzhushaka/web/controller/tool/TestController.java`

- [ ] **步骤 1：选择隔离方式**

推荐删除该 Controller。如果仍需 Swagger 示例，改为仅在 `dev` profile 加载，并移除明文密码字段返回。

```java
@Profile("dev")
@RestController
@RequestMapping("/test/user")
public class TestController extends BaseController
{
}
```

- [ ] **步骤 2：补充显式访问语义**

如果保留接口，按仓库规范显式添加 `@Anonymous` 或 `@PreAuthorize`。推荐使用权限：

```java
@PreAuthorize("@ss.hasPermi('tool:test:list')")
@GetMapping("/list")
public R<List<UserEntity>> userList()
```

- [ ] **步骤 3：运行验证**

运行：`mvn -pl manzhushaka-ry-admin -am test`

预期：后端测试通过；生产 profile 下 `/test/user/list` 不可访问。

- [ ] **步骤 4：Commit**

```bash
git add manzhushaka-ry-admin/src/main/java/com/manzhushaka/web/controller/tool/TestController.java
git commit -m "fix(安全): 隔离测试用户接口"
```

---

## 任务 3：修复公告状态绕过和富文本净化

**文件：**
- 修改：`manzhushaka-ry-admin/src/main/java/com/manzhushaka/web/controller/system/SysNoticeController.java:56-60`
- 修改：`manzhushaka-ry-system/src/main/resources/mapper/system/SysNoticeMapper.xml:25-28`
- 修改：`ui-admin/src/layout/components/HeaderNotice/DetailView.vue:45`
- 创建：`manzhushaka-ry-admin/src/test/java/com/manzhushaka/web/controller/system/SysNoticeControllerTest.java`

- [ ] **步骤 1：拆分前台详情和后台详情语义**

后台管理详情继续使用 `system:notice:query`。顶部铃铛使用的前台详情只允许读取正常公告：

```java
@PreAuthorize("@ss.hasPermi('system:notice:query')")
@GetMapping(value = "/{noticeId}")
public AjaxResult getInfo(@PathVariable Long noticeId)
{
    return success(noticeService.selectNoticeById(noticeId));
}

@GetMapping(value = "/published/{noticeId}")
public AjaxResult getPublishedInfo(@PathVariable Long noticeId)
{
    return success(noticeService.selectPublishedNoticeById(noticeId));
}
```

- [ ] **步骤 2：Mapper 增加状态条件**

新增已发布详情查询：

```xml
<select id="selectPublishedNoticeById" parameterType="Long" resultMap="SysNoticeResult">
    <include refid="selectNoticeVo"/>
    where notice_id = #{noticeId} and status = '0'
</select>
```

- [ ] **步骤 3：前端改用前台详情接口并净化 HTML**

在公告详情渲染前使用白名单净化。可以引入 `dompurify`：

```bash
cd ui-admin && npm install dompurify
```

组件中使用：

```js
import DOMPurify from 'dompurify'

const safeNoticeContent = computed(() => DOMPurify.sanitize(detail.value?.noticeContent || ''))
```

模板改为：

```vue
<div v-if="hasContent" class="notice-content" v-html="safeNoticeContent" />
```

- [ ] **步骤 4：编写后端测试**

覆盖：

```java
@Test
void getPublishedInfoShouldNotReturnClosedNotice()
{
    // 构造关闭公告，调用 /system/notice/published/{id}
    // 断言返回 data 为空或业务错误，不能返回 noticeContent
}
```

- [ ] **步骤 5：运行验证**

运行：`mvn -pl manzhushaka-ry-admin -am test`

运行：`cd ui-admin && npm run build:prod`

预期：关闭公告不能通过前台详情读取；富文本仍能正常展示白名单标签。

- [ ] **步骤 6：Commit**

```bash
git add manzhushaka-ry-admin/src/main/java/com/manzhushaka/web/controller/system/SysNoticeController.java manzhushaka-ry-system/src/main/resources/mapper/system/SysNoticeMapper.xml ui-admin/src/layout/components/HeaderNotice/DetailView.vue ui-admin/package.json ui-admin/package-lock.json manzhushaka-ry-admin/src/test/java/com/manzhushaka/web/controller/system/SysNoticeControllerTest.java
git commit -m "fix(公告): 限制前台公告详情并净化富文本"
```

---

## 任务 4：补齐菜单和按钮权限初始化 SQL

**文件：**
- 修改：`sql/manzhushaka_db_init.sql:162-299`
- 修改：`ui-admin/src/views/monitor/operlog/index.vue:139`
- 修改：`manzhushaka-ry-admin/src/main/java/com/manzhushaka/web/controller/monitor/SysOperlogController.java`

- [ ] **步骤 1：补齐缺失菜单记录**

根据前端实际页面和按钮权限，补齐 `sys_menu` 中缺失的按钮权限记录，至少包括：

```sql
insert into sys_menu values('500', '用户查询', '100', '1',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:user:query',  '#', 'admin', sysdate(), '', null, '');
insert into sys_menu values('501', '用户新增', '100', '2',  '', '', '', '', 1, 0, 'F', '0', '0', 'system:user:add',    '#', 'admin', sysdate(), '', null, '');
```

继续补齐 `system:user:*`、`system:role:*`、`system:menu:*`、`system:dept:*`、`system:post:*`、`system:dict:*`、`system:config:*`、`system:notice:*`、`monitor:*` 等前端已使用权限。

- [ ] **步骤 2：修复操作日志详情权限**

二选一：

1. 如果操作日志详情只展示当前行数据，把前端按钮权限改为 `monitor:operlog:list`。
2. 如果要单独查询详情，新增后端 `GET /monitor/operlog/{operId}` 和权限 `monitor:operlog:query`。

推荐新增明确权限：

```java
@PreAuthorize("@ss.hasPermi('monitor:operlog:query')")
@GetMapping(value = "/{operId}")
public AjaxResult getInfo(@PathVariable Long operId)
{
    return success(operLogService.selectOperLogById(operId));
}
```

- [ ] **步骤 3：校验 SQL 引用完整性**

运行：

```bash
python3 - <<'PY'
import re
from pathlib import Path
s = Path('sql/manzhushaka_db_init.sql').read_text()
menus = set(re.findall(r"insert into sys_menu values\('([^']+)'", s))
refs = set(re.findall(r"insert into sys_role_menu values \('2', '([^']+)'\)", s))
missing = sorted(refs - menus, key=lambda x: int(x) if x.isdigit() else x)
assert not missing, missing
PY
```

预期：无缺失菜单 ID。

- [ ] **步骤 4：Commit**

```bash
git add sql/manzhushaka_db_init.sql ui-admin/src/views/monitor/operlog/index.vue manzhushaka-ry-admin/src/main/java/com/manzhushaka/web/controller/monitor/SysOperlogController.java
git commit -m "fix(权限): 补齐菜单按钮权限初始化数据"
```

---

## 任务 5：修复 JUnit 5 和 ArchUnit 测试执行

**文件：**
- 修改：`pom.xml`
- 修改：`manzhushaka-ry-admin/src/test/java/com/manzhushaka/architecture/AdminBoundaryArchTest.java`
- 修改：`manzhushaka-ry-framework/src/test/java/com/manzhushaka/architecture/FrameworkBoundaryArchTest.java`
- 修改：`manzhushaka-ry-system/src/test/java/com/manzhushaka/architecture/SystemBoundaryArchTest.java`

- [ ] **步骤 1：升级 Surefire 插件**

在根 `pom.xml` 的 build plugins 中显式配置支持 JUnit 5 的 Surefire：

```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <version>3.2.5</version>
    <configuration>
        <useModulePath>false</useModulePath>
    </configuration>
</plugin>
```

- [ ] **步骤 2：运行测试并确认 ArchUnit 生效**

运行：`mvn test -DskipTests=false`

预期：Surefire 报告中 `AdminBoundaryArchTest`、`FrameworkBoundaryArchTest`、`SystemBoundaryArchTest` 不再是 `Tests run: 0`。

- [ ] **步骤 3：补充一个故意失败的本地验证**

临时在测试中加入违反规则的断言或引入一个临时违规类，确认 ArchUnit 会失败。验证后删除临时代码。

- [ ] **步骤 4：Commit**

```bash
git add pom.xml manzhushaka-ry-admin/src/test/java/com/manzhushaka/architecture/AdminBoundaryArchTest.java manzhushaka-ry-framework/src/test/java/com/manzhushaka/architecture/FrameworkBoundaryArchTest.java manzhushaka-ry-system/src/test/java/com/manzhushaka/architecture/SystemBoundaryArchTest.java
git commit -m "test(架构): 修复 ArchUnit 测试执行"
```

---

## 任务 6：收敛 System 模块 Mapper 和实体扫描边界

**文件：**
- 修改：`manzhushaka-ry-framework/src/main/java/com/manzhushaka/framework/config/ApplicationConfig.java:16`
- 修改：`manzhushaka-ry-admin/src/main/resources/application.yml:107`
- 修改：`manzhushaka-ry-system/src/main/java/com/manzhushaka/system/application/service/impl/SystemUserAppServiceImpl.java`
- 修改：`manzhushaka-ry-system/src/main/java/com/manzhushaka/system/service/impl/SysUserServiceImpl.java`
- 修改：`manzhushaka-ry-system/src/main/java/com/manzhushaka/system/infrastructure/persistence/repository/UserRepositoryImpl.java`

- [ ] **步骤 1：确定目标边界**

推荐目标：新代码只依赖 `application`、`domain/repository`、`infrastructure/persistence`；旧 `service/mapper/domain` 作为兼容层逐步迁移。

- [ ] **步骤 2：先收紧扫描范围**

将 Mapper 扫描从宽泛通配改为明确包路径，避免两套 Mapper 被无意注入：

```java
@MapperScan({
    "com.manzhushaka.system.infrastructure.persistence.mapper",
    "com.manzhushaka.quartz.mapper"
})
```

如果旧 `system.mapper` 仍被旧 Service 使用，则先不删除，但需要给迁移留出清单。

- [ ] **步骤 3：增加迁移清单**

在本计划执行过程中，按服务逐个迁移旧 `Sys*ServiceImpl` 的 Mapper 依赖到 Repository。以用户模块为第一批，目标是让 `SystemUserAppServiceImpl` 不再同时依赖 Repository 和旧 Service 做同类数据访问。

- [ ] **步骤 4：运行验证**

运行：`mvn -pl manzhushaka-ry-system,manzhushaka-ry-admin -am test`

预期：用户、角色、菜单相关编译和测试通过；Mapper 注入无二义性。

- [ ] **步骤 5：Commit**

```bash
git add manzhushaka-ry-framework/src/main/java/com/manzhushaka/framework/config/ApplicationConfig.java manzhushaka-ry-admin/src/main/resources/application.yml manzhushaka-ry-system/src/main/java/com/manzhushaka/system/application/service/impl/SystemUserAppServiceImpl.java manzhushaka-ry-system/src/main/java/com/manzhushaka/system/service/impl/SysUserServiceImpl.java manzhushaka-ry-system/src/main/java/com/manzhushaka/system/infrastructure/persistence/repository/UserRepositoryImpl.java
git commit -m "refactor(system): 收敛用户模块持久化边界"
```

---

## 任务 7：统一前端错误处理和上传失败反馈

**文件：**
- 修改：`ui-admin/src/utils/request.js:70-120`
- 修改：`ui-admin/src/components/ExcelImportDialog/index.vue:1-135`
- 修改：`ui-admin/src/layout/components/HeaderNotice/index.vue:87-103`

- [ ] **步骤 1：移除请求封装中的 `console.log`**

将日志输出改为统一错误提示，并确保 Promise 正确返回：

```js
}, error => {
  return Promise.reject(error)
})
```

- [ ] **步骤 2：上传组件增加失败处理**

模板增加：

```vue
:on-error="handleError"
```

脚本增加：

```js
function handleError(error) {
  isUploading.value = false
  const message = error?.message || '上传失败，请稍后重试'
  proxy.$modal.msgError(message)
}
```

- [ ] **步骤 3：公告已读失败不静默吞错**

将 `.catch(() => {})` 改成轻量提示或回滚本地状态：

```js
markNoticeRead(item.noticeId).catch(() => {
  proxy.$modal.msgError('标记已读失败，请稍后重试')
})
```

- [ ] **步骤 4：运行验证**

运行：`cd ui-admin && npm run build:prod`

手工验证：上传失败后按钮状态恢复，用户能看到错误提示。

- [ ] **步骤 5：Commit**

```bash
git add ui-admin/src/utils/request.js ui-admin/src/components/ExcelImportDialog/index.vue ui-admin/src/layout/components/HeaderNotice/index.vue
git commit -m "fix(前端): 统一请求错误与上传失败处理"
```

---

## 任务 8：分批拆分前端大组件

**文件：**
- 修改：`ui-admin/src/components/TreePanel/index.vue`
- 创建：`ui-admin/src/components/TreePanel/useTreePanelState.js`
- 创建：`ui-admin/src/components/TreePanel/useTreePanelResize.js`
- 修改：`ui-admin/src/views/system/role/index.vue`
- 创建：`ui-admin/src/views/system/role/useRoleQuery.js`
- 创建：`ui-admin/src/views/system/role/useRoleForm.js`

- [ ] **步骤 1：拆分 TreePanel 状态和拖拽逻辑**

将宽度、折叠、拖拽事件提取到 `useTreePanelResize.js`：

```js
export function useTreePanelResize(defaultWidth) {
  const sidebarWidth = ref(defaultWidth)
  const isResizing = ref(false)
  return {
    sidebarWidth,
    isResizing
  }
}
```

- [ ] **步骤 2：保持组件外部 API 不变**

`TreePanel` 的 props、slots、事件名保持不变，避免影响用户、部门等页面。

- [ ] **步骤 3：拆分角色页面查询和表单逻辑**

将列表查询、重置、分页状态放入 `useRoleQuery.js`；将新增、编辑、提交、校验规则放入 `useRoleForm.js`。

- [ ] **步骤 4：运行验证**

运行：`cd ui-admin && npm run build:prod`

手工验证：角色列表查询、新增、修改、删除、数据权限、分配用户入口仍可操作。

- [ ] **步骤 5：Commit**

```bash
git add ui-admin/src/components/TreePanel/index.vue ui-admin/src/components/TreePanel/useTreePanelState.js ui-admin/src/components/TreePanel/useTreePanelResize.js ui-admin/src/views/system/role/index.vue ui-admin/src/views/system/role/useRoleQuery.js ui-admin/src/views/system/role/useRoleForm.js
git commit -m "refactor(前端): 拆分树面板和角色页面逻辑"
```

---

## 总体验证清单

- [ ] 后端全量测试：`mvn test -DskipTests=false`
- [ ] 后端打包：`mvn clean package`
- [ ] 前端生产构建：`cd ui-admin && npm run build:prod`
- [ ] SQL 初始化校验：`sys_role_menu.menu_id` 全部能在 `sys_menu.menu_id` 找到。
- [ ] 权限闭环校验：前端 `v-hasPermi`、后端 `@PreAuthorize`、`sys_menu.perms` 三方一致。
- [ ] 手工安全验证：未登录不能访问 Druid、Swagger、测试 Controller、后台管理公告详情。
- [ ] 手工公告验证：顶部公告只能查看正常公告，关闭公告不能通过前台详情读取。

---

## 执行建议

推荐按任务 1 到任务 5 优先执行，这些是安全、权限和测试基线问题。任务 6 到任务 8 属于质量治理，可以拆成单独迭代，避免一次 PR 过大。
