package com.manzhushaka.web.controller.pii;

import com.manzhushaka.biz.pii.application.command.ChangeMerchantStatusCommand;
import com.manzhushaka.biz.pii.application.command.CreateMerchantCommand;
import com.manzhushaka.biz.pii.application.command.UpdateMerchantCommand;
import com.manzhushaka.biz.pii.application.query.MerchantPageQuery;
import com.manzhushaka.biz.pii.application.service.MerchantService;
import com.manzhushaka.common.annotation.Log;
import com.manzhushaka.common.core.controller.BaseController;
import com.manzhushaka.common.core.domain.AjaxResult;
import com.manzhushaka.common.core.page.TableDataInfo;
import com.manzhushaka.common.enums.BusinessType;
import com.manzhushaka.web.dto.pii.ChangeStatusRequest;
import com.manzhushaka.web.dto.pii.CreateMerchantRequest;
import com.manzhushaka.web.dto.pii.MerchantPageRequest;
import com.manzhushaka.web.dto.pii.UpdateMerchantRequest;
import com.manzhushaka.web.vo.pii.MerchantVO;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/pii/merchant")
public class MerchantController extends BaseController {

    private final MerchantService merchantService;

    public MerchantController(MerchantService merchantService) {
        this.merchantService = merchantService;
    }

    @PreAuthorize("@ss.hasPermi('biz:merchant:list')")
    @GetMapping("/list")
    public TableDataInfo list(MerchantPageRequest request) {
        startPage();
        List<MerchantVO> rows = merchantService.page(new MerchantPageQuery(
                        request.getMerchantName(), request.getUmsMerchantId(), request.getStatus()))
                .stream().map(MerchantVO::from).collect(Collectors.toList());
        return getDataTable(rows);
    }

    @PreAuthorize("@ss.hasPermi('biz:merchant:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Long id) {
        return success(MerchantVO.from(merchantService.get(id)));
    }

    @PreAuthorize("@ss.hasPermi('biz:merchant:add')")
    @Log(title = "商户管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody CreateMerchantRequest request) {
        return success(merchantService.create(toCreateCommand(request)));
    }

    @PreAuthorize("@ss.hasPermi('biz:merchant:edit')")
    @Log(title = "商户管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody UpdateMerchantRequest request) {
        return toAjax(merchantService.update(toUpdateCommand(request)));
    }

    @PreAuthorize("@ss.hasPermi('biz:merchant:remove')")
    @Log(title = "商户管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        Arrays.stream(ids).forEach(merchantService::delete);
        return success();
    }

    @PreAuthorize("@ss.hasPermi('biz:merchant:changeStatus')")
    @Log(title = "商户管理", businessType = BusinessType.UPDATE)
    @PutMapping("/changeStatus")
    public AjaxResult changeStatus(@Validated @RequestBody ChangeStatusRequest request) {
        return toAjax(merchantService.changeStatus(new ChangeMerchantStatusCommand(request.getId(), request.getStatus())));
    }

    private CreateMerchantCommand toCreateCommand(CreateMerchantRequest request) {
        return new CreateMerchantCommand(request.getParentDeptId(), request.getMerchantName(),
                request.getAdminUserName(), request.getAdminPassword(), request.getAdminPhone(),
                request.getAdminEmail(), request.getUmsMerchantId(), request.getUmsTerminalId(),
                request.getUmsPaySignKey(), request.getUmsInvoiceSignKey(), request.getInvoiceMsgSrc(),
                request.getStatus(), request.getRemark());
    }

    private UpdateMerchantCommand toUpdateCommand(UpdateMerchantRequest request) {
        return new UpdateMerchantCommand(request.getId(), request.getMerchantName(), request.getUmsMerchantId(),
                request.getUmsTerminalId(), request.getUmsPaySignKey(), request.getUmsInvoiceSignKey(),
                request.getInvoiceMsgSrc(), request.getStatus(), request.getRemark());
    }
}
