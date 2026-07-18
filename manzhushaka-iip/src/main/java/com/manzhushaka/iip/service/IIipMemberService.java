package com.manzhushaka.iip.service;

import com.manzhushaka.iip.domain.IipMember;

/**
 * 用户跨域契约 服务层（骨架阶段只定义签名，实现归用户域代理）
 * 
 * @author manzhushaka
 * @date 2026-07-18
 */
public interface IIipMemberService
{
    /**
     * 按平台与openid查询用户，不存在则创建用户及平台账号（小程序登录入口）
     * 
     * @param platform 平台（wechat/alipay/unionpay）
     * @param openid 平台用户标识
     * @param unionid 平台联合标识，可为null
     * @param nickname 昵称，可为null
     * @param avatar 头像，可为null
     * @return 用户信息（新建时携带新分配的 memberId）
     */
    public IipMember getOrCreateByOpenid(String platform, String openid, String unionid, String nickname, String avatar);

    /**
     * 通过ID查询用户
     * 
     * @param memberId 用户ID
     * @return 用户信息，不存在时返回null
     */
    public IipMember selectMemberById(Long memberId);
}
