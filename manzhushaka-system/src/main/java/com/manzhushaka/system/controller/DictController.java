package com.manzhushaka.system.controller;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.manzhushaka.common.model.ApiResponse;
import com.manzhushaka.system.dto.dict.DictItemForm;
import com.manzhushaka.system.dto.dict.DictTypeForm;
import com.manzhushaka.system.dto.dict.DictTypeQuery;
import com.manzhushaka.system.service.DictService;
import com.manzhushaka.system.vo.LabelValueOption;
import com.manzhushaka.system.vo.PageResult;
import com.manzhushaka.system.vo.dict.DictItemVO;
import com.manzhushaka.system.vo.dict.DictTypeVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * 提供 DictController 相关的 Web 接口。
 */
@RestController
@RequestMapping({"/system/dicts", "/api/system/dicts"})
public class DictController {

    private final DictService dictService;

    /**
     * 创建 DictController 实例。
     *
     * @param dictService dictService 参数
     */
    public DictController(DictService dictService) {
        this.dictService = dictService;
    }

    /**
     * 查询 page Types 结果。
     *
     * @param query 查询条件
     * @return 查询结果
     */
    @GetMapping("/types")
    @SaCheckPermission("system:dict:list")
    public ApiResponse<PageResult<DictTypeVO>> pageTypes(DictTypeQuery query) {
        return ApiResponse.success(dictService.pageTypes(query));
    }

    /**
     * 执行 type Options 逻辑。
     *
     * @return 处理结果
     */
    @GetMapping("/types/options")
    @SaCheckPermission(value = {"system:dict:list", "system:dict:add", "system:dict:update"}, mode = cn.dev33.satoken.annotation.SaMode.OR)
    public ApiResponse<List<LabelValueOption>> typeOptions() {
        return ApiResponse.success(dictService.typeOptions());
    }

    /**
     * 返回 typeById。
     *
     * @param id 主键 ID
     * @return 字段值
     */
    @GetMapping("/types/{id}")
    @SaCheckPermission("system:dict:query")
    public ApiResponse<DictTypeVO> getTypeById(@PathVariable("id") Long id) {
        return ApiResponse.success(dictService.getTypeById(id));
    }

    /**
     * 查询 list Items By Type Id 结果。
     *
     * @param id 主键 ID
     * @return 查询结果
     */
    @GetMapping("/types/{id}/items")
    @SaCheckPermission("system:dict:query")
    public ApiResponse<List<DictItemVO>> listItemsByTypeId(@PathVariable("id") Long id) {
        return ApiResponse.success(dictService.listItemsByTypeId(id));
    }

    /**
     * 查询 list Items By Type Code 结果。
     *
     * @param dictCode dictCode 参数
     * @return 查询结果
     */
    @GetMapping("/items/by-code")
    @SaCheckPermission("system:dict:query")
    public ApiResponse<List<DictItemVO>> listItemsByTypeCode(@RequestParam("dictCode") String dictCode) {
        return ApiResponse.success(dictService.listItemsByTypeCode(dictCode));
    }

    /**
     * 创建 create Type 数据。
     *
     * @param form 表单参数
     * @return 创建结果
     */
    @PostMapping("/types")
    @SaCheckPermission("system:dict:add")
    public ApiResponse<Long> createType(@Valid @RequestBody DictTypeForm form) {
        return ApiResponse.success(dictService.createType(form));
    }

    /**
     * 更新 update Type 数据。
     *
     * @param id 主键 ID
     * @param form 表单参数
     * @return 处理结果
     */
    @PutMapping("/types/{id}")
    @SaCheckPermission("system:dict:update")
    public ApiResponse<Void> updateType(@PathVariable("id") Long id, @Valid @RequestBody DictTypeForm form) {
        dictService.updateType(id, form);
        return ApiResponse.success(null);
    }

    /**
     * 清理 delete Type 数据。
     *
     * @param id 主键 ID
     * @return 处理结果
     */
    @DeleteMapping("/types/{id}")
    @SaCheckPermission("system:dict:delete")
    public ApiResponse<Void> deleteType(@PathVariable("id") Long id) {
        dictService.deleteType(id);
        return ApiResponse.success(null);
    }

    /**
     * 创建 create Item 数据。
     *
     * @param form 表单参数
     * @return 创建结果
     */
    @PostMapping("/items")
    @SaCheckPermission("system:dict:add")
    public ApiResponse<Long> createItem(@Valid @RequestBody DictItemForm form) {
        return ApiResponse.success(dictService.createItem(form));
    }

    /**
     * 更新 update Item 数据。
     *
     * @param id 主键 ID
     * @param form 表单参数
     * @return 处理结果
     */
    @PutMapping("/items/{id}")
    @SaCheckPermission("system:dict:update")
    public ApiResponse<Void> updateItem(@PathVariable("id") Long id, @Valid @RequestBody DictItemForm form) {
        dictService.updateItem(id, form);
        return ApiResponse.success(null);
    }

    /**
     * 清理 delete Item 数据。
     *
     * @param id 主键 ID
     * @return 处理结果
     */
    @DeleteMapping("/items/{id}")
    @SaCheckPermission("system:dict:delete")
    public ApiResponse<Void> deleteItem(@PathVariable("id") Long id) {
        dictService.deleteItem(id);
        return ApiResponse.success(null);
    }
}
