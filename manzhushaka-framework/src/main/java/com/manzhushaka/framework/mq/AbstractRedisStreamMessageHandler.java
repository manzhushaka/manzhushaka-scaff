package com.manzhushaka.framework.mq;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import com.manzhushaka.common.utils.ExceptionUtil;
import com.manzhushaka.common.utils.StringUtils;
import com.manzhushaka.system.domain.SysMqMessageDetailStatusEnum;
import com.manzhushaka.system.domain.SysMqMessageLog;
import com.manzhushaka.system.domain.SysMqMessageLogDetail;
import com.manzhushaka.system.domain.SysMqMessageStatusEnum;
import com.manzhushaka.system.service.ISysMqMessageLogService;

/**
 * Redis Stream 消息处理模板方法父类。
 * <p>
 * 统一处理台账创建、幂等判断、消费内重试、失败重试投递、死信投递和 ACK。
 *
 * @author manzhushaka
 * @date 2026-06-29
 */
public abstract class AbstractRedisStreamMessageHandler implements RedisStreamMessageHandler
{
    private final RedisStreamGateway gateway;

    private final ISysMqMessageLogService logService;

    protected AbstractRedisStreamMessageHandler(RedisStreamGateway gateway, ISysMqMessageLogService logService)
    {
        this.gateway = gateway;
        this.logService = logService;
    }

    @Override
    public void handle(RedisStreamRecord record)
    {
        // 1. 创建或获取主台账
        SysMqMessageLog messageLog = buildMessageLog(record);
        messageLog = logService.createOrGetMessageLog(messageLog);

        // 2. 幂等跳过判断
        if (isAlreadyProcessed(record))
        {
            writeSkippedDetail(messageLog, record);
            logService.updateMessageLog(messageLog);
            gateway.acknowledge(record.getStreamKey(), consumerGroup(), record.getMessageId());
            return;
        }

        // 3. 写执行中明细
        SysMqMessageLogDetail detail = createProcessingDetail(messageLog, record);
        detail = logService.insertMessageLogDetail(detail);

        Date startTime = new Date();
        boolean success = false;
        int currentRetryTimes = parseRetryTimes(record);

        try
        {
            doHandle(record);
            success = true;
        }
        catch (Exception ex)
        {
            // 消费内立即重试
            boolean immediateRetried = false;
            for (int i = 0; i < immediateRetryTimes(); i++)
            {
                try
                {
                    doHandle(record);
                    success = true;
                    immediateRetried = true;
                    break;
                }
                catch (Exception ignored)
                {
                    // 继续重试
                }
            }
            if (!immediateRetried)
            {
                // 更新明细失败
                Date endTime = new Date();
                detail.setStatus(SysMqMessageDetailStatusEnum.FAILED.getCode());
                detail.setEndTime(endTime);
                detail.setCostTime(endTime.getTime() - startTime.getTime());
                detail.setErrorMsg(resolveErrorMessage(ex));
                logService.updateMessageLogDetail(detail);

                int nextRetryTimes = currentRetryTimes + 1;
                if (nextRetryTimes < maxRetryTimes())
                {
                    // 写 retry stream
                    writeRetryStream(record, nextRetryTimes);

                    messageLog.setStatus(SysMqMessageStatusEnum.FAILED.getCode());
                    messageLog.setRetryTimes(nextRetryTimes);
                    messageLog.setLastErrorMsg(resolveErrorMessage(ex));
                    messageLog.setLastConsumeTime(new Date());
                    logService.updateMessageLog(messageLog);
                }
                else
                {
                    // 写 dead-letter stream
                    writeDeadLetterStream(record);

                    messageLog.setStatus(SysMqMessageStatusEnum.DEAD_LETTER.getCode());
                    messageLog.setRetryTimes(nextRetryTimes);
                    messageLog.setLastErrorMsg(resolveErrorMessage(ex));
                    messageLog.setLastConsumeTime(new Date());
                    messageLog.setDeadLetterTime(new Date());
                    logService.updateMessageLog(messageLog);
                }

                gateway.acknowledge(record.getStreamKey(), consumerGroup(), record.getMessageId());
                return;
            }
        }

        if (success)
        {
            Date endTime = new Date();
            detail.setStatus(SysMqMessageDetailStatusEnum.SUCCESS.getCode());
            detail.setEndTime(endTime);
            detail.setCostTime(endTime.getTime() - startTime.getTime());
            logService.updateMessageLogDetail(detail);

            messageLog.setStatus(SysMqMessageStatusEnum.SUCCESS.getCode());
            messageLog.setRetryTimes(currentRetryTimes + 1);
            messageLog.setLastConsumeTime(new Date());
            messageLog.setSuccessTime(new Date());
            logService.updateMessageLog(messageLog);

            gateway.acknowledge(record.getStreamKey(), consumerGroup(), record.getMessageId());
        }
    }

