package com.manzhushaka.framework.web.command;

/**
 * 操作审计记录
 * <p>
 * 用于 framework 模块向 system 模块传递操作日志数据，
 * 避免 framework 直接依赖 system 的内部实体 {@code SysOperLog}。
 * </p>
 *
 * @param operIp         操作 IP
 * @param operLocation   操作地点
 * @param operName       操作人员
 * @param deptName       部门名称
 * @param method         请求方法
 * @param requestMethod  请求方式
 * @param operUrl        请求 URL
 * @param operParam      请求参数
 * @param jsonResult     返回参数
 * @param status         操作状态（0 正常 1 异常）
 * @param errorMsg       错误消息
 * @param businessType   业务类型（0 其它 1 新增 2 修改 3 删除）
 * @param title          操作模块
 * @param operatorType   操作类别（0 其它 1 后台用户 2 手机端用户）
 * @param costTime       消耗时间（毫秒）
 * 
 * @author manzhushaka
 */
public record OperationAuditRecord(String operIp, String operLocation, String operName, String deptName,
                                    String method, String requestMethod, String operUrl, String operParam,
                                    String jsonResult, Integer status, String errorMsg, Integer businessType,
                                    String title, Integer operatorType, Long costTime)
{
    public static Builder builder()
    {
        return new Builder();
    }

    /**
     * OperationAuditRecord 构建器
     */
    public static final class Builder
    {
        private String operIp;
        private String operLocation;
        private String operName;
        private String deptName;
        private String method;
        private String requestMethod;
        private String operUrl;
        private String operParam;
        private String jsonResult;
        private Integer status;
        private String errorMsg;
        private Integer businessType;
        private String title;
        private Integer operatorType;
        private Long costTime;

        public Builder operIp(String operIp)
        {
            this.operIp = operIp;
            return this;
        }

        public Builder operLocation(String operLocation)
        {
            this.operLocation = operLocation;
            return this;
        }

        public Builder operName(String operName)
        {
            this.operName = operName;
            return this;
        }

        public Builder deptName(String deptName)
        {
            this.deptName = deptName;
            return this;
        }

        public Builder method(String method)
        {
            this.method = method;
            return this;
        }

        public Builder requestMethod(String requestMethod)
        {
            this.requestMethod = requestMethod;
            return this;
        }

        public Builder operUrl(String operUrl)
        {
            this.operUrl = operUrl;
            return this;
        }

        public Builder operParam(String operParam)
        {
            this.operParam = operParam;
            return this;
        }

        public Builder jsonResult(String jsonResult)
        {
            this.jsonResult = jsonResult;
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

        public Builder businessType(Integer businessType)
        {
            this.businessType = businessType;
            return this;
        }

        public Builder title(String title)
        {
            this.title = title;
            return this;
        }

        public Builder operatorType(Integer operatorType)
        {
            this.operatorType = operatorType;
            return this;
        }

        public Builder costTime(Long costTime)
        {
            this.costTime = costTime;
            return this;
        }

        public OperationAuditRecord build()
        {
            return new OperationAuditRecord(operIp, operLocation, operName, deptName,
                    method, requestMethod, operUrl, operParam, jsonResult, status, errorMsg,
                    businessType, title, operatorType, costTime);
        }
    }
}