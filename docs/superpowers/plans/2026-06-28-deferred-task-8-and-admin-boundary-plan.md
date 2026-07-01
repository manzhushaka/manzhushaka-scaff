# 任务 8 与 Admin 边界预存失败处理方案

> **面向 AI 代理的工作者：** 本文不是本轮安全或权限修复的实现计划，而是两个已识别技术债的处理手册。任务 8 按独立前端重构执行；`AdminBoundaryArchTest` 按 DDD 模块化迁移第二阶段执行，并在迁移前用冻结基线守住新增违规。

**目标：** 明确「前端大组件拆分」和「Admin Controller 直用 Entity」两个问题如何处理、何时处理、如何验证，以及本次修复分支应该做到什么程度。

**结论：**

1. 任务 8 不进入本次修复目标，单独开前端重构分支执行。
2. `AdminBoundaryArchTest` 的失败是真实架构债，不在本次修复里强行改业务链路。
3. 如果当前 CI 需要架构测试保持绿色，推荐使用 ArchUnit freeze 冻结存量违规，只拦新增违规；不推荐删除测试或弱化规则。

---

## 1. 背景与当前证据

### 1.1 任务 8 的当前状态

涉及文件：

- `ui-admin/src/components/TreePanel/index.vue`
- `ui-admin/src/views/system/role/index.vue`

当前规模：

```bash
wc -l ui-admin/src/components/TreePanel/index.vue ui-admin/src/views/system/role/index.vue
```

结果：

```text
756 ui-admin/src/components/TreePanel/index.vue
588 ui-admin/src/views/system/role/index.vue
```

`TreePanel` 同时承担模板、搜索、展开收起、宽度拖拽、本地存储、树实例转发、样式等职责。`role/index.vue` 同时承担查询、分页、批量选择、增删改、状态切换、菜单授权树、数据权限抽屉、分配用户跳转等职责。

这类拆分属于可维护性重构，不属于安全、权限、接口行为修复。把它混入当前修复会扩大回归面，尤其会影响角色管理、用户管理中的树形侧栏和权限树交互。

### 1.2 `AdminBoundaryArchTest` 的当前状态

当前规则文件：

- `manzhushaka-admin/src/test/java/com/manzhushaka/architecture/AdminBoundaryArchTest.java`

当前规则意图：

- Controller 禁止直接依赖 `..infrastructure.persistence.entity..`
- Controller 禁止直接依赖 `..mapper..`

验证命令：

```bash
mvn -pl manzhushaka-admin -am -Dtest=AdminBoundaryArchTest -Dsurefire.failIfNoSpecifiedTests=false test
```

当前结果：

```text
Tests run: 2, Failures: 1, Errors: 0, Skipped: 0
Rule 'no classes that reside in a package '..web.controller..' should depend on classes that reside in any package ['..infrastructure.persistence.entity..', '..mapper..']' was violated (103 times)
```

主要违规集中在：

- `SysDeptController`
- `SysDictDataController`
- `SysDictTypeController`
- `SysMenuController`
- `SysProfileController`
- `SysRoleController`
- `SysUserController`
- `SysIndexController`

静态扫描也能看到 Controller 直接引用持久化实体：

```bash
rg -n "com\\.manzhushaka\\.system\\.(infrastructure\\.persistence\\.entity|mapper|domain)" \
  manzhushaka-admin/src/main/java/com/manzhushaka/web/controller
```

这不是单点测试误报，而是 `admin` 层仍然暴露 `system.infrastructure.persistence.entity` 和部分旧 `system.domain` 模型的结构性问题。它应该放入 `docs/superpowers/plans/2026-06-28-domain-modularization-phase-2-plan.md` 的 HTTP 边界收口中系统处理。

---

## 2. 本次修复分支怎么处理

### 2.1 任务 8 的处理

本次修复分支只做记录，不改代码：

1. 不拆 `TreePanel`。
2. 不拆 `role/index.vue`。
3. 不新增 `useTreePanelResize.js`、`useRoleQuery.js` 等重构文件。
4. 如果当前修复确实碰到这两个文件，只允许做与本轮问题直接相关的最小修改。

推荐在本轮修复总结中写明：

```text
任务 8（前端大组件拆分）已推迟：涉及 TreePanel 与 role/index.vue，属于独立前端重构，不纳入本次安全/权限/测试修复。
```

