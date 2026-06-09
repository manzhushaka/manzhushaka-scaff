# Redis Stream 通用消息台账设计

## 1. 背景

当前仓库已经落地了基于 Redis Stream 的异步消息链路，至少包含操作日志异步落库这一条路径：

- 生产端通过 `OpLogAspect` 生成 `OpLogRecord`，再调用 `OpLogPublisher` 发布消息。
- 默认实现 `RedisOpLogPublisher` 将 `OpLogRecord` 包装为 `MqEvent` 后写入 Redis Stream。
- 消费端 `OpLogStreamConsumer` 轮询 `manzhushaka:stream:oplog`，消费成功后将数据写入 `sys_op_log`。

现状存在几个问题：

1. 消息发布后没有数据库台账，无法确认一条消息是否已发出、是否已消费、是否失败。
2. 消费端当前只有 Redis ACK，没有业务层面的状态机，发生异常后排查成本高。
3. 失败消息虽然存在重投和死信思路，但没有统一的管理入口，也没有人工补救闭环。
4. 目前 Redis Stream 只承载“传输”，不承载“可观测性”和“补偿入口”，因此无法满足“担心漏执行”的诉求。

本次设计目标是为仓库内**所有通过统一 Publisher 发出的 Redis Stream 消息**提供一套通用消息台账能力。

## 2. 目标与边界

### 2.1 目标

- 所有通过统一 Publisher 发出的 Redis Stream 消息都要先进入数据库台账。
- 管理台可以查询消息的发布、消费、失败和重试状态。
- 消费失败后支持人工手动重试。
- 整体语义采用“至少一次”，优先避免“漏执行”，允许在极端情况下重复执行。
- 后续新增 Redis Stream 业务链路时，只需要接入统一发布器和统一消费执行器即可复用台账能力。

### 2.2 本期边界

- 仅纳管**本系统通过统一 Publisher 发出的消息**。
- 本期提供**手动重试**，不提供定时自动补偿。
- 本期不承诺“严格只执行一次”，业务消费方仍需自行保证幂等。
- 本期先保留现有 Redis Stream 作为消息传输介质，不替换为其他 MQ。

### 2.3 非目标

- 不纳管外部系统直接写入 Redis Stream 的消息。
- 不在本期引入告警中心、短信/邮件通知或自动巡检。
- 不在本期提供批量重试、批量回滚、动态消费组管理等平台化能力。
- 不在本期改造为分布式事务或 Outbox 模式。

## 3. 核心设计原则

### 3.1 统一入口

应用内所有 Redis Stream 消息发布必须走统一发布器，不允许业务代码直接调用 `StringRedisTemplate.opsForStream().add(...)`。

### 3.2 台账先行

无论消息最终是否成功写入 Redis，都必须先在数据库中保留一条可查询记录。

### 3.3 至少一次

设计优先保证“不漏”，不追求平台层面的“绝对不重”。消息重试、人工补发时，消费者应基于业务键或事件 ID 做幂等处理。

### 3.4 最小侵入

尽量在 `manzhushaka-mq`、`manzhushaka-db`、`manzhushaka-system` 三个模块内完成通用能力建设，减少对现有业务模块的侵入式改造。

## 4. 数据模型

新增表：`sys_mq_message`

建议字段如下：

| 字段 | 类型 | 说明 |
| --- | --- | --- |
| `id` | bigint | 主键 |
| `event_id` | varchar(64) | 逻辑事件 ID，全局唯一 |
| `stream_key` | varchar(128) | Redis Stream Key |
| `event_type` | varchar(64) | 事件类型 |
| `biz_key` | varchar(128) | 业务键，可为空 |
| `trace_id` | varchar(64) | 链路追踪 ID，可为空 |
| `source` | varchar(64) | 事件来源 |
| `status` | varchar(32) | 台账状态 |
| `payload_snapshot` | longtext | 发布时消息体快照 |
| `retry_count` | int | 已重试次数 |
| `last_error` | varchar(1000) | 最近一次错误摘要 |
| `consumer_group` | varchar(64) | 最近一次消费组 |
| `consumer_name` | varchar(64) | 最近一次消费者名称 |
| `processing_deadline_at` | datetime | 本次处理超时时间，用于判断卡住的处理中消息 |
| `published_at` | datetime | 成功发布到 Stream 的时间 |
| `consume_started_at` | datetime | 最近一次开始消费时间 |
| `consumed_at` | datetime | 最后成功消费时间 |
| `create_by` | varchar(64) | 创建人 |
| `create_time` | datetime | 创建时间 |
| `update_by` | varchar(64) | 更新人 |
| `update_time` | datetime | 更新时间 |

