package com.manzhushaka.framework.web.command;

import java.util.Date;

/**
 * 请求日志记录。
 *
 * @author manzhushaka
 * @date 2026-06-29
 */
public record RequestLogRecord(String requestUri, String requestMethod, String controllerMethod, String queryString,
                               String requestParams, String ipaddr, String userName, Integer statusCode,
                               Integer status, String errorMsg, String userAgent, Long costTime, Date requestTime)
{
    public static Builder builder()
    {
        return new Builder();
    }

    /**
     * 请求日志构建器。
     */
    public static final class Builder
    {
        private String requestUri;
        private String requestMethod;
        private String controllerMethod;
        private String queryString;
        private String requestParams;
        private String ipaddr;
        private String userName;
        private Integer statusCode;
        private Integer status;
        private String errorMsg;
        private String userAgent;
        private Long costTime;
        private Date requestTime;

        public Builder requestUri(String requestUri)
        {
            this.requestUri = requestUri;
            return this;
        }

        public Builder requestMethod(String requestMethod)
        {
            this.requestMethod = requestMethod;
            return this;
        }

        public Builder controllerMethod(String controllerMethod)
        {
            this.controllerMethod = controllerMethod;
            return this;
        }

        public Builder queryString(String queryString)
        {
            this.queryString = queryString;
            return this;
        }

        public Builder requestParams(String requestParams)
        {
            this.requestParams = requestParams;
            return this;
        }

        public Builder ipaddr(String ipaddr)
        {
            this.ipaddr = ipaddr;
            return this;
        }

        public Builder userName(String userName)
        {
            this.userName = userName;
            return this;
        }

        public Builder statusCode(Integer statusCode)
        {
            this.statusCode = statusCode;
            return this;
        }

        public Builder status(Integer status)
        {
            this.status = status;
            return this;
        }

        public Builder errorMsg(String errorMsg)
        {
            this.errorMsg = errorMsg;
            return this;
        }

        public Builder userAgent(String userAgent)
        {
            this.userAgent = userAgent;
            return this;
        }

        public Builder costTime(Long costTime)
        {
            this.costTime = costTime;
            return this;
        }

        public Builder requestTime(Date requestTime)
        {
            this.requestTime = requestTime;
            return this;
        }

        public RequestLogRecord build()
        {
            return new RequestLogRecord(requestUri, requestMethod, controllerMethod, queryString, requestParams,
                    ipaddr, userName, statusCode, status, errorMsg, userAgent, costTime, requestTime);
        }
    }
}
