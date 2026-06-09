package com.manzhushaka.system.service.impl;

import com.manzhushaka.db.system.entity.SysDictItem;
import com.manzhushaka.db.system.entity.SysDictType;
import com.manzhushaka.db.system.mapper.SysDictItemMapper;
import com.manzhushaka.db.system.mapper.SysDictTypeMapper;
import com.manzhushaka.system.vo.dict.DictItemVO;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DictServiceImplTest {

    @Test
    void shouldLoadDictItemsByTypeCode() {
        SysDictTypeMapper typeMapper = mock(SysDictTypeMapper.class);
        SysDictItemMapper itemMapper = mock(SysDictItemMapper.class);
        DictServiceImpl service = new DictServiceImpl(typeMapper, itemMapper);

        SysDictType type = new SysDictType();
        type.setId(10L);
        type.setDictCode("gender");
        when(typeMapper.selectOne(any())).thenReturn(type);

        SysDictItem male = new SysDictItem();
        male.setId(1L);
        male.setDictTypeId(10L);
        male.setItemLabel("男");
        male.setItemValue("M");
        male.setSort(1);

        SysDictItem female = new SysDictItem();
        female.setId(2L);
        female.setDictTypeId(10L);
        female.setItemLabel("女");
        female.setItemValue("F");
        female.setSort(2);
        when(itemMapper.selectList(any())).thenReturn(List.of(male, female));

        List<DictItemVO> items = service.listItemsByTypeCode("gender");

        assertEquals(List.of("男", "女"), items.stream().map(DictItemVO::getItemLabel).toList());
        assertEquals(List.of("M", "F"), items.stream().map(DictItemVO::getItemValue).toList());
    }
}
