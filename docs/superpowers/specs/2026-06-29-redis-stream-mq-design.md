# Redis Stream MQ 设计规格

## 背景

当前项目已经具备 Redis 基础配置、`RedisTemplate`、系统监控菜单、日志台账页面、MyBatis XML 持久化和 Vue 3 + Element Plus 管理端页面。新增 MQ 功能应复用这些既有能力，避免引入 RabbitMQ、Kafka 或额外中间件。

本次目标是在项目内新增基于 Redis Stream 的消息队列能力，并提供消息队列台账，记录每条消息的最终状态和每次执行明细。不同消息类型的处理方式、重试策略和幂等规则不同，因此采用模板方法封装公共流程，由具体消息处理器声明自己的配置和业务处理逻辑。

## 范围

本规格覆盖完整后台闭环：

- Redis Stream 消息发布、消费、ACK、消费者组和死信流。
- 消息处理模板方法。
- 每种消息类型独立 Stream、独立 retry stream、独立 dead-letter stream。
- 每个具体消息处理器自行配置 stream、consumer group、consumer name、重试和幂等策略。
- 主表 + 明细表的消息队列台账。
- 管理端查询接口、前端页面、菜单和按钮权限 SQL。

本规格不包含：

- RabbitMQ、Kafka 等外部 MQ 接入。
- 第一版后台死信重放、暂停消费者、动态修改 handler 配置等运维操作。
- 全局 MQ 配置中心。
- 具体业务消息类型示例。若实现阶段需要示例，可用测试 handler 覆盖模板行为，不默认新增真实业务场景。

## 已确认设计决策

- 功能范围采用完整后台闭环。
- 超过最大重试次数后写入 dead-letter stream，并 ACK 原消息。
- 台账采用主表 + 明细表。
- 每种消息类型使用独立 Redis Stream。
- 幂等由模板提供钩子，具体 handler 自定义规则。
- 重试、stream、group、dead-letter 等参数由具体 handler 通过覆写方法或成员变量声明。
- retry stream 每种消息类型一个，例如 `mq:retry:order_paid`。

## 总体架构

模块划分如下：

- `manzhushaka-ry-framework`：Redis Stream 消费容器、发布器、抽象模板处理器、retry/dead-letter 调度器。
- `manzhushaka-ry-system`：消息台账主表、执行明细表、Mapper、XML、Service。
- `manzhushaka-ry-admin`：消息队列台账 Controller。
- `ui-admin`：消息队列台账 API 和页面。
- `sql/manzhushaka_db_init.sql`：新增表、菜单、按钮权限和必要角色授权。

数据流如下：

```text
业务代码 publish
  -> Redis Stream
  -> 消费容器
  -> 模板处理器
  -> 创建或读取主台账
  -> 写入执行明细
  -> 执行业务处理
  -> 成功 ACK / 失败进入 retry stream / 最终失败进入 dead-letter stream
  -> 后台页面查询台账
```

## Redis Stream 约定

每种消息类型由一个具体 handler 负责，handler 声明自己的 Redis key：

- 原始 Stream：`mq:stream:<message_type>`
- 重试 Stream：`mq:retry:<message_type>`
- 死信 Stream：`mq:dead:<message_type>`

具体命名由 handler 覆写方法返回，以上只是默认推荐格式。

消息字段建议包含：

- `messageType`：消息类型。
- `businessKey`：业务幂等 key。
- `payload`：消息 JSON 内容。
- `retryTimes`：当前已重试次数。
- `originalStreamKey`：原始 Stream。
- `originalMessageId`：原始消息 ID。
- `nextRetryTime`：下次可重试时间，仅 retry stream 使用。

## 模板方法

抽象父类固定执行流程：

1. 解析 Redis Stream 消息。
2. 根据 `streamKey + messageId` 创建或读取主台账。
3. 调用幂等钩子，若业务已处理则写入“已跳过”明细并 ACK。
4. 写入本次执行明细，状态为“执行中”。
5. 调用 `doHandle(message)` 执行业务处理。
6. 成功后更新明细成功、主表成功，并 ACK 原消息。
7. 失败后执行立即重试。
8. 立即重试仍失败时，根据重试次数写入 retry stream 或 dead-letter stream。
9. 进入 retry stream 或 dead-letter stream 后 ACK 原消息，避免原消费者组 pending 堆积。

具体 handler 可覆写：

- `messageType()`
- `streamKey()`
- `consumerGroup()`
- `consumerName()`
- `retryStreamKey()`
- `deadLetterStreamKey()`
- `maxRetryTimes()`
- `immediateRetryTimes()`
- `retryIntervalSeconds()`
- `idempotentKey(message)`
- `isAlreadyProcessed(message)`
- `doHandle(message)`

父类提供保守默认值，但具体业务 handler 应显式声明关键配置。

## 重试与死信

重试策略分两层：

- 消费内立即重试：同一次消费失败后，按 `immediateRetryTimes()` 立即重试。
- 延迟重试：立即重试仍失败且未超过 `maxRetryTimes()` 时，写入 retry stream，携带 `nextRetryTime`。

retry 调度器按 handler 维度扫描对应 retry stream。到达 `nextRetryTime` 的消息重新投递回原始 Stream，并携带递增后的 `retryTimes`。未到时间的消息不执行业务处理。

当重试次数达到或超过 `maxRetryTimes()` 后：

- 写入 dead-letter stream。
- 更新主台账状态为死信或最终失败。
- 写入最后一次失败明细。
- ACK 当前消息。

第一版不提供后台死信重放按钮。后续可单独增加 `monitor:mqlog:replay` 权限和重放接口。

## 幂等

