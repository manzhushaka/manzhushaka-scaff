# Redis Stream MQ 实现计划

> **面向 AI 代理的工作者：** 必需子技能：使用 superpowers:subagent-driven-development（推荐）或 superpowers:executing-plans 逐任务实现此计划。步骤使用复选框（`- [ ]`）语法来跟踪进度。

**目标：** 在当前若依多模块项目中新增基于 Redis Stream 的 MQ 封装、模板方法重试/死信机制，以及可查询主表和执行明细的消息队列台账。

**架构：** `framework` 承载 Redis Stream 发布、消费、模板和 retry 调度；`system` 承载 MQ 台账领域对象、Mapper、XML 和 Service；`admin` 暴露监控接口；`ui-admin` 提供消息队列台账页面。每种消息类型对应独立 stream、retry stream、dead-letter stream，并由具体 handler 覆写方法声明配置和幂等规则。

**技术栈：** Java 17、Spring Boot 4、Spring Data Redis Streams、MyBatis XML、JUnit 5、Mockito、Vue 3、Element Plus、Vite。

---

## 参考资料

- Spring Data Redis Streams 官方文档：https://docs.spring.io/spring-data/redis/reference/redis/redis-streams.html
- `StreamOperations` API：https://docs.spring.io/spring-data/redis/docs/current/api/org/springframework/data/redis/core/StreamOperations.html
- `StreamMessageListenerContainer` API：https://docs.spring.io/spring-data/redis/docs/current/api/org/springframework/data/redis/stream/StreamMessageListenerContainer.html
- Redis Streams 官方文档：https://redis.io/docs/latest/develop/data-types/streams/

## 文件结构

### System 台账层

- 创建：`manzhushaka-ry-system/src/main/java/com/manzhushaka/system/domain/SysMqMessageLog.java`
  - 台账主表实体，继承 `BaseEntity`，手写 getter、setter、`toString()`。
- 创建：`manzhushaka-ry-system/src/main/java/com/manzhushaka/system/domain/SysMqMessageLogDetail.java`
  - 台账明细表实体，继承 `BaseEntity`，手写 getter、setter、`toString()`。
- 创建：`manzhushaka-ry-system/src/main/java/com/manzhushaka/system/domain/SysMqMessageStatusEnum.java`
  - 主表状态枚举，定义执行中、成功、失败、已跳过、死信。
- 创建：`manzhushaka-ry-system/src/main/java/com/manzhushaka/system/domain/SysMqMessageDetailStatusEnum.java`
  - 明细状态枚举，定义执行中、成功、失败、已跳过。
- 创建：`manzhushaka-ry-system/src/main/java/com/manzhushaka/system/mapper/SysMqMessageLogMapper.java`
  - 主表 Mapper 接口。
- 创建：`manzhushaka-ry-system/src/main/java/com/manzhushaka/system/mapper/SysMqMessageLogDetailMapper.java`
  - 明细表 Mapper 接口。
- 创建：`manzhushaka-ry-system/src/main/resources/mapper/system/SysMqMessageLogMapper.xml`
  - 主表 MyBatis 映射。
- 创建：`manzhushaka-ry-system/src/main/resources/mapper/system/SysMqMessageLogDetailMapper.xml`
  - 明细表 MyBatis 映射。
- 创建：`manzhushaka-ry-system/src/main/java/com/manzhushaka/system/service/ISysMqMessageLogService.java`
  - 台账写入、查询、删除、清空和明细查询服务接口。
- 创建：`manzhushaka-ry-system/src/main/java/com/manzhushaka/system/service/impl/SysMqMessageLogServiceImpl.java`
  - 台账服务实现，处理 `streamKey + messageId` 幂等创建。
- 创建：`manzhushaka-ry-system/src/test/java/com/manzhushaka/system/service/SysMqMessageLogServiceTest.java`
  - Service 层 Mockito 单测。

### Framework MQ 层

- 创建：`manzhushaka-ry-framework/src/main/java/com/manzhushaka/framework/mq/RedisStreamRecord.java`
  - 框架内部的 Redis Stream 记录模型。
- 创建：`manzhushaka-ry-framework/src/main/java/com/manzhushaka/framework/mq/RedisStreamGateway.java`
  - Redis Stream 底层操作接口，隔离 Spring Data Redis API。
- 创建：`manzhushaka-ry-framework/src/main/java/com/manzhushaka/framework/mq/RedisStreamGatewayImpl.java`
  - 基于 `RedisTemplate<Object, Object>` 的 Stream 操作实现。
- 创建：`manzhushaka-ry-framework/src/main/java/com/manzhushaka/framework/mq/RedisStreamMessagePublisher.java`
  - 业务发布入口。
- 创建：`manzhushaka-ry-framework/src/main/java/com/manzhushaka/framework/mq/RedisStreamMessageHandler.java`
  - handler 接口，声明 stream、group、retry、dead-letter 和执行入口。
- 创建：`manzhushaka-ry-framework/src/main/java/com/manzhushaka/framework/mq/AbstractRedisStreamMessageHandler.java`
  - 模板方法父类，统一台账、幂等、重试、死信、ACK。
- 创建：`manzhushaka-ry-framework/src/main/java/com/manzhushaka/framework/mq/RedisStreamMessageHandlerRegistry.java`
  - 收集全部 handler，按 stream 和 messageType 建索引。
- 创建：`manzhushaka-ry-framework/src/main/java/com/manzhushaka/framework/mq/RedisStreamMessageListenerRegistrar.java`
  - 启动 `StreamMessageListenerContainer` 并注册消费者组监听。
- 创建：`manzhushaka-ry-framework/src/main/java/com/manzhushaka/framework/mq/RedisStreamRetryScheduler.java`
  - 扫描 retry stream，到达 `nextRetryTime` 后重新投递。
- 创建：`manzhushaka-ry-framework/src/main/java/com/manzhushaka/framework/config/RedisStreamMqConfig.java`
  - MQ 配置类，创建 listener container 并启用 scheduling。
- 创建：`manzhushaka-ry-framework/src/test/java/com/manzhushaka/framework/mq/RedisStreamMessagePublisherTest.java`
- 创建：`manzhushaka-ry-framework/src/test/java/com/manzhushaka/framework/mq/AbstractRedisStreamMessageHandlerTest.java`
- 创建：`manzhushaka-ry-framework/src/test/java/com/manzhushaka/framework/mq/RedisStreamRetrySchedulerTest.java`
- 创建：`manzhushaka-ry-framework/src/test/java/com/manzhushaka/framework/mq/RedisStreamMessageHandlerRegistryTest.java`

### Admin 接口层

- 创建：`manzhushaka-ry-admin/src/main/java/com/manzhushaka/web/controller/monitor/SysMqMessageLogController.java`
  - 消息队列台账 Controller。
- 创建：`manzhushaka-ry-admin/src/test/java/com/manzhushaka/web/controller/monitor/SysMqMessageLogControllerTest.java`
  - Controller 单测。

### Frontend 页面

- 创建：`ui-admin/src/api/monitor/mqLog.js`
  - 台账列表、详情、明细、删除、清空 API。
- 创建：`ui-admin/src/views/monitor/mqLog/index.vue`
  - 消息队列台账页面。

### SQL

- 修改：`sql/manzhushaka_db_init.sql`
  - 新增菜单、按钮权限、角色默认授权、`sys_mq_message_log` 和 `sys_mq_message_log_detail` 表。

---

## 任务 1：实现 MQ 台账实体、枚举和 Service

**文件：**
- 创建：`manzhushaka-ry-system/src/main/java/com/manzhushaka/system/domain/SysMqMessageLog.java`
- 创建：`manzhushaka-ry-system/src/main/java/com/manzhushaka/system/domain/SysMqMessageLogDetail.java`
- 创建：`manzhushaka-ry-system/src/main/java/com/manzhushaka/system/domain/SysMqMessageStatusEnum.java`
- 创建：`manzhushaka-ry-system/src/main/java/com/manzhushaka/system/domain/SysMqMessageDetailStatusEnum.java`
- 创建：`manzhushaka-ry-system/src/main/java/com/manzhushaka/system/mapper/SysMqMessageLogMapper.java`
- 创建：`manzhushaka-ry-system/src/main/java/com/manzhushaka/system/mapper/SysMqMessageLogDetailMapper.java`
- 创建：`manzhushaka-ry-system/src/main/java/com/manzhushaka/system/service/ISysMqMessageLogService.java`
- 创建：`manzhushaka-ry-system/src/main/java/com/manzhushaka/system/service/impl/SysMqMessageLogServiceImpl.java`
- 创建：`manzhushaka-ry-system/src/test/java/com/manzhushaka/system/service/SysMqMessageLogServiceTest.java`

