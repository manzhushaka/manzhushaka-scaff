package com.manzhushaka.system.application.result.system;

import java.util.Date;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.manzhushaka.common.annotation.Excel;
import com.manzhushaka.common.annotation.Excel.ColumnType;

/**
 * 消息队列台账结果。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
public record MqMessageLogResult(
        @Excel(name = "消息台账编号", cellType = ColumnType.NUMERIC) Long messageLogId,
        @Excel(name = "消息类型") String messageType,
        @Excel(name = "原始Stream") String streamKey,
        @Excel(name = "消息ID") String messageId,
        @Excel(name = "消费者组") String consumerGroup,
        @Excel(name = "业务幂等键") String businessKey,
        String payload,
        @Excel(name = "状态", readConverterExp = "0=执行中,1=成功,2=失败,3=已跳过,4=死信") String status,
        @Excel(name = "已尝试次数", cellType = ColumnType.NUMERIC) Integer retryTimes,
        @Excel(name = "最大重试次数", cellType = ColumnType.NUMERIC) Integer maxRetryTimes,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        @Excel(name = "首次消费时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss") Date firstConsumeTime,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        @Excel(name = "最后消费时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss") Date lastConsumeTime,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        @Excel(name = "成功时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss") Date successTime,
        @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
        @Excel(name = "进入死信时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss") Date deadLetterTime,
        String lastErrorMsg, String createBy, Date createTime, String updateBy, Date updateTime)
{
    @Override
    public String toString()
    {
        return "MqMessageLogResult[messageLogId=" + messageLogId + ", messageType=" + messageType
                + ", streamKey=" + streamKey + ", messageId=" + messageId + ", status=" + status + "]";
    }
}
