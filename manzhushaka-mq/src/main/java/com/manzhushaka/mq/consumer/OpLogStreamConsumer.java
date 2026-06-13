package com.manzhushaka.mq.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.manzhushaka.common.model.OpLogRecord;
import com.manzhushaka.db.system.entity.SysOpLog;
import com.manzhushaka.db.system.mapper.SysOpLogMapper;
import com.manzhushaka.mq.core.MqStreams;
import com.manzhushaka.mq.properties.MqProperties;
import com.manzhushaka.mq.service.MqMessageLedgerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.connection.stream.StreamReadOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * 定义 OpLogStreamConsumer。
 */
@Component
public class OpLogStreamConsumer {
    /**
     * 返回 logger。
     *
     * @param OpLogStreamConsumer.class OpLogStreamConsumer.class 参数
     * @return 字段值
     */
    private static final Logger log = LoggerFactory.getLogger(OpLogStreamConsumer.class);
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final SysOpLogMapper sysOpLogMapper;
    private final MqMessageConsumeExecutor messageConsumeExecutor;
    private final MqMessageLedgerService ledgerService;
    private final MqProperties mqProperties;

    public OpLogStreamConsumer(
        StringRedisTemplate redisTemplate,
        ObjectMapper mqObjectMapper,
        SysOpLogMapper sysOpLogMapper,
        MqMessageConsumeExecutor messageConsumeExecutor,
        MqMessageLedgerService ledgerService,
        MqProperties mqProperties
    ) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = mqObjectMapper;
        this.sysOpLogMapper = sysOpLogMapper;
        this.messageConsumeExecutor = messageConsumeExecutor;
        this.ledgerService = ledgerService;
        this.mqProperties = mqProperties;
    }

    /**
     * 定义 StreamBootstrap。
     */
    @Component
    class StreamBootstrap implements ApplicationRunner {
        /**
         * 执行 run 操作。
         *
         * @param args args 参数
         */
        @Override
        public void run(org.springframework.boot.ApplicationArguments args) {
            try {
                redisTemplate.opsForStream().createGroup(MqStreams.OP_LOG, ReadOffset.latest(), mqProperties.getGroup());
            } catch (Exception exception) {
                log.debug("初始化 Redis Stream 消费组时跳过异常: {}", exception.getMessage());
            }
        }
    }

    /**
     * 轮询消费操作日志消息。
     */
    @Scheduled(fixedDelay = 5000L, initialDelay = 5000L)
    public void consume() {
        try {
            int recycledCount = ledgerService.recycleTimedOutProcessingMessages(LocalDateTime.now());
            if (recycledCount > 0) {
                log.debug("已回收 {} 条超时中的 MQ 消息", recycledCount);
            }
            var records = redisTemplate.opsForStream().read(
                Consumer.from(mqProperties.getGroup(), mqProperties.getConsumer()),
                StreamReadOptions.empty().count(20).block(Duration.ofSeconds(1)),
                StreamOffset.create(MqStreams.OP_LOG, ReadOffset.lastConsumed())
            );
            if (records == null || records.isEmpty()) {
                return;
            }
            for (MapRecord<String, Object, Object> record : records) {
                handleRecord(record);
            }
        } catch (Exception exception) {
            log.error("消费操作日志消息失败", exception);
        }
    }

    /**
     * 处理单条操作日志消息。
     *
     * @param record Redis Stream 消息
     */
    private void handleRecord(MapRecord<String, Object, Object> record) {
        messageConsumeExecutor.execute(
            record,
            mqProperties.getGroup(),
            mqProperties.getConsumer(),
            this::persistOpLog,
            this::acknowledge
        );
    }

    /**
     * 将消息中的操作日志持久化到数据库。
     *
     * @param record Redis Stream 消息
     * @throws JsonProcessingException 消息反序列化失败时抛出
     */
    private void persistOpLog(MapRecord<String, Object, Object> record) throws JsonProcessingException {
        OpLogRecord payload = objectMapper.readValue(String.valueOf(record.getValue().get("payload")), OpLogRecord.class);
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
    }

    /**
     * 确认 Redis Stream 消息已被成功消费。
     *
     * @param recordId 消息记录 ID
     */
    private void acknowledge(RecordId recordId) {
        redisTemplate.opsForStream().acknowledge(MqStreams.OP_LOG, mqProperties.getGroup(), recordId);
    }
}