- [ ] **步骤 1：编写失败的 Service 单测**

创建 `SysMqMessageLogServiceTest`，覆盖幂等创建、主表更新和明细插入。

```java
package com.manzhushaka.system.service;

import java.util.Date;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.test.util.ReflectionTestUtils;
import com.manzhushaka.system.domain.SysMqMessageLog;
import com.manzhushaka.system.domain.SysMqMessageLogDetail;
import com.manzhushaka.system.domain.SysMqMessageStatusEnum;
import com.manzhushaka.system.mapper.SysMqMessageLogDetailMapper;
import com.manzhushaka.system.mapper.SysMqMessageLogMapper;
import com.manzhushaka.system.service.impl.SysMqMessageLogServiceImpl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 消息队列台账服务测试。
 *
 * @author manzhushaka
 * @date 2026-06-29
 */
class SysMqMessageLogServiceTest {

    /**
     * 同一 stream 和 messageId 已存在时，应返回已有主表记录。
     */
    @Test
    void createOrGetMessageLogShouldReturnExistingLog() {
        SysMqMessageLogMapper logMapper = mock(SysMqMessageLogMapper.class);
        SysMqMessageLogDetailMapper detailMapper = mock(SysMqMessageLogDetailMapper.class);
        SysMqMessageLogServiceImpl service = new SysMqMessageLogServiceImpl();
        ReflectionTestUtils.setField(service, "mqMessageLogMapper", logMapper);
        ReflectionTestUtils.setField(service, "mqMessageLogDetailMapper", detailMapper);
        SysMqMessageLog existing = new SysMqMessageLog();
        existing.setMessageLogId(100L);
        when(logMapper.selectMessageLogByStreamAndMessageId("mq:stream:order_paid", "168-0")).thenReturn(existing);

        SysMqMessageLog input = new SysMqMessageLog();
        input.setStreamKey("mq:stream:order_paid");
        input.setMessageId("168-0");

        SysMqMessageLog result = service.createOrGetMessageLog(input);

        assertThat(result.getMessageLogId()).isEqualTo(100L);
    }

    /**
     * 并发插入触发唯一索引冲突时，应重新查询已有记录。
     */
    @Test
    void createOrGetMessageLogShouldRecoverFromDuplicateKey() {
        SysMqMessageLogMapper logMapper = mock(SysMqMessageLogMapper.class);
        SysMqMessageLogDetailMapper detailMapper = mock(SysMqMessageLogDetailMapper.class);
        SysMqMessageLogServiceImpl service = new SysMqMessageLogServiceImpl();
        ReflectionTestUtils.setField(service, "mqMessageLogMapper", logMapper);
        ReflectionTestUtils.setField(service, "mqMessageLogDetailMapper", detailMapper);
        SysMqMessageLog insertedByOtherThread = new SysMqMessageLog();
        insertedByOtherThread.setMessageLogId(101L);
        when(logMapper.selectMessageLogByStreamAndMessageId("mq:stream:order_paid", "169-0"))
                .thenReturn(null)
                .thenReturn(insertedByOtherThread);
        org.mockito.Mockito.doThrow(new DuplicateKeyException("uk_stream_message"))
                .when(logMapper).insertMessageLog(any(SysMqMessageLog.class));

        SysMqMessageLog input = new SysMqMessageLog();
        input.setStreamKey("mq:stream:order_paid");
        input.setMessageId("169-0");

        SysMqMessageLog result = service.createOrGetMessageLog(input);

        assertThat(result.getMessageLogId()).isEqualTo(101L);
    }

    /**
     * 更新主表状态时应委托 Mapper。
     */
    @Test
    void updateMessageLogShouldDelegateMapper() {
        SysMqMessageLogMapper logMapper = mock(SysMqMessageLogMapper.class);
        SysMqMessageLogDetailMapper detailMapper = mock(SysMqMessageLogDetailMapper.class);
        SysMqMessageLogServiceImpl service = new SysMqMessageLogServiceImpl();
        ReflectionTestUtils.setField(service, "mqMessageLogMapper", logMapper);
        ReflectionTestUtils.setField(service, "mqMessageLogDetailMapper", detailMapper);
        SysMqMessageLog log = new SysMqMessageLog();
        log.setMessageLogId(100L);
        log.setStatus(SysMqMessageStatusEnum.SUCCESS.getCode());
        log.setSuccessTime(new Date());

        service.updateMessageLog(log);

        verify(logMapper).updateMessageLog(log);
    }

    /**
     * 新增明细后应返回带主键的明细对象。
     */
    @Test
    void insertMessageLogDetailShouldReturnDetail() {
        SysMqMessageLogMapper logMapper = mock(SysMqMessageLogMapper.class);
        SysMqMessageLogDetailMapper detailMapper = mock(SysMqMessageLogDetailMapper.class);
        SysMqMessageLogServiceImpl service = new SysMqMessageLogServiceImpl();
        ReflectionTestUtils.setField(service, "mqMessageLogMapper", logMapper);
        ReflectionTestUtils.setField(service, "mqMessageLogDetailMapper", detailMapper);
        SysMqMessageLogDetail detail = new SysMqMessageLogDetail();
        detail.setMessageLogId(100L);
        org.mockito.Mockito.doAnswer(invocation -> {
            SysMqMessageLogDetail value = invocation.getArgument(0);
            value.setDetailId(200L);
            return 1;
        }).when(detailMapper).insertMessageLogDetail(detail);

        SysMqMessageLogDetail result = service.insertMessageLogDetail(detail);

        assertThat(result.getDetailId()).isEqualTo(200L);
    }
}
```

- [ ] **步骤 2：运行测试验证失败**

运行：

```bash
mvn -pl manzhushaka-ry-system -Dtest=SysMqMessageLogServiceTest test
```

预期：FAIL，原因是 `SysMqMessageLog`、`SysMqMessageLogServiceImpl`、Mapper 等类型不存在。

- [ ] **步骤 3：实现枚举**

创建 `SysMqMessageStatusEnum`：

```java
package com.manzhushaka.system.domain;

/**
 * 消息队列主台账状态枚举。
 *
 * @author manzhushaka
 * @date 2026-06-29
 */
public enum SysMqMessageStatusEnum {
    PROCESSING("0", "执行中"),
    SUCCESS("1", "成功"),
    FAILED("2", "失败"),
    SKIPPED("3", "已跳过"),
    DEAD_LETTER("4", "死信");

    private final String code;
    private final String info;

    SysMqMessageStatusEnum(String code, String info) {
        this.code = code;
        this.info = info;
    }

    public String getCode() {
        return code;
    }

    public String getInfo() {
        return info;
    }
}
```

创建 `SysMqMessageDetailStatusEnum`：

```java
package com.manzhushaka.system.domain;

/**
 * 消息队列执行明细状态枚举。
 *
 * @author manzhushaka
 * @date 2026-06-29
 */
public enum SysMqMessageDetailStatusEnum {
    PROCESSING("0", "执行中"),
    SUCCESS("1", "成功"),
    FAILED("2", "失败"),
    SKIPPED("3", "已跳过");

    private final String code;
    private final String info;

    SysMqMessageDetailStatusEnum(String code, String info) {
        this.code = code;
        this.info = info;
    }

    public String getCode() {
        return code;
    }

    public String getInfo() {
        return info;
    }
}
```

- [ ] **步骤 4：实现实体**

创建 `SysMqMessageLog`，字段必须完整包含：

```java
private Long messageLogId;
private String messageType;
private String streamKey;
private String messageId;
private String consumerGroup;
private String businessKey;
private String payload;
private String status;
private Integer retryTimes;
private Integer maxRetryTimes;
private Date firstConsumeTime;
private Date lastConsumeTime;
private Date successTime;
private Date deadLetterTime;
private String lastErrorMsg;
```