    /**
     * 业务幂等键，默认使用 businessKey。
     *
     * @param record Redis Stream 记录
     * @return 幂等键
     */
    protected String idempotentKey(RedisStreamRecord record)
    {
        return record.getBodyValue("businessKey");
    }

    /**
     * 判断消息是否已处理过，子类可覆写。
     *
     * @param record Redis Stream 记录
     * @return true 表示已处理过，跳过本次处理
     */
    protected boolean isAlreadyProcessed(RedisStreamRecord record)
    {
        return false;
    }

    /**
     * 实际业务处理逻辑，子类必须实现。
     *
     * @param record Redis Stream 记录
     */
    protected abstract void doHandle(RedisStreamRecord record);

    /**
     * 从 record 构建 SysMqMessageLog。
     */
    private SysMqMessageLog buildMessageLog(RedisStreamRecord record)
    {
        SysMqMessageLog log = new SysMqMessageLog();
        log.setMessageType(messageType());
        log.setStreamKey(record.getStreamKey());
        log.setMessageId(record.getMessageId());
        log.setConsumerGroup(consumerGroup());
        log.setBusinessKey(idempotentKey(record));
        log.setPayload(record.getBodyValue("payload"));
        log.setStatus(SysMqMessageStatusEnum.PROCESSING.getCode());
        log.setRetryTimes(parseRetryTimes(record));
        log.setMaxRetryTimes(maxRetryTimes());
        log.setFirstConsumeTime(new Date());
        log.setLastConsumeTime(new Date());
        return log;
    }

    /**
     * 写已跳过的明细记录。
     */
    private void writeSkippedDetail(SysMqMessageLog messageLog, RedisStreamRecord record)
    {
        SysMqMessageLogDetail detail = new SysMqMessageLogDetail();
        detail.setMessageLogId(messageLog.getMessageLogId());
        detail.setAttemptNo(messageLog.getRetryTimes() + 1);
        detail.setConsumerName(consumerName());
        detail.setStatus(SysMqMessageDetailStatusEnum.SKIPPED.getCode());
        detail.setStartTime(new Date());
        detail.setEndTime(new Date());
        logService.insertMessageLogDetail(detail);

        messageLog.setStatus(SysMqMessageStatusEnum.SKIPPED.getCode());
        messageLog.setLastConsumeTime(new Date());
    }

    /**
     * 创建执行中的明细。
     */
    private SysMqMessageLogDetail createProcessingDetail(SysMqMessageLog messageLog, RedisStreamRecord record)
    {
        SysMqMessageLogDetail detail = new SysMqMessageLogDetail();
        detail.setMessageLogId(messageLog.getMessageLogId());
        detail.setAttemptNo(messageLog.getRetryTimes() + 1);
        detail.setConsumerName(consumerName());
        detail.setStatus(SysMqMessageDetailStatusEnum.PROCESSING.getCode());
        detail.setStartTime(new Date());
        return detail;
    }

    /**
     * 写 retry stream。
     */
    private void writeRetryStream(RedisStreamRecord record, int nextRetryTimes)
    {
        Map<String, String> body = new LinkedHashMap<>();
        body.put("messageType", messageType());
        body.put("businessKey", idempotentKey(record));
        body.put("payload", record.getBodyValue("payload"));
        body.put("retryTimes", String.valueOf(nextRetryTimes));
        body.put("originalStreamKey", record.getStreamKey());
        body.put("originalMessageId", record.getMessageId());
        body.put("nextRetryTime", String.valueOf(System.currentTimeMillis() + retryIntervalSeconds() * 1000L));
        gateway.add(retryStreamKey(), body);
    }

    /**
     * 写 dead-letter stream。
     */
    private void writeDeadLetterStream(RedisStreamRecord record)
    {
        Map<String, String> body = new LinkedHashMap<>();
        body.put("messageType", messageType());
        body.put("businessKey", idempotentKey(record));
        body.put("payload", record.getBodyValue("payload"));
        body.put("originalStreamKey", record.getStreamKey());
        body.put("originalMessageId", record.getMessageId());
        gateway.add(deadLetterStreamKey(), body);
    }

    /**
     * 从 record 中解析 retryTimes。
     */
    private int parseRetryTimes(RedisStreamRecord record)
    {
        String retryTimes = record.getBodyValue("retryTimes");
        if (retryTimes == null)
        {
            return 0;
        }
        try
        {
            return Integer.parseInt(retryTimes);
        }
        catch (NumberFormatException e)
        {
            return 0;
        }
    }

    /**
     * 解析异常信息并截断。
     */
    private String resolveErrorMessage(Exception ex)
    {
        return StringUtils.substring(ExceptionUtil.getExceptionMessage(ex), 0, 2000);
    }
}