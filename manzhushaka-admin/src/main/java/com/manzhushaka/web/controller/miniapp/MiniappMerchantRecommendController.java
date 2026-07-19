package com.manzhushaka.web.controller.miniapp;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.manzhushaka.common.annotation.Anonymous;
import com.manzhushaka.common.annotation.Log;
import com.manzhushaka.common.core.controller.BaseController;
import com.manzhushaka.common.core.domain.AjaxResult;
import com.manzhushaka.common.enums.BusinessType;
import com.manzhushaka.iip.application.result.merchant.MiniappMerchantRecommendResult;
import com.manzhushaka.iip.domain.IipMerchant;
import com.manzhushaka.iip.service.IIipMerchantService;

/**
 * 小程序推荐商户 信息操作处理
 *
 * @author manzhushaka
 * @date 2026-07-19
 */
@RestController
@RequestMapping("/miniapp/merchant")
public class MiniappMerchantRecommendController extends BaseController
{
    /** 推荐商户默认返回条数 */
    private static final int RECOMMEND_DEFAULT_LIMIT = 6;

    /** 推荐商户最大返回条数 */
    private static final int RECOMMEND_MAX_LIMIT = 20;

    @Autowired
    private IIipMerchantService iipMerchantService;

    /**
     * 查询推荐商户列表。
     * 仅返回 status='0' 正常且 is_recommend='0' 推荐的商户，按 update_time desc, merchant_id desc；
     * 游客可访问；返回字段裁剪为 merchantId、merchantName、category、city、logo、description、
     * businessHours、address，不输出联系方式、绑定用户与审计信息。
     *
     * @param category 商户类别，可选，精确匹配
     * @param excludeCategory 排除的商户类别，可选
     * @param limit 返回条数，可选，默认 6，超过 20 按 20 处理
     * @return 推荐商户列表，无推荐商户时返回空集合
     */
    @Anonymous
    @Log(title = "小程序查询推荐商户列表", businessType = BusinessType.OTHER,
            isSaveRequestData = false, isSaveResponseData = false)
    @GetMapping("/recommend")
    public AjaxResult recommend(@RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "excludeCategory", required = false) String excludeCategory,
            @RequestParam(value = "limit", required = false) Integer limit)
    {
        int queryLimit = limit == null || limit < 1 ? RECOMMEND_DEFAULT_LIMIT
                : Math.min(limit, RECOMMEND_MAX_LIMIT);
        List<IipMerchant> list = iipMerchantService.selectRecommendMerchantList(category, excludeCategory,
                queryLimit);
        return success(list.stream().map(MiniappMerchantRecommendResult::from).toList());
    }
}
