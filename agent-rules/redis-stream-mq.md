# Redis Stream MQ 规则

本文件适用于 Redis Stream 网关、发布器、处理器、监听器、重试调度、消息台账、管理接口和前端监控页面。

## Redis Stream MQ 使用与开发规范

### 架构总览

本仓库使用 Redis Stream 作为异步消息中间件，采用"框架层基础设施 + 系统层台账记录 + 模板方法处理器"的架构。相关文件统一放在以下路径：


| 层                                 | 模块                    | 包路径                                                   |
| ---------------------------------- | ----------------------- | -------------------------------------------------------- |
| 基础设施（网关、发布器、处理框架） | `manzhushaka-framework` | `com.manzhushaka.framework.mq`                           |
| 配置（容器 Bean、调度开关）        | `manzhushaka-framework` | `com.manzhushaka.framework.config`                       |
| 台账（实体、Mapper、Service）      | `manzhushaka-system`    | 现有`domain`、`mapper`、`service` 目录                   |
| 管理接口（Controller）             | `manzhushaka-admin`     | `com.manzhushaka.web.controller.monitor`                 |
| 前端页面                           | `ui-admin`              | `api/monitor/mqLog.ts` + `views/monitor/mqLog/index.vue` |

### 核心概念与术语


| 术语                              | 含义                                                            |
| --------------------------------- | --------------------------------------------------------------- |
| Stream                            | Redis Stream 的 Key，每种消息类型一个独立的 Stream              |
| Consumer Group                    | 每个 Stream 对应的消费者组，使用 Stream 名称作为组名            |
| 消息类型 (`messageType`)          | 每种消息类型对应一个 Stream、一个 Handler、一套重试/死信策略    |
| 台账 (`SysMqMessageLog`)          | 每条消息的处理记录的主表，记录消息元数据、状态和重试信息        |
| 明细 (`SysMqMessageLogDetail`)    | 每次尝试处理的详细记录，包括开始时间、耗时、错误消息            |
| 重试流 (`mq:retry:{messageType}`) | 保存需要重试的消息，`nextRetryTime` 到达后重新投递到原始 Stream |
| 死信流 (`mq:dead:{messageType}`)  | 保存超过最大重试次数的消息，仅作归档不自动处理                  |

### 消息状态流转

```text
消息发布到 Stream → new
    ↓
Handler 接收到消息 → PROCESSING (0)
    ├── 处理成功 → SUCCESS (1) → ACK
    ├── 幂等跳过 → SKIPPED (3) → ACK
    ├── 处理失败（未超最大重试）
    │     └── 写入重试流 `mq:retry:{type}` → RetryScheduler 5s 后重新投递
    │         └── 再次消费 → 循环
    └── 处理失败（超过最大重试）→ DEAD_LETTER (4) → ACK
```

### 框架层基础设施

#### RedisStreamGateway ([`RedisStreamGateway.java`](../manzhushaka-framework/src/main/java/com/manzhushaka/framework/mq/RedisStreamGateway.java))

网关接口封装了对 Redis Stream 的 5 个核心操作：

- `add(streamKey, body)`：向 Stream 追加消息，返回消息 ID
- `acknowledge(streamKey, group, messageId)`：确认消费完成
- `createGroupIfAbsent(streamKey, group)`：创建消费者组，已存在时不抛异常
- `range(streamKey, count)`：反向读取 Stream 中最近的 N 条消息
- `delete(streamKey, messageId)`：从 Stream 删除消息

实现类 `RedisStreamGatewayImpl` 使用 `RedisTemplate<Object, Object>` 操作。**注意**：`range()` 方法在 Spring Data Redis 4 中使用 `Range.unbounded()` 而非 `StreamOffset`，且因泛型兼容性需要使用 raw types。

#### RedisStreamMessagePublisher ([`RedisStreamMessagePublisher.java`](../manzhushaka-framework/src/main/java/com/manzhushaka/framework/mq/RedisStreamMessagePublisher.java))

发布器组件通过 `publish(streamKey, messageType, businessKey, payload)` 构造标准 body，并调用 `gateway.add()` 写入 Stream。发布的 body 固定包含以下字段：

```text
messageType  — 消息类型（用于路由到对应的 Handler）
businessKey  — 业务唯一标识（用于幂等判断）
payload      — 消息内容体
retryTimes   — 重试次数（初始为 "0"，由 RetryScheduler 递增）
```

### 模板方法处理器

#### RedisStreamMessageHandler 接口 ([`RedisStreamMessageHandler.java`](../manzhushaka-framework/src/main/java/com/manzhushaka/framework/mq/RedisStreamMessageHandler.java))

所有消息处理器必须实现此接口，定义以下契约：

