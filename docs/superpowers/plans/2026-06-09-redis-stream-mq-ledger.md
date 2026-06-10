# Redis Stream 通用消息台账实现计划

> **面向 AI 代理的工作者：** 必需子技能：使用 superpowers:subagent-driven-development（推荐）或 superpowers:executing-plans 逐任务实现此计划。步骤使用复选框（`- [ ]`）语法来跟踪进度。

**目标：** 为仓库内所有通过统一 Publisher 发出的 Redis Stream 消息增加通用消息台账、手动重试和管理台查询能力，并先完成操作日志链路接入。

**架构：** 后端在 `common + db + mq + system` 四层完成通用能力拼装：`common` 放共享状态枚举，`db` 放台账实体与 Mapper，`mq` 负责发布落账、消费状态流转和手动重试，`system` 暴露查询与运维接口。前端在 `ui-admin` 增加消息台账页、状态映射与重试交互，同时补齐 mock 菜单一致性。

**技术栈：** Java 17、Spring Boot 3、MyBatis-Plus、Redis Stream、JUnit 5、Mockito、Vue 3、TypeScript、Arco Design Vue、Node test runner

---

## 文件结构与职责

**共享模型与数据库：**

- 创建：`manzhushaka-common/src/main/java/com/manzhushaka/common/enums/MqMessageStatus.java`
  负责定义 `INIT / PUBLISHED / PROCESSING / SUCCESS / FAIL` 五态，以及“是否允许人工重试”的共享规则。
- 创建：`manzhushaka-db/src/main/java/com/manzhushaka/db/system/entity/SysMqMessage.java`
  负责承接消息台账表字段映射。
- 创建：`manzhushaka-db/src/main/java/com/manzhushaka/db/system/mapper/SysMqMessageMapper.java`
  负责消息台账表的 MyBatis-Plus 访问入口。
- 修改：`sql/manzhushaka_init.sql`
  负责新增 `sys_mq_message` 表、索引、日志管理菜单和权限按钮种子数据。

**MQ 通用能力：**

- 修改：`manzhushaka-mq/pom.xml`
  负责引入 `spring-boot-starter-test`，让消息模块可写单测。
- 创建：`manzhushaka-mq/src/main/java/com/manzhushaka/mq/service/MqMessageLedgerService.java`
  负责台账状态流转接口。
- 创建：`manzhushaka-mq/src/main/java/com/manzhushaka/mq/service/MqMessageAdminService.java`
  负责手动重试接口，供 `system` 模块调用。
- 创建：`manzhushaka-mq/src/main/java/com/manzhushaka/mq/service/impl/MqMessageLedgerServiceImpl.java`
  负责 `INIT / PUBLISHED / PROCESSING / SUCCESS / FAIL` 的持久化更新。
- 创建：`manzhushaka-mq/src/main/java/com/manzhushaka/mq/service/impl/MqMessageAdminServiceImpl.java`
  负责按原台账重建事件并重新投递。
- 创建：`manzhushaka-mq/src/main/java/com/manzhushaka/mq/core/LedgeredRedisStreamPublisher.java`
  负责“先落台账，再写 Stream，再回写发布状态”的统一发布闭环。
- 创建：`manzhushaka-mq/src/main/java/com/manzhushaka/mq/consumer/MqMessageConsumeExecutor.java`
  负责消息消费状态流转、超时截止时间和 ACK 策略。
- 修改：`manzhushaka-mq/src/main/java/com/manzhushaka/mq/core/RedisStreamPublisher.java`
  保持为底层 Redis Stream 写入器，不再自己承担台账职责。
- 修改：`manzhushaka-mq/src/main/java/com/manzhushaka/mq/core/RedisOpLogPublisher.java`
  改为调用 `LedgeredRedisStreamPublisher`。
- 修改：`manzhushaka-mq/src/main/java/com/manzhushaka/mq/consumer/OpLogStreamConsumer.java`
  改为通过 `MqMessageConsumeExecutor` 承担状态流转和 ACK。
- 修改：`manzhushaka-mq/src/main/java/com/manzhushaka/mq/properties/MqProperties.java`
  增加 `processingTimeoutSeconds`，供 `PROCESSING` 超时判断使用。

**后端查询与重试接口：**

- 修改：`manzhushaka-system/pom.xml`
  让 `system` 模块依赖 `manzhushaka-mq`，以调用消息重试服务。
- 创建：`manzhushaka-system/src/main/java/com/manzhushaka/system/dto/log/MqMessageQuery.java`
  负责消息台账列表查询入参。
- 创建：`manzhushaka-system/src/main/java/com/manzhushaka/system/vo/log/MqMessageVO.java`
  负责消息台账列表与详情行返回字段。
- 修改：`manzhushaka-system/src/main/java/com/manzhushaka/system/service/LogQueryService.java`
  增加消息台账分页查询方法。
- 修改：`manzhushaka-system/src/main/java/com/manzhushaka/system/service/impl/LogQueryServiceImpl.java`
  负责消息台账查询条件拼装、分页查询和 VO 映射。
- 修改：`manzhushaka-system/src/main/java/com/manzhushaka/system/controller/LogQueryController.java`
  增加 `GET /system/logs/mq-messages` 和 `POST /system/logs/mq-messages/{id}/retry`。

**前端页面与测试：**

- 创建：`ui-admin/src/views/system/mq-messages-support.ts`
  负责列表查询参数拼装、重试按钮可用性判断。
- 创建：`ui-admin/src/views/system/mq-messages.vue`
  负责消息台账列表页、重试交互和 payload / error 展示。
