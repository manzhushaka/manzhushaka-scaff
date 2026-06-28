package com.manzhushaka.common.core.controller;

import java.beans.PropertyEditorSupport;
import java.util.Date;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.manzhushaka.common.constant.HttpStatus;
import com.manzhushaka.common.core.domain.AjaxResult;
import com.manzhushaka.common.core.page.PageDomain;
import com.manzhushaka.common.core.page.TableDataInfo;
import com.manzhushaka.common.core.page.TableSupport;
import com.manzhushaka.common.utils.DateUtils;
import com.manzhushaka.common.utils.PageUtils;
import com.manzhushaka.common.utils.StringUtils;
import com.manzhushaka.common.utils.sql.SqlUtil;

/**
 * web层通用数据处理
 * 
 * @author manzhushaka
 */
public class BaseController
{
    protected final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * 将前台传递过来的日期格式的字符串，自动转化为Date类型
     */
    @InitBinder
    public void initBinder(WebDataBinder binder)
    {
        // Date 类型转换
        binder.registerCustomEditor(Date.class, new PropertyEditorSupport()
        {
            @Override
            public void setAsText(String text)
            {
                setValue(DateUtils.parseDate(text));
            }
        });
    }

    /**
     * 设置请求分页数据
     */
    protected void startPage()
    {
        PageUtils.startPage();
    }

    /**
     * 设置请求排序数据
     */
    protected void startOrderBy()
    {
        PageDomain pageDomain = TableSupport.buildPageRequest();
        if (StringUtils.isNotEmpty(pageDomain.getOrderBy()))
        {
            String orderBy = SqlUtil.escapeOrderBySql(pageDomain.getOrderBy());
            PageHelper.orderBy(orderBy);
        }
    }

    /**
     * 清理分页的线程变量
     */
    protected void clearPage()
    {
        PageUtils.clearPage();
    }

    /**
     * 响应请求分页数据
     */
    @SuppressWarnings({ "rawtypes", "unchecked" })
    protected TableDataInfo getDataTable(List<?> list)
    {
        TableDataInfo rspData = new TableDataInfo();
        rspData.setCode(HttpStatus.SUCCESS);
        rspData.setMsg("查询成功");
        rspData.setRows(list);
        rspData.setTotal(new PageInfo(list).getTotal());
        return rspData;
    }

    /**
     * 返回成功
     */
    public AjaxResult success()
    {
        return AjaxResult.success();
    }

    /**
     * 返回失败消息
     */
    public AjaxResult error()
    {
        return AjaxResult.error();
    }

    /**
     * 返回成功消息
     */
    public AjaxResult success(String message)
    {
        return AjaxResult.success(message);
    }
    
    /**
     * 返回成功消息
     */
    public AjaxResult success(Object data)
    {
        return AjaxResult.success(data);
    }

    /**
     * 返回失败消息
     */
    public AjaxResult error(String message)
    {
        return AjaxResult.error(message);
    }

    /**
     * 返回警告消息
     */
    public AjaxResult warn(String message)
    {
        return AjaxResult.warn(message);
    }

    /**
     * 响应返回结果
     * 
     * @param rows 影响行数
     * @return 操作结果
     */
    protected AjaxResult toAjax(int rows)
    {
        return rows > 0 ? AjaxResult.success() : AjaxResult.error();
    }

    /**
     * 响应返回结果
     * 
     * @param result 结果
     * @return 操作结果
     */
    protected AjaxResult toAjax(boolean result)
    {
        return result ? success() : error();
    }

    /**
     * 页面跳转
     */
    public String redirect(String url)
    {
        return StringUtils.format("redirect:{}", url);
    }

    /**
     * 获取用户缓存信息
     * 
     * @deprecated 请使用 {@code SecurityContextHelper.getPrincipal()} 或直接调用 getUserId/getDeptId/getUsername
     */
    @Deprecated
    public Object getLoginUser()
    {
        return getUserId();
    }

    /**
     * 获取登录用户id
     */
    public Long getUserId()
    {
        return getPrincipalField("getUserId");
    }

    /**
     * 获取登录部门id
     */
    public Long getDeptId()
    {
        return getPrincipalField("getDeptId");
    }

    /**
     * 获取登录用户名
     */
    public String getUsername()
    {
        return getPrincipalField("getUsername");
    }

    /**
     * 通过反射从安全上下文获取 LoginPrincipal 的字段值
     * <p>
     * common 模块不直接依赖 framework，使用反射避免编译期依赖。
     * 运行时 LoginPrincipal 一定在 classpath 上。
     */
    @SuppressWarnings("unchecked")
    private static <T> T getPrincipalField(String methodName)
    {
        try
        {
            Object auth = org.springframework.security.core.context.SecurityContextHolder
                    .getContext().getAuthentication();
            if (auth == null)
            {
                return null;
            }
            java.lang.reflect.Method getPrincipalMethod = auth.getClass().getMethod("getPrincipal");
            Object principal = getPrincipalMethod.invoke(auth);
            if (principal == null)
            {
                return null;
            }
            java.lang.reflect.Method fieldMethod = principal.getClass().getMethod(methodName);
            return (T) fieldMethod.invoke(principal);
        }
        catch (Exception e)
        {
            return null;
        }
    }
}
