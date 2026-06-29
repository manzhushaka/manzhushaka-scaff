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
 * 请求日志记录表 sys_request_log。
 *
 * @author manzhushaka
 * @date 2026-06-29
 */
public class SysRequestLog extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 请求日志主键 */
    @Excel(name = "请求编号", cellType = ColumnType.NUMERIC)
    private Long requestId;

    /** 请求地址 */
    @Excel(name = "请求地址")
    private String requestUri;

    /** 请求方式 */
    @Excel(name = "请求方式")
    private String requestMethod;

    /** 控制器方法 */
    @Excel(name = "控制器方法")
    private String controllerMethod;

    /** URL 查询参数 */
    private String queryString;

    /** 请求参数 */
    private String requestParams;

    /** 请求 IP */
    @Excel(name = "请求IP")
    private String ipaddr;

    /** 用户账号 */
    @Excel(name = "用户账号")
    private String userName;

    /** HTTP 状态码 */
    @Excel(name = "状态码")
    private Integer statusCode;

    /** 状态（0正常 1异常） */
    @Excel(name = "状态", readConverterExp = "0=正常,1=异常")
    private Integer status;

    /** 错误消息 */
    private String errorMsg;

    /** User-Agent */
    private String userAgent;

    /** 消耗时间 */
    @Excel(name = "消耗时间", suffix = "毫秒")
    private Long costTime;

    /** 请求时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "请求时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date requestTime;

    public Long getRequestId()
    {
        return requestId;
    }

    public void setRequestId(Long requestId)
    {
        this.requestId = requestId;
    }

    public String getRequestUri()
    {
        return requestUri;
    }

    public void setRequestUri(String requestUri)
    {
        this.requestUri = requestUri;
    }

    public String getRequestMethod()
    {
        return requestMethod;
    }

    public void setRequestMethod(String requestMethod)
    {
        this.requestMethod = requestMethod;
    }

    public String getControllerMethod()
    {
        return controllerMethod;
    }

    public void setControllerMethod(String controllerMethod)
    {
        this.controllerMethod = controllerMethod;
    }

    public String getQueryString()
    {
        return queryString;
    }

    public void setQueryString(String queryString)
    {
        this.queryString = queryString;
    }

    public String getRequestParams()
    {
        return requestParams;
    }

    public void setRequestParams(String requestParams)
    {
        this.requestParams = requestParams;
    }

    public String getIpaddr()
    {
        return ipaddr;
    }

    public void setIpaddr(String ipaddr)
    {
        this.ipaddr = ipaddr;
    }

    public String getUserName()
    {
        return userName;
    }

    public void setUserName(String userName)
    {
        this.userName = userName;
    }

    public Integer getStatusCode()
    {
        return statusCode;
    }

    public void setStatusCode(Integer statusCode)
    {
        this.statusCode = statusCode;
    }

    public Integer getStatus()
    {
        return status;
    }

    public void setStatus(Integer status)
    {
        this.status = status;
    }

    public String getErrorMsg()
    {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg)
    {
        this.errorMsg = errorMsg;
    }

    public String getUserAgent()
    {
        return userAgent;
    }

    public void setUserAgent(String userAgent)
    {
        this.userAgent = userAgent;
    }

    public Long getCostTime()
    {
        return costTime;
    }

    public void setCostTime(Long costTime)
    {
        this.costTime = costTime;
    }

    public Date getRequestTime()
    {
        return requestTime;
    }

    public void setRequestTime(Date requestTime)
    {
        this.requestTime = requestTime;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("requestId", getRequestId())
                .append("requestUri", getRequestUri())
                .append("requestMethod", getRequestMethod())
                .append("controllerMethod", getControllerMethod())
                .append("queryString", getQueryString())
                .append("requestParams", StringUtils.substring(getRequestParams(), 0, 256))
                .append("ipaddr", getIpaddr())
                .append("userName", getUserName())
                .append("statusCode", getStatusCode())
                .append("status", getStatus())
                .append("errorMsg", StringUtils.substring(getErrorMsg(), 0, 256))
                .append("userAgent", StringUtils.substring(getUserAgent(), 0, 128))
                .append("costTime", getCostTime())
                .append("requestTime", getRequestTime())
                .toString();
    }
}