框架默认保证 `streamKey + messageId` 只对应一条主台账记录。

业务幂等由具体 handler 决定：

- `idempotentKey(message)` 返回业务幂等 key。
- `isAlreadyProcessed(message)` 判断业务是否已经处理。
- 如果返回已处理，模板记录“已跳过”明细并 ACK。

这样可以支持不同业务差异：

- 订单支付消息可按订单号幂等。
- 通知类消息可允许重复。
- 库存类消息可按业务流水号幂等。

## 台账数据模型

### 主表 `sys_mq_message_log`

建议字段：

- `message_log_id`：主键。
- `message_type`：消息类型。
- `stream_key`：原始 Stream。
- `message_id`：Redis Stream message id。
- `consumer_group`：消费者组。
- `business_key`：业务幂等 key。
- `payload`：消息内容，需限制长度。
- `status`：执行中、成功、失败、已跳过、死信。
- `retry_times`：已尝试次数。
- `max_retry_times`：最大重试次数。
- `first_consume_time`：首次消费时间。
- `last_consume_time`：最后消费时间。
- `success_time`：成功时间。
- `dead_letter_time`：进入死信时间。
- `last_error_msg`：最后异常信息，需截断。
- `create_time`：创建时间。
- `update_time`：更新时间。

索引建议：

- 唯一索引 `uk_stream_message(stream_key, message_id)`。
- 普通索引 `idx_sys_mq_message_log_type(message_type)`。
- 普通索引 `idx_sys_mq_message_log_status(status)`。
- 普通索引 `idx_sys_mq_message_log_business_key(business_key)`。
- 普通索引 `idx_sys_mq_message_log_ct(create_time)`。

### 明细表 `sys_mq_message_log_detail`

建议字段：

- `detail_id`：主键。
- `message_log_id`：主表 ID。
- `attempt_no`：第几次执行。
- `consumer_name`：消费者名称。
- `status`：执行中、成功、失败、已跳过。
- `start_time`：开始时间。
- `end_time`：结束时间。
- `cost_time`：耗时毫秒。
- `error_msg`：异常信息，需截断。
- `create_time`：创建时间。

索引建议：

- 普通索引 `idx_sys_mq_message_log_detail_log_id(message_log_id)`。
- 普通索引 `idx_sys_mq_message_log_detail_status(status)`。
- 普通索引 `idx_sys_mq_message_log_detail_ct(create_time)`。

## 后台页面与接口

菜单放在系统监控下，名称为“消息队列台账”。

权限：

- 页面权限：`monitor:mqlog:list`
- 详情权限：`monitor:mqlog:query`
- 删除权限：`monitor:mqlog:remove`
- 导出权限：`monitor:mqlog:export`

Controller 要求：

- 列表接口加 `@PreAuthorize("@ss.hasPermi('monitor:mqlog:list')")`。
- 详情接口加 `@PreAuthorize("@ss.hasPermi('monitor:mqlog:query')")`。
- 删除、清空、导出接口加 `@Log` 和对应 `@PreAuthorize`。
- Controller 只依赖 system service，不直接依赖 Mapper。

前端页面：

- 位置：`ui-admin/src/views/monitor/mqLog/index.vue`。
- API：`ui-admin/src/api/monitor/mqLog.js`。
- 筛选项：消息类型、Stream、业务 key、状态、创建时间范围。
- 列表列：编号、消息类型、业务 key、Stream、状态、重试次数、最后消费时间、创建时间、操作。
- 详情：展示 payload、最后错误、主表时间信息和执行明细表。
- 操作：详情、删除、清空、导出。

## 模块边界

- `common` 只放必要常量或枚举，不放 MQ 台账业务实体。
- `framework` 可以依赖 `system` 服务记录台账，符合当前模块依赖方向。
- `system` 不依赖 `admin` 的 DTO/VO。
- `admin` Controller 不依赖持久化实体或 Mapper。
- MyBatis XML、实体、Mapper、SQL 初始化脚本同步维护。

## 错误处理与安全

- 业务异常和基础设施异常都要记录到明细和主表最后错误信息。
- 异常信息、payload 和响应类内容必须截断，避免超长字段和敏感信息落库。
- handler 的 `toString()` 或日志不得输出密码、Token、密钥、验证码等敏感数据。
- Redis Stream 消息 payload 默认不直接写入业务敏感字段；确需写入时由具体业务先脱敏。
- 进入 retry stream 或 dead-letter stream 后必须 ACK 当前消息，避免 pending 长期堆积。
- retry 调度器和消费者线程池使用明确线程名，不使用 `Executors` 快捷工厂。

## 测试与验证

后端验证：

- 模板方法单测覆盖成功、业务异常、立即重试成功、超过重试进入死信、幂等跳过。
- retry 调度器单测覆盖未到时间不投递、到时间重新投递。
- 台账 Service/Mapper 验证主表和明细查询、删除、清空。
- Controller 轻量测试覆盖列表和详情。
- 架构测试确认 admin controller 不依赖 Mapper，不触碰持久化实体边界。
- 最终运行 `mvn test`，或至少运行受影响模块测试。

前端验证：

- 运行 `cd ui-admin && npm run build:prod`。
- 手工验证菜单可见、列表筛选、详情弹窗、删除、清空、导出和按钮权限。

## 实施顺序建议

1. 新增台账表、实体、Mapper、Service 和 SQL 初始化。
2. 新增 Redis Stream 发布器和抽象模板处理器。
3. 新增消费容器注册和 retry 调度器。
4. 新增后台 Controller。
5. 新增前端 API、页面和菜单权限。
6. 补充单元测试、架构测试和前端构建验证。
