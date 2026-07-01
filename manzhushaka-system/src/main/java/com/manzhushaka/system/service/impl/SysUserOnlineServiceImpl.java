package com.manzhushaka.system.service.impl;

import java.lang.reflect.Method;
import org.springframework.stereotype.Service;
import com.manzhushaka.common.utils.StringUtils;
import com.manzhushaka.system.domain.SysUserOnline;
import com.manzhushaka.system.service.ISysUserOnlineService;

/**
 * 在线用户 服务层处理
 *
 * @author manzhushaka
 */
@Service
public class SysUserOnlineServiceImpl implements ISysUserOnlineService
{
    private String invokeStringGetter(Object obj, String methodName)
    {
        try
        {
            Method method = obj.getClass().getMethod(methodName);
            Object result = method.invoke(obj);
            return result != null ? (String) result : null;
        }
        catch (Exception e)
        {
            return null;
        }
    }

    private Long invokeLongGetter(Object obj, String methodName)
    {
        try
        {
            Method method = obj.getClass().getMethod(methodName);
            Object result = method.invoke(obj);
            return result != null ? (Long) result : null;
        }
        catch (Exception e)
        {
            return null;
        }
    }

    /**
     * 通过登录地址查询信息
     *
     * @param ipaddr 登录地址
     * @param user 用户信息
     * @return 在线用户信息
     */
    @Override
    public SysUserOnline selectOnlineByIpaddr(String ipaddr, Object user)
    {
        String userIpaddr = invokeStringGetter(user, "getIpaddr");
        if (StringUtils.equals(ipaddr, userIpaddr))
        {
            return loginUserToUserOnline(user);
        }
        return null;
    }

    /**
     * 通过用户名称查询信息
     *
     * @param userName 用户名称
     * @param user 用户信息
     * @return 在线用户信息
     */
    @Override
    public SysUserOnline selectOnlineByUserName(String userName, Object user)
    {
        String username = invokeStringGetter(user, "getUsername");
        if (StringUtils.equals(userName, username))
        {
            return loginUserToUserOnline(user);
        }
        return null;
    }

    /**
     * 通过登录地址/用户名称查询信息
     *
     * @param ipaddr 登录地址
     * @param userName 用户名称
     * @param user 用户信息
     * @return 在线用户信息
     */
    @Override
    public SysUserOnline selectOnlineByInfo(String ipaddr, String userName, Object user)
    {
        String userIpaddr = invokeStringGetter(user, "getIpaddr");
        String username = invokeStringGetter(user, "getUsername");
        if (StringUtils.equals(ipaddr, userIpaddr) && StringUtils.equals(userName, username))
        {
            return loginUserToUserOnline(user);
        }
        return null;
    }

    /**
     * 设置在线用户信息
     *
     * @param user 用户信息
     * @return 在线用户
     */
    @Override
    public SysUserOnline loginUserToUserOnline(Object user)
    {
        if (StringUtils.isNull(user))
        {
            return null;
        }
        SysUserOnline sysUserOnline = new SysUserOnline();
        sysUserOnline.setTokenId(invokeStringGetter(user, "getToken"));
        sysUserOnline.setUserName(invokeStringGetter(user, "getUsername"));
        sysUserOnline.setIpaddr(invokeStringGetter(user, "getIpaddr"));
        sysUserOnline.setLoginLocation(invokeStringGetter(user, "getLoginLocation"));
        sysUserOnline.setBrowser(invokeStringGetter(user, "getBrowser"));
        sysUserOnline.setOs(invokeStringGetter(user, "getOs"));
        sysUserOnline.setLoginTime(invokeLongGetter(user, "getLoginTime"));
        sysUserOnline.setDeptName(invokeStringGetter(user, "getDeptName"));
        return sysUserOnline;
    }
}
