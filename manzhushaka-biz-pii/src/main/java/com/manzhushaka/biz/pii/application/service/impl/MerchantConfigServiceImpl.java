package com.manzhushaka.biz.pii.application.service.impl;

import com.manzhushaka.biz.pii.application.command.UpdateMerchantConfigCommand;
import com.manzhushaka.biz.pii.application.result.MerchantConfigResult;
import com.manzhushaka.biz.pii.application.service.MerchantConfigService;
import com.manzhushaka.biz.pii.domain.model.MerchantProfile;
import com.manzhushaka.biz.pii.domain.repository.MerchantProfileRepository;
import com.manzhushaka.common.exception.ServiceException;
import com.manzhushaka.common.utils.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
public class MerchantConfigServiceImpl implements MerchantConfigService {

    private final MerchantProfileRepository merchantProfileRepository;

    public MerchantConfigServiceImpl(MerchantProfileRepository merchantProfileRepository) {
        this.merchantProfileRepository = merchantProfileRepository;
    }

    @Override
    public MerchantConfigResult getByDeptId(Long deptId) {
        return MerchantConfigResult.from(findByDeptId(deptId));
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int update(UpdateMerchantConfigCommand command) {
        MerchantProfile profile = findByDeptId(command.deptId());
        profile.setUmsMerchantId(command.umsMerchantId());
        profile.setUmsTerminalId(command.umsTerminalId());
        if (StringUtils.isNotBlank(command.umsPaySignKey())) {
            profile.setUmsPaySignKeyEnc(command.umsPaySignKey());
        }
        if (StringUtils.isNotBlank(command.umsInvoiceSignKey())) {
            profile.setUmsInvoiceSignKeyEnc(command.umsInvoiceSignKey());
        }
        profile.setInvoiceMsgSrc(command.invoiceMsgSrc());
        profile.setInvoiceSellerName(command.invoiceSellerName());
        profile.setInvoiceSellerTaxCode(command.invoiceSellerTaxCode());
        profile.setInvoiceSellerAddress(command.invoiceSellerAddress());
        profile.setInvoiceSellerTelephone(command.invoiceSellerTelephone());
        profile.setInvoiceSellerBank(command.invoiceSellerBank());
        profile.setInvoiceSellerAccount(command.invoiceSellerAccount());
        profile.setInvoicePayee(command.invoicePayee());
        profile.setInvoiceChecker(command.invoiceChecker());
        profile.setInvoiceDrawer(command.invoiceDrawer());
        profile.setNotifyUrl(command.notifyUrl());
        profile.setRemark(command.remark());
        profile.setUpdateTime(LocalDateTime.now());
        return merchantProfileRepository.updateById(profile);
    }

    private MerchantProfile findByDeptId(Long deptId) {
        return merchantProfileRepository.findByDeptId(deptId)
                .orElseThrow(() -> new ServiceException("商户参数不存在"));
    }
}
