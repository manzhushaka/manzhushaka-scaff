package com.manzhushaka.framework.web.command;

/**
 * 登录审计记录
 * <p>
 * 用于 framework 模块向 system 模块传递登录日志数据，
 * 避免 framework 直接依赖 system 的内部实体 {@code SysLogininfor}。
 * </p>
 *
 * @param username  用户名
 * @param status    状态
 * @param message   消息
 * @param ip        IP 地址
 * @param os        操作系统
 * @param browser   浏览器
 * @param address   地理位置
 * 
 * @author manzhushaka
 */
public record LoginAuditRecord(String username, String status, String message, String ip, String os, String browser, String address)
{
}