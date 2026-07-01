package com.manzhushaka.system.infrastructure.persistence.support;

import com.manzhushaka.common.crypto.SensitiveFieldCryptoHolder;
import com.manzhushaka.common.utils.StringUtils;
import com.manzhushaka.system.infrastructure.persistence.entity.SysUser;

/**
 * 用户敏感字段检索摘要处理。
 *
 * @author manzhushaka
 */
public final class SysUserSensitiveFieldSupport {

    /**
     * 填充用户敏感字段检索摘要。
     *
     * @param user 用户对象
     */
    public static void fillHashes(SysUser user)
    {
        if (user == null)
        {
            return;
        }
        if (StringUtils.isNotEmpty(user.getEmail()))
        {
            user.setEmailHash(SensitiveFieldCryptoHolder.hash(user.getEmail()));
        }
        if (StringUtils.isNotEmpty(user.getPhonenumber()))
        {
            user.setPhonenumberHash(SensitiveFieldCryptoHolder.hash(user.getPhonenumber()));
        }
    }

    private SysUserSensitiveFieldSupport()
    {
        // 工具类，防止实例化
    }
}