# Redis 缓存管理设计

## 1. 背景

当前仓库已经有 Redis 运行态监控，但仍缺少面向管理员的 Key 级排查入口。排查验证码、会话、配置缓存或异步链路辅助数据时，只能连 Redis 客户端手工看数据，无法在管理台直接确认 Key、值和剩余生存时间。

## 2. 目标与边界

### 2.1 目标

- 提供一个独立的“缓存管理”页面。
- 支持按关键词或前缀查询 Redis Key。
- 支持查看 Key 类型、剩余 TTL、预计过期时间和值详情。
- 保持只读，优先服务排查和观察。

### 2.2 本期边界

- 只支持查询和查看详情，不提供删除、编辑、批量清理。
- 仅面向当前 Redis 默认库。
- 仅展示常见类型的可读值：`string`、`hash`、`list`、`set`、`zset`；未知类型回退为文本提示。
- 为避免重扫 Redis，本期限制查询结果数量并要求输入关键词后再查。

### 2.3 非目标

- 不提供跨库切换。
- 不提供值修改、TTL 续期或手动失效。
- 不做大规模巡检、统计报表或热 Key 分析。

## 3. 方案

### 3.1 后端

- 在 `manzhushaka-system` 增加缓存查询控制器和服务。
- 复用现有 `RedisConnectionFactory` / `StringRedisTemplate` 读取 Redis。
- 提供两个接口：
  - `GET /system/cache/entries`：分页式返回匹配到的 Key 摘要。
  - `GET /system/cache/entries/detail`：按完整 Key 返回值详情。
- 列表摘要包含：`key`、`type`、`ttlSeconds`、`expireAt`、`valuePreview`。
- 详情包含：上述摘要 + 完整值载荷。
- 查询使用 `SCAN`，并在服务端限制最大返回条数，避免阻塞。

### 3.2 前端

- 在 `ui-admin` 增加 `system/cache.vue` 页面。
- 页面形态采用“查询栏 + 表格 + 详情弹窗”。
- 查询栏包含关键词输入、数量限制选择和刷新按钮。
- 表格展示 Key、类型、TTL、过期时间、值预览和“详情”按钮。
- 详情弹窗根据类型展示结构化内容或格式化文本。

### 3.3 菜单与权限

- 新增菜单：`缓存管理`
- 新增按钮权限：
  - `system:cache:query`
  - `system:cache:detail`
- 初始化 SQL 与 mock 菜单同时补齐，保证新环境和前端 mock 一致。

## 4. 验证

- 后端：新增控制器测试和服务单测，覆盖 Redis Key 摘要与详情读取。
- 前端：补页面参数与数据映射的单测，至少执行 `pnpm build`。
- 初始化：更新 `sql/manzhushaka_init.sql` 菜单种子数据。
