package com.manzhushaka.system.service;

import com.manzhushaka.system.application.result.system.UserOnlineResult;

/**
 * 在线用户 服务层
 *
 * @author manzhushaka
 */
public interface ISysUserOnlineService
{
    /**
     * 通过登录地址查询信息
     *
     * @param ipaddr 登录地址
     * @param user 用户信息
     * @return 在线用户信息
     */
    UserOnlineResult selectOnlineByIpaddr(String ipaddr, Object user);

    /**
     * 通过用户名称查询信息
     *
     * @param userName 用户名称
     * @param user 用户信息
     * @return 在线用户信息
     */
    UserOnlineResult selectOnlineByUserName(String userName, Object user);

    /**
     * 通过登录地址/用户名称查询信息
     *
     * @param ipaddr 登录地址
     * @param userName 用户名称
     * @param user 用户信息
     * @return 在线用户信息
     */
    UserOnlineResult selectOnlineByInfo(String ipaddr, String userName, Object user);

    /**
     * 设置在线用户信息
     *
     * @param user 用户信息
     * @return 在线用户
     */
    UserOnlineResult loginUserToUserOnline(Object user);
}