创建 `SysMqMessageLogDetail`，字段必须完整包含：

```java
private Long detailId;
private Long messageLogId;
private Integer attemptNo;
private String consumerName;
private String status;
private Date startTime;
private Date endTime;
private Long costTime;
private String errorMsg;
```

两个实体都要：

- 继承 `BaseEntity`。
- 为时间字段加 `@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")`。
- 为导出字段加 `@Excel`。
- 手写所有 getter 和 setter。
- `toString()` 使用 `ToStringBuilder`，`payload`、`lastErrorMsg`、`errorMsg` 使用 `StringUtils.substring(value, 0, 256)` 截断。

- [ ] **步骤 5：实现 Mapper 接口**

`SysMqMessageLogMapper`：

```java
package com.manzhushaka.system.mapper;

import java.util.List;
import com.manzhushaka.system.domain.SysMqMessageLog;

/**
 * 消息队列主台账 数据层。
 *
 * @author manzhushaka
 * @date 2026-06-29
 */
public interface SysMqMessageLogMapper {
    int insertMessageLog(SysMqMessageLog messageLog);

    int updateMessageLog(SysMqMessageLog messageLog);

    SysMqMessageLog selectMessageLogById(Long messageLogId);

    SysMqMessageLog selectMessageLogByStreamAndMessageId(String streamKey, String messageId);

    List<SysMqMessageLog> selectMessageLogList(SysMqMessageLog messageLog);

    int deleteMessageLogByIds(Long[] messageLogIds);

    void cleanMessageLog();
}
```

`SysMqMessageLogDetailMapper`：

```java
package com.manzhushaka.system.mapper;

import java.util.List;
import com.manzhushaka.system.domain.SysMqMessageLogDetail;

/**
 * 消息队列执行明细 数据层。
 *
 * @author manzhushaka
 * @date 2026-06-29
 */
public interface SysMqMessageLogDetailMapper {
    int insertMessageLogDetail(SysMqMessageLogDetail detail);

    int updateMessageLogDetail(SysMqMessageLogDetail detail);

    List<SysMqMessageLogDetail> selectDetailListByMessageLogId(Long messageLogId);

    int deleteDetailByMessageLogIds(Long[] messageLogIds);

    void cleanMessageLogDetail();
}
```

- [ ] **步骤 6：实现 Service 接口和实现类**

`ISysMqMessageLogService`：

```java
package com.manzhushaka.system.service;

import java.util.List;
import com.manzhushaka.system.domain.SysMqMessageLog;
import com.manzhushaka.system.domain.SysMqMessageLogDetail;

/**
 * 消息队列台账 服务层。
 *
 * @author manzhushaka
 * @date 2026-06-29
 */
public interface ISysMqMessageLogService {
    SysMqMessageLog createOrGetMessageLog(SysMqMessageLog messageLog);

    void updateMessageLog(SysMqMessageLog messageLog);

    SysMqMessageLogDetail insertMessageLogDetail(SysMqMessageLogDetail detail);

    void updateMessageLogDetail(SysMqMessageLogDetail detail);

    List<SysMqMessageLog> selectMessageLogList(SysMqMessageLog messageLog);

    SysMqMessageLog selectMessageLogById(Long messageLogId);

    List<SysMqMessageLogDetail> selectDetailListByMessageLogId(Long messageLogId);

    int deleteMessageLogByIds(Long[] messageLogIds);

    void cleanMessageLog();
}
```

`SysMqMessageLogServiceImpl` 要点：

```java
@Service
public class SysMqMessageLogServiceImpl implements ISysMqMessageLogService {
    @Autowired
    private SysMqMessageLogMapper mqMessageLogMapper;

    @Autowired
    private SysMqMessageLogDetailMapper mqMessageLogDetailMapper;

    @Override
    public SysMqMessageLog createOrGetMessageLog(SysMqMessageLog messageLog) {
        SysMqMessageLog existing = mqMessageLogMapper.selectMessageLogByStreamAndMessageId(
                messageLog.getStreamKey(), messageLog.getMessageId());
        if (existing != null) {
            return existing;
        }
        try {
            mqMessageLogMapper.insertMessageLog(messageLog);
            return messageLog;
        } catch (DuplicateKeyException ex) {
            return mqMessageLogMapper.selectMessageLogByStreamAndMessageId(
                    messageLog.getStreamKey(), messageLog.getMessageId());
        }
    }
}
```

实现类其余方法按 Mapper 一对一委托。`deleteMessageLogByIds` 先删明细再删主表；`cleanMessageLog` 先清明细再清主表。

- [ ] **步骤 7：运行测试验证通过**

运行：

```bash
mvn -pl manzhushaka-ry-system -Dtest=SysMqMessageLogServiceTest test
```

预期：PASS。

- [ ] **步骤 8：Commit**

```bash
git add manzhushaka-ry-system/src/main/java/com/manzhushaka/system/domain/SysMqMessageLog.java \
  manzhushaka-ry-system/src/main/java/com/manzhushaka/system/domain/SysMqMessageLogDetail.java \
  manzhushaka-ry-system/src/main/java/com/manzhushaka/system/domain/SysMqMessageStatusEnum.java \
  manzhushaka-ry-system/src/main/java/com/manzhushaka/system/domain/SysMqMessageDetailStatusEnum.java \
  manzhushaka-ry-system/src/main/java/com/manzhushaka/system/mapper/SysMqMessageLogMapper.java \
  manzhushaka-ry-system/src/main/java/com/manzhushaka/system/mapper/SysMqMessageLogDetailMapper.java \
  manzhushaka-ry-system/src/main/java/com/manzhushaka/system/service/ISysMqMessageLogService.java \
  manzhushaka-ry-system/src/main/java/com/manzhushaka/system/service/impl/SysMqMessageLogServiceImpl.java \
  manzhushaka-ry-system/src/test/java/com/manzhushaka/system/service/SysMqMessageLogServiceTest.java
git commit -m "feat(mq): add message ledger domain service"
```

---

## 任务 2：实现 MyBatis XML 和初始化 SQL

**文件：**
- 创建：`manzhushaka-ry-system/src/main/resources/mapper/system/SysMqMessageLogMapper.xml`
- 创建：`manzhushaka-ry-system/src/main/resources/mapper/system/SysMqMessageLogDetailMapper.xml`
- 修改：`sql/manzhushaka_db_init.sql`

- [ ] **步骤 1：创建主表 Mapper XML**

创建 `SysMqMessageLogMapper.xml`，必须包含：

- `SysMqMessageLogResult` resultMap。
- `selectMessageLogVo` SQL 片段。
- `insertMessageLog`，使用 `useGeneratedKeys="true"` 和 `keyProperty="messageLogId"`。
- `updateMessageLog`。
- `selectMessageLogById`。
- `selectMessageLogByStreamAndMessageId`。
- `selectMessageLogList`，支持 `messageType`、`streamKey`、`businessKey`、`status`、`params.beginTime`、`params.endTime`。
- `deleteMessageLogByIds`。
- `cleanMessageLog`。

`insertMessageLog` 的字段顺序使用：

```xml
insert into sys_mq_message_log(message_type, stream_key, message_id, consumer_group, business_key, payload,
                               status, retry_times, max_retry_times, first_consume_time, last_consume_time,
                               success_time, dead_letter_time, last_error_msg, create_time, update_time)
values (#{messageType}, #{streamKey}, #{messageId}, #{consumerGroup}, #{businessKey}, #{payload},
        #{status}, #{retryTimes}, #{maxRetryTimes}, #{firstConsumeTime}, #{lastConsumeTime},
        #{successTime}, #{deadLetterTime}, #{lastErrorMsg}, sysdate(), sysdate())
```

- [ ] **步骤 2：创建明细 Mapper XML**

创建 `SysMqMessageLogDetailMapper.xml`，必须包含：

- `SysMqMessageLogDetailResult` resultMap。
- `insertMessageLogDetail`，使用 `useGeneratedKeys="true"` 和 `keyProperty="detailId"`。
- `updateMessageLogDetail`。
- `selectDetailListByMessageLogId`，按 `attempt_no asc, detail_id asc` 排序。
- `deleteDetailByMessageLogIds`。
- `cleanMessageLogDetail`。