- 修改：`ui-admin/src/router/component-map.ts`
  注册 `system/mq-messages` 页面组件映射。
- 修改：`ui-admin/src/api/system.ts`
  增加消息台账列表与重试接口封装。
- 修改：`ui-admin/src/types/system.ts`
  增加消息台账查询、VO、Row 类型。
- 修改：`ui-admin/src/views/system/shared.ts`
  增加状态选项、格式化和 `mapMqMessageRow`。
- 修改：`ui-admin/src/api/mock.ts`
  补齐 mock 菜单与菜单管理记录，保证 `check:mock-menus` 通过。

**测试文件：**

- 创建：`manzhushaka-mq/src/test/java/com/manzhushaka/mq/core/MqMessageStatusRuleTest.java`
- 创建：`manzhushaka-mq/src/test/java/com/manzhushaka/mq/core/LedgeredRedisStreamPublisherTest.java`
- 创建：`manzhushaka-mq/src/test/java/com/manzhushaka/mq/consumer/MqMessageConsumeExecutorTest.java`
- 创建：`manzhushaka-system/src/test/java/com/manzhushaka/system/service/impl/LogQueryServiceMqMessageTest.java`
- 创建：`manzhushaka-system/src/test/java/com/manzhushaka/system/controller/LogQueryControllerMqMessageEndpointTest.java`
- 创建：`ui-admin/tests/mq-messages-support.test.ts`

---

### 任务 1：落地共享状态规则与台账持久化模型

**文件：**
- 创建：`manzhushaka-common/src/main/java/com/manzhushaka/common/enums/MqMessageStatus.java`
- 修改：`manzhushaka-common/pom.xml`
- 创建：`manzhushaka-common/src/test/java/com/manzhushaka/common/enums/MqMessageStatusRuleTest.java`
- 创建：`manzhushaka-db/src/main/java/com/manzhushaka/db/system/entity/SysMqMessage.java`
- 创建：`manzhushaka-db/src/main/java/com/manzhushaka/db/system/mapper/SysMqMessageMapper.java`

- [ ] **步骤 1：编写失败的状态规则测试**

```java
package com.manzhushaka.common.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MqMessageStatusRuleTest {

    @Test
    void allowsManualRetryShouldMatchLedgerDesign() {
        assertTrue(MqMessageStatus.INIT.allowsManualRetry(false));
        assertTrue(MqMessageStatus.FAIL.allowsManualRetry(false));
        assertFalse(MqMessageStatus.PUBLISHED.allowsManualRetry(false));
        assertFalse(MqMessageStatus.PROCESSING.allowsManualRetry(false));
        assertTrue(MqMessageStatus.PROCESSING.allowsManualRetry(true));
        assertFalse(MqMessageStatus.SUCCESS.allowsManualRetry(false));
        assertFalse(MqMessageStatus.SUCCESS.allowsManualRetry(true));
    }
}
```

- [ ] **步骤 2：运行测试确认失败**

运行：`mvn -pl manzhushaka-common -Dtest=MqMessageStatusRuleTest test`

预期：FAIL，报错 `cannot find symbol: class MqMessageStatus`

- [ ] **步骤 3：编写最少共享实现与台账实体**

```java
package com.manzhushaka.common.enums;

public enum MqMessageStatus {
    INIT,
    PUBLISHED,
    PROCESSING,
    SUCCESS,
    FAIL;

    public boolean allowsManualRetry(boolean processingTimedOut) {
        return this == INIT
            || this == FAIL
            || (this == PROCESSING && processingTimedOut);
    }
}
```

```java
package com.manzhushaka.db.system.entity;

@TableName("sys_mq_message")
public class SysMqMessage {
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private String eventId;
    private String streamKey;
    private String eventType;
    private String bizKey;
    private String traceId;
    private String source;
    private String status;
    private String payloadSnapshot;
    private Integer retryCount;
    private String lastError;
    private String consumerGroup;
    private String consumerName;
    private LocalDateTime processingDeadlineAt;
    private LocalDateTime publishedAt;
    private LocalDateTime consumeStartedAt;
    private LocalDateTime consumedAt;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
```

```java
package com.manzhushaka.db.system.mapper;

public interface SysMqMessageMapper extends BaseMapper<SysMqMessage> {
}
```

```xml
<dependency>
    <groupId>org.junit.jupiter</groupId>
    <artifactId>junit-jupiter</artifactId>
    <scope>test</scope>
</dependency>
```

- [ ] **步骤 4：运行测试验证通过**

运行：`mvn -pl manzhushaka-common -Dtest=MqMessageStatusRuleTest test`

运行：`mvn -pl manzhushaka-mq -am -DskipTests compile`

预期：PASS，状态规则测试通过，且消息模块依赖链编译通过

- [ ] **步骤 5：Commit**

```bash
git add manzhushaka-common/src/main/java/com/manzhushaka/common/enums/MqMessageStatus.java \
  manzhushaka-common/pom.xml \
  manzhushaka-common/src/test/java/com/manzhushaka/common/enums/MqMessageStatusRuleTest.java \
  manzhushaka-db/src/main/java/com/manzhushaka/db/system/entity/SysMqMessage.java \
  manzhushaka-db/src/main/java/com/manzhushaka/db/system/mapper/SysMqMessageMapper.java
git commit -m "feat(MQ): 增加消息台账状态与持久化模型"
```

### 任务 2：实现统一发布器与发布侧台账闭环

