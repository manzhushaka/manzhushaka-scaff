package com.manzhushaka.biz.pii.application.service;

import com.manzhushaka.biz.pii.application.command.CreateQrcodeCommand;
import com.manzhushaka.biz.pii.application.command.QrcodeTaxItemCommand;
import com.manzhushaka.biz.pii.application.service.impl.QrcodeServiceImpl;
import com.manzhushaka.biz.pii.domain.model.PayQrcode;
import com.manzhushaka.biz.pii.domain.model.PayQrcodeTaxItem;
import com.manzhushaka.biz.pii.domain.repository.PayQrcodeRepository;
import com.manzhushaka.biz.pii.domain.repository.PayQrcodeTaxItemRepository;
import com.manzhushaka.common.exception.ServiceException;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class QrcodeServiceTest {
    private final PayQrcodeRepository qrcodeRepository = mock(PayQrcodeRepository.class);
    private final PayQrcodeTaxItemRepository qrcodeTaxItemRepository = mock(PayQrcodeTaxItemRepository.class);
    private final QrcodeService service = new QrcodeServiceImpl(qrcodeRepository, qrcodeTaxItemRepository);

    @Test
    void createShouldThrowWhenCodeExists() {
        when(qrcodeRepository.findByCode("QR001")).thenReturn(Optional.of(new PayQrcode()));

        assertThatThrownBy(() -> service.create(createCommand()))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("二维码编码已存在");
    }

    @Test
    void createShouldBindTaxItemsAfterQrcodeCreated() {
        when(qrcodeRepository.findByCode("QR001")).thenReturn(Optional.empty());
        when(qrcodeRepository.insert(any(PayQrcode.class))).thenReturn(20L);

        service.create(createCommand());

        verify(qrcodeRepository).insert(any(PayQrcode.class));
        verify(qrcodeTaxItemRepository).insert(any(PayQrcodeTaxItem.class));
    }

    private CreateQrcodeCommand createCommand() {
        return new CreateQrcodeCommand(1L, "QR001", "门店收款码", 1, null, "测试",
                List.of(new QrcodeTaxItemCommand(100L, 9900L)));
    }
}
