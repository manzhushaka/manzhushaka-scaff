package com.manzhushaka.web.dto.system;

/**
 * 屏幕解锁请求。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
public class UnlockScreenRequest
{
    /** 当前用户密码 */
    private String password;

    public String getPassword()
    {
        return password;
    }

    public void setPassword(String password)
    {
        this.password = password;
    }

    @Override
    public String toString()
    {
        return "UnlockScreenRequest{}";
    }
}