**文件：**
- 修改：`manzhushaka-mq/pom.xml`
- 创建：`manzhushaka-mq/src/main/java/com/manzhushaka/mq/service/MqMessageLedgerService.java`
- 创建：`manzhushaka-mq/src/main/java/com/manzhushaka/mq/service/impl/MqMessageLedgerServiceImpl.java`
- 创建：`manzhushaka-mq/src/main/java/com/manzhushaka/mq/core/LedgeredRedisStreamPublisher.java`
- 修改：`manzhushaka-mq/src/main/java/com/manzhushaka/mq/core/RedisStreamPublisher.java`
- 修改：`manzhushaka-mq/src/main/java/com/manzhushaka/mq/core/RedisOpLogPublisher.java`
- 创建：`manzhushaka-mq/src/test/java/com/manzhushaka/mq/core/LedgeredRedisStreamPublisherTest.java`

- [ ] **步骤 1：编写失败的统一发布器测试**

```java
package com.manzhushaka.mq.core;

import com.manzhushaka.common.model.OpLogRecord;
import com.manzhushaka.mq.service.MqMessageLedgerService;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class LedgeredRedisStreamPublisherTest {

    @Test
    void publishShouldCreateLedgerBeforeMarkingPublished() {
        MqMessageLedgerService ledgerService = mock(MqMessageLedgerService.class);
        RedisStreamPublisher rawPublisher = mock(RedisStreamPublisher.class);
        LedgeredRedisStreamPublisher publisher = new LedgeredRedisStreamPublisher(rawPublisher, ledgerService);

        MqEvent<OpLogRecord> event = new MqEvent<>();
        event.setEventId(UUID.randomUUID().toString().replace("-", ""));
        event.setEventType("OP_LOG_CREATED");
        event.setOccurredAt(LocalDateTime.now());
        event.setPayload(new OpLogRecord());

        publisher.publish(MqStreams.OP_LOG, event);

        verify(ledgerService).createInitRecord(MqStreams.OP_LOG, event);
        verify(rawPublisher).publish(MqStreams.OP_LOG, event);
        verify(ledgerService).markPublished(event.getEventId());
    }

    @Test
    void publishShouldMarkFailedWhenRedisWriteThrows() {
        MqMessageLedgerService ledgerService = mock(MqMessageLedgerService.class);
        RedisStreamPublisher rawPublisher = mock(RedisStreamPublisher.class);
        LedgeredRedisStreamPublisher publisher = new LedgeredRedisStreamPublisher(rawPublisher, ledgerService);

        MqEvent<String> event = new MqEvent<>();
        event.setEventId("evt-fail");
        event.setEventType("NOTICE");
        event.setOccurredAt(LocalDateTime.now());
        event.setPayload("payload");

        doThrow(new IllegalStateException("redis down")).when(rawPublisher).publish(anyString(), org.mockito.ArgumentMatchers.same(event));

        try {
            publisher.publish(MqStreams.NOTIFY, event);
        } catch (IllegalStateException ignored) {
        }

        verify(ledgerService).markFailed("evt-fail", "redis down");
    }
}
```

- [ ] **步骤 2：运行测试确认失败**

运行：`mvn -pl manzhushaka-mq -am -Dtest=LedgeredRedisStreamPublisherTest -DfailIfNoTests=false -Dsurefire.failIfNoSpecifiedTests=false test`

预期：FAIL，报错缺少 `LedgeredRedisStreamPublisher`、`MqMessageLedgerService`

- [ ] **步骤 3：实现最小发布闭环**

```java
package com.manzhushaka.mq.service;

public interface MqMessageLedgerService {
    void createInitRecord(String streamKey, MqEvent<?> event);
    boolean isSuccess(String eventId);
    void markPublished(String eventId);
    void markProcessing(String eventId, String consumerGroup, String consumerName, int processingTimeoutSeconds);
    void markSuccess(String eventId);
    void markFailed(String eventId, String errorMessage);
}
```

```java
package com.manzhushaka.mq.core;

public class LedgeredRedisStreamPublisher {
    private final RedisStreamPublisher rawPublisher;
    private final MqMessageLedgerService ledgerService;

    public LedgeredRedisStreamPublisher(RedisStreamPublisher rawPublisher, MqMessageLedgerService ledgerService) {
        this.rawPublisher = rawPublisher;
        this.ledgerService = ledgerService;
    }

    public void publish(String streamKey, MqEvent<?> event) {
        ledgerService.createInitRecord(streamKey, event);
        try {
            rawPublisher.publish(streamKey, event);
            ledgerService.markPublished(event.getEventId());
        } catch (RuntimeException exception) {
            ledgerService.markFailed(event.getEventId(), exception.getMessage());
            throw exception;
        }
    }
}
```

```java
package com.manzhushaka.mq.core;

@Component
public class RedisOpLogPublisher implements OpLogPublisher {
    private final LedgeredRedisStreamPublisher ledgeredPublisher;

    @Override
    public void publish(OpLogRecord record) {
        MqEvent<OpLogRecord> event = new MqEvent<>();
        event.setEventId(UUID.randomUUID().toString().replace("-", ""));
        event.setEventType("OP_LOG_CREATED");
        event.setBizKey(record.getTraceId());
        event.setOccurredAt(LocalDateTime.now());
        event.setTraceId(record.getTraceId());
        event.setSource("manzhushaka-framework");
        event.setRetryCount(0);
        event.setPayload(record);
        ledgeredPublisher.publish(MqStreams.OP_LOG, event);
    }
}
```

- [ ] **步骤 4：运行测试验证通过**