`insertMessageLogDetail` 的字段顺序使用：

```xml
insert into sys_mq_message_log_detail(message_log_id, attempt_no, consumer_name, status,
                                      start_time, end_time, cost_time, error_msg, create_time)
values (#{messageLogId}, #{attemptNo}, #{consumerName}, #{status},
        #{startTime}, #{endTime}, #{costTime}, #{errorMsg}, sysdate())
```

- [ ] **步骤 3：修改 SQL 初始化脚本新增菜单**

在 `系统监控 -> 日志中心` 菜单下增加页面菜单，ID 使用当前未占用的 `175`：

```sql
insert into sys_menu values('175', '消息队列台账', '108', '6', 'mqLog', 'monitor/mqLog/index', '', '', 1, 0, 'C', '0', '0', 'monitor:mqlog:list', 'message', 'admin', sysdate(), '', null, '消息队列台账菜单');
```

在监控按钮权限处增加：

```sql
insert into sys_menu values('176', '消息队列台账查询', '175', '1', '', null, '', '', 1, 0, 'F', '0', '0', 'monitor:mqlog:list',   '#', 'admin', sysdate(), '', null, '消息队列台账查询按钮');
insert into sys_menu values('177', '消息队列台账详情', '175', '2', '', null, '', '', 1, 0, 'F', '0', '0', 'monitor:mqlog:query',  '#', 'admin', sysdate(), '', null, '消息队列台账详情按钮');
insert into sys_menu values('178', '消息队列台账删除', '175', '3', '', null, '', '', 1, 0, 'F', '0', '0', 'monitor:mqlog:remove', '#', 'admin', sysdate(), '', null, '消息队列台账删除按钮');
insert into sys_menu values('179', '消息队列台账导出', '175', '4', '', null, '', '', 1, 0, 'F', '0', '0', 'monitor:mqlog:export', '#', 'admin', sysdate(), '', null, '消息队列台账导出按钮');
```

在 `sys_role_menu` 初始化中为普通角色增加 `175`、`176`、`177`、`178`、`179`，位置跟随日志中心已有菜单授权。

- [ ] **步骤 4：修改 SQL 初始化脚本新增表**

在 `sys_slow_sql_log` 后新增表：

```sql
-- ----------------------------
-- 10.2、消息队列主台账
-- ----------------------------
drop table if exists sys_mq_message_log;
create table sys_mq_message_log (
  message_log_id     bigint(20)      not null auto_increment    comment '消息台账主键',
  message_type       varchar(100)    default ''                 comment '消息类型',
  stream_key         varchar(200)    default ''                 comment '原始Stream',
  message_id         varchar(100)    default ''                 comment 'Redis Stream消息ID',
  consumer_group     varchar(100)    default ''                 comment '消费者组',
  business_key       varchar(200)    default ''                 comment '业务幂等键',
  payload            text                                       comment '消息内容',
  status             char(1)         default '0'                comment '状态（0执行中 1成功 2失败 3已跳过 4死信）',
  retry_times        int(4)          default 0                  comment '已尝试次数',
  max_retry_times    int(4)          default 0                  comment '最大重试次数',
  first_consume_time datetime                                   comment '首次消费时间',
  last_consume_time  datetime                                   comment '最后消费时间',
  success_time       datetime                                   comment '成功时间',
  dead_letter_time   datetime                                   comment '进入死信时间',
  last_error_msg     varchar(2000)   default ''                 comment '最后错误信息',
  create_time        datetime                                   comment '创建时间',
  update_time        datetime                                   comment '更新时间',
  primary key (message_log_id),
  unique key uk_stream_message (stream_key, message_id),
  key idx_sys_mq_message_log_type (message_type),
  key idx_sys_mq_message_log_status (status),
  key idx_sys_mq_message_log_business_key (business_key),
  key idx_sys_mq_message_log_ct (create_time)
) engine=innodb auto_increment=100 comment = '消息队列主台账';

-- ----------------------------
-- 10.3、消息队列执行明细
-- ----------------------------
drop table if exists sys_mq_message_log_detail;
create table sys_mq_message_log_detail (
  detail_id      bigint(20)      not null auto_increment    comment '执行明细主键',
  message_log_id bigint(20)      not null                   comment '消息台账主键',
  attempt_no     int(4)          default 0                  comment '执行次数',
  consumer_name  varchar(100)    default ''                 comment '消费者名称',
  status         char(1)         default '0'                comment '状态（0执行中 1成功 2失败 3已跳过）',
  start_time     datetime                                   comment '开始时间',
  end_time       datetime                                   comment '结束时间',
  cost_time      bigint(20)      default 0                  comment '耗时毫秒',
  error_msg      varchar(2000)   default ''                 comment '错误信息',
  create_time    datetime                                   comment '创建时间',
  primary key (detail_id),
  key idx_sys_mq_message_log_detail_log_id (message_log_id),
  key idx_sys_mq_message_log_detail_status (status),
  key idx_sys_mq_message_log_detail_ct (create_time)
) engine=innodb auto_increment=100 comment = '消息队列执行明细';
```

- [ ] **步骤 5：运行后端编译验证 XML 与接口匹配**

运行：

```bash
mvn -pl manzhushaka-ry-system -DskipTests compile
```

预期：BUILD SUCCESS。

- [ ] **步骤 6：运行 SQL 文本检查**

运行：

```bash
rg -n "sys_mq_message_log|monitor:mqlog|消息队列台账" sql/manzhushaka_db_init.sql
```

预期：能看到两个建表段、五条 `sys_menu` 记录和五条 `sys_role_menu` 授权记录。

- [ ] **步骤 7：Commit**

```bash
git add manzhushaka-ry-system/src/main/resources/mapper/system/SysMqMessageLogMapper.xml \
  manzhushaka-ry-system/src/main/resources/mapper/system/SysMqMessageLogDetailMapper.xml \
  sql/manzhushaka_db_init.sql
git commit -m "feat(mq): add message ledger persistence"
```

---

## 任务 3：实现 Redis Stream Gateway 与发布器

**文件：**
- 创建：`manzhushaka-ry-framework/src/main/java/com/manzhushaka/framework/mq/RedisStreamRecord.java`
- 创建：`manzhushaka-ry-framework/src/main/java/com/manzhushaka/framework/mq/RedisStreamGateway.java`
- 创建：`manzhushaka-ry-framework/src/main/java/com/manzhushaka/framework/mq/RedisStreamGatewayImpl.java`
- 创建：`manzhushaka-ry-framework/src/main/java/com/manzhushaka/framework/mq/RedisStreamMessagePublisher.java`
- 创建：`manzhushaka-ry-framework/src/test/java/com/manzhushaka/framework/mq/RedisStreamMessagePublisherTest.java`

- [ ] **步骤 1：编写失败的发布器单测**

```java
package com.manzhushaka.framework.mq;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Redis Stream 消息发布器测试。
 *
 * @author manzhushaka
 * @date 2026-06-29
 */
class RedisStreamMessagePublisherTest {

    /**
     * 发布消息时应写入标准字段。
     */
    @Test
    void publishShouldWriteStandardFields() {
        RedisStreamGateway gateway = mock(RedisStreamGateway.class);
        RedisStreamMessagePublisher publisher = new RedisStreamMessagePublisher(gateway);
        when(gateway.add(eq("mq:stream:order_paid"), org.mockito.ArgumentMatchers.anyMap())).thenReturn("168-0");

        String messageId = publisher.publish("mq:stream:order_paid", "order_paid", "ORDER-100", "{\"orderId\":100}");

        assertThat(messageId).isEqualTo("168-0");
        org.mockito.ArgumentCaptor<java.util.Map<String, String>> captor =
                org.mockito.ArgumentCaptor.forClass(java.util.Map.class);
        verify(gateway).add(eq("mq:stream:order_paid"), captor.capture());
        assertThat(captor.getValue())
                .containsEntry("messageType", "order_paid")
                .containsEntry("businessKey", "ORDER-100")
                .containsEntry("payload", "{\"orderId\":100}")
                .containsEntry("retryTimes", "0");
    }
}
```

- [ ] **步骤 2：运行测试验证失败**

运行：

```bash
mvn -pl manzhushaka-ry-framework -Dtest=RedisStreamMessagePublisherTest test
```

