package com.manzhushaka.web.controller.system;

import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.manzhushaka.common.annotation.Log;
import com.manzhushaka.common.core.controller.BaseController;
import com.manzhushaka.common.core.domain.AjaxResult;
import com.manzhushaka.common.core.page.TableDataInfo;
import com.manzhushaka.common.enums.BusinessType;
import com.manzhushaka.common.utils.poi.ExcelUtil;
import com.manzhushaka.framework.security.context.SecurityContextHelper;
import com.manzhushaka.system.application.result.system.DictTypeResult;
import com.manzhushaka.system.application.service.SystemDictAppService;
import com.manzhushaka.web.converter.system.DictAdminConverter;
import com.manzhushaka.web.dto.system.DictTypeRequest;

/**
 * 数据字典信息
 * 
 * @author manzhushaka
 */
@RestController
@RequestMapping("/system/dict/type")
public class SysDictTypeController extends BaseController
{
    @Autowired
    private SystemDictAppService dictAppService;

    @PreAuthorize("@ss.hasPermi('system:dict:list')")
    @GetMapping("/list")
    public TableDataInfo list(DictTypeRequest request)
    {
        startPage();
        List<DictTypeResult> list = dictAppService.listDictTypes(DictAdminConverter.toQuery(request));
        return getDataTable(list);
    }

    @Log(title = "字典类型", businessType = BusinessType.EXPORT)
    @PreAuthorize("@ss.hasPermi('system:dict:export')")
    @PostMapping("/export")
    public void export(HttpServletResponse response, DictTypeRequest request)
    {
        List<DictTypeResult> list = dictAppService.listDictTypes(DictAdminConverter.toQuery(request));
        ExcelUtil<DictTypeResult> util = new ExcelUtil<DictTypeResult>(DictTypeResult.class);
        util.exportExcel(response, list, "字典类型");
    }

    /**
     * 查询字典类型详细
     */
    @PreAuthorize("@ss.hasPermi('system:dict:query')")
    @GetMapping(value = "/{dictId}")
    public AjaxResult getInfo(@PathVariable Long dictId)
    {
        return success(dictAppService.getDictType(dictId));
    }

    /**
     * 新增字典类型
     */
    @PreAuthorize("@ss.hasPermi('system:dict:add')")
    @Log(title = "字典类型", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody DictTypeRequest request)
    {
        return toAjax(dictAppService.createDictType(DictAdminConverter.toCommand(request),
                SecurityContextHelper.getUsername()));
    }

    /**
     * 修改字典类型
     */
    @PreAuthorize("@ss.hasPermi('system:dict:edit')")
    @Log(title = "字典类型", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody DictTypeRequest request)
    {
        return toAjax(dictAppService.updateDictType(DictAdminConverter.toCommand(request),
                SecurityContextHelper.getUsername()));
    }

    /**
     * 删除字典类型
     */
    @PreAuthorize("@ss.hasPermi('system:dict:remove')")
    @Log(title = "字典类型", businessType = BusinessType.DELETE)
    @DeleteMapping("/{dictIds}")
    public AjaxResult remove(@PathVariable Long[] dictIds)
    {
        dictAppService.deleteDictTypes(dictIds);
        return success();
    }

    /**
     * 刷新字典缓存
     */
    @PreAuthorize("@ss.hasPermi('system:dict:remove')")
    @Log(title = "字典类型", businessType = BusinessType.CLEAN)
    @DeleteMapping("/refreshCache")
    public AjaxResult refreshCache()
    {
        dictAppService.refreshCache();
        return success();
    }

    /**
     * 获取字典选择框列表
     */
    @GetMapping("/optionselect")
    public AjaxResult optionselect()
    {
        return success(dictAppService.listAllDictTypes());
    }
}