运行：`mvn -pl manzhushaka-mq -am -Dtest=LedgeredRedisStreamPublisherTest -DfailIfNoTests=false -Dsurefire.failIfNoSpecifiedTests=false test`

预期：PASS，`LedgeredRedisStreamPublisherTest` 通过

- [ ] **步骤 5：Commit**

```bash
git add manzhushaka-mq/pom.xml \
  manzhushaka-mq/src/main/java/com/manzhushaka/mq/service/MqMessageLedgerService.java \
  manzhushaka-mq/src/main/java/com/manzhushaka/mq/service/impl/MqMessageLedgerServiceImpl.java \
  manzhushaka-mq/src/main/java/com/manzhushaka/mq/core/LedgeredRedisStreamPublisher.java \
  manzhushaka-mq/src/main/java/com/manzhushaka/mq/core/RedisStreamPublisher.java \
  manzhushaka-mq/src/main/java/com/manzhushaka/mq/core/RedisOpLogPublisher.java \
  manzhushaka-mq/src/test/java/com/manzhushaka/mq/core/LedgeredRedisStreamPublisherTest.java
git commit -m "feat(MQ): 接入发布侧消息台账"
```

### 任务 3：实现通用消费执行器并改造操作日志链路

**文件：**
- 创建：`manzhushaka-mq/src/main/java/com/manzhushaka/mq/consumer/MqMessageConsumeExecutor.java`
- 修改：`manzhushaka-mq/src/main/java/com/manzhushaka/mq/properties/MqProperties.java`
- 修改：`manzhushaka-mq/src/main/java/com/manzhushaka/mq/consumer/OpLogStreamConsumer.java`
- 创建：`manzhushaka-mq/src/test/java/com/manzhushaka/mq/consumer/MqMessageConsumeExecutorTest.java`

- [ ] **步骤 1：编写失败的消费执行器测试**

```java
package com.manzhushaka.mq.consumer;

import com.manzhushaka.common.enums.MqMessageStatus;
import com.manzhushaka.mq.core.MqEvent;
import com.manzhushaka.mq.service.MqMessageLedgerService;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.stream.MapRecord;

import java.time.LocalDateTime;
import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class MqMessageConsumeExecutorTest {

    @Test
    void successfulHandlerShouldMarkProcessingThenSuccessAndAck() {
        MqMessageLedgerService ledgerService = mock(MqMessageLedgerService.class);
        Runnable acknowledge = mock(Runnable.class);
        MqProperties properties = new MqProperties();
        properties.setProcessingTimeoutSeconds(300);
        MqMessageConsumeExecutor executor = new MqMessageConsumeExecutor(ledgerService, properties);

        MapRecord<String, Object, Object> record = MapRecord.create(
            "manzhushaka:stream:oplog",
            Map.of(
                "eventId", "evt-100",
                "eventType", "OP_LOG_CREATED",
                "bizKey", "trace-100",
                "traceId", "trace-100",
                "source", "test",
                "retryCount", "0",
                "occurredAt", String.valueOf(LocalDateTime.now()),
                "payload", "{\"traceId\":\"trace-100\"}"
            )
        );

        executor.consume(record, "manzhushaka-group", "oplog-consumer", event -> { }, acknowledge);

        verify(ledgerService).markProcessing("evt-100", "manzhushaka-group", "oplog-consumer", 300);
        verify(ledgerService).markSuccess("evt-100");
        verify(acknowledge).run();
    }
}
```

- [ ] **步骤 2：运行测试确认失败**

运行：`mvn -pl manzhushaka-mq -am -Dtest=MqMessageConsumeExecutorTest -DfailIfNoTests=false test`

预期：FAIL，报错缺少 `MqMessageConsumeExecutor`、`markProcessing`、`markSuccess`

- [ ] **步骤 3：实现通用消费执行器并接入操作日志消费者**

```java
package com.manzhushaka.mq.consumer;

@Component
public class MqMessageConsumeExecutor {
    private final MqMessageLedgerService ledgerService;
    private final int processingTimeoutSeconds;

    public MqMessageConsumeExecutor(MqMessageLedgerService ledgerService, MqProperties mqProperties) {
        this.ledgerService = ledgerService;
        this.processingTimeoutSeconds = mqProperties.getProcessingTimeoutSeconds();
    }

    public void consume(
        MapRecord<String, Object, Object> record,
        String group,
        String consumer,
        Consumer<MqEvent<Map<String, Object>>> handler,
        Runnable acknowledge
    ) {
        String eventId = String.valueOf(record.getValue().get("eventId"));
        if (ledgerService.isSuccess(eventId)) {
            acknowledge.run();
            return;
        }
        ledgerService.markProcessing(eventId, group, consumer, processingTimeoutSeconds);
        try {
            handler.accept(toEvent(record));
            ledgerService.markSuccess(eventId);
            acknowledge.run();
        } catch (Exception exception) {
            ledgerService.markFailed(eventId, exception.getMessage());
            acknowledge.run();
        }
    }
}
```

```java
package com.manzhushaka.mq.properties;

@ConfigurationProperties(prefix = "manzhushaka.mq")
public class MqProperties {
    private String group = "manzhushaka-group";
    private String consumer = "oplog-consumer";
    private int maxRetry = 3;
    private int processingTimeoutSeconds = 300;
}
```