- `messageType()`：消息类型，必须全局唯一
- `streamKey()`：对应的 Redis Stream Key，必须全局唯一
- `consumerGroup()`：消费者组名
- `consumerName()`：消费者名称（用于台账明细记录）
- `maxRetryTimes()`：最大重试次数（默认 3），可覆写
- `immediateRetryTimes()`：立即重试次数（默认 0），可覆写
- `retryIntervalSeconds()`：重试间隔秒数（默认 60），可覆写
- `retryStreamKey()`：重试流 Key，默认 `mq:retry:{messageType}`
- `deadLetterStreamKey()`：死信流 Key，默认 `mq:dead:{messageType}`
- `handle(RedisStreamRecord)`：处理器主入口

#### AbstractRedisStreamMessageHandler 抽象基类 ([`AbstractRedisStreamMessageHandler.java`](../manzhushaka-framework/src/main/java/com/manzhushaka/framework/mq/AbstractRedisStreamMessageHandler.java))

提供完整的模板方法实现 `handle()`，调用链如下：

1. 构建 `SysMqMessageLog` 对象
2. 调用 `createOrGetMessageLog` 避免重复插入（处理 `DuplicateKeyException`）
3. 调用 `isAlreadyProcessed()` 判断是否已成功处理过（可覆写）
4. 已处理 → 写 SKIPPED 明细 → ACK
5. 未处理 → 创建 PROCESSING 明细
6. 调用 `doHandle(record)` 执行业务逻辑（子类必须实现）
7. 成功 → 更新明细+主表状态为 SUCCESS → ACK
8. 失败（未超最大重试）→ 写入 retry stream → 更新主表状态为 FAILED → ACK
9. 失败（超最大重试）→ 写入 dead letter stream → 更新主表为 DEAD_LETTER → ACK

**子类需实现的方法**：

```java
@Override
public String messageType() { return "demo"; }
@Override
public String streamKey() { return "mq:stream:demo"; }
@Override
public String consumerGroup() { return "mq:stream:demo"; }
@Override
public String consumerName() { return "demoConsumer"; }

@Override
protected String idempotentKey(RedisStreamRecord record) {
    return record.getBodyValue("businessKey");
}

@Override
protected boolean isAlreadyProcessed(RedisStreamRecord record) {
    // 可选覆写，默认基于 businessKey 查台账状态
    return false;
}

@Override
protected void doHandle(RedisStreamRecord record) {
    String payload = record.getBodyValue("payload");
    // 业务处理逻辑
}
```

### 处理器注册与监听

#### RedisStreamMessageHandlerRegistry ([`RedisStreamMessageHandlerRegistry.java`](../manzhushaka-framework/src/main/java/com/manzhushaka/framework/mq/RedisStreamMessageHandlerRegistry.java))

- 构造时自动收集所有 `RedisStreamMessageHandler` Bean
- 构建 `streamKey → Handler` 和 `messageType → Handler` 两个索引
- 启动时检查 `messageType` 和 `streamKey` 是否全局唯一，重复则抛出 `IllegalStateException`

#### RedisStreamMessageListenerRegistrar ([`RedisStreamMessageListenerRegistrar.java`](../manzhushaka-framework/src/main/java/com/manzhushaka/framework/mq/RedisStreamMessageListenerRegistrar.java))

- 实现 `SmartLifecycle`，在 Spring 容器启动完成后自动注册所有 Listener
- 为每个 Handler 执行 `createGroupIfAbsent()`（幂等）
- 注册 `StreamMessageListenerContainer.receive()` 回调并转交 Handler；ACK 由 `AbstractRedisStreamMessageHandler` 在跳过、成功、转入重试或死信后显式执行
- 应用关闭时自动取消订阅并停止容器

#### RedisStreamRetryScheduler ([`RedisStreamRetryScheduler.java`](../manzhushaka-framework/src/main/java/com/manzhushaka/framework/mq/RedisStreamRetryScheduler.java))

- `@Scheduled(fixedDelay = 5000L)` 每隔 5 秒扫描一次
- 遍历所有 Handler 的 retryStreamKey
- 检查 `nextRetryTime` 是否到达（`System.currentTimeMillis()` 比较）
- 到达 → 复制 body 并移除 `nextRetryTime` → 重新投递到原始 Stream → 从 retry stream 删除
- **注意**：`record.getBody()` 返回 `Collections.unmodifiableMap`，操作前必须 `new HashMap<>(record.getBody())` 复制为可变 Map

### 配置

在 [`RedisStreamMqConfig.java`](../manzhushaka-framework/src/main/java/com/manzhushaka/framework/config/RedisStreamMqConfig.java) 中定义：

- `@Configuration` + `@EnableScheduling` 启用调度
- `StreamMessageListenerContainer<String, MapRecord<String, String, String>>` Bean，`pollTimeout` 为 2 秒

### 台账模型

#### SysMqMessageLog（主台账实体）


