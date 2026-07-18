package com.manzhushaka.system.application.result.system;

/**
 * 在线用户结果。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
public record UserOnlineResult(String tokenId, String deptName, String userName,
        String ipaddr, String loginLocation, String browser, String os, Long loginTime)
{
    @Override
    public String toString()
    {
        return "UserOnlineResult[deptName=" + deptName + ", userName=" + userName
                + ", loginLocation=" + loginLocation + ", browser=" + browser + ", os=" + os + "]";
    }
}
