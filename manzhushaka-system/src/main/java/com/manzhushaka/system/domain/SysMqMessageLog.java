package com.manzhushaka.system.domain;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.manzhushaka.common.annotation.Excel;
import com.manzhushaka.common.annotation.Excel.ColumnType;
import com.manzhushaka.common.core.domain.BaseEntity;
import com.manzhushaka.common.utils.StringUtils;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 消息队列主台账表 sys_mq_message_log。
 *
 * @author manzhushaka
 * @date 2026-06-29
 */
public class SysMqMessageLog extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 消息台账主键 */
    @Excel(name = "消息台账编号", cellType = ColumnType.NUMERIC)
    private Long messageLogId;

    /** 消息类型 */
    @Excel(name = "消息类型")
    private String messageType;

    /** 原始 Stream */
    @Excel(name = "原始Stream")
    private String streamKey;

    /** Redis Stream 消息 ID */
    @Excel(name = "消息ID")
    private String messageId;

    /** 消费者组 */
    @Excel(name = "消费者组")
    private String consumerGroup;

    /** 业务幂等键 */
    @Excel(name = "业务幂等键")
    private String businessKey;

    /** 消息内容 */
    private String payload;

    /** 状态（0执行中 1成功 2失败 3已跳过 4死信） */
    @Excel(name = "状态", readConverterExp = "0=执行中,1=成功,2=失败,3=已跳过,4=死信")
    private String status;

    /** 已尝试次数 */
    @Excel(name = "已尝试次数", cellType = ColumnType.NUMERIC)
    private Integer retryTimes;

    /** 最大重试次数 */
    @Excel(name = "最大重试次数", cellType = ColumnType.NUMERIC)
    private Integer maxRetryTimes;

    /** 首次消费时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "首次消费时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date firstConsumeTime;

    /** 最后消费时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "最后消费时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date lastConsumeTime;

    /** 成功时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "成功时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date successTime;

    /** 进入死信时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "进入死信时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date deadLetterTime;

    /** 最后错误信息 */
    private String lastErrorMsg;

    public Long getMessageLogId()
    {
        return messageLogId;
    }

    public void setMessageLogId(Long messageLogId)
    {
        this.messageLogId = messageLogId;
    }

    public String getMessageType()
    {
        return messageType;
    }

    public void setMessageType(String messageType)
    {
        this.messageType = messageType;
    }

    public String getStreamKey()
    {
        return streamKey;
    }

    public void setStreamKey(String streamKey)
    {
        this.streamKey = streamKey;
    }

    public String getMessageId()
    {
        return messageId;
    }

    public void setMessageId(String messageId)
    {
        this.messageId = messageId;
    }

    public String getConsumerGroup()
    {
        return consumerGroup;
    }

    public void setConsumerGroup(String consumerGroup)
    {
        this.consumerGroup = consumerGroup;
    }

    public String getBusinessKey()
    {
        return businessKey;
    }

    public void setBusinessKey(String businessKey)
    {
        this.businessKey = businessKey;
    }

    public String getPayload()
    {
        return payload;
    }

    public void setPayload(String payload)
    {
        this.payload = payload;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public Integer getRetryTimes()
    {
        return retryTimes;
    }

    public void setRetryTimes(Integer retryTimes)
    {
        this.retryTimes = retryTimes;
    }

    public Integer getMaxRetryTimes()
    {
        return maxRetryTimes;
    }

    public void setMaxRetryTimes(Integer maxRetryTimes)
    {
        this.maxRetryTimes = maxRetryTimes;
    }

    public Date getFirstConsumeTime()
    {
        return firstConsumeTime;
    }

    public void setFirstConsumeTime(Date firstConsumeTime)
    {
        this.firstConsumeTime = firstConsumeTime;
    }

    public Date getLastConsumeTime()
    {
        return lastConsumeTime;
    }

    public void setLastConsumeTime(Date lastConsumeTime)
    {
        this.lastConsumeTime = lastConsumeTime;
    }

    public Date getSuccessTime()
    {
        return successTime;
    }

    public void setSuccessTime(Date successTime)
    {
        this.successTime = successTime;
    }

    public Date getDeadLetterTime()
    {
        return deadLetterTime;
    }

    public void setDeadLetterTime(Date deadLetterTime)
    {
        this.deadLetterTime = deadLetterTime;
    }

    public String getLastErrorMsg()
    {
        return lastErrorMsg;
    }

    public void setLastErrorMsg(String lastErrorMsg)
    {
        this.lastErrorMsg = lastErrorMsg;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("messageLogId", getMessageLogId())
                .append("messageType", getMessageType())
                .append("streamKey", getStreamKey())
                .append("messageId", getMessageId())
                .append("consumerGroup", getConsumerGroup())
                .append("businessKey", getBusinessKey())
                .append("payload", StringUtils.substring(getPayload(), 0, 256))
                .append("status", getStatus())
                .append("retryTimes", getRetryTimes())
                .append("maxRetryTimes", getMaxRetryTimes())
                .append("firstConsumeTime", getFirstConsumeTime())
                .append("lastConsumeTime", getLastConsumeTime())
                .append("successTime", getSuccessTime())
                .append("deadLetterTime", getDeadLetterTime())
                .append("lastErrorMsg", StringUtils.substring(getLastErrorMsg(), 0, 256))
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .toString();
    }
}