预期：FAIL，原因是 MQ 类不存在。

- [ ] **步骤 3：实现 `RedisStreamRecord`**

```java
package com.manzhushaka.framework.mq;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Redis Stream 记录。
 *
 * @author manzhushaka
 * @date 2026-06-29
 */
public class RedisStreamRecord {
    private final String streamKey;
    private final String messageId;
    private final Map<String, String> body;

    public RedisStreamRecord(String streamKey, String messageId, Map<String, String> body) {
        this.streamKey = streamKey;
        this.messageId = messageId;
        this.body = Collections.unmodifiableMap(new HashMap<>(body));
    }

    public String getStreamKey() {
        return streamKey;
    }

    public String getMessageId() {
        return messageId;
    }

    public Map<String, String> getBody() {
        return body;
    }

    public String getBodyValue(String key) {
        return body.get(key);
    }
}
```

- [ ] **步骤 4：实现 `RedisStreamGateway` 接口**

```java
package com.manzhushaka.framework.mq;

import java.util.List;
import java.util.Map;

/**
 * Redis Stream 底层操作接口。
 *
 * @author manzhushaka
 * @date 2026-06-29
 */
public interface RedisStreamGateway {
    String add(String streamKey, Map<String, String> body);

    void acknowledge(String streamKey, String consumerGroup, String messageId);

    void createGroupIfAbsent(String streamKey, String consumerGroup);

    List<RedisStreamRecord> range(String streamKey, int count);

    void delete(String streamKey, String messageId);
}
```

- [ ] **步骤 5：实现 `RedisStreamGatewayImpl`**

实现要点：

- 注入 `RedisTemplate<Object, Object>`。
- `add` 调用 `redisTemplate.opsForStream().add(streamKey, body)` 并返回 `RecordId.getValue()`。
- `acknowledge` 调用 `redisTemplate.opsForStream().acknowledge(streamKey, consumerGroup, RecordId.of(messageId))`。
- `createGroupIfAbsent` 先执行 `createGroup`，捕获 Redis 返回 BUSYGROUP 的异常时忽略，其余异常继续抛出。
- `range` 读取最早的 retry stream 记录，返回 `RedisStreamRecord` 列表。
- `delete` 删除 retry stream 中已重新投递的记录。

- [ ] **步骤 6：实现 `RedisStreamMessagePublisher`**

```java
package com.manzhushaka.framework.mq;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.stereotype.Component;

/**
 * Redis Stream 消息发布器。
 *
 * @author manzhushaka
 * @date 2026-06-29
 */
@Component
public class RedisStreamMessagePublisher {
    private final RedisStreamGateway gateway;

    public RedisStreamMessagePublisher(RedisStreamGateway gateway) {
        this.gateway = gateway;
    }

    public String publish(String streamKey, String messageType, String businessKey, String payload) {
        Map<String, String> body = new LinkedHashMap<>();
        body.put("messageType", messageType);
        body.put("businessKey", businessKey);
        body.put("payload", payload);
        body.put("retryTimes", "0");
        return gateway.add(streamKey, body);
    }
}
```

- [ ] **步骤 7：运行测试验证通过**

运行：

```bash
mvn -pl manzhushaka-ry-framework -Dtest=RedisStreamMessagePublisherTest test
```

预期：PASS。

- [ ] **步骤 8：Commit**

```bash
git add manzhushaka-ry-framework/src/main/java/com/manzhushaka/framework/mq/RedisStreamRecord.java \
  manzhushaka-ry-framework/src/main/java/com/manzhushaka/framework/mq/RedisStreamGateway.java \
  manzhushaka-ry-framework/src/main/java/com/manzhushaka/framework/mq/RedisStreamGatewayImpl.java \
  manzhushaka-ry-framework/src/main/java/com/manzhushaka/framework/mq/RedisStreamMessagePublisher.java \
  manzhushaka-ry-framework/src/test/java/com/manzhushaka/framework/mq/RedisStreamMessagePublisherTest.java
git commit -m "feat(mq): add redis stream gateway publisher"
```

---

## 任务 4：实现模板方法 handler

**文件：**
- 创建：`manzhushaka-ry-framework/src/main/java/com/manzhushaka/framework/mq/RedisStreamMessageHandler.java`
- 创建：`manzhushaka-ry-framework/src/main/java/com/manzhushaka/framework/mq/AbstractRedisStreamMessageHandler.java`
- 创建：`manzhushaka-ry-framework/src/test/java/com/manzhushaka/framework/mq/AbstractRedisStreamMessageHandlerTest.java`

- [ ] **步骤 1：编写失败的模板方法单测**

创建测试类，至少覆盖成功、幂等跳过、失败进 retry、超过最大次数进 dead-letter。

核心测试方法：

```java
/**
 * 业务处理成功时应更新主表成功、明细成功并 ACK。
 */
@Test
void handleShouldMarkSuccessAndAck() {
    RedisStreamGateway gateway = mock(RedisStreamGateway.class);
    ISysMqMessageLogService logService = mock(ISysMqMessageLogService.class);
    TestHandler handler = new TestHandler(gateway, logService);
    SysMqMessageLog log = new SysMqMessageLog();
    log.setMessageLogId(100L);
    log.setRetryTimes(0);
    when(logService.createOrGetMessageLog(org.mockito.ArgumentMatchers.any(SysMqMessageLog.class))).thenReturn(log);
    SysMqMessageLogDetail detail = new SysMqMessageLogDetail();
    detail.setDetailId(200L);
    when(logService.insertMessageLogDetail(org.mockito.ArgumentMatchers.any(SysMqMessageLogDetail.class))).thenReturn(detail);

    handler.handle(new RedisStreamRecord("mq:stream:test", "168-0", body("test", "BIZ-1", "{}", "0")));

    assertThat(handler.handled).isTrue();
    verify(gateway).acknowledge("mq:stream:test", "mq-group-test", "168-0");
    org.mockito.ArgumentCaptor<SysMqMessageLog> logCaptor =
            org.mockito.ArgumentCaptor.forClass(SysMqMessageLog.class);
    verify(logService, org.mockito.Mockito.atLeastOnce()).updateMessageLog(logCaptor.capture());
    assertThat(logCaptor.getAllValues()).anyMatch(value -> SysMqMessageStatusEnum.SUCCESS.getCode().equals(value.getStatus()));
}
```

测试辅助代码：

```java
private static Map<String, String> body(String messageType, String businessKey, String payload, String retryTimes) {
    Map<String, String> body = new HashMap<>();
    body.put("messageType", messageType);
    body.put("businessKey", businessKey);
    body.put("payload", payload);
    body.put("retryTimes", retryTimes);
    return body;
}

private static class TestHandler extends AbstractRedisStreamMessageHandler {
    private boolean handled;
    private boolean alreadyProcessed;
    private RuntimeException failure;

    TestHandler(RedisStreamGateway gateway, ISysMqMessageLogService logService) {
        super(gateway, logService);
    }

    @Override
    public String messageType() {
        return "test";
    }

    @Override
    public String streamKey() {
        return "mq:stream:test";
    }

    @Override
    public String consumerGroup() {
        return "mq-group-test";
    }

    @Override
    public String consumerName() {
        return "mq-consumer-test";
    }

    @Override
    protected boolean isAlreadyProcessed(RedisStreamRecord record) {
        return alreadyProcessed;
    }

    @Override
    protected void doHandle(RedisStreamRecord record) {
        handled = true;
        if (failure != null) {
            throw failure;
        }
    }
}
```

- [ ] **步骤 2：运行测试验证失败**

运行：

```bash
mvn -pl manzhushaka-ry-framework -Dtest=AbstractRedisStreamMessageHandlerTest test
```

预期：FAIL，原因是 handler 接口和模板父类不存在。

- [ ] **步骤 3：实现 `RedisStreamMessageHandler`**

```java
package com.manzhushaka.framework.mq;

/**
 * Redis Stream 消息处理器。
 *
 * @author manzhushaka
 * @date 2026-06-29
 */
public interface RedisStreamMessageHandler {
    String messageType();

    String streamKey();

    String consumerGroup();

    String consumerName();

    default String retryStreamKey() {
        return "mq:retry:" + messageType();
    }

    default String deadLetterStreamKey() {
        return "mq:dead:" + messageType();
    }

    default int maxRetryTimes() {
        return 3;
    }

    default int immediateRetryTimes() {
        return 0;
    }

    default long retryIntervalSeconds() {
        return 60L;
    }

    void handle(RedisStreamRecord record);
}
```

