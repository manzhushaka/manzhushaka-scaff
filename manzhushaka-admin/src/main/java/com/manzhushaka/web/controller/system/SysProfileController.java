package com.manzhushaka.web.controller.system;

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
import com.manzhushaka.common.utils.file.FileUploadUtils;
import com.manzhushaka.common.utils.file.FileUtils;
import com.manzhushaka.common.utils.file.MimeTypeUtils;
import com.manzhushaka.framework.security.context.SecurityContextHelper;
import com.manzhushaka.framework.security.model.LoginPrincipal;
import com.manzhushaka.framework.web.service.TokenService;
import com.manzhushaka.system.application.command.UpdateOwnPasswordCommand;
import com.manzhushaka.system.application.command.UpdateProfileCommand;
import com.manzhushaka.system.application.result.system.UserResult;
import com.manzhushaka.system.application.service.SystemUserAppService;
import com.manzhushaka.web.dto.system.user.UpdateOwnPasswordRequest;
import com.manzhushaka.web.dto.system.user.UpdateProfileRequest;

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
    private SystemUserAppService userAppService;

    @Autowired
    private TokenService tokenService;

    /**
     * 个人信息
     */
    @GetMapping
    public AjaxResult profile()
    {
        LoginPrincipal principal = SecurityContextHelper.getPrincipal();
        UserResult user = userAppService.getUserResult(principal.getUserId());
        AjaxResult ajax = AjaxResult.success(user);
        ajax.put("roleGroup", userAppService.getUserRoleGroup(principal.getUsername()));
        return ajax;
    }

    /**
     * 修改用户
     */
    @Log(title = "个人信息", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult updateProfile(@RequestBody UpdateProfileRequest request)
    {
        LoginPrincipal principal = SecurityContextHelper.getPrincipal();
        userAppService.updateProfile(new UpdateProfileCommand(principal.getUserId(),
                principal.getUsername(), request.getNickName(), request.getEmail(),
                request.getPhonenumber(), request.getSex()));
        return success();
    }

    /**
     * 重置密码
     */
    @Log(title = "个人信息", businessType = BusinessType.UPDATE)
    @PutMapping("/updatePwd")
    public AjaxResult updatePwd(@RequestBody UpdateOwnPasswordRequest request)
    {
        LoginPrincipal principal = SecurityContextHelper.getPrincipal();
        String encryptedPassword = userAppService.updateOwnPassword(new UpdateOwnPasswordCommand(
                principal.getUserId(), principal.getUsername(), request.getOldPassword(),
                request.getNewPassword()));
        if (SecurityContextHelper.getPrincipalQuietly() != null)
        {
            LoginPrincipal refreshed = SecurityContextHelper.getPrincipal();
            refreshed.setPassword(encryptedPassword);
            tokenService.setLoginUser(refreshed);
        }
        return success();
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
            String avatar = FileUploadUtils.upload(ManzhushakaConfig.getAvatarPath(), file,
                    MimeTypeUtils.IMAGE_EXTENSION, true);
            if (userAppService.updateAvatar(principal.getUserId(), avatar))
            {
                AjaxResult ajax = AjaxResult.success();
                ajax.put("imgUrl", avatar);
                return ajax;
            }
        }
        return error("上传图片异常，请联系管理员");
    }
}