建议索引：

- 唯一索引：`uk_sys_mq_message_event_id (event_id)`
- 普通索引：`idx_sys_mq_message_stream_key (stream_key)`
- 普通索引：`idx_sys_mq_message_status (status)`
- 普通索引：`idx_sys_mq_message_biz_key (biz_key)`
- 普通索引：`idx_sys_mq_message_trace_id (trace_id)`
- 普通索引：`idx_sys_mq_message_create_time (create_time)`

## 5. 状态机

本期状态统一采用以下 5 个值：

| 状态 | 含义 |
| --- | --- |
| `INIT` | 台账已创建，但尚未确认写入 Redis Stream |
| `PUBLISHED` | 已成功写入 Redis Stream，等待消费 |
| `PROCESSING` | 消费端已开始处理 |
| `SUCCESS` | 消费成功并完成业务落库或业务处理 |
| `FAIL` | 最近一次消费失败，等待人工重试 |

状态流转如下：

```text
INIT -> PUBLISHED -> PROCESSING -> SUCCESS
INIT -> FAIL
PUBLISHED -> PROCESSING -> FAIL
FAIL -> PUBLISHED
```

说明：

- `INIT -> FAIL` 用于发布到 Redis 失败，但台账已存在。
- `FAIL -> PUBLISHED` 表示人工手动重试成功重新投递。
- 本期不单独引入 `DEAD` 状态，避免与“自动补偿”语义绑定。后续若加自动补偿，可在此基础上扩展。

## 6. 后端模块落点

### 6.1 `manzhushaka-db`

新增：

- `SysMqMessage` 实体
- `SysMqMessageMapper`

### 6.2 `manzhushaka-mq`

新增或调整：

- 通用台账发布器，例如 `LedgeredRedisStreamPublisher`
- 通用消费执行器，例如 `MqMessageConsumeExecutor`
- 台账状态更新服务，例如 `MqMessageLedgerService`
- 与状态相关的枚举，例如 `MqMessageStatus`

### 6.3 `manzhushaka-system`

新增：

- 消息台账查询 DTO / VO
- 消息台账查询接口
- 消息手动重试接口

### 6.4 `ui-admin`

新增：

- 消息台账列表页
- 查询条件、状态展示、详情查看、手动重试交互
- 路由、组件映射、类型定义、API 封装

## 7. 发布链路设计

### 7.1 统一发布接口

建议保留当前 `MqEvent<T>` 作为事件信封，但统一由通用发布器接管落账与发送：

```text
publish(streamKey, event)
  -> 写 sys_mq_message，状态 INIT
  -> 写 Redis Stream
  -> 更新 sys_mq_message 状态为 PUBLISHED
```

### 7.2 发布规则

发布时必须保证：

- `event_id` 存在；如果调用方未显式设置，则由发布器生成。
- `payload_snapshot` 保存发布时的序列化快照，后续人工重试直接复用该快照。
- `retry_count` 初始值为 `0`。
- 若 Redis 写入失败，台账必须更新为 `FAIL`，并写入 `last_error`。

### 7.3 事务取舍

数据库与 Redis 无法共享事务，本期接受以下事实：

- 若台账写成功、Redis 写失败：保留 `FAIL` 状态，允许人工重试。
- 若 Redis 写成功、但更新 `PUBLISHED` 状态失败：理论上会留下少量 `INIT` 脏数据，需要通过日志排查和后续补偿处理。

本期先接受这一致性级别，因为核心目标是“可见、可补救”，而不是跨存储强一致。

## 8. 消费链路设计

### 8.1 通用消费执行器