```java
private void handleRecord(MapRecord<String, Object, Object> record) {
    consumeExecutor.consume(
        record,
        mqProperties.getGroup(),
        mqProperties.getConsumer(),
        event -> {
            OpLogRecord payload = objectMapper.readValue(String.valueOf(event.getPayload().get("payload")), OpLogRecord.class);
            SysOpLog entity = new SysOpLog();
            entity.setTraceId(payload.getTraceId());
            entity.setModule(payload.getModule());
            entity.setAction(payload.getAction());
            entity.setBusinessType(payload.getBusinessType());
            entity.setRequestUri(payload.getRequestUri());
            entity.setRequestMethod(payload.getRequestMethod());
            entity.setOperatorId(payload.getOperatorId());
            entity.setOperatorName(payload.getOperatorName());
            entity.setCostMs(payload.getCostMs());
            entity.setSuccess(payload.getSuccess());
            entity.setErrorMsg(payload.getErrorMsg());
            entity.setRequestSnapshot(payload.getRequestSnapshot());
            entity.setResponseSnapshot(payload.getResponseSnapshot());
            entity.setCreateTime(payload.getCreateTime() == null ? LocalDateTime.now() : payload.getCreateTime());
            sysOpLogMapper.insert(entity);
        },
        () -> acknowledge(record.getId())
    );
}
```

- [ ] **步骤 4：运行测试验证通过**

运行：`mvn -pl manzhushaka-mq -am -Dtest=MqMessageConsumeExecutorTest,LedgeredRedisStreamPublisherTest -DfailIfNoTests=false test`

预期：PASS，两个消息模块测试文件均通过

- [ ] **步骤 5：Commit**

```bash
git add manzhushaka-mq/src/main/java/com/manzhushaka/mq/consumer/MqMessageConsumeExecutor.java \
  manzhushaka-mq/src/main/java/com/manzhushaka/mq/properties/MqProperties.java \
  manzhushaka-mq/src/main/java/com/manzhushaka/mq/consumer/OpLogStreamConsumer.java \
  manzhushaka-mq/src/test/java/com/manzhushaka/mq/consumer/MqMessageConsumeExecutorTest.java
git commit -m "feat(MQ): 接入通用消费执行器"
```

### 任务 4：补齐消息台账查询与手动重试后端接口

**文件：**
- 修改：`manzhushaka-system/pom.xml`
- 创建：`manzhushaka-system/src/main/java/com/manzhushaka/system/dto/log/MqMessageQuery.java`
- 创建：`manzhushaka-system/src/main/java/com/manzhushaka/system/vo/log/MqMessageVO.java`
- 修改：`manzhushaka-system/src/main/java/com/manzhushaka/system/service/LogQueryService.java`
- 修改：`manzhushaka-system/src/main/java/com/manzhushaka/system/service/impl/LogQueryServiceImpl.java`
- 修改：`manzhushaka-system/src/main/java/com/manzhushaka/system/controller/LogQueryController.java`
- 创建：`manzhushaka-mq/src/main/java/com/manzhushaka/mq/service/MqMessageAdminService.java`
- 创建：`manzhushaka-mq/src/main/java/com/manzhushaka/mq/service/impl/MqMessageAdminServiceImpl.java`
- 创建：`manzhushaka-system/src/test/java/com/manzhushaka/system/service/impl/LogQueryServiceMqMessageTest.java`
- 创建：`manzhushaka-system/src/test/java/com/manzhushaka/system/controller/LogQueryControllerMqMessageEndpointTest.java`

- [ ] **步骤 1：编写失败的查询映射与重试委托测试**

```java
package com.manzhushaka.system.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.manzhushaka.db.system.entity.SysMqMessage;
import com.manzhushaka.db.system.mapper.SysMqMessageMapper;
import com.manzhushaka.system.dto.log.MqMessageQuery;
import com.manzhushaka.system.vo.PageResult;
import com.manzhushaka.system.vo.log.MqMessageVO;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class LogQueryServiceMqMessageTest {

    @Test
    void pageMqMessagesShouldExposeRetryMetadata() {
        SysMqMessageMapper mapper = mock(SysMqMessageMapper.class);
        LogQueryServiceImpl service = new LogQueryServiceImpl(mock(com.manzhushaka.db.system.mapper.SysLoginLogMapper.class), mock(com.manzhushaka.db.system.mapper.SysOpLogMapper.class), mapper);

        SysMqMessage entity = new SysMqMessage();
        entity.setId(1L);
        entity.setEventId("evt-1");
        entity.setStreamKey("manzhushaka:stream:oplog");
        entity.setStatus("PROCESSING");
        entity.setProcessingDeadlineAt(LocalDateTime.now().minusMinutes(1));

        Page<SysMqMessage> page = new Page<>(1, 10);
        page.setRecords(List.of(entity));
        page.setTotal(1);
        when(mapper.selectPage(any(), any())).thenReturn(page);

        PageResult<MqMessageVO> result = service.pageMqMessages(new MqMessageQuery());

        assertEquals(1, result.getTotal());
        assertEquals(true, result.getRecords().get(0).getProcessingTimedOut());
    }
}
```

```java
package com.manzhushaka.system.controller;

import com.manzhushaka.mq.service.MqMessageAdminService;
import com.manzhushaka.system.service.LogQueryService;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class LogQueryControllerMqMessageEndpointTest {

    @Test
    void retryMqMessageShouldDelegateToAdminService() {
        LogQueryService logQueryService = mock(LogQueryService.class);
        MqMessageAdminService adminService = mock(MqMessageAdminService.class);
        LogQueryController controller = new LogQueryController(logQueryService, adminService);

        controller.retryMqMessage(100L);

        verify(adminService).retry(100L);
    }
}
```

- [ ] **步骤 2：运行测试确认失败**