### 2.2 `AdminBoundaryArchTest` 的处理

如果当前目标是不让 CI 因预存架构债失败，推荐做一个独立的小提交：

```text
test(架构): 冻结 Admin Controller 存量边界违规
```

这个提交只做两件事：

1. 用 ArchUnit freeze 记录当前已存在的 Controller 直用 Entity 违规。
2. 保留规则，让新增违规继续失败。

不推荐的做法：

1. 删除 `AdminBoundaryArchTest`。
2. 把规则改成永远不会命中的包名。
3. 用 `@Disabled` 长期跳过测试。
4. 为了让测试通过，临时把 Entity 挪包或把 Controller 逻辑塞进 Converter。
5. 在本次修复中顺手改造所有 Controller。

---

## 3. 任务 8 后续执行方案

### 3.1 目标

在不改变页面行为和组件外部 API 的前提下，降低 `TreePanel` 和角色管理页的单文件复杂度。

完成后应满足：

1. `TreePanel` 的 props、emit、slot、`defineExpose` 对外契约不变。
2. 用户管理页使用 `TreePanel` 的行为不变。
3. 角色管理页的查询、新增、修改、删除、导出、状态切换、数据权限、分配用户入口行为不变。
4. 前端生产构建通过。

### 3.2 建议分支

```bash
git checkout -b codex/refactor-tree-panel-role-page
```

如果当前仓库已经在功能分支上，使用团队当前分支策略即可，关键是不要与安全修复、权限修复混在同一个提交里。

### 3.3 第一阶段：建立行为基线

先不改代码，记录当前行为。

运行：

```bash
cd ui-admin
npm run build:prod
```

手工验证用户管理页：

1. 部门树加载。
2. 搜索部门。
3. 点击部门后用户列表过滤。
4. 展开和收起树。
5. 拖动树面板宽度。
6. 刷新页面后宽度按 `storageKey` 恢复。

手工验证角色管理页：

1. 角色列表加载。
2. 搜索、重置、分页。
3. 新增角色。
4. 修改角色。
5. 删除角色。
6. 切换角色状态。
7. 打开数据权限抽屉并保存。
8. 点击分配用户入口。
9. 导出按钮能触发下载请求。

这一步的产出是验证记录，不改业务文件。

### 3.4 第二阶段：拆 `TreePanel`

只先拆逻辑，不急着拆模板。

建议文件：

- 修改：`ui-admin/src/components/TreePanel/index.vue`
- 创建：`ui-admin/src/components/TreePanel/useTreePanelResize.js`
- 创建：`ui-admin/src/components/TreePanel/useTreePanelSearch.js`
- 创建：`ui-admin/src/components/TreePanel/useTreePanelExpansion.js`

职责划分：

| 文件 | 职责 |
| --- | --- |
| `index.vue` | 保留模板、props、emit、slot、`defineExpose` 和样式入口 |
| `useTreePanelResize.js` | 宽度、拖拽、折叠宽度、本地存储、鼠标和触摸事件清理 |
| `useTreePanelSearch.js` | 搜索关键词、树过滤、`search` 事件 |
| `useTreePanelExpansion.js` | 展开全部、收起全部、节点遍历、`expanded-all-change` 事件 |

拆分原则：

1. 不改 props 名称和默认值。
2. 不改事件名。
3. 不改 slot 名。
4. 不改 `setCurrentKey`、`getCurrentNode`、`setCheckedKeys`、`resetWidth` 等暴露方法。
5. 每拆出一个 composable，就运行一次构建或至少运行一次 Vite 类型解析。

`useTreePanelResize.js` 的接口建议保持窄：

```js
export function useTreePanelResize(props) {
  return {
    collapsed,
    sidebarWidth,
    isResizing,
    isLoadingFromStorage,
    startResize,
    toggleCollapsed,
    resetWidth,
    getCurrentWidth,
    setWidth,
    cleanupResize,
    loadSavedWidth
  }
}
```

`TreePanel/index.vue` 继续统一调用 `onMounted`、`onBeforeUnmount`，不要让 composable 自己隐藏生命周期太多细节，便于排查宽度和事件清理问题。

### 3.5 第三阶段：拆 `role/index.vue`