建议消费者不再直接写“查消息 + 业务处理 + ACK”的散装逻辑，而是统一走一个执行器：

```text
consume(record, group, consumer, handler)
  -> 解析 eventId
  -> 查询 sys_mq_message
  -> 若状态为 SUCCESS，直接 ACK 并跳过
  -> 更新状态为 PROCESSING，记录 consumer_group / consumer_name / consume_started_at / processing_deadline_at
  -> 执行业务 handler
  -> 成功：更新状态为 SUCCESS，记录 consumed_at，并清空 processing_deadline_at，再 ACK
  -> 失败：更新状态为 FAIL 和 last_error，并清空 processing_deadline_at，再 ACK
```

### 8.2 ACK 策略

本期对失败消息采用“**失败后也 ACK 原消息**”的策略，原因如下：

- 本期恢复入口是数据库台账和人工重试，而不是 Redis Pending List。
- 如果失败消息长期停留在 Pending 中，状态会分散在 Redis 和 MySQL 两处，运维入口不统一。
- 手动重试时重新投递一条新消息，比长期占用 Pending 更容易治理。

### 8.3 幂等要求

由于本期采用“至少一次”语义，消费处理器必须接受以下情况：

- 同一 `event_id` 可能被人工重新投递。
- 同一业务对象可能因为重复消息被再次处理。

因此：

- 台账层面按 `event_id` 判断是否已成功消费。
- 业务层面仍应按 `biz_key` 或业务主键保证幂等。

### 8.4 卡住的 `PROCESSING` 恢复

如果消费者在“状态已更新为 `PROCESSING`，但还没来得及写 `SUCCESS` / `FAIL` 和 ACK”时异常退出，消息会出现以下特征：

- 台账状态停留在 `PROCESSING`
- `consume_started_at` 已写入
- `processing_deadline_at` 已过期

本期虽然不提供自动补偿，但必须提供人工恢复入口：

- 管理台识别“超时中的 `PROCESSING` 消息”
- 允许运维对超时中的 `PROCESSING` 消息执行手动重试
- 手动重试后仍然复用原 `event_id`

这意味着原 Redis Pending 消息未来若被再次投递，也会因为台账已成功或重新处理而被幂等拦住，符合“至少一次”的设计前提。

## 9. 手动重试设计

### 9.1 允许重试的状态

本期允许对以下状态执行手动重试：

- `FAIL`
- `INIT`
- 超时中的 `PROCESSING`

不允许对以下状态直接重试：

- `SUCCESS`
- `PUBLISHED`
- 未超时的 `PROCESSING`

### 9.2 重试流程

```text
retry(messageId)
  -> 查询台账
  -> 校验状态是否允许重试
  -> 若为超时中的 PROCESSING，则先判定已超时，再允许人工接管
  -> 基于 payload_snapshot 重建原消息
  -> retry_count + 1
  -> 重新写入原 stream_key
  -> 状态更新为 PUBLISHED
  -> 清空 last_error
  -> 清空 processing_deadline_at
```

### 9.3 重试约束

- 手动重试不修改 `event_id`，以便完整追踪同一逻辑消息的生命周期。
- Redis Stream 中会产生新的 record ID，但仍然对应同一逻辑事件。
- `PROCESSING` 只有在超时后才允许重试，避免对正在执行中的消费者造成重复干扰。
- 若未来需要保留每次投递明细，可在后续扩展“投递历史表”；本期不新增该表。

## 10. 查询与管理接口

建议新增消息台账管理接口，路径风格与现有日志接口保持一致：

- `GET /system/logs/mq-messages`
- `POST /system/logs/mq-messages/{id}/retry`

查询条件建议支持：

- `streamKey`
- `eventType`
- `bizKey`
- `traceId`
- `status`
- `source`

返回字段至少包含：

- 基础信息：`id`、`eventId`、`streamKey`、`eventType`、`bizKey`、`traceId`、`source`
- 状态信息：`status`、`retryCount`
- 恢复信息：`processingDeadlineAt`、是否超时
- 异常信息：`lastError`
- 时间信息：`publishedAt`、`consumeStartedAt`、`consumedAt`、`createTime`
- 快照信息：`payloadSnapshot`

