package com.manzhushaka.biz.pii.application.service;

import com.manzhushaka.biz.pii.application.command.CreateMerchantCommand;
import com.manzhushaka.biz.pii.application.command.UpdateMerchantCommand;
import com.manzhushaka.biz.pii.application.service.impl.MerchantServiceImpl;
import com.manzhushaka.biz.pii.domain.model.MerchantProfile;
import com.manzhushaka.biz.pii.domain.repository.MerchantProfileRepository;
import com.manzhushaka.common.exception.ServiceException;
import com.manzhushaka.system.infrastructure.persistence.entity.SysDept;
import com.manzhushaka.system.infrastructure.persistence.entity.SysUser;
import com.manzhushaka.system.service.ISysDeptService;
import com.manzhushaka.system.service.ISysUserService;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MerchantServiceTest {

    private final MerchantProfileRepository merchantProfileRepository = mock(MerchantProfileRepository.class);
    private final ISysDeptService deptService = mock(ISysDeptService.class);
    private final ISysUserService userService = mock(ISysUserService.class);
    private final BiCacheService biCacheService = mock(BiCacheService.class);
    private final MerchantService service = new MerchantServiceImpl(merchantProfileRepository, deptService, userService, biCacheService);

    @Test
    void createShouldThrowWhenUmsMerchantAndTerminalExists() {
        when(merchantProfileRepository.findByUmsMerchantAndTerminal("MID", "TID"))
                .thenReturn(Optional.of(new MerchantProfile()));

        assertThatThrownBy(() -> service.create(createCommand()))
                .isInstanceOf(ServiceException.class)
                .hasMessageContaining("银商商户参数已存在");
    }

    @Test
    void createShouldCreateDeptUserAndProfileAtomically() {
        when(merchantProfileRepository.findByUmsMerchantAndTerminal("MID", "TID")).thenReturn(Optional.empty());
        when(userService.checkUserNameUnique(any(SysUser.class))).thenReturn(true);
        when(deptService.insertDept(any(SysDept.class))).thenAnswer(invocation -> {
            SysDept dept = invocation.getArgument(0);
            dept.setDeptId(100L);
            return 1;
        });

        service.create(createCommand());

        verify(deptService).insertDept(any(SysDept.class));
        verify(userService).insertUser(any(SysUser.class));
        verify(merchantProfileRepository).insert(any(MerchantProfile.class));
        verify(biCacheService).evictAll();
    }

    @Test
    void updateShouldKeepExistingKeysWhenKeyFieldsBlank() {
        MerchantProfile existing = new MerchantProfile();
        existing.setId(9L);
        existing.setUmsPaySignKeyEnc("OLD_PAY_KEY");
        existing.setUmsInvoiceSignKeyEnc("OLD_INVOICE_KEY");
        when(merchantProfileRepository.findById(9L)).thenReturn(Optional.of(existing));
        when(merchantProfileRepository.findByUmsMerchantAndTerminal("MID2", "TID2")).thenReturn(Optional.empty());

        service.update(new UpdateMerchantCommand(9L, "新商户", "MID2", "TID2", "", null, "MSG2", 1, "更新"));

        ArgumentCaptor<MerchantProfile> captor = ArgumentCaptor.forClass(MerchantProfile.class);
        verify(merchantProfileRepository).updateById(captor.capture());
        Assertions.assertEquals("OLD_PAY_KEY", captor.getValue().getUmsPaySignKeyEnc());
        Assertions.assertEquals("OLD_INVOICE_KEY", captor.getValue().getUmsInvoiceSignKeyEnc());
    }

    @Test
    void getShouldReturnRegionParentInfo() {
        MerchantProfile existing = new MerchantProfile();
        existing.setId(9L);
        existing.setDeptId(300L);
        existing.setMerchantName("测试商户");
        SysDept merchantDept = new SysDept();
        merchantDept.setDeptId(300L);
        merchantDept.setParentId(200L);
        merchantDept.setParentName("滨江区");
        when(merchantProfileRepository.findById(9L)).thenReturn(Optional.of(existing));
        when(deptService.selectDeptById(eq(300L))).thenReturn(merchantDept);

        var result = service.get(9L);

        assertThat(result.getParentDeptId()).isEqualTo(200L);
        assertThat(result.getRegionName()).isEqualTo("滨江区");
    }

    private CreateMerchantCommand createCommand() {
        return new CreateMerchantCommand(
                1L,
                "测试商户",
                "merchant001",
                "Merchant@123",
                "13800000000",
                "merchant@example.com",
                "MID",
                "TID",
                "PAYKEY",
                "INVKEY",
                "MSG",
                1,
                "测试"
        );
    }
}
