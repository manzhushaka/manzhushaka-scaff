package com.manzhushaka.system.service;

import com.manzhushaka.system.dto.dict.DictItemForm;
import com.manzhushaka.system.dto.dict.DictTypeForm;
import com.manzhushaka.system.dto.dict.DictTypeQuery;
import com.manzhushaka.system.vo.LabelValueOption;
import com.manzhushaka.system.vo.PageResult;
import com.manzhushaka.system.vo.dict.DictItemVO;
import com.manzhushaka.system.vo.dict.DictTypeVO;

import java.util.List;

public interface DictService {
    PageResult<DictTypeVO> pageTypes(DictTypeQuery query);

    DictTypeVO getTypeById(Long id);

    List<DictItemVO> listItemsByTypeId(Long dictTypeId);

    List<DictItemVO> listItemsByTypeCode(String dictCode);

    List<LabelValueOption> typeOptions();

    Long createType(DictTypeForm form);

    void updateType(Long id, DictTypeForm form);

    void deleteType(Long id);

    Long createItem(DictItemForm form);

    void updateItem(Long id, DictItemForm form);

    void deleteItem(Long id);
}