先拆组合式逻辑，模板暂时保留在 `index.vue`，避免一次性拆出多个子组件导致回归面过大。

建议文件：

- 修改：`ui-admin/src/views/system/role/index.vue`
- 创建：`ui-admin/src/views/system/role/useRoleList.js`
- 创建：`ui-admin/src/views/system/role/useRoleForm.js`
- 创建：`ui-admin/src/views/system/role/useRolePermissionTree.js`
- 创建：`ui-admin/src/views/system/role/useRoleDataScope.js`

职责划分：

| 文件 | 职责 |
| --- | --- |
| `useRoleList.js` | `roleList`、`loading`、`queryParams`、`dateRange`、`getList`、`handleQuery`、`resetQuery`、选择项状态、删除、导出、状态切换 |
| `useRoleForm.js` | 新增和修改弹窗、表单模型、校验规则、`handleAdd`、`handleUpdate`、`submitForm`、`cancel` |
| `useRolePermissionTree.js` | 菜单树加载、菜单勾选、半选节点、展开折叠、父子联动 |
| `useRoleDataScope.js` | 数据权限抽屉、部门树加载、部门勾选、权限范围变化、提交数据权限 |

拆分顺序：

1. 先抽 `useRoleList.js`，因为它不依赖弹窗树。
2. 再抽 `useRolePermissionTree.js`，隔离菜单树操作。
3. 再抽 `useRoleForm.js`，让新增和修改共用菜单树接口。
4. 最后抽 `useRoleDataScope.js`，隔离部门树和数据权限抽屉。

每一步完成后运行：

```bash
cd ui-admin
npm run build:prod
```

并至少手工验证该步骤涉及的页面动作。

### 3.6 第四阶段：视情况拆模板组件

只有当前三类 composable 稳定后，再考虑拆模板组件。

可选组件：

- `RoleSearchForm.vue`
- `RoleActionBar.vue`
- `RoleEditDialog.vue`
- `RoleDataScopeDrawer.vue`

这个阶段不是必需项。若拆完逻辑后 `index.vue` 已经足够清楚，可以停止，不为拆而拆。

### 3.7 任务 8 验收标准

运行：

```bash
cd ui-admin
npm run build:prod
```

手工验证：

1. 用户管理页部门树行为完整。
2. 角色管理页全流程完整。
3. 浏览器控制台无新增错误。
4. `TreePanel` 对外 API 未变化。
5. `role/index.vue` 的接口调用路径未变化。

建议提交拆成两到三个：

```bash
git add ui-admin/src/components/TreePanel
git commit -m "refactor(前端): 拆分树面板交互逻辑"

git add ui-admin/src/views/system/role
git commit -m "refactor(前端): 拆分角色管理页面逻辑"
```

---

## 4. `AdminBoundaryArchTest` 预存失败处理方案

### 4.1 处理目标

短期目标：

1. 承认当前 103 个违规是真实存量问题。
2. 让 CI 不因存量问题持续红灯。
3. 防止新增 Controller 继续依赖 `infrastructure.persistence.entity` 或 `mapper`。

长期目标：

1. Controller 只接受 DTO、返回 VO 或 `AjaxResult` 包装的 VO。
2. Controller 不再直接依赖持久化实体、Mapper 和旧业务实体。
3. Admin 边界规则从 freeze 回到普通强制规则。

### 4.2 推荐短期做法：冻结存量违规

修改：

- `manzhushaka-admin/src/test/java/com/manzhushaka/architecture/AdminBoundaryArchTest.java`
- `manzhushaka-admin/src/test/resources/archunit.properties`
- `manzhushaka-admin/src/test/resources/archunit/frozen/*`

把 Entity 规则拆成「冻结 Entity」和「严格 Mapper」两条：

```java
package com.manzhushaka.architecture;

import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.freeze.FreezingArchRule.freeze;

/**
 * Admin 模块边界守护。
 *
 * @author manzhushaka
 * @date 2026-06-28
 */
@AnalyzeClasses(packages = "com.manzhushaka")
public class AdminBoundaryArchTest {

    @ArchTest
    static final ArchRule CONTROLLER_SHOULD_NOT_DEPEND_ON_ENTITY =
            freeze(noClasses()
                    .that().resideInAPackage("..web.controller..")
                    .should().dependOnClassesThat()
                    .resideInAnyPackage("..infrastructure.persistence.entity.."))
                    .as("admin_controllers_should_not_depend_on_persistence_entity");

    @ArchTest
    static final ArchRule CONTROLLER_SHOULD_NOT_DEPEND_ON_MAPPER =
            noClasses()
                    .that().resideInAPackage("..web.controller..")
                    .should().dependOnClassesThat()
                    .resideInAnyPackage("..mapper..");
}
```