运行：`mvn -pl manzhushaka-system,manzhushaka-mq -am -Dtest=LogQueryServiceMqMessageTest,LogQueryControllerMqMessageEndpointTest -DfailIfNoTests=false test`

预期：FAIL，报错缺少 `MqMessageQuery`、`MqMessageVO`、`MqMessageAdminService`

- [ ] **步骤 3：实现查询、超时标记和重试接口**

```java
package com.manzhushaka.system.dto.log;

public class MqMessageQuery extends PageQuery {
    private String streamKey;
    private String eventType;
    private String bizKey;
    private String traceId;
    private String status;
    private String source;
}
```

```java
package com.manzhushaka.system.vo.log;

public class MqMessageVO {
    private Long id;
    private String eventId;
    private String streamKey;
    private String eventType;
    private String bizKey;
    private String traceId;
    private String source;
    private String status;
    private Integer retryCount;
    private String lastError;
    private LocalDateTime processingDeadlineAt;
    private Boolean processingTimedOut;
    private LocalDateTime publishedAt;
    private LocalDateTime consumeStartedAt;
    private LocalDateTime consumedAt;
    private String payloadSnapshot;
    private LocalDateTime createTime;
}
```

```java
@GetMapping("/mq-messages")
public ApiResponse<PageResult<MqMessageVO>> pageMqMessages(MqMessageQuery query) {
    return ApiResponse.success(logQueryService.pageMqMessages(query));
}

@PostMapping("/mq-messages/{id}/retry")
public ApiResponse<Void> retryMqMessage(@PathVariable Long id) {
    mqMessageAdminService.retry(id);
    return ApiResponse.success();
}
```

```java
LambdaQueryWrapper<SysMqMessage> wrapper = new LambdaQueryWrapper<SysMqMessage>()
    .like(StringUtils.hasText(query.getStreamKey()), SysMqMessage::getStreamKey, query.getStreamKey())
    .like(StringUtils.hasText(query.getEventType()), SysMqMessage::getEventType, query.getEventType())
    .like(StringUtils.hasText(query.getBizKey()), SysMqMessage::getBizKey, query.getBizKey())
    .like(StringUtils.hasText(query.getTraceId()), SysMqMessage::getTraceId, query.getTraceId())
    .eq(StringUtils.hasText(query.getStatus()), SysMqMessage::getStatus, query.getStatus())
    .eq(StringUtils.hasText(query.getSource()), SysMqMessage::getSource, query.getSource())
    .orderByDesc(SysMqMessage::getCreateTime, SysMqMessage::getId);
```

```java
public interface MqMessageAdminService {
    void retry(Long id);
}
```

```java
@Service
public class MqMessageAdminServiceImpl implements MqMessageAdminService {
    private final SysMqMessageMapper mqMessageMapper;
    private final RedisStreamPublisher rawPublisher;
    private final MqMessageLedgerService ledgerService;
    private final ObjectMapper objectMapper;

    @Override
    public void retry(Long id) {
        SysMqMessage entity = Optional.ofNullable(mqMessageMapper.selectById(id))
            .orElseThrow(() -> new BizException("消息台账不存在"));
        boolean timedOut = entity.getProcessingDeadlineAt() != null
            && entity.getProcessingDeadlineAt().isBefore(LocalDateTime.now());
        if (!MqMessageStatus.valueOf(entity.getStatus()).allowsManualRetry(timedOut)) {
            throw new BizException("当前状态不允许重试");
        }

        MqEvent<Object> event = new MqEvent<>();
        event.setEventId(entity.getEventId());
        event.setEventType(entity.getEventType());
        event.setBizKey(entity.getBizKey());
        event.setTraceId(entity.getTraceId());
        event.setSource(entity.getSource());
        event.setRetryCount(entity.getRetryCount() + 1);
        event.setOccurredAt(LocalDateTime.now());
        event.setPayload(objectMapper.readValue(entity.getPayloadSnapshot(), Object.class));

        rawPublisher.publish(entity.getStreamKey(), event);
        ledgerService.markPublished(entity.getEventId());
    }
}
```

- [ ] **步骤 4：运行测试验证通过**

运行：`mvn -pl manzhushaka-system,manzhushaka-mq -am -Dtest=LogQueryServiceMqMessageTest,LogQueryControllerMqMessageEndpointTest -DfailIfNoTests=false test`

预期：PASS，系统侧查询与重试测试通过

- [ ] **步骤 5：Commit**

```bash
git add manzhushaka-system/pom.xml \
  manzhushaka-system/src/main/java/com/manzhushaka/system/dto/log/MqMessageQuery.java \
  manzhushaka-system/src/main/java/com/manzhushaka/system/vo/log/MqMessageVO.java \
  manzhushaka-system/src/main/java/com/manzhushaka/system/service/LogQueryService.java \
  manzhushaka-system/src/main/java/com/manzhushaka/system/service/impl/LogQueryServiceImpl.java \
  manzhushaka-system/src/main/java/com/manzhushaka/system/controller/LogQueryController.java \
  manzhushaka-mq/src/main/java/com/manzhushaka/mq/service/MqMessageAdminService.java \
  manzhushaka-mq/src/main/java/com/manzhushaka/mq/service/impl/MqMessageAdminServiceImpl.java \
  manzhushaka-system/src/test/java/com/manzhushaka/system/service/impl/LogQueryServiceMqMessageTest.java \
  manzhushaka-system/src/test/java/com/manzhushaka/system/controller/LogQueryControllerMqMessageEndpointTest.java
git commit -m "feat(日志): 增加消息台账查询与重试接口"
```

