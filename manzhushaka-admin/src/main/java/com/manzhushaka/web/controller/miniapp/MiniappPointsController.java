package com.manzhushaka.web.controller.miniapp;

import java.util.List;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.manzhushaka.common.annotation.Log;
import com.manzhushaka.common.core.controller.BaseController;
import com.manzhushaka.common.core.page.TableDataInfo;
import com.manzhushaka.common.enums.BusinessType;
import com.manzhushaka.common.exception.ServiceException;
import com.manzhushaka.common.utils.StringUtils;
import com.manzhushaka.framework.security.context.SecurityContextHelper;
import com.manzhushaka.iip.application.points.result.PointsRecordResult;
import com.manzhushaka.iip.application.points.service.PointsAdminAppService;

/**
 * 小程序积分 信息操作处理（小程序端）。
 *
 * /miniapp/** 默认要求登录 token，接口内通过 SecurityContextHelper.getUserId() 取当前用户ID，
 * 只允许查询本人积分明细，不使用后台 perms 体系。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
@RestController
@RequestMapping("/miniapp/points")
public class MiniappPointsController extends BaseController
{
    /** 积分变动类型白名单：earn 获得、consume 消费、expire 过期、adjust 调整 */
    private static final Set<String> CHANGE_TYPE_WHITELIST = Set.of("earn", "consume", "expire", "adjust");

    @Autowired
    private PointsAdminAppService pointsAdminAppService;

    /**
     * 查询当前用户的积分明细（分页）。
     *
     * 分页参数 pageNum/pageSize 由请求查询参数传入（与后台列表一致，由 startPage 读取）；
     * changeType 可选，仅允许 earn/consume/expire/adjust，其余值直接拒绝；
     * 返回 TableDataInfo（rows 为积分明细列表，total 为总数）。
     */
    @Log(title = "小程序积分", businessType = BusinessType.OTHER)
    @GetMapping("/records")
    public TableDataInfo records(@RequestParam(value = "changeType", required = false) String changeType)
    {
        Long memberId = SecurityContextHelper.getUserId();
        if (StringUtils.isNotBlank(changeType) && !CHANGE_TYPE_WHITELIST.contains(changeType))
        {
            throw new ServiceException("不支持的积分变动类型");
        }
        startPage();
        List<PointsRecordResult> list = pointsAdminAppService.listMemberRecords(memberId, changeType);
        return getDataTable(list);
    }
}