新增 `manzhushaka-admin/src/test/resources/archunit.properties`：

```properties
freeze.store.default.path=src/test/resources/archunit/frozen
freeze.store.default.allowStoreCreation=true
freeze.store.default.allowStoreUpdate=true
```

首次运行生成冻结文件：

```bash
mvn -pl manzhushaka-admin -am -Dtest=AdminBoundaryArchTest -Dsurefire.failIfNoSpecifiedTests=false test
```

检查生成内容：

```bash
find manzhushaka-admin/src/test/resources/archunit/frozen -type f -maxdepth 1 -print
```

确认冻结文件只记录当前已知违规后，把 `allowStoreCreation` 改为 `false`：

```properties
freeze.store.default.path=src/test/resources/archunit/frozen
freeze.store.default.allowStoreCreation=false
freeze.store.default.allowStoreUpdate=true
```

日常配置解释：

| 配置 | 日常值 | 原因 |
| --- | --- | --- |
| `allowStoreCreation` | `false` | 防止新规则或新基线无审查地自动生成 |
| `allowStoreUpdate` | `true` | 允许已经修掉的旧违规从冻结文件中移除 |
| `freeze.refreeze` | 不配置 | 不自动接受当前所有违规为新基线 |

再次运行：

```bash
mvn -pl manzhushaka-admin -am -Dtest=AdminBoundaryArchTest -Dsurefire.failIfNoSpecifiedTests=false test
```

预期：

1. `AdminBoundaryArchTest` 通过。
2. 新增 Controller 依赖 `infrastructure.persistence.entity` 会失败。
3. 新增 Controller 依赖 `mapper` 会失败。
4. 移除旧违规后，冻结文件会随迁移逐步减少。

提交：

```bash
git add manzhushaka-admin/src/test/java/com/manzhushaka/architecture/AdminBoundaryArchTest.java \
  manzhushaka-admin/src/test/resources/archunit.properties \
  manzhushaka-admin/src/test/resources/archunit/frozen
git commit -m "test(架构): 冻结 Admin Controller 存量边界违规"
```

### 4.3 备选短期做法：显式跳过规则

只有在团队不接受冻结文件时才使用：

```java
@Disabled("预存失败：Controller 直用 Entity 将在 domain-modularization-phase-2-plan 中系统解决")
@ArchTest
static final ArchRule CONTROLLER_SHOULD_NOT_DEPEND_ON_ENTITY =
        noClasses()
                .that().resideInAPackage("..web.controller..")
                .should().dependOnClassesThat()
                .resideInAnyPackage("..infrastructure.persistence.entity..");
```

该方案缺点明显：

1. 无法拦截新增 Entity 依赖。
2. 很容易被长期遗忘。
3. 测试报告不会展示债务是否减少。

如果采用跳过规则，必须同步创建一个明确的迁移任务，并写清恢复条件：

```text
恢复条件：menu/dept/dict/profile/user/role/index 等 Controller 不再直接依赖 infrastructure.persistence.entity 后，移除 @Disabled 并恢复 AdminBoundaryArchTest。
```

---

## 5. Admin 边界长期迁移步骤

长期迁移应纳入：

- `docs/superpowers/plans/2026-06-28-domain-modularization-phase-2-plan.md`

### 5.1 第一批：`menu/dept/dict/profile`

这些是第二阶段计划中已经明确的重点。

需要新增或补齐：

