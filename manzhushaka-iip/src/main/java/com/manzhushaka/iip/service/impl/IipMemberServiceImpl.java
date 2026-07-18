package com.manzhushaka.iip.service.impl;

import java.util.List;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;
import com.manzhushaka.common.exception.ServiceException;
import com.manzhushaka.common.utils.DateUtils;
import com.manzhushaka.common.utils.StringUtils;
import com.manzhushaka.iip.domain.IipMember;
import com.manzhushaka.iip.domain.IipMemberAccount;
import com.manzhushaka.iip.mapper.IipMemberAccountMapper;
import com.manzhushaka.iip.mapper.IipMemberMapper;
import com.manzhushaka.iip.service.IIipMemberService;

/**
 * 小程序用户 服务层实现（实现跨域契约 IIipMemberService，并承载本域后台管理与登录轨迹能力）
 * 
 * @author manzhushaka
 * @date 2026-07-18
 */
@Service
public class IipMemberServiceImpl implements IIipMemberService
{
    /** 新建用户默认昵称 */
    private static final String DEFAULT_NICKNAME = "小程序用户";

    /** 性别：未知 */
    private static final String GENDER_UNKNOWN = "2";

    /** 状态：正常 */
    private static final String STATUS_NORMAL = "0";

    @Autowired
    private IipMemberMapper memberMapper;

    @Autowired
    private IipMemberAccountMapper memberAccountMapper;

    @Autowired
    private PlatformTransactionManager transactionManager;

    /** 独立事务模板（REQUIRES_NEW），保证并发冲突回滚后可在新快照中重查 */
    private TransactionTemplate requiresNewTemplate;

    /**
     * 初始化独立事务模板
     */
    @PostConstruct
    public void init()
    {
        requiresNewTemplate = new TransactionTemplate(transactionManager);
        requiresNewTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
    }

    /**
     * 按平台与openid查询用户，不存在则创建用户及平台账号（小程序登录入口）
     * 
     * 并发安全：先查后插，依赖 uk_platform_openid 唯一键兜底；插入在 REQUIRES_NEW 独立事务中执行，
     * 命中唯一键冲突时独立事务整体回滚（避免留下无账号的孤儿用户），随后在独立事务中重查已存在账号。
     * 方法本身不加 @Transactional，避免 REPEATABLE READ 快照导致冲突后重查不到他事务已提交的数据。
     * 
     * @param platform 平台（wechat/alipay/unionpay）
     * @param openid 平台用户标识
     * @param unionid 平台联合标识，可为null
     * @param nickname 昵称，为空时使用默认昵称
     * @param avatar 头像，可为null
     * @return 用户信息（新建时携带新分配的 memberId）
     */
    @Override
    public IipMember getOrCreateByOpenid(String platform, String openid, String unionid, String nickname, String avatar)
    {
        if (StringUtils.isBlank(platform) || StringUtils.isBlank(openid))
        {
            throw new ServiceException("平台与openid不能为空");
        }
        IipMemberAccount account = memberAccountMapper.selectByPlatformOpenid(platform, openid);
        if (account != null)
        {
            return memberMapper.selectIipMemberById(account.getMemberId());
        }
        try
        {
            return requiresNewTemplate.execute(status -> insertMemberWithAccount(platform, openid, unionid, nickname, avatar));
        }
        catch (DuplicateKeyException e)
        {
            // 并发重复注册：独立事务已回滚，重新查询已存在的平台账号
            IipMemberAccount existAccount = memberAccountMapper.selectByPlatformOpenid(platform, openid);
            if (existAccount == null)
            {
                throw e;
            }
            return memberMapper.selectIipMemberById(existAccount.getMemberId());
        }
    }

    /**
     * 通过ID查询用户
     * 
     * @param memberId 用户ID
     * @return 用户信息，不存在时返回null
     */
    @Override
    public IipMember selectMemberById(Long memberId)
    {
        return memberMapper.selectIipMemberById(memberId);
    }

    /**
     * 查询用户列表（后台管理）
     * 
     * @param member 查询条件（params 支持 keyword/beginTime/endTime）
     * @return 用户集合
     */
    public List<IipMember> selectMemberList(IipMember member)
    {
        return memberMapper.selectIipMemberList(member);
    }

    /**
     * 修改用户状态（后台启停切换）
     * 
     * @param memberId 用户ID
     * @param status 状态（0正常 1停用）
     * @param updateBy 操作人账号
     * @return 影响行数
     */
    @Transactional
    public int updateMemberStatus(Long memberId, String status, String updateBy)
    {
        IipMember member = new IipMember();
        member.setMemberId(memberId);
        member.setStatus(status);
        member.setUpdateBy(updateBy);
        return memberMapper.updateIipMember(member);
    }

    /**
     * 更新最近登录时间与登录IP（小程序登录成功后调用）
     * 
     * @param memberId 用户ID
     * @param loginIp 登录IP
     * @return 影响行数
     */
    @Transactional
    public int updateLoginTrace(Long memberId, String loginIp)
    {
        IipMember member = new IipMember();
        member.setMemberId(memberId);
        member.setLastLoginTime(DateUtils.getNowDate());
        member.setLastLoginIp(loginIp);
        return memberMapper.updateIipMember(member);
    }

    /**
     * 插入用户及平台账号（独立事务内执行）
     * 
     * @param platform 平台
     * @param openid 平台用户标识
     * @param unionid 平台联合标识
     * @param nickname 昵称
     * @param avatar 头像
     * @return 新建用户信息
     */
    private IipMember insertMemberWithAccount(String platform, String openid, String unionid, String nickname, String avatar)
    {
        IipMember member = new IipMember();
        member.setNickname(StringUtils.isBlank(nickname) ? DEFAULT_NICKNAME : nickname);
        member.setAvatar(avatar == null ? "" : avatar);
        member.setGender(GENDER_UNKNOWN);
        member.setStatus(STATUS_NORMAL);
        memberMapper.insertIipMember(member);

        IipMemberAccount account = new IipMemberAccount();
        account.setMemberId(member.getMemberId());
        account.setPlatform(platform);
        account.setOpenid(openid);
        account.setUnionid(unionid);
        memberAccountMapper.insertIipMemberAccount(account);
        return member;
    }
}
