package com.manzhushaka.system.service.impexp;

import com.manzhushaka.common.exception.BizException;
import com.manzhushaka.system.vo.LabelValueOption;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 定义 ImportTaskTemplateRegistry。
 */
@Component
public class ImportTaskTemplateRegistry {

    /**
     * 执行 method 逻辑。
     *
     * @return 处理结果
     */
    private final Map<String, AbstractImportTaskTemplate<?>> templates = new LinkedHashMap<>();

    /**
     * 创建 ImportTaskTemplateRegistry 实例。
     *
     * @param templates templates 参数
     */
    public ImportTaskTemplateRegistry(List<AbstractImportTaskTemplate<?>> templates) {
        for (AbstractImportTaskTemplate<?> template : templates) {
            this.templates.put(template.bizType(), template);
        }
    }

    /**
     * 返回 required。
     *
     * @param bizType bizType 参数
     * @return 字段值
     */
    public AbstractImportTaskTemplate<?> getRequired(String bizType) {
        AbstractImportTaskTemplate<?> template = templates.get(bizType);
        if (template == null) {
            throw new BizException(404, "未找到导入场景");
        }
        return template;
    }

    /**
     * 查询下拉选项。
     *
     * @return 查询结果
     */
    public List<LabelValueOption> options() {
        return templates.values().stream()
            .map(template -> new LabelValueOption(template.bizLabel(), template.bizType()))
            .toList();
    }
}