权限建议：

- 查询权限：`system:mq-message:query`
- 重试权限：`system:mq-message:retry`

## 11. 前端页面设计

管理台建议在“日志管理”下新增“消息台账”菜单，复用现有日志列表页模式。

页面能力：

1. 列表查询
2. 状态标签展示
3. 展开或弹窗查看 `payload_snapshot` 和 `last_error`
4. 对 `INIT` / `FAIL` / 超时中的 `PROCESSING` 显示“重试”按钮

建议列表列：

- Stream
- 事件类型
- 业务键
- 链路 ID
- 状态
- 重试次数
- 最近错误
- 处理超时时间
- 发布时间
- 消费开始时间
- 消费完成时间
- 操作

## 12. 现有链路改造策略

本期至少改造以下现有链路：

- `RedisOpLogPublisher`：从“直接写 Redis Stream”改为“走统一台账发布器”
- `OpLogStreamConsumer`：从“自己处理状态和 ACK”改为“走统一消费执行器”

改造完成后，操作日志链路将成为第一条受消息台账纳管的 Redis Stream 业务路径，也作为后续其他 Stream 业务的接入样板。

## 13. 兼容性与迁移

### 13.1 对现有业务的影响

- 对业务调用方影响较小，仍然是“构造 `MqEvent` 并发布消息”。
- 对消费者有一定改造，需要将分散的处理逻辑接入统一执行器。

### 13.2 数据迁移

- 新增 `sys_mq_message` 表和菜单数据。
- 旧消息不会自动补录台账。
- 上线后新增发布的消息才受该能力纳管。

### 13.3 失败恢复

若发布时 Redis 异常，运维人员可以通过消息台账页看到 `FAIL` 或 `INIT` 状态消息，并手动执行重试。

## 14. 风险与取舍

### 14.1 无法彻底消除重复

因为本期不做分布式事务，也不做严格唯一消费，所以仍可能出现重复执行。这是“至少一次”语义的可接受代价。

### 14.2 `INIT` 脏状态

在“写入 Redis 成功但更新台账失败”的极小概率场景下，台账可能残留为 `INIT`。本期先接受这一风险，后续可通过补偿任务或对账任务收敛。

### 14.3 超时 `PROCESSING` 的人工判断成本

本期不做自动补偿，因此对“超时中的 `PROCESSING`”是否立即手动重试，仍需要运维结合上下文判断。平台负责暴露状态和提供恢复入口，但不替代业务判断。

### 14.4 消费方幂等是必要前提

平台台账只能降低“漏执行”和“不可见”的风险，不能替代业务本身的幂等控制。

## 15. 验收标准

本期完成后，应满足以下可验证结果：

1. 统一发布器发布消息时，数据库中能生成对应台账。
2. 消息成功写入 Redis 后，台账状态从 `INIT` 变为 `PUBLISHED`。
3. 消费成功后，台账状态变为 `SUCCESS`。
4. 消费失败后，台账状态变为 `FAIL`，并记录错误摘要。
5. 管理台可以查询消息台账，并对失败消息执行手动重试。
6. 现有操作日志 Redis Stream 链路完成接入改造，并作为验收样板通过验证。

## 16. 实现顺序建议

建议按以下顺序实现：

1. 数据库表、实体、Mapper、状态枚举
2. 通用台账服务与统一发布器
3. 通用消费执行器
4. 操作日志链路接入改造
5. 后端查询与手动重试接口
6. 前端消息台账页面
7. 针对操作日志链路做集成验证

## 17. 最终决策摘要

本设计采用以下明确决策：

- 纳管范围：仅限本系统统一 Publisher 发出的消息
- 恢复策略：支持人工手动重试，不做自动补偿
- 一致性语义：至少一次
- 失败处理：消费失败后更新台账为 `FAIL`，并 ACK 原消息
- 复用方式：所有新 Redis Stream 业务统一接入发布器和消费执行器

该方案的重点不是“做成一个重型 MQ 平台”，而是以最小可落地改造，为现有仓库补上“消息可见、状态可查、失败可补救”的通用基础能力。
