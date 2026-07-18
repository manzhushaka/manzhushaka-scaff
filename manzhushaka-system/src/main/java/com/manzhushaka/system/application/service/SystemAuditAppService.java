package com.manzhushaka.system.application.service;

import java.util.List;
import com.manzhushaka.system.application.query.LoginLogQuery;
import com.manzhushaka.system.application.query.MqMessageLogQuery;
import com.manzhushaka.system.application.query.OperLogQuery;
import com.manzhushaka.system.application.query.SlowSqlLogQuery;
import com.manzhushaka.system.application.result.system.LoginLogResult;
import com.manzhushaka.system.application.result.system.MqMessageLogDetailResult;
import com.manzhushaka.system.application.result.system.MqMessageLogResult;
import com.manzhushaka.system.application.result.system.OperLogResult;
import com.manzhushaka.system.application.result.system.SlowSqlLogResult;

/**
 * 系统审计应用服务接口
 * 封装操作日志和登录日志的记录，供 framework 模块调用。
 * 避免 framework 直接依赖 system 的内部实体和持久化服务。
 * 
 * @author manzhushaka
 */
public interface SystemAuditAppService
{
    /**
     * 查询操作日志。
     *
     * @param query 查询条件
     * @return 操作日志列表
     */
    List<OperLogResult> listOperationLogs(OperLogQuery query);

    /**
     * 查询操作日志详情。
     *
     * @param operId 操作日志 ID
     * @return 操作日志详情
     */
    OperLogResult getOperationLog(Long operId);

    /**
     * 删除操作日志。
     *
     * @param operIds 操作日志 ID 数组
     * @return 影响行数
     */
    int deleteOperationLogs(Long[] operIds);

    /** 清空操作日志。 */
    void cleanOperationLogs();

    /**
     * 查询登录日志。
     *
     * @param query 查询条件
     * @return 登录日志列表
     */
    List<LoginLogResult> listLoginLogs(LoginLogQuery query);

    /**
     * 删除登录日志。
     *
     * @param infoIds 登录日志 ID 数组
     * @return 影响行数
     */
    int deleteLoginLogs(Long[] infoIds);

    /** 清空登录日志。 */
    void cleanLoginLogs();

    /**
     * 查询慢 SQL 日志。
     *
     * @param query 查询条件
     * @return 慢 SQL 日志列表
     */
    List<SlowSqlLogResult> listSlowSqlLogs(SlowSqlLogQuery query);

    /**
     * 查询慢 SQL 日志详情。
     *
     * @param slowSqlId 慢 SQL 日志 ID
     * @return 慢 SQL 日志详情
     */
    SlowSqlLogResult getSlowSqlLog(Long slowSqlId);

    /**
     * 删除慢 SQL 日志。
     *
     * @param slowSqlIds 慢 SQL 日志 ID 数组
     * @return 影响行数
     */
    int deleteSlowSqlLogs(Long[] slowSqlIds);

    /** 清空慢 SQL 日志。 */
    void cleanSlowSqlLogs();

    /**
     * 查询消息队列台账。
     *
     * @param query 查询条件
     * @return 消息队列台账列表
     */
    List<MqMessageLogResult> listMqMessageLogs(MqMessageLogQuery query);

    /**
     * 查询消息队列台账详情。
     *
     * @param messageLogId 消息台账 ID
     * @return 消息队列台账详情
     */
    MqMessageLogResult getMqMessageLog(Long messageLogId);

    /**
     * 查询消息执行明细。
     *
     * @param messageLogId 消息台账 ID
     * @return 消息执行明细列表
     */
    List<MqMessageLogDetailResult> listMqMessageLogDetails(Long messageLogId);

    /**
     * 删除消息队列台账。
     *
     * @param messageLogIds 消息台账 ID 数组
     * @return 影响行数
     */
    int deleteMqMessageLogs(Long[] messageLogIds);

    /** 清空消息队列台账。 */
    void cleanMqMessageLogs();

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
    void recordLoginAudit(String username, String status, String message, String ip, String os,
            String browser, String address);

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
