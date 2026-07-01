package com.manzhushaka.system.application.service;

/**
 * 系统审计应用服务接口
 * <p>
 * 封装操作日志和登录日志的记录，供 framework 模块调用。
 * 避免 framework 直接依赖 system 的内部实体和持久化服务。
 * </p>
 * 
 * @author manzhushaka
 */
public interface SystemAuditAppService
{
    /**
     * 记录登录日志
     *
     * @param username  用户名
     * @param status    状态
     * @param message   消息
     * @param ip        IP 地址
     * @param os        操作系统
     * @param browser   浏览器
     * @param address   地理位置
     */
    void recordLoginAudit(String username, String status, String message, String ip, String os, String browser, String address);

    /**
     * 记录操作日志
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
     * @param status         操作状态
     * @param errorMsg       错误消息
     * @param businessType   业务类型
     * @param title          操作模块
     * @param operatorType   操作类别
     * @param costTime       消耗时间
     */
    void recordOperationAudit(String operIp, String operLocation, String operName, String deptName,
                              String method, String requestMethod, String operUrl, String operParam,
                              String jsonResult, Integer status, String errorMsg, Integer businessType,
                              String title, Integer operatorType, Long costTime);

    /**
     * 记录慢 SQL 日志。
     *
     * @param mapperId       Mapper 方法
     * @param sqlText        SQL 文本
     * @param dataSourceName 数据源名称
     * @param costTime       消耗时间
     * @param errorMsg       错误消息
     * @param executeTime    执行时间
     */
    void recordSlowSqlLog(String mapperId, String sqlText, String dataSourceName, Long costTime,
                          String errorMsg, java.util.Date executeTime);
}
