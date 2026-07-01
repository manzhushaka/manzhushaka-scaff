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
 * 消息队列执行明细表 sys_mq_message_log_detail。
 *
 * @author manzhushaka
 * @date 2026-06-29
 */
public class SysMqMessageLogDetail extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 执行明细主键 */
    @Excel(name = "执行明细编号", cellType = ColumnType.NUMERIC)
    private Long detailId;

    /** 消息台账主键 */
    @Excel(name = "消息台账编号", cellType = ColumnType.NUMERIC)
    private Long messageLogId;

    /** 执行次数 */
    @Excel(name = "执行次数", cellType = ColumnType.NUMERIC)
    private Integer attemptNo;

    /** 消费者名称 */
    @Excel(name = "消费者名称")
    private String consumerName;

    /** 状态（0执行中 1成功 2失败 3已跳过） */
    @Excel(name = "状态", readConverterExp = "0=执行中,1=成功,2=失败,3=已跳过")
    private String status;

    /** 开始时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "开始时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date startTime;

    /** 结束时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "结束时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date endTime;

    /** 耗时毫秒 */
    @Excel(name = "耗时(毫秒)", cellType = ColumnType.NUMERIC)
    private Long costTime;

    /** 错误信息 */
    private String errorMsg;

    public Long getDetailId()
    {
        return detailId;
    }

    public void setDetailId(Long detailId)
    {
        this.detailId = detailId;
    }

    public Long getMessageLogId()
    {
        return messageLogId;
    }

    public void setMessageLogId(Long messageLogId)
    {
        this.messageLogId = messageLogId;
    }

    public Integer getAttemptNo()
    {
        return attemptNo;
    }

    public void setAttemptNo(Integer attemptNo)
    {
        this.attemptNo = attemptNo;
    }

    public String getConsumerName()
    {
        return consumerName;
    }

    public void setConsumerName(String consumerName)
    {
        this.consumerName = consumerName;
    }

    public String getStatus()
    {
        return status;
    }

    public void setStatus(String status)
    {
        this.status = status;
    }

    public Date getStartTime()
    {
        return startTime;
    }

    public void setStartTime(Date startTime)
    {
        this.startTime = startTime;
    }

    public Date getEndTime()
    {
        return endTime;
    }

    public void setEndTime(Date endTime)
    {
        this.endTime = endTime;
    }

    public Long getCostTime()
    {
        return costTime;
    }

    public void setCostTime(Long costTime)
    {
        this.costTime = costTime;
    }

    public String getErrorMsg()
    {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg)
    {
        this.errorMsg = errorMsg;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("detailId", getDetailId())
                .append("messageLogId", getMessageLogId())
                .append("attemptNo", getAttemptNo())
                .append("consumerName", getConsumerName())
                .append("status", getStatus())
                .append("startTime", getStartTime())
                .append("endTime", getEndTime())
                .append("costTime", getCostTime())
                .append("errorMsg", StringUtils.substring(getErrorMsg(), 0, 256))
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .toString();
    }
}