- [ ] **步骤 4：实现 `AbstractRedisStreamMessageHandler` 固定流程**

实现固定流程：

1. 构造 `SysMqMessageLog`，字段来自 `RedisStreamRecord`。
2. 调用 `createOrGetMessageLog`。
3. 如果 `isAlreadyProcessed(record)` 返回 true，写“已跳过”明细、更新主表“已跳过”、ACK。
4. 写“执行中”明细。
5. 执行 `doHandle(record)`，失败时做消费内立即重试。
6. 成功：更新明细和主表成功、ACK。
7. 失败且 `retryTimes + 1 < maxRetryTimes()`：写 retry stream，更新主表失败、ACK。
8. 失败且达到最大次数：写 dead-letter stream，更新主表死信、ACK。

父类必须提供这些 protected 方法：

```java
protected String idempotentKey(RedisStreamRecord record) {
    return record.getBodyValue("businessKey");
}

protected boolean isAlreadyProcessed(RedisStreamRecord record) {
    return false;
}

protected abstract void doHandle(RedisStreamRecord record);
```

异常信息截断：

```java
private String resolveErrorMessage(Exception ex) {
    return StringUtils.substring(ExceptionUtil.getExceptionMessage(ex), 0, 2000);
}
```

retry body 必须包含：

```java
body.put("messageType", messageType());
body.put("businessKey", idempotentKey(record));
body.put("payload", record.getBodyValue("payload"));
body.put("retryTimes", String.valueOf(nextRetryTimes));
body.put("originalStreamKey", record.getStreamKey());
body.put("originalMessageId", record.getMessageId());
body.put("nextRetryTime", String.valueOf(System.currentTimeMillis() + retryIntervalSeconds() * 1000L));
```

- [ ] **步骤 5：运行模板测试验证通过**

运行：

```bash
mvn -pl manzhushaka-ry-framework -Dtest=AbstractRedisStreamMessageHandlerTest test
```

预期：PASS。

- [ ] **步骤 6：Commit**

```bash
git add manzhushaka-ry-framework/src/main/java/com/manzhushaka/framework/mq/RedisStreamMessageHandler.java \
  manzhushaka-ry-framework/src/main/java/com/manzhushaka/framework/mq/AbstractRedisStreamMessageHandler.java \
  manzhushaka-ry-framework/src/test/java/com/manzhushaka/framework/mq/AbstractRedisStreamMessageHandlerTest.java
git commit -m "feat(mq): add redis stream handler template"
```

---

## 任务 5：实现 handler 注册、监听容器和 retry 调度

**文件：**
- 创建：`manzhushaka-ry-framework/src/main/java/com/manzhushaka/framework/mq/RedisStreamMessageHandlerRegistry.java`
- 创建：`manzhushaka-ry-framework/src/main/java/com/manzhushaka/framework/mq/RedisStreamMessageListenerRegistrar.java`
- 创建：`manzhushaka-ry-framework/src/main/java/com/manzhushaka/framework/mq/RedisStreamRetryScheduler.java`
- 创建：`manzhushaka-ry-framework/src/main/java/com/manzhushaka/framework/config/RedisStreamMqConfig.java`
- 创建：`manzhushaka-ry-framework/src/test/java/com/manzhushaka/framework/mq/RedisStreamMessageHandlerRegistryTest.java`
- 创建：`manzhushaka-ry-framework/src/test/java/com/manzhushaka/framework/mq/RedisStreamRetrySchedulerTest.java`

- [ ] **步骤 1：编写 Registry 失败测试**

```java
@Test
void registryShouldRejectDuplicateMessageType() {
    TestHandler first = new TestHandler("order_paid", "mq:stream:order_paid");
    TestHandler second = new TestHandler("order_paid", "mq:stream:order_paid_v2");

    assertThatThrownBy(() -> new RedisStreamMessageHandlerRegistry(java.util.Arrays.asList(first, second)))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("order_paid");
}

@Test
void registryShouldFindHandlerByStreamKey() {
    TestHandler handler = new TestHandler("order_paid", "mq:stream:order_paid");
    RedisStreamMessageHandlerRegistry registry =
            new RedisStreamMessageHandlerRegistry(java.util.Collections.singletonList(handler));

    assertThat(registry.getByStreamKey("mq:stream:order_paid")).isSameAs(handler);
}
```

- [ ] **步骤 2：编写 RetryScheduler 失败测试**

```java
@Test
void retrySchedulerShouldRequeueDueMessages() {
    RedisStreamGateway gateway = mock(RedisStreamGateway.class);
    RedisStreamMessageHandler handler = mock(RedisStreamMessageHandler.class);
    when(handler.retryStreamKey()).thenReturn("mq:retry:test");
    when(handler.streamKey()).thenReturn("mq:stream:test");
    when(handler.messageType()).thenReturn("test");
    RedisStreamMessageHandlerRegistry registry =
            new RedisStreamMessageHandlerRegistry(java.util.Collections.singletonList(handler));
    Map<String, String> body = new HashMap<>();
    body.put("messageType", "test");
    body.put("businessKey", "BIZ-1");
    body.put("payload", "{}");
    body.put("retryTimes", "1");
    body.put("nextRetryTime", String.valueOf(System.currentTimeMillis() - 1000L));
    when(gateway.range("mq:retry:test", 100)).thenReturn(java.util.Collections.singletonList(
            new RedisStreamRecord("mq:retry:test", "1-0", body)));
    RedisStreamRetryScheduler scheduler = new RedisStreamRetryScheduler(gateway, registry);

    scheduler.scanRetryStreams();

    verify(gateway).add(eq("mq:stream:test"), org.mockito.ArgumentMatchers.anyMap());
    verify(gateway).delete("mq:retry:test", "1-0");
}
```

- [ ] **步骤 3：运行测试验证失败**

运行：

```bash
mvn -pl manzhushaka-ry-framework -Dtest=RedisStreamMessageHandlerRegistryTest,RedisStreamRetrySchedulerTest test
```

预期：FAIL，原因是 registry 和 scheduler 不存在。

- [ ] **步骤 4：实现 Registry**

`RedisStreamMessageHandlerRegistry`：

- 构造器接收 `List<RedisStreamMessageHandler>`。
- 用 `LinkedHashMap` 保存 handler 顺序。
- 如果 `messageType` 或 `streamKey` 重复，抛 `IllegalStateException`。
- 提供：

```java
public List<RedisStreamMessageHandler> listHandlers()
public RedisStreamMessageHandler getByStreamKey(String streamKey)
public RedisStreamMessageHandler getByMessageType(String messageType)
```

- [ ] **步骤 5：实现 RedisStreamMqConfig**

配置类：

```java
package com.manzhushaka.framework.config;

import java.time.Duration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Redis Stream MQ 配置。
 *
 * @author manzhushaka
 * @date 2026-06-29
 */
@Configuration
@EnableScheduling
public class RedisStreamMqConfig {
    @Bean
    public StreamMessageListenerContainer<String, org.springframework.data.redis.connection.stream.MapRecord<String, String, String>>
            redisStreamMessageListenerContainer(RedisConnectionFactory connectionFactory) {
        StreamMessageListenerContainer.StreamMessageListenerContainerOptions<String,
                org.springframework.data.redis.connection.stream.MapRecord<String, String, String>> options =
                StreamMessageListenerContainer.StreamMessageListenerContainerOptions.builder()
                        .pollTimeout(Duration.ofSeconds(2))
                        .build();
        return StreamMessageListenerContainer.create(connectionFactory, options);
    }
}
```

使用以下导入，保持泛型与当前 Spring Data Redis 4 API 一致：

```java
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
```

- [ ] **步骤 6：实现 ListenerRegistrar**

实现 `SmartLifecycle`：