- `manzhushaka-admin/src/main/java/com/manzhushaka/web/dto/system/menu/*`
- `manzhushaka-admin/src/main/java/com/manzhushaka/web/dto/system/dept/*`
- `manzhushaka-admin/src/main/java/com/manzhushaka/web/dto/system/dict/*`
- `manzhushaka-admin/src/main/java/com/manzhushaka/web/dto/system/profile/*`
- `manzhushaka-admin/src/main/java/com/manzhushaka/web/vo/system/menu/*`
- `manzhushaka-admin/src/main/java/com/manzhushaka/web/vo/system/dept/*`
- `manzhushaka-admin/src/main/java/com/manzhushaka/web/vo/system/dict/*`
- `manzhushaka-admin/src/main/java/com/manzhushaka/web/vo/system/profile/*`
- `manzhushaka-admin/src/main/java/com/manzhushaka/web/converter/system/menu/*`
- `manzhushaka-admin/src/main/java/com/manzhushaka/web/converter/system/dept/*`
- `manzhushaka-admin/src/main/java/com/manzhushaka/web/converter/system/dict/*`
- `manzhushaka-admin/src/main/java/com/manzhushaka/web/converter/system/profile/*`
- `manzhushaka-system/src/main/java/com/manzhushaka/system/application/service/SystemMenuAppService.java`
- `manzhushaka-system/src/main/java/com/manzhushaka/system/application/service/SystemDeptAppService.java`
- `manzhushaka-system/src/main/java/com/manzhushaka/system/application/service/SystemDictAppService.java`
- `manzhushaka-system/src/main/java/com/manzhushaka/system/application/service/SystemProfileAppService.java`

迁移方向：

| 当前 Controller 签名 | 目标签名 |
| --- | --- |
| `list(SysMenu menu)` | `list(MenuListRequest request)` |
| `add(@RequestBody SysMenu menu)` | `add(@RequestBody CreateMenuRequest request)` |
| `edit(@RequestBody SysMenu menu)` | `edit(@RequestBody UpdateMenuRequest request)` |
| `list(SysDept dept)` | `list(DeptListRequest request)` |
| `add(@RequestBody SysDept dept)` | `add(@RequestBody CreateDeptRequest request)` |
| `edit(@RequestBody SysDept dept)` | `edit(@RequestBody UpdateDeptRequest request)` |
| `list(SysDictType dictType)` | `list(DictTypeListRequest request)` |
| `add(@RequestBody SysDictType dict)` | `add(@RequestBody CreateDictTypeRequest request)` |
| `list(SysDictData dictData)` | `list(DictDataListRequest request)` |
| `add(@RequestBody SysDictData dict)` | `add(@RequestBody CreateDictDataRequest request)` |
| `updateProfile(@RequestBody SysUser user)` | `updateProfile(@RequestBody UpdateProfileRequest request)` |
| `updatePwd(@RequestBody Map<String, String> params)` | `updatePwd(@RequestBody UpdatePasswordRequest request)` |

业务校验建议逐步下沉到 AppService：

1. 菜单名称唯一性。
2. 路由配置唯一性。
3. 部门名称唯一性。
4. 部门数据权限。
5. 字典类型唯一性。
6. 个人资料手机和邮箱唯一性。
7. 修改密码旧密码校验。

Controller 最终只保留：

1. 权限注解。
2. HTTP 参数接收。
3. DTO 校验。
4. Converter 调用。
5. AppService 调用。
6. `AjaxResult` 或 `TableDataInfo` 包装。

### 5.2 第二批：`user/role` 剩余直连点

`SysUserController` 和 `SysRoleController` 已经有 DTO 试点，但仍有不少 Entity 依赖。

重点处理：

| 文件 | 当前问题 | 建议处理 |
| --- | --- | --- |
| `SysUserController` | `export`、`importData`、`importTemplate` 仍用 `SysUser` | 新增 `UserExportVO`、`UserImportRow`，由 AppService 转换 |
| `SysUserController` | `getInfo`、`authRole` 返回 `SysUser`、`SysRole` | 返回 `UserDetailVO`、`RoleOptionVO` |
| `SysUserController` | 唯一性校验构造 `new SysUser()` | 改为 AppService 接受校验 Command |
| `SysUserController` | `deptTree(SysDept dept)` | 改为 `DeptTreeRequest` |
| `SysRoleController` | `export` 仍用 `SysRole` | 新增 `RoleExportVO` |
| `SysRoleController` | `allocatedList(SysUser user)`、`unallocatedList(SysUser user)` | 新增 `AuthUserListRequest` |
| `SysRoleController` | 权限校验构造 `new SysRole()` | 改为 AppService 封装角色校验 |
| `SysRoleController` | `deptTree` 构造 `new SysDept()` | 改为 `SystemDeptAppService` 返回树节点 |

