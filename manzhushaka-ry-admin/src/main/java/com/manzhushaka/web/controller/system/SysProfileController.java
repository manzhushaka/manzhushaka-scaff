package com.manzhushaka.web.controller.system;

import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import com.manzhushaka.common.annotation.Log;
import com.manzhushaka.common.config.ManzhushakaConfig;
import com.manzhushaka.common.core.controller.BaseController;
import com.manzhushaka.common.core.domain.AjaxResult;
import com.manzhushaka.common.enums.BusinessType;
import com.manzhushaka.common.utils.DateUtils;
import com.manzhushaka.common.utils.StringUtils;
import com.manzhushaka.common.utils.file.FileUploadUtils;
import com.manzhushaka.common.utils.file.FileUtils;
import com.manzhushaka.common.utils.file.MimeTypeUtils;
import com.manzhushaka.common.utils.security.PasswordUtils;
import com.manzhushaka.framework.security.context.SecurityContextHelper;
import com.manzhushaka.framework.security.model.LoginPrincipal;
import com.manzhushaka.framework.web.service.TokenService;
import com.manzhushaka.system.infrastructure.persistence.entity.SysUser;
import com.manzhushaka.system.service.ISysUserService;

/**
 * 个人信息 业务处理
 * 
 * @author manzhushaka
 */
@RestController
@RequestMapping("/system/user/profile")
public class SysProfileController extends BaseController
{
    @Autowired
    private ISysUserService userService;

    @Autowired
    private TokenService tokenService;

    /**
     * 个人信息
     */
    @GetMapping
    public AjaxResult profile()
    {
        LoginPrincipal principal = SecurityContextHelper.getPrincipal();
        SysUser user = userService.selectUserById(principal.getUserId());
        AjaxResult ajax = AjaxResult.success(user);
        ajax.put("roleGroup", userService.selectUserRoleGroup(principal.getUsername()));
        ajax.put("postGroup", userService.selectUserPostGroup(principal.getUsername()));
        return ajax;
    }

    /**
     * 修改用户
     */
    @Log(title = "个人信息", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult updateProfile(@RequestBody SysUser user)
    {
        LoginPrincipal principal = SecurityContextHelper.getPrincipal();
        SysUser currentUser = userService.selectUserById(principal.getUserId());
        currentUser.setNickName(user.getNickName());
        currentUser.setEmail(user.getEmail());
        currentUser.setPhonenumber(user.getPhonenumber());
        currentUser.setSex(user.getSex());
        if (StringUtils.isNotEmpty(user.getPhonenumber()) && !userService.checkPhoneUnique(currentUser))
        {
            return error("修改用户'" + principal.getUsername() + "'失败，手机号码已存在");
        }
        if (StringUtils.isNotEmpty(user.getEmail()) && !userService.checkEmailUnique(currentUser))
        {
            return error("修改用户'" + principal.getUsername() + "'失败，邮箱账号已存在");
        }
        if (userService.updateUserProfile(currentUser) > 0)
        {
            return success();
        }
        return error("修改个人信息异常，请联系管理员");
    }

    /**
     * 重置密码
     */
    @Log(title = "个人信息", businessType = BusinessType.UPDATE)
    @PutMapping("/updatePwd")
    public AjaxResult updatePwd(@RequestBody Map<String, String> params)
    {
        String oldPassword = params.get("oldPassword");
        String newPassword = params.get("newPassword");
        LoginPrincipal principal = SecurityContextHelper.getPrincipal();
        Long userId = principal.getUserId();
        SysUser user = userService.selectUserById(userId);
        String password = user.getPassword();
        if (!PasswordUtils.matches(oldPassword, password))
        {
            return error("修改密码失败，旧密码错误");
        }
        if (PasswordUtils.matches(newPassword, password))
        {
            return error("新密码不能与旧密码相同");
        }
        newPassword = PasswordUtils.encrypt(newPassword);
        if (userService.resetUserPwd(userId, newPassword) > 0)
        {
            // 更新缓存中的密码
            if (SecurityContextHelper.getPrincipalQuietly() != null)
            {
                LoginPrincipal refreshed = SecurityContextHelper.getPrincipal();
                refreshed.setPassword(newPassword);
                tokenService.setLoginUser(refreshed);
            }
            return success();
        }
        return error("修改密码异常，请联系管理员");
    }

    /**
     * 头像上传
     */
    @Log(title = "用户头像", businessType = BusinessType.UPDATE)
    @PostMapping("/avatar")
    public AjaxResult avatar(@RequestParam("avatarfile") MultipartFile file) throws Exception
    {
        if (!file.isEmpty())
        {
            LoginPrincipal principal = SecurityContextHelper.getPrincipal();
            String avatar = FileUploadUtils.upload(ManzhushakaConfig.getAvatarPath(), file, MimeTypeUtils.IMAGE_EXTENSION, true);
            if (userService.updateUserAvatar(principal.getUserId(), avatar))
            {
                AjaxResult ajax = AjaxResult.success();
                ajax.put("imgUrl", avatar);
                return ajax;
            }
        }
        return error("上传图片异常，请联系管理员");
    }
}