- `start()` 中遍历 registry 的 handler。
- 对每个 handler 调用 `gateway.createGroupIfAbsent(handler.streamKey(), handler.consumerGroup())`。
- 使用 `container.receive(Consumer.from(group, consumerName), StreamOffset.create(streamKey, ReadOffset.lastConsumed()), listener)` 注册监听。
- listener 将 `MapRecord<String, String, String>` 转为 `RedisStreamRecord`，调用 `handler.handle(record)`。
- `stop()` 中取消所有 subscription 并停止 container。
- `isRunning()` 返回内部 running 状态。

- [ ] **步骤 7：实现 RetryScheduler**

`RedisStreamRetryScheduler`：

- 注入 `RedisStreamGateway` 和 `RedisStreamMessageHandlerRegistry`。
- 方法 `scanRetryStreams()` 加 `@Scheduled(fixedDelay = 5000L)`。
- 遍历每个 handler 的 `retryStreamKey()`。
- 调用 `gateway.range(retryStreamKey, 100)`。
- 对每条记录读取 `nextRetryTime`，大于当前时间则跳过。
- 到期后复制 body，移除 `nextRetryTime`，写回 handler 的原始 `streamKey()`。
- 写回成功后 `gateway.delete(retryStreamKey, retryMessageId)`。

- [ ] **步骤 8：运行测试验证通过**

运行：

```bash
mvn -pl manzhushaka-ry-framework -Dtest=RedisStreamMessageHandlerRegistryTest,RedisStreamRetrySchedulerTest test
```

预期：PASS。

- [ ] **步骤 9：运行 Framework 编译验证 Spring Data Redis API**

运行：

```bash
mvn -pl manzhushaka-ry-framework -DskipTests compile
```

预期：BUILD SUCCESS。

- [ ] **步骤 10：Commit**

```bash
git add manzhushaka-ry-framework/src/main/java/com/manzhushaka/framework/mq/RedisStreamMessageHandlerRegistry.java \
  manzhushaka-ry-framework/src/main/java/com/manzhushaka/framework/mq/RedisStreamMessageListenerRegistrar.java \
  manzhushaka-ry-framework/src/main/java/com/manzhushaka/framework/mq/RedisStreamRetryScheduler.java \
  manzhushaka-ry-framework/src/main/java/com/manzhushaka/framework/config/RedisStreamMqConfig.java \
  manzhushaka-ry-framework/src/test/java/com/manzhushaka/framework/mq/RedisStreamMessageHandlerRegistryTest.java \
  manzhushaka-ry-framework/src/test/java/com/manzhushaka/framework/mq/RedisStreamRetrySchedulerTest.java
git commit -m "feat(mq): register redis stream consumers"
```

---

## 任务 6：实现消息队列台账 Controller

**文件：**
- 创建：`manzhushaka-ry-admin/src/main/java/com/manzhushaka/web/controller/monitor/SysMqMessageLogController.java`
- 创建：`manzhushaka-ry-admin/src/test/java/com/manzhushaka/web/controller/monitor/SysMqMessageLogControllerTest.java`

- [ ] **步骤 1：编写失败的 Controller 单测**

```java
package com.manzhushaka.web.controller.monitor;

import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import com.manzhushaka.common.core.domain.AjaxResult;
import com.manzhushaka.system.domain.SysMqMessageLog;
import com.manzhushaka.system.domain.SysMqMessageLogDetail;
import com.manzhushaka.system.service.ISysMqMessageLogService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 消息队列台账控制器测试。
 *
 * @author manzhushaka
 * @date 2026-06-29
 */
class SysMqMessageLogControllerTest {

    /**
     * 详情接口应返回主台账。
     */
    @Test
    void getInfoShouldReturnMessageLog() {
        ISysMqMessageLogService service = mock(ISysMqMessageLogService.class);
        SysMqMessageLogController controller = new SysMqMessageLogController();
        ReflectionTestUtils.setField(controller, "mqMessageLogService", service);
        SysMqMessageLog log = new SysMqMessageLog();
        log.setMessageLogId(100L);
        when(service.selectMessageLogById(100L)).thenReturn(log);

        AjaxResult result = controller.getInfo(100L);

        assertThat(result.get("data")).isSameAs(log);
    }

    /**
     * 明细接口应返回指定主台账下的执行明细。
     */
    @Test
    void detailListShouldReturnDetails() {
        ISysMqMessageLogService service = mock(ISysMqMessageLogService.class);
        SysMqMessageLogController controller = new SysMqMessageLogController();
        ReflectionTestUtils.setField(controller, "mqMessageLogService", service);
        SysMqMessageLogDetail detail = new SysMqMessageLogDetail();
        detail.setMessageLogId(100L);
        when(service.selectDetailListByMessageLogId(100L)).thenReturn(Collections.singletonList(detail));

        AjaxResult result = controller.detailList(100L);

        assertThat(result.get("data")).isEqualTo(Collections.singletonList(detail));
    }

    /**
     * 删除接口应委托服务删除主表和明细。
     */
    @Test
    void removeShouldDeleteLogs() {
        ISysMqMessageLogService service = mock(ISysMqMessageLogService.class);
        SysMqMessageLogController controller = new SysMqMessageLogController();
        ReflectionTestUtils.setField(controller, "mqMessageLogService", service);
        when(service.deleteMessageLogByIds(new Long[] {100L})).thenReturn(1);

        controller.remove(new Long[] {100L});

        verify(service).deleteMessageLogByIds(new Long[] {100L});
    }
}
```

- [ ] **步骤 2：运行测试验证失败**

运行：

```bash
mvn -pl manzhushaka-ry-admin -Dtest=SysMqMessageLogControllerTest test
```

预期：FAIL，原因是 Controller 不存在。

- [ ] **步骤 3：实现 Controller**

创建 `SysMqMessageLogController`：

- 类注解：`@RestController`、`@RequestMapping("/monitor/mqLog")`。
- 继承 `BaseController`。
- 注入 `ISysMqMessageLogService mqMessageLogService`。
- 接口：

```java
@PreAuthorize("@ss.hasPermi('monitor:mqlog:list')")
@GetMapping("/list")
public TableDataInfo list(SysMqMessageLog messageLog)

@PreAuthorize("@ss.hasPermi('monitor:mqlog:query')")
@GetMapping("/{messageLogId}")
public AjaxResult getInfo(@PathVariable Long messageLogId)

@PreAuthorize("@ss.hasPermi('monitor:mqlog:query')")
@GetMapping("/{messageLogId}/details")
public AjaxResult detailList(@PathVariable Long messageLogId)

@Log(title = "消息队列台账", businessType = BusinessType.EXPORT)
@PreAuthorize("@ss.hasPermi('monitor:mqlog:export')")
@PostMapping("/export")
public void export(HttpServletResponse response, SysMqMessageLog messageLog)

@Log(title = "消息队列台账", businessType = BusinessType.DELETE)
@PreAuthorize("@ss.hasPermi('monitor:mqlog:remove')")
@DeleteMapping("/{messageLogIds}")
public AjaxResult remove(@PathVariable Long[] messageLogIds)

@Log(title = "消息队列台账", businessType = BusinessType.CLEAN)
@PreAuthorize("@ss.hasPermi('monitor:mqlog:remove')")
@DeleteMapping("/clean")
public AjaxResult clean()
```

- [ ] **步骤 4：运行测试验证通过**

运行：

```bash
mvn -pl manzhushaka-ry-admin -Dtest=SysMqMessageLogControllerTest test
```

预期：PASS。

- [ ] **步骤 5：Commit**

```bash
git add manzhushaka-ry-admin/src/main/java/com/manzhushaka/web/controller/monitor/SysMqMessageLogController.java \
  manzhushaka-ry-admin/src/test/java/com/manzhushaka/web/controller/monitor/SysMqMessageLogControllerTest.java
git commit -m "feat(mq): add message ledger controller"
```

---

## 任务 7：实现前端 API 和消息队列台账页面

**文件：**
- 创建：`ui-admin/src/api/monitor/mqLog.js`
- 创建：`ui-admin/src/views/monitor/mqLog/index.vue`

- [ ] **步骤 1：创建 API 文件**

`ui-admin/src/api/monitor/mqLog.js`：

