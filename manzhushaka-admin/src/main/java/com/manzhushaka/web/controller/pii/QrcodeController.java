package com.manzhushaka.web.controller.pii;

import com.manzhushaka.biz.pii.application.command.ChangeQrcodeStatusCommand;
import com.manzhushaka.biz.pii.application.command.CreateQrcodeCommand;
import com.manzhushaka.biz.pii.application.command.QrcodeTaxItemCommand;
import com.manzhushaka.biz.pii.application.command.UpdateQrcodeCommand;
import com.manzhushaka.biz.pii.application.query.QrcodePageQuery;
import com.manzhushaka.biz.pii.application.service.QrcodeService;
import com.manzhushaka.common.annotation.Log;
import com.manzhushaka.common.core.controller.BaseController;
import com.manzhushaka.common.core.domain.AjaxResult;
import com.manzhushaka.common.core.page.TableDataInfo;
import com.manzhushaka.common.enums.BusinessType;
import com.manzhushaka.web.dto.pii.ChangeStatusRequest;
import com.manzhushaka.web.dto.pii.CreateQrcodeRequest;
import com.manzhushaka.web.dto.pii.QrcodePageRequest;
import com.manzhushaka.web.dto.pii.QrcodeTaxItemRequest;
import com.manzhushaka.web.dto.pii.UpdateQrcodeRequest;
import com.manzhushaka.web.vo.pii.QrcodeVO;
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
@RequestMapping("/pii/qrcode")
public class QrcodeController extends BaseController {

    private final QrcodeService qrcodeService;

    public QrcodeController(QrcodeService qrcodeService) {
        this.qrcodeService = qrcodeService;
    }

    @PreAuthorize("@ss.hasPermi('biz:qrcode:list')")
    @GetMapping("/list")
    public TableDataInfo list(QrcodePageRequest request) {
        startPage();
        List<QrcodeVO> rows = qrcodeService.page(new QrcodePageQuery(request.getMerchantId()))
                .stream().map(QrcodeVO::from).collect(Collectors.toList());
        return getDataTable(rows);
    }

    @PreAuthorize("@ss.hasPermi('biz:qrcode:query')")
    @GetMapping("/{id}")
    public AjaxResult getInfo(@PathVariable Long id) {
        return success(QrcodeVO.from(qrcodeService.get(id)));
    }

    @PreAuthorize("@ss.hasPermi('biz:qrcode:add')")
    @Log(title = "二维码管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody CreateQrcodeRequest request) {
        return success(qrcodeService.create(toCreateCommand(request)));
    }

    @PreAuthorize("@ss.hasPermi('biz:qrcode:edit')")
    @Log(title = "二维码管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody UpdateQrcodeRequest request) {
        return toAjax(qrcodeService.update(toUpdateCommand(request)));
    }

    @PreAuthorize("@ss.hasPermi('biz:qrcode:remove')")
    @Log(title = "二维码管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        Arrays.stream(ids).forEach(qrcodeService::delete);
        return success();
    }

    @PreAuthorize("@ss.hasPermi('biz:qrcode:changeStatus')")
    @Log(title = "二维码管理", businessType = BusinessType.UPDATE)
    @PutMapping("/changeStatus")
    public AjaxResult changeStatus(@Validated @RequestBody ChangeStatusRequest request) {
        return toAjax(qrcodeService.changeStatus(new ChangeQrcodeStatusCommand(request.getId(), request.getStatus())));
    }

    private CreateQrcodeCommand toCreateCommand(CreateQrcodeRequest request) {
        return new CreateQrcodeCommand(request.getMerchantId(), request.getQrcodeCode(), request.getName(),
                request.getStatus(), request.getExpireTime(), request.getRemark(), toTaxItemCommands(request.getTaxItems()));
    }

    private UpdateQrcodeCommand toUpdateCommand(UpdateQrcodeRequest request) {
        return new UpdateQrcodeCommand(request.getId(), request.getMerchantId(), request.getQrcodeCode(), request.getName(),
                request.getStatus(), request.getExpireTime(), request.getRemark(), toTaxItemCommands(request.getTaxItems()));
    }

    private List<QrcodeTaxItemCommand> toTaxItemCommands(List<QrcodeTaxItemRequest> taxItems) {
        return taxItems == null ? List.of() : taxItems.stream()
                .map(item -> new QrcodeTaxItemCommand(item.getTaxItemId(), item.getDefaultAmount()))
                .collect(Collectors.toList());
    }
}