### 任务 5：实现管理台消息台账页面与 mock 菜单一致性

**文件：**
- 创建：`ui-admin/src/views/system/mq-messages-support.ts`
- 创建：`ui-admin/src/views/system/mq-messages.vue`
- 修改：`ui-admin/src/router/component-map.ts`
- 修改：`ui-admin/src/api/system.ts`
- 修改：`ui-admin/src/types/system.ts`
- 修改：`ui-admin/src/views/system/shared.ts`
- 修改：`ui-admin/src/api/mock.ts`
- 创建：`ui-admin/tests/mq-messages-support.test.ts`

- [ ] **步骤 1：编写失败的前端支持函数测试**

```ts
import test from 'node:test';
import assert from 'node:assert/strict';
import { buildMqMessageListQuery, canRetryMqMessage } from '../src/views/system/mq-messages-support.ts';

test('builds keyword into all supported mq message filters', () => {
  assert.deepEqual(
    buildMqMessageListQuery({
      pageNum: 1,
      pageSize: 10,
      keyword: 'trace-100',
      status: 'FAIL',
      source: 'manzhushaka-framework',
    }),
    {
      pageNum: 1,
      pageSize: 10,
      streamKey: 'trace-100',
      eventType: 'trace-100',
      bizKey: 'trace-100',
      traceId: 'trace-100',
      status: 'FAIL',
      source: 'manzhushaka-framework',
    },
  );
});

test('allows retry for failed init and timed out processing rows only', () => {
  assert.equal(canRetryMqMessage({ statusValue: 'FAIL', processingTimedOut: false }), true);
  assert.equal(canRetryMqMessage({ statusValue: 'INIT', processingTimedOut: false }), true);
  assert.equal(canRetryMqMessage({ statusValue: 'PROCESSING', processingTimedOut: true }), true);
  assert.equal(canRetryMqMessage({ statusValue: 'PROCESSING', processingTimedOut: false }), false);
  assert.equal(canRetryMqMessage({ statusValue: 'SUCCESS', processingTimedOut: true }), false);
});
```

- [ ] **步骤 2：运行测试确认失败**

运行：`cd ui-admin && node --experimental-strip-types --test tests/mq-messages-support.test.ts`

预期：FAIL，报错 `Cannot find module '../src/views/system/mq-messages-support.ts'`

- [ ] **步骤 3：实现页面支持函数、类型、API 与视图**

```ts
export function buildMqMessageListQuery(input: {
  pageNum: number;
  pageSize: number;
  keyword: string;
  status?: string;
  source?: string;
}) {
  const keyword = input.keyword.trim();
  return {
    pageNum: input.pageNum,
    pageSize: input.pageSize,
    streamKey: keyword || undefined,
    eventType: keyword || undefined,
    bizKey: keyword || undefined,
    traceId: keyword || undefined,
    status: input.status,
    source: input.source,
  };
}

export function canRetryMqMessage(input: {
  statusValue: string;
  processingTimedOut: boolean;
}) {
  return input.statusValue === 'INIT'
    || input.statusValue === 'FAIL'
    || (input.statusValue === 'PROCESSING' && input.processingTimedOut);
}
```

```ts
listMqMessages(params: MqMessageQuery) {
  return get<PageResult<MqMessageVO>>('/system/logs/mq-messages', params);
},
retryMqMessage(id: number) {
  return post<void>(`/system/logs/mq-messages/${id}/retry`);
},
```

```ts
export interface MqMessageVO {
  id: number;
  eventId: string;
  streamKey: string | null;
  eventType: string | null;
  bizKey: string | null;
  traceId: string | null;
  source: string | null;
  status: string;
  retryCount: number;
  lastError: string | null;
  processingDeadlineAt: string | null;
  processingTimedOut: boolean;
  publishedAt: string | null;
  consumeStartedAt: string | null;
  consumedAt: string | null;
  payloadSnapshot: string | null;
  createTime: string | null;
}
```

```vue
<a-button
  v-if="canRetryMqMessage(record)"
  size="mini"
  type="primary"
  @click="handleRetry(record.id)"
>
  重试
</a-button>
```

```ts
export const componentMap = {
  'dashboard/index': DashboardView,
  'system/users': UsersView,
  'system/roles': RolesView,
  'system/depts': DeptsView,
  'system/menus': MenusView,
  'system/dicts': DictsView,
  'system/params': ParamsView,
  'system/platform-config': PlatformConfigView,
  'system/login-logs': LoginLogsView,
  'system/op-logs': OpLogsView,
  'system/mq-messages': MqMessagesView,
  'system/export-tasks': ExportTasksView,
  'system/import-tasks': ImportTasksView,
} as const;
```

```vue
<a-button size="mini" @click="openDetail(record)">
  详情
</a-button>

<a-drawer v-model:visible="detailVisible" width="720" title="消息详情">
  <a-descriptions :column="1" bordered>
    <a-descriptions-item label="事件 ID">{{ currentRow?.eventId }}</a-descriptions-item>
    <a-descriptions-item label="最近错误">{{ currentRow?.lastError || '--' }}</a-descriptions-item>
    <a-descriptions-item label="Payload">
      <pre class="detail-json">{{ currentRow?.payloadSnapshot || '--' }}</pre>
    </a-descriptions-item>
  </a-descriptions>
</a-drawer>
```

- [ ] **步骤 4：运行前端测试与构建验证通过**

运行：`cd ui-admin && node --experimental-strip-types --test tests/mq-messages-support.test.ts`

预期：PASS，`mq-messages-support.test.ts` 通过