```javascript
import request from '@/utils/request'

// 查询消息队列台账列表
export function listMqLog(query) {
  return request({
    url: '/monitor/mqLog/list',
    method: 'get',
    params: query
  })
}

// 查询消息队列台账详细
export function getMqLog(messageLogId) {
  return request({
    url: '/monitor/mqLog/' + messageLogId,
    method: 'get'
  })
}

// 查询消息队列执行明细
export function listMqLogDetails(messageLogId) {
  return request({
    url: '/monitor/mqLog/' + messageLogId + '/details',
    method: 'get'
  })
}

// 删除消息队列台账
export function delMqLog(messageLogId) {
  return request({
    url: '/monitor/mqLog/' + messageLogId,
    method: 'delete'
  })
}

// 清空消息队列台账
export function cleanMqLog() {
  return request({
    url: '/monitor/mqLog/clean',
    method: 'delete'
  })
}
```

- [ ] **步骤 2：创建 Vue 页面**

`ui-admin/src/views/monitor/mqLog/index.vue` 按 `requestLog/index.vue` 风格实现：

- 筛选表单字段：`messageType`、`streamKey`、`businessKey`、`status`、创建时间范围。
- 按钮：删除、清空、导出。
- 表格列：选择、编号、消息类型、业务 key、Stream、状态、重试次数、最后消费时间、创建时间、操作。
- 详情弹窗：主台账描述列表、payload、最后错误、明细表。

状态映射放页面内：

```javascript
const mqStatusMap = {
  '0': { label: '执行中', type: 'info' },
  '1': { label: '成功', type: 'success' },
  '2': { label: '失败', type: 'danger' },
  '3': { label: '已跳过', type: 'warning' },
  '4': { label: '死信', type: 'danger' }
}
```

核心脚本方法：

```javascript
function getList() {
  loading.value = true
  listMqLog(proxy.addDateRange(queryParams.value, dateRange.value)).then(response => {
    mqLogList.value = response.rows
    total.value = response.total
    loading.value = false
  })
}

function handleDetail(row) {
  detailRow.value = row
  listMqLogDetails(row.messageLogId).then(response => {
    detailList.value = response.data || []
    detailVisible.value = true
  })
}

function handleDelete(row) {
  const messageLogIds = row.messageLogId || ids.value
  proxy.$modal.confirm('是否确认删除消息队列台账编号为"' + messageLogIds + '"的数据项?').then(function () {
    return delMqLog(messageLogIds)
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess('删除成功')
  }).catch(() => {})
}

function handleClean() {
  proxy.$modal.confirm('是否确认清空所有消息队列台账数据项?').then(function () {
    return cleanMqLog()
  }).then(() => {
    getList()
    proxy.$modal.msgSuccess('清空成功')
  }).catch(() => {})
}

function handleExport() {
  proxy.download('monitor/mqLog/export', { ...queryParams.value }, `mq_log_${new Date().getTime()}.xlsx`)
}
```

权限：

- 详情按钮：`v-hasPermi="['monitor:mqlog:query']"`
- 删除和清空按钮：`v-hasPermi="['monitor:mqlog:remove']"`
- 导出按钮：`v-hasPermi="['monitor:mqlog:export']"`

- [ ] **步骤 3：运行前端构建验证**

运行：

```bash
cd ui-admin && npm run build:prod
```

预期：构建成功，无 Vue 编译错误。

- [ ] **步骤 4：Commit**

```bash
git add ui-admin/src/api/monitor/mqLog.js ui-admin/src/views/monitor/mqLog/index.vue
git commit -m "feat(mq): add message ledger page"
```

---

## 任务 8：补齐集成验证与架构检查

**文件：**
- 修改：受前面任务影响的测试文件。
- 不新增生产代码，除非测试暴露出编译错误或边界违规。

- [ ] **步骤 1：运行 System 模块测试**

运行：

```bash
mvn -pl manzhushaka-ry-system test
```

预期：BUILD SUCCESS。

- [ ] **步骤 2：运行 Framework 模块测试**

运行：

```bash
mvn -pl manzhushaka-ry-framework test
```

预期：BUILD SUCCESS。

- [ ] **步骤 3：运行 Admin 模块测试**

运行：

```bash
mvn -pl manzhushaka-ry-admin test
```

预期：BUILD SUCCESS，`AdminBoundaryArchTest` 不应出现 controller 依赖 Mapper 或持久化实体违规。

- [ ] **步骤 4：运行全量后端测试**

运行：

```bash
mvn test
```

预期：BUILD SUCCESS。

- [ ] **步骤 5：运行前端生产构建**

运行：

```bash
cd ui-admin && npm run build:prod
```

预期：构建成功。

- [ ] **步骤 6：检查权限闭环**

运行：

```bash
rg -n "monitor:mqlog:(list|query|remove|export)" manzhushaka-ry-admin ui-admin sql/manzhushaka_db_init.sql
```

预期：能同时看到 Controller、Vue 页面和 SQL 菜单按钮权限。

- [ ] **步骤 7：检查敏感输出和超长字段处理**

运行：

```bash
rg -n "payload|lastErrorMsg|errorMsg|toString|substring" manzhushaka-ry-system/src/main/java/com/manzhushaka/system/domain/SysMqMessageLog*.java manzhushaka-ry-framework/src/main/java/com/manzhushaka/framework/mq
```

预期：实体 `toString()` 对 payload/error 字段截断，模板对异常信息截断。

- [ ] **步骤 8：Commit 验证修复**

如果步骤 1-7 暴露出必须修复的问题，修复后提交：

```bash
git add manzhushaka-ry-admin manzhushaka-ry-framework manzhushaka-ry-system ui-admin sql/manzhushaka_db_init.sql
git commit -m "test(mq): verify redis stream message ledger"
```

如果步骤 1-7 均无新增修改，不创建空 commit。

---

## 任务 9：人工验证清单

**文件：**
- 不新增文件。

- [ ] **步骤 1：启动后端**

运行：

```bash
mvn -pl manzhushaka-ry-admin -am spring-boot:run
```

预期：后端启动成功；如果本机没有 Redis 或 MySQL，记录缺失服务名称，不修改代码绕过真实依赖。

- [ ] **步骤 2：启动前端**

运行：

```bash
cd ui-admin && npm run dev
```

预期：Vite 启动成功并给出本地访问地址。

- [ ] **步骤 3：验证页面和菜单**

手工检查：

- 系统监控 -> 日志中心下存在“消息队列台账”。
- 页面列表可打开。
- 搜索、重置、详情弹窗、删除、清空、导出按钮存在。
- 非授权用户不显示对应按钮。

- [ ] **步骤 4：验证 Redis Stream 流程**

在测试环境准备一个继承 `AbstractRedisStreamMessageHandler` 的临时测试 handler，配置：

```java
messageType = "test_mq"
streamKey = "mq:stream:test_mq"
consumerGroup = "mq-group-test-mq"
consumerName = "mq-consumer-test-mq"
retryStreamKey = "mq:retry:test_mq"
deadLetterStreamKey = "mq:dead:test_mq"
maxRetryTimes = 2
immediateRetryTimes = 0
retryIntervalSeconds = 1
```

通过 `RedisStreamMessagePublisher.publish("mq:stream:test_mq", "test_mq", "BIZ-1", "{\"id\":1}")` 发布消息。

检查：

- 成功处理时主表状态为成功，明细有一次成功记录。
- handler 抛异常时消息进入 retry stream。
- retry 达到最大次数后进入 dead-letter stream。
- 原始 stream 消息被 ACK，不产生长期 pending 堆积。

- [ ] **步骤 5：移除临时测试 handler**

如果步骤 4 为手工验证创建了临时生产代码，验证结束后删除临时 handler，并确认：

```bash
rg -n "test_mq|TestMq" manzhushaka-ry-*
```

预期：没有临时生产代码残留。

---

## 规格覆盖自检

- Redis Stream 发布、消费、ACK、消费者组：任务 3、5 覆盖。
- 模板方法：任务 4 覆盖。
- 每种消息独立 stream/retry/dead-letter：任务 4、5 覆盖。
- handler 自行配置：任务 4 的接口默认方法和覆写点覆盖。
- 主表 + 明细表台账：任务 1、2 覆盖。
- 管理端接口、页面、菜单权限：任务 2、6、7 覆盖。
- 错误截断和敏感输出控制：任务 1、4、8 覆盖。
- 验证方式：任务 8、9 覆盖。
