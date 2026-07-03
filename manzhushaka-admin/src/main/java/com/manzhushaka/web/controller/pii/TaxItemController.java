package com.manzhushaka.web.controller.pii;

import com.manzhushaka.biz.pii.application.command.ChangeTaxItemStatusCommand;
import com.manzhushaka.biz.pii.application.command.CreateTaxItemCommand;
import com.manzhushaka.biz.pii.application.command.UpdateTaxItemCommand;
import com.manzhushaka.biz.pii.application.query.TaxItemPageQuery;
import com.manzhushaka.biz.pii.application.service.TaxItemService;
import com.manzhushaka.common.annotation.Log;
import com.manzhushaka.common.core.controller.BaseController;
import com.manzhushaka.common.core.domain.AjaxResult;
import com.manzhushaka.common.core.page.TableDataInfo;
import com.manzhushaka.common.enums.BusinessType;
import com.manzhushaka.web.dto.pii.ChangeStatusRequest;
import com.manzhushaka.web.dto.pii.CreateTaxItemRequest;
import com.manzhushaka.web.dto.pii.TaxItemPageRequest;
import com.manzhushaka.web.dto.pii.UpdateTaxItemRequest;
import com.manzhushaka.web.vo.pii.TaxItemVO;
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
@RequestMapping("/pii/taxItem")
public class TaxItemController extends BaseController {

    private final TaxItemService taxItemService;

    public TaxItemController(TaxItemService taxItemService) {
        this.taxItemService = taxItemService;
    }

    @PreAuthorize("@ss.hasPermi('biz:taxItem:list')")
    @GetMapping("/list")
    public TableDataInfo list(TaxItemPageRequest request) {
        startPage();
        List<TaxItemVO> rows = taxItemService.page(new TaxItemPageQuery(
                        request.getTaxItemCode(), request.getName(), request.getStatus()))
                .stream().map(TaxItemVO::from).collect(Collectors.toList());
        return getDataTable(rows);
    }

    @PreAuthorize("@ss.hasPermi('biz:taxItem:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Long id) {
        return success(TaxItemVO.from(taxItemService.get(id)));
    }

    @PreAuthorize("@ss.hasPermi('biz:taxItem:add')")
    @Log(title = "税目管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody CreateTaxItemRequest request) {
        return success(taxItemService.create(toCreateCommand(request)));
    }

    @PreAuthorize("@ss.hasPermi('biz:taxItem:edit')")
    @Log(title = "税目管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody UpdateTaxItemRequest request) {
        return toAjax(taxItemService.update(toUpdateCommand(request)));
    }

    @PreAuthorize("@ss.hasPermi('biz:taxItem:remove')")
    @Log(title = "税目管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        Arrays.stream(ids).forEach(taxItemService::delete);
        return success();
    }

    @PreAuthorize("@ss.hasPermi('biz:taxItem:changeStatus')")
    @Log(title = "税目管理", businessType = BusinessType.UPDATE)
    @PutMapping("/changeStatus")
    public AjaxResult changeStatus(@Validated @RequestBody ChangeStatusRequest request) {
        return toAjax(taxItemService.changeStatus(new ChangeTaxItemStatusCommand(request.getId(), request.getStatus())));
    }

    private CreateTaxItemCommand toCreateCommand(CreateTaxItemRequest request) {
        return new CreateTaxItemCommand(request.getTaxItemCode(), request.getName(), request.getBrevityCode(),
                request.getCategory(), request.getTaxRate(), request.getVatSpecial(), request.getFreeTaxType(),
                request.getPreferPolicyFlag(), request.getSort(), request.getStatus(), request.getRemark());
    }

    private UpdateTaxItemCommand toUpdateCommand(UpdateTaxItemRequest request) {
        return new UpdateTaxItemCommand(request.getId(), request.getName(), request.getBrevityCode(),
                request.getCategory(), request.getTaxRate(), request.getVatSpecial(), request.getFreeTaxType(),
                request.getPreferPolicyFlag(), request.getSort(), request.getStatus(), request.getRemark());
    }
}