运行：`cd ui-admin && pnpm check:mock-menus`

预期：PASS，输出 `mock menu consistency checks passed`

运行：`cd ui-admin && pnpm build`

预期：PASS，`vue-tsc` 与 `vite build` 完成

- [ ] **步骤 5：Commit**

```bash
git add ui-admin/src/views/system/mq-messages-support.ts \
  ui-admin/src/views/system/mq-messages.vue \
  ui-admin/src/router/component-map.ts \
  ui-admin/src/api/system.ts \
  ui-admin/src/types/system.ts \
  ui-admin/src/views/system/shared.ts \
  ui-admin/src/api/mock.ts \
  ui-admin/tests/mq-messages-support.test.ts
git commit -m "feat(管理台): 增加消息台账页面"
```

### 任务 6：补齐 SQL 种子数据并做端到端验证

**文件：**
- 修改：`sql/manzhushaka_init.sql`
- 测试：`manzhushaka-mq` 与 `manzhushaka-system` 模块测试
- 测试：`ui-admin` 构建与菜单一致性检查

- [ ] **步骤 1：补齐消息台账表与菜单 SQL**

```sql
CREATE TABLE `sys_mq_message` (
  `id` bigint NOT NULL COMMENT '主键',
  `event_id` varchar(64) NOT NULL COMMENT '逻辑事件 ID',
  `stream_key` varchar(128) NOT NULL COMMENT 'Redis Stream Key',
  `event_type` varchar(64) NOT NULL COMMENT '事件类型',
  `biz_key` varchar(128) DEFAULT NULL COMMENT '业务键',
  `trace_id` varchar(64) DEFAULT NULL COMMENT '链路追踪 ID',
  `source` varchar(64) DEFAULT NULL COMMENT '事件来源',
  `status` varchar(32) NOT NULL COMMENT '台账状态',
  `payload_snapshot` longtext COMMENT '消息体快照',
  `retry_count` int NOT NULL DEFAULT 0 COMMENT '重试次数',
  `last_error` varchar(1000) DEFAULT NULL COMMENT '最近一次错误摘要',
  `consumer_group` varchar(64) DEFAULT NULL COMMENT '最近一次消费组',
  `consumer_name` varchar(64) DEFAULT NULL COMMENT '最近一次消费者',
  `processing_deadline_at` datetime DEFAULT NULL COMMENT '处理超时时间',
  `published_at` datetime DEFAULT NULL COMMENT '发布时间',
  `consume_started_at` datetime DEFAULT NULL COMMENT '开始消费时间',
  `consumed_at` datetime DEFAULT NULL COMMENT '消费完成时间',
  `create_by` varchar(64) DEFAULT NULL COMMENT '创建人',
  `create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_by` varchar(64) DEFAULT NULL COMMENT '更新人',
  `update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`),
  UNIQUE KEY `uk_sys_mq_message_event_id` (`event_id`),
  KEY `idx_sys_mq_message_stream_key` (`stream_key`),
  KEY `idx_sys_mq_message_status` (`status`),
  KEY `idx_sys_mq_message_biz_key` (`biz_key`),
  KEY `idx_sys_mq_message_trace_id` (`trace_id`),
  KEY `idx_sys_mq_message_create_time` (`create_time`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='消息台账表';
```

```sql
INSERT INTO `sys_menu` (`id`, `parent_id`, `menu_name`, `menu_type`, `route_name`, `route_path`, `component`, `perms`, `icon`, `sort`, `visible`, `status`, `keep_alive`, `always_show`, `remark`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES
(273, 270, '消息台账', 'MENU', 'SystemMqMessages', 'mq-messages', 'system/mq-messages', 'system:mq-message:query', 'icon-list', 3, 1, 1, 0, 0, '消息台账菜单', 'system', NOW(), 'system', NOW()),
(274, 273, '消息查询', 'BUTTON', NULL, NULL, NULL, 'system:mq-message:query', NULL, 1, 1, 1, 0, 0, '消息查询按钮', 'system', NOW(), 'system', NOW()),
(275, 273, '消息重试', 'BUTTON', NULL, NULL, NULL, 'system:mq-message:retry', NULL, 2, 1, 1, 0, 0, '消息重试按钮', 'system', NOW(), 'system', NOW());
```

```sql
INSERT INTO `sys_role_menu` (`id`, `role_id`, `menu_id`, `create_by`, `create_time`, `update_by`, `update_time`) VALUES
(1027, 100, 273, 'system', NOW(), 'system', NOW()),
(1028, 100, 274, 'system', NOW(), 'system', NOW()),
(1029, 100, 275, 'system', NOW(), 'system', NOW());
```

- [ ] **步骤 2：运行后端测试验证模块通过**

运行：`mvn -pl manzhushaka-system,manzhushaka-mq -am test`

预期：PASS，消息模块与系统模块测试全部通过

- [ ] **步骤 3：运行前端验证命令**

运行：`cd ui-admin && pnpm check:mock-menus && pnpm test:unit && pnpm build`

预期：PASS，菜单一致性、单测和构建全部通过

- [ ] **步骤 4：检查改动范围与最终差异**

运行：`git status --short`

预期：只包含本计划列出的 MQ 台账相关文件改动

运行：`git diff --stat`

预期：diff 主要集中在 `common/db/mq/system/sql/ui-admin` 这几个直接受影响的路径

- [ ] **步骤 5：Commit**

```bash
git add sql/manzhushaka_init.sql
git commit -m "chore(SQL): 补齐消息台账初始化数据"
```
