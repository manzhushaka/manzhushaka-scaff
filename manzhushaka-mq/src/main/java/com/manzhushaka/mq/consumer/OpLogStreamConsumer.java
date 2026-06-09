package com.manzhushaka.mq.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.manzhushaka.common.model.OpLogRecord;
import com.manzhushaka.db.system.entity.SysOpLog;
import com.manzhushaka.db.system.mapper.SysOpLogMapper;
import com.manzhushaka.mq.core.MqEvent;
import com.manzhushaka.mq.core.MqStreams;
import com.manzhushaka.mq.core.RedisStreamPublisher;
import com.manzhushaka.mq.properties.MqProperties;
import org.springframework.boot.ApplicationRunner;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.connection.stream.StreamReadOptions;
import org.springframework.data.redis.connection.stream.StringRecord;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Component
public class OpLogStreamConsumer {
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final SysOpLogMapper sysOpLogMapper;
    private final RedisStreamPublisher streamPublisher;
    private final MqProperties mqProperties;

    public OpLogStreamConsumer(
        StringRedisTemplate redisTemplate,
        ObjectMapper mqObjectMapper,
        SysOpLogMapper sysOpLogMapper,
        RedisStreamPublisher streamPublisher,
        MqProperties mqProperties
    ) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = mqObjectMapper;
        this.sysOpLogMapper = sysOpLogMapper;
        this.streamPublisher = streamPublisher;
        this.mqProperties = mqProperties;
    }

    @Component
    class StreamBootstrap implements ApplicationRunner {
        @Override
        public void run(org.springframework.boot.ApplicationArguments args) {
            try {
                redisTemplate.opsForStream().createGroup(MqStreams.OP_LOG, ReadOffset.latest(), mqProperties.getGroup());
            } catch (Exception ignored) {
            }
        }
    }

    @Scheduled(fixedDelay = 5000L, initialDelay = 5000L)
    public void consume() {
        try {
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
        } catch (Exception ignored) {
        }
    }

    private void handleRecord(MapRecord<String, Object, Object> record) {
        try {
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
            acknowledge(record.getId());
        } catch (Exception exception) {
            retryOrDead(record, exception);
        }
    }

    private void retryOrDead(MapRecord<String, Object, Object> record, Exception exception) {
        int retryCount = Integer.parseInt(String.valueOf(record.getValue().getOrDefault("retryCount", "0")));
        if (retryCount + 1 > mqProperties.getMaxRetry()) {
            streamPublisher.publish(MqStreams.DEAD, toEvent(record.getValue(), retryCount + 1, exception));
        } else {
            streamPublisher.publish(MqStreams.OP_LOG, toEvent(record.getValue(), retryCount + 1, exception));
        }
        acknowledge(record.getId());
    }

    private MqEvent<Map<String, Object>> toEvent(Map<Object, Object> value, int retryCount, Exception exception) {
        MqEvent<Map<String, Object>> event = new MqEvent<>();
        event.setEventId(UUID.randomUUID().toString().replace("-", ""));
        event.setEventType(String.valueOf(value.get("eventType")));
        event.setBizKey(String.valueOf(value.get("bizKey")));
        event.setOccurredAt(LocalDateTime.now());
        event.setTraceId(String.valueOf(value.get("traceId")));
        event.setSource("manzhushaka-mq-retry");
        event.setRetryCount(retryCount);
        event.setPayload(Map.of(
            "payload", String.valueOf(value.get("payload")),
            "error", exception.getMessage()
        ));
        return event;
    }

    private void acknowledge(RecordId recordId) {
        redisTemplate.opsForStream().acknowledge(MqStreams.OP_LOG, mqProperties.getGroup(), recordId);
    }
}