Excel 导入导出不要继续使用持久化 Entity 作为行模型。推荐做法：

1. `admin` 定义 Excel 行模型，例如 `UserImportRow`、`UserExportVO`、`RoleExportVO`。
2. Excel 行模型可以带 `@Excel` 注解。
3. AppService 负责 Excel 行模型和应用命令之间的转换。
4. Controller 不接触 `SysUser`、`SysRole`。

### 5.3 第三批：`index`、`config`、`notice`、`post`、`monitor`

当前 `AdminBoundaryArchTest` 主要约束 `infrastructure.persistence.entity` 和 `mapper`，但从 DDD 边界看，Controller 直接使用旧 `com.manzhushaka.system.domain.*` 也属于遗留问题。

涉及文件包括：

- `SysIndexController`
- `SysConfigController`
- `SysNoticeController`
- `SysPostController`
- `SysLogininforController`
- `SysOperlogController`
- `SysUserOnlineController`
- `CacheController`

处理顺序建议：

1. 先处理 `SysIndexController` 的 `unlockScreen`，新增 `UnlockScreenRequest`，密码校验下沉到 AppService。
2. 再处理 `SysConfigController`、`SysNoticeController`、`SysPostController`，补齐 DTO/VO/Converter。
3. 最后处理 monitor 相关 Controller，因为它们多是查询和导出，适合统一建立查询请求与导出 VO。

当这些 Controller 都完成后，再把架构规则扩大到旧 `system.domain`：

```java
noClasses()
        .that().resideInAPackage("..web.controller..")
        .should().dependOnClassesThat()
        .resideInAnyPackage("..infrastructure.persistence.entity..", "..mapper..", "..system.domain..");
```

这一步不要提前做，否则会把已经知道的大量旧域模型债务一次性打到当前修复分支。

### 5.4 去除 freeze 的条件

当以下命令无输出时，可以开始移除 freeze：

```bash
rg -n "com\\.manzhushaka\\.system\\.infrastructure\\.persistence\\.entity" \
  manzhushaka-admin/src/main/java/com/manzhushaka/web/controller

rg -n "com\\.manzhushaka\\.system\\.mapper" \
  manzhushaka-admin/src/main/java/com/manzhushaka/web/controller
```

然后把 `AdminBoundaryArchTest` 改回普通强规则：

```java
@ArchTest
static final ArchRule CONTROLLER_SHOULD_NOT_DEPEND_ON_ENTITY =
        noClasses()
                .that().resideInAPackage("..web.controller..")
                .should().dependOnClassesThat()
                .resideInAnyPackage("..infrastructure.persistence.entity..");
```

删除冻结文件：

```bash
rm -rf manzhushaka-admin/src/test/resources/archunit/frozen
```

运行：

```bash
mvn -pl manzhushaka-admin -am -Dtest=AdminBoundaryArchTest -Dsurefire.failIfNoSpecifiedTests=false test
mvn test
```

预期：

1. `AdminBoundaryArchTest` 通过。
2. 全量测试通过。
3. Controller 对持久化实体和 Mapper 的依赖不再回潮。

---

## 6. 当前优先级建议

建议执行顺序：

1. 当前修复分支：不做任务 8。
2. 当前修复分支或单独小提交：冻结 `AdminBoundaryArchTest` 存量违规，让测试能拦新增违规。
3. DDD 第二阶段：按 `menu/dept/dict/profile` 优先迁移 Controller HTTP 边界。
4. DDD 第二阶段后半段：收掉 `user/role` 试点中剩余的 Entity 依赖。
5. DDD 第三阶段：处理旧 `system.domain` 模型和更严格的架构规则。
6. 独立前端重构分支：执行任务 8。

这样拆的好处是每个分支的成功标准都清楚：

| 分支 | 成功标准 |
| --- | --- |
| 当前修复分支 | 安全、权限、测试工具链问题收口；不引入大组件重构 |
| Admin freeze 小提交 | 存量违规被记录，新增 Entity/Mapper 依赖能被拦住 |
| DDD 第二阶段 | Admin Controller 不再直接依赖 `infrastructure.persistence.entity` |
| 任务 8 分支 | 前端大组件拆分，页面行为不变，构建通过 |
