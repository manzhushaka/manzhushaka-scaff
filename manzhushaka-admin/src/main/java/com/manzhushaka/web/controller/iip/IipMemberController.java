package com.manzhushaka.web.controller.iip;

import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.manzhushaka.common.annotation.Log;
import com.manzhushaka.common.core.controller.BaseController;
import com.manzhushaka.common.core.domain.AjaxResult;
import com.manzhushaka.common.core.page.TableDataInfo;
import com.manzhushaka.common.enums.BusinessType;
import com.manzhushaka.common.utils.poi.ExcelUtil;
import com.manzhushaka.framework.security.context.SecurityContextHelper;
import com.manzhushaka.iip.application.member.result.MemberResult;
import com.manzhushaka.iip.application.member.service.IipMemberAppService;
import com.manzhushaka.web.converter.iip.MemberAdminConverter;
import com.manzhushaka.web.dto.iip.MemberRequest;
import com.manzhushaka.web.dto.iip.MemberStatusRequest;

/**
 * 小程序用户 信息操作处理
 * 
 * @author manzhushaka
 */
@RestController
@RequestMapping("/iip/member")
public class IipMemberController extends BaseController
{
    @Autowired
    private IipMemberAppService memberAppService;

    /**
     * 获取用户列表
     */
    @PreAuthorize("@ss.hasPermi('iip:member:list')")
    @GetMapping("/list")
    public TableDataInfo list(MemberRequest request)
    {
        startPage();
        List<MemberResult> list = memberAppService.listMembers(MemberAdminConverter.toQuery(request));
        return getDataTable(list);
    }

    /**
     * 根据用户ID获取详细信息
     */
    @PreAuthorize("@ss.hasPermi('iip:member:query')")
    @GetMapping(value = "/getInfo/{memberId}")
    public AjaxResult getInfo(@PathVariable Long memberId)
    {
        return success(memberAppService.getMember(memberId));
    }

    /**
     * 修改用户状态（启停切换）
     */
    @PreAuthorize("@ss.hasPermi('iip:member:edit')")
    @Log(title = "用户管理", businessType = BusinessType.UPDATE)
    @PutMapping("/status")
    public AjaxResult changeStatus(@Validated @RequestBody MemberStatusRequest request)
    {
        return toAjax(memberAppService.changeMemberStatus(MemberAdminConverter.toCommand(request),
                SecurityContextHelper.getUsername()));
    }

    /**
     * 导出用户列表
     */
    @PreAuthorize("@ss.hasPermi('iip:member:export')")
    @Log(title = "用户管理", businessType = BusinessType.EXPORT)
    @GetMapping("/export")
    public void export(HttpServletResponse response, MemberRequest request)
    {
        List<MemberResult> list = memberAppService.listMembers(MemberAdminConverter.toQuery(request));
        ExcelUtil<MemberResult> util = new ExcelUtil<MemberResult>(MemberResult.class);
        util.exportExcel(response, list, "用户数据");
    }
}
