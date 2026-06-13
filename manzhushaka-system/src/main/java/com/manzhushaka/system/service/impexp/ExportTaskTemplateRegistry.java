package com.manzhushaka.system.service.impexp;

import com.manzhushaka.common.exception.BizException;
import com.manzhushaka.system.vo.LabelValueOption;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 定义 ExportTaskTemplateRegistry。
 */
@Component
public class ExportTaskTemplateRegistry {

    /**
     * 执行 method 逻辑。
     *
     * @return 处理结果
     */
    private final Map<String, AbstractExportTaskTemplate<?>> templates = new LinkedHashMap<>();

    /**
     * 创建 ExportTaskTemplateRegistry 实例。
     *
     * @param templates templates 参数
     */
    public ExportTaskTemplateRegistry(List<AbstractExportTaskTemplate<?>> templates) {
        for (AbstractExportTaskTemplate<?> template : templates) {
            this.templates.put(template.bizType(), template);
        }
    }

    /**
     * 返回 required。
     *
     * @param bizType bizType 参数
     * @return 字段值
     */
    public AbstractExportTaskTemplate<?> getRequired(String bizType) {
        AbstractExportTaskTemplate<?> template = templates.get(bizType);
        if (template == null) {
            throw new BizException(404, "未找到导出场景");
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