| 字段               | 类型    | 说明                 |
| ------------------ | ------- | -------------------- |
| `messageLogId`     | Long    | 主键                 |
| `messageType`      | String  | 消息类型             |
| `streamKey`        | String  | Stream 名称          |
| `messageId`        | String  | Redis Stream 消息 ID |
| `consumerGroup`    | String  | 消费者组             |
| `businessKey`      | String  | 业务 Key（用于幂等） |
| `payload`          | String  | 消息内容             |
| `status`           | String  | 状态码（枚举值）     |
| `retryTimes`       | Integer | 当前重试次数         |
| `maxRetryTimes`    | Integer | 最大重试次数         |
| `firstConsumeTime` | Date    | 首次消费时间         |
| `lastConsumeTime`  | Date    | 最后消费时间         |
| `successTime`      | Date    | 成功时间             |
| `deadLetterTime`   | Date    | 死信时间             |
| `lastErrorMsg`     | String  | 最后错误消息         |

**约束**：`toString()` 必须截断 `payload` 和 `lastErrorMsg` 字段，防止超长内容撑爆日志。

#### SysMqMessageLogDetail（明细实体）


| 字段           | 类型    | 说明         |
| -------------- | ------- | ------------ |
| `detailId`     | Long    | 主键         |
| `messageLogId` | Long    | 关联主台账   |
| `attemptNo`    | Integer | 尝试次数序号 |
| `consumerName` | String  | 消费者名称   |
| `status`       | String  | 状态码       |
| `startTime`    | Date    | 开始时间     |
| `endTime`      | Date    | 结束时间     |
| `costTime`     | Long    | 耗时（毫秒） |
| `errorMsg`     | String  | 错误消息     |

### 开发流程：新增一个消息类型

**步骤 1**：实现 Handler

在任意 Spring Bean 可扫描的模块中创建 Handler 类，继承 `AbstractRedisStreamMessageHandler`：

```java
@Component
public class DemoMessageHandler extends AbstractRedisStreamMessageHandler
{
    public DemoMessageHandler(RedisStreamGateway gateway, ISysMqMessageLogService mqMessageLogService) {
        super(gateway, mqMessageLogService);
    }

    @Override public String messageType() { return "demo"; }
    @Override public String streamKey() { return "mq:stream:demo"; }
    @Override public String consumerGroup() { return "mq:stream:demo"; }
    @Override public String consumerName() { return "demoConsumer"; }

    @Override
    protected String idempotentKey(RedisStreamRecord record) {
        return record.getBodyValue("businessKey");
    }

    @Override
    protected void doHandle(RedisStreamRecord record) {
        // 业务处理
    }
}
```

**步骤 2**：发布消息

```java
@Autowired
private RedisStreamMessagePublisher publisher;

public void sendDemoMessage(String businessKey, String payload) {
    publisher.publish("mq:stream:demo", "demo", businessKey, payload);
}
```

**步骤 3**：按 `agent-rules/api-permission.md` 的“菜单、按钮与接口权限约束”章节规范，新增菜单权限和 SQL 初始化脚本。

### 幂等性设计

- 主台账表在 `stream_key` + `message_id` 上有唯一索引，防止同一条消息被重复插入台账
- `createOrGetMessageLog()` 捕获 `DuplicateKeyException` 后重新查询返回已有记录
- `isAlreadyProcessed()` 钩子允许子类自定义幂等判断，默认基于 `businessKey` 查询台账状态
- 如果消息已成功（SUCCESS）或已死信（DEAD_LETTER），直接写 SKIPPED 明细并 ACK

### 开发约束

- 每个 `messageType` 和 `streamKey` 必须全局唯一，注册阶段会校验，重复则报错
- `AbstractRedisStreamMessageHandler.handle()` 中捕获 `Exception` 属于框架边界场景（第 333 条规约允许），但必须在 catch 块中记录日志并更新台账状态
- `RedisStreamRecord.getBody()` 返回不可修改的 Map，不允许直接调用 `remove()` 或 `put()`
- 重试调度中的 body 操作必须 `new HashMap<>(record.getBody())` 复制后再修改
- `payload`、`lastErrorMsg`、`errorMsg` 等字段在 `toString()` 中必须截断，避免打印超长内容
- Controller 层接口必须同步添加 `@Log` 和 `@PreAuthorize`，前端页面同步添加 `v-hasPermi`
- 新增 MQ 相关 SQL（建表、菜单、权限）必须同步维护 `sql/manzhushaka_db_init.sql`

### 系统支持边界补充

- 当前 Agent 可支持：检查 Redis Stream MQ 的 `@Component` Handler 是否遗漏 `messageType()`/`streamKey()` 实现、检查 Handler 是否被 `RedisStreamMessageHandlerRegistry` 自动收集（通过验证 Spring 注解）、检查台账 `toString()` 字段截断、检查权限字符串闭合。
- 当前无法完全自动保证：Handler 中的 `doHandle()` 业务逻辑正确性、重试间隔秒数是否合理、死信后的人工处理机制、Stream 消息体与台账字段的映射一致性。
