package com.manzhushaka.framework.security.handle;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import com.alibaba.fastjson2.JSON;
import com.manzhushaka.common.constant.Constants;
import com.manzhushaka.common.core.domain.AjaxResult;
import com.manzhushaka.common.utils.MessageUtils;
import com.manzhushaka.common.utils.ServletUtils;
import com.manzhushaka.common.utils.StringUtils;
import com.manzhushaka.framework.manager.AsyncManager;
import com.manzhushaka.framework.manager.factory.AsyncFactory;
import com.manzhushaka.framework.security.model.LoginPrincipal;
import com.manzhushaka.framework.web.service.TokenService;

/**
 * 自定义退出处理类 返回成功
 *
 * @author manzhushaka
 */
@Configuration
public class LogoutSuccessHandlerImpl implements LogoutSuccessHandler
{
    @Autowired
    private TokenService tokenService;

    /**
     * 退出处理
     *
     * @return
     */
    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication)
            throws IOException, ServletException
    {
        LoginPrincipal loginPrincipal = tokenService.getLoginUser(request);
        if (StringUtils.isNotNull(loginPrincipal))
        {
            String userName = loginPrincipal.getUsername();
            // 删除用户缓存记录
            tokenService.delLoginUser(loginPrincipal.getToken());
            // 记录用户退出日志
            AsyncManager.me().execute(AsyncFactory.recordLogininfor(userName, Constants.LOGOUT, MessageUtils.message("user.logout.success")));
        }
        ServletUtils.renderString(response, JSON.toJSONString(AjaxResult.success(MessageUtils.message("user.logout.success"))));
    }
}
