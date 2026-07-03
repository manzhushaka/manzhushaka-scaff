package com.manzhushaka.biz.pii.application.service;

import com.manzhushaka.biz.pii.application.command.UpdateMerchantConfigCommand;
import com.manzhushaka.biz.pii.application.result.MerchantConfigResult;
import com.manzhushaka.biz.pii.application.service.impl.MerchantConfigServiceImpl;
import com.manzhushaka.biz.pii.domain.model.MerchantProfile;
import com.manzhushaka.biz.pii.domain.repository.MerchantProfileRepository;
import com.manzhushaka.common.exception.ServiceException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MerchantConfigServiceTest {

    private final MerchantProfileRepository merchantProfileRepository = mock(MerchantProfileRepository.class);
    private final MerchantConfigService service = new MerchantConfigServiceImpl(merchantProfileRepository);

    @Test
    void getByDeptIdShouldReturnMaskedSensitiveKeys() {
        MerchantProfile profile = profile();
        when(merchantProfileRepository.findByDeptId(10L)).thenReturn(Optional.of(profile));

        MerchantConfigResult result = service.getByDeptId(10L);

        Assertions.assertEquals("*******", result.getUmsPaySignKeyMasked());
        Assertions.assertEquals("***********", result.getUmsInvoiceSignKeyMasked());
    }

    @Test
    void updateShouldKeepBlankKeysAndUpdateNonBlankKey() {
        MerchantProfile profile = profile();
        when(merchantProfileRepository.findByDeptId(10L)).thenReturn(Optional.of(profile));

        service.update(new UpdateMerchantConfigCommand(10L, "MID", "TID", "", "NEW_INV_KEY", "MSG2",
                "销方名称", "91310000", "上海市", "02100000000", "开户行", "6222",
                "收款人", "复核人", "开票人", "https://notify.example.com", "备注"));

        ArgumentCaptor<MerchantProfile> captor = ArgumentCaptor.forClass(MerchantProfile.class);
        verify(merchantProfileRepository).updateById(captor.capture());
        MerchantProfile updated = captor.getValue();
        Assertions.assertEquals("PAY_KEY", updated.getUmsPaySignKeyEnc());
        Assertions.assertEquals("NEW_INV_KEY", updated.getUmsInvoiceSignKeyEnc());
        Assertions.assertEquals("销方名称", updated.getInvoiceSellerName());
    }

    @Test
    void getByDeptIdShouldThrowWhenMerchantMissing() {
        when(merchantProfileRepository.findByDeptId(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getByDeptId(10L))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("商户参数不存在");
    }

    private MerchantProfile profile() {
        MerchantProfile profile = new MerchantProfile();
        profile.setId(1L);
        profile.setDeptId(10L);
        profile.setMerchantName("测试商户");
        profile.setUmsMerchantId("MID");
        profile.setUmsTerminalId("TID");
        profile.setUmsPaySignKeyEnc("PAY_KEY");
        profile.setUmsInvoiceSignKeyEnc("INVOICE_KEY");
        profile.setInvoiceMsgSrc("MSG");
        return profile;
    }
}
