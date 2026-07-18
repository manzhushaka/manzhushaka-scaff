package com.manzhushaka.system.application.result.system;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 消息队列执行明细结果。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
public record MqMessageLogDetailResult(Long detailId, Long messageLogId, Integer attemptNo,
        String consumerName, String status,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date startTime,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date endTime,
        Long costTime, String errorMsg)
{
    @Override
    public String toString()
    {
        return "MqMessageLogDetailResult[detailId=" + detailId + ", messageLogId=" + messageLogId
                + ", attemptNo=" + attemptNo + ", consumerName=" + consumerName + ", status=" + status + "]";
    }
}
