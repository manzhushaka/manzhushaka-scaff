package com.manzhushaka.system.service.impl;

import com.manzhushaka.common.exception.ServiceException;
import com.manzhushaka.system.infrastructure.persistence.entity.SysDept;
import com.manzhushaka.system.mapper.SysDeptMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SysDeptServiceDeptTypeTest {

    @Mock
    private SysDeptMapper deptMapper;

    @InjectMocks
    private SysDeptServiceImpl service;

    @Test
    void deleteRegionTypeDeptShouldThrow() {
        SysDept region = new SysDept();
        region.setDeptId(200L);
        region.setDeptType("region");
        when(deptMapper.selectDeptById(200L)).thenReturn(region);

        assertThrows(ServiceException.class, () -> service.deleteDeptById(200L));
    }

    @Test
    void deleteMerchantTypeDeptShouldThrow() {
        SysDept merchant = new SysDept();
        merchant.setDeptId(500L);
        merchant.setDeptType("merchant");
        when(deptMapper.selectDeptById(500L)).thenReturn(merchant);

        assertThrows(ServiceException.class, () -> service.deleteDeptById(500L));
    }
}
