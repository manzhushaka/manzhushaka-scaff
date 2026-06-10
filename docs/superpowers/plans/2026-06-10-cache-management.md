# Redis 缓存管理实现计划

> **面向 AI 代理的工作者：** 必需子技能：使用 superpowers:subagent-driven-development（推荐）或 superpowers:executing-plans 逐任务实现此计划。步骤使用复选框（`- [ ]`）语法来跟踪进度。

**目标：** 为管理台新增只读 Redis 缓存管理页，支持按关键词查看 Key、类型、TTL、过期时间和值详情。

**架构：** 后端在 `manzhushaka-system` 内新增缓存查询服务和控制器，复用现有 Redis 基础设施并限制扫描规模；前端在 `ui-admin` 新增独立系统页，通过列表 + 详情弹窗承接查询体验，同时补齐菜单、权限与 mock 数据。

**技术栈：** Java 17、Spring Boot 3、Spring Data Redis、JUnit 5、Mockito、Vue 3、TypeScript、Arco Design Vue

---

## 文件结构与职责

- 创建：`manzhushaka-system/src/main/java/com/manzhushaka/system/controller/CacheController.java`
- 创建：`manzhushaka-system/src/main/java/com/manzhushaka/system/service/CacheQueryService.java`
- 创建：`manzhushaka-system/src/main/java/com/manzhushaka/system/service/impl/CacheQueryServiceImpl.java`
- 创建：`manzhushaka-system/src/main/java/com/manzhushaka/system/dto/cache/CacheEntryQuery.java`
- 创建：`manzhushaka-system/src/main/java/com/manzhushaka/system/vo/cache/CacheEntryVO.java`
- 创建：`manzhushaka-system/src/main/java/com/manzhushaka/system/vo/cache/CacheEntryDetailVO.java`
- 创建：`manzhushaka-system/src/test/java/com/manzhushaka/system/controller/CacheControllerTest.java`
- 创建：`manzhushaka-system/src/test/java/com/manzhushaka/system/service/impl/CacheQueryServiceImplTest.java`
- 修改：`manzhushaka-system/src/test/java/com/manzhushaka/system/controller/SystemControllerRequestMappingTest.java`
- 创建：`ui-admin/src/views/system/cache.vue`
- 创建：`ui-admin/src/views/system/cache-support.ts`
- 修改：`ui-admin/src/api/system.ts`
- 修改：`ui-admin/src/types/system.ts`
- 修改：`ui-admin/src/router/component-map.ts`
- 修改：`ui-admin/src/api/mock.ts`
- 创建：`ui-admin/tests/cache-support.test.ts`
- 修改：`sql/manzhushaka_init.sql`

## 任务拆分

### 任务 1：先用测试钉住后端接口与 Redis 查询边界

- [ ] 编写 `CacheControllerTest`，先验证列表接口和详情接口返回结构。
- [ ] 编写 `CacheQueryServiceImplTest`，先验证字符串 Key 的 TTL、预览和值详情读取。
- [ ] 运行受影响测试并确认先失败。

### 任务 2：实现后端只读缓存查询能力

- [ ] 增加查询 DTO、列表/详情 VO 和服务接口。
- [ ] 用 `SCAN` 实现按关键词过滤的 Key 摘要查询，并限制最大返回条数。
- [ ] 实现详情接口，按 Redis 类型读取值内容并返回格式化载荷。
- [ ] 补齐控制器和请求映射测试。

### 任务 3：实现前端缓存管理页和接线

- [ ] 先写 `cache-support` 单测，约束 TTL 文案和详情展示映射。
- [ ] 增加 API、类型定义、组件映射和页面。
- [ ] 完成查询栏、表格和详情弹窗，并为按钮补权限指令。
- [ ] 同步更新 mock 数据，保证 mock 模式也能打开页面。

### 任务 4：补菜单权限并完成验证

- [ ] 更新 `sql/manzhushaka_init.sql` 的菜单与按钮权限。
- [ ] 运行 `mvn -pl manzhushaka-system test` 或最小受影响测试集。
- [ ] 运行 `cd ui-admin && pnpm build`。
- [ ] 根据验证结果收尾说明风险或阻塞。
