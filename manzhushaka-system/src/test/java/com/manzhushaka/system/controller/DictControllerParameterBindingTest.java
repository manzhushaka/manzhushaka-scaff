package com.manzhushaka.system.controller;

import com.manzhushaka.system.service.DictService;
import com.manzhushaka.system.vo.dict.DictItemVO;
import com.manzhushaka.system.vo.dict.DictTypeVO;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class DictControllerParameterBindingTest {

    @Test
    void shouldBindPathVariableForDictTypeId() throws Exception {
        DictService dictService = mock(DictService.class);
        DictController controller = new DictController(dictService);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        DictTypeVO dictType = new DictTypeVO();
        dictType.setId(7L);
        dictType.setDictName("用户状态");
        dictType.setDictCode("sys_user_status");
        when(dictService.getTypeById(7L)).thenReturn(dictType);

        mockMvc.perform(get("/system/dicts/types/7"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data.id").value(7L))
            .andExpect(jsonPath("$.data.dictCode").value("sys_user_status"));

        verify(dictService).getTypeById(7L);
    }

    @Test
    void shouldBindRequestParamForDictCode() throws Exception {
        DictService dictService = mock(DictService.class);
        DictController controller = new DictController(dictService);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(controller).build();

        DictItemVO item = new DictItemVO();
        item.setId(1L);
        item.setDictTypeId(7L);
        item.setItemLabel("启用");
        item.setItemValue("0");
        when(dictService.listItemsByTypeCode("sys_user_status")).thenReturn(List.of(item));

        mockMvc.perform(get("/system/dicts/items/by-code").param("dictCode", "sys_user_status"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.code").value(0))
            .andExpect(jsonPath("$.data[0].itemLabel").value("启用"))
            .andExpect(jsonPath("$.data[0].itemValue").value("0"));

        verify(dictService).listItemsByTypeCode("sys_user_status");
    }
}
