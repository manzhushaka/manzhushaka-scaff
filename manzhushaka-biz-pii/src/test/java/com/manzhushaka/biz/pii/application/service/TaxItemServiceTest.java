package com.manzhushaka.biz.pii.application.service;

import com.manzhushaka.biz.pii.application.command.ChangeTaxItemStatusCommand;
import com.manzhushaka.biz.pii.application.command.CreateTaxItemCommand;
import com.manzhushaka.biz.pii.application.service.impl.TaxItemServiceImpl;
import com.manzhushaka.biz.pii.domain.model.TaxItem;
import com.manzhushaka.biz.pii.domain.repository.TaxItemRepository;
import com.manzhushaka.common.exception.ServiceException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class TaxItemServiceTest {

    private final TaxItemRepository taxItemRepository = mock(TaxItemRepository.class);
    private final TaxItemService service = new TaxItemServiceImpl(taxItemRepository);

    @Test
    void createShouldThrowWhenTaxItemCodeExists() {
        TaxItem existing = new TaxItem();
        existing.setId(1L);
        when(taxItemRepository.findByTaxItemCode("3070401000000000000")).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> service.create(createCommand()))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("税目编码已存在");
    }

    @Test
    void changeStatusShouldThrowWhenTaxItemNotFound() {
        when(taxItemRepository.findById(10L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.changeStatus(new ChangeTaxItemStatusCommand(10L, 0)))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("税目不存在");
    }

    @Test
    void changeStatusShouldUpdateRepositoryWhenTaxItemExists() {
        TaxItem existing = new TaxItem();
        existing.setId(10L);
        when(taxItemRepository.findById(10L)).thenReturn(Optional.of(existing));

        service.changeStatus(new ChangeTaxItemStatusCommand(10L, 0));

        verify(taxItemRepository).updateStatus(10L, 0);
    }

    private CreateTaxItemCommand createCommand() {
        return new CreateTaxItemCommand(
                "3070401000000000000",
                "信息技术服务",
                null,
                "服务",
                new BigDecimal("0.06"),
                "N",
                null,
                "N",
                1,
                1,
                "测试"
        );
    }
}
