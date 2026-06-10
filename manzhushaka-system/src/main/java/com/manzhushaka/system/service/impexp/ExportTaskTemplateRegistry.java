package com.manzhushaka.system.service.impexp;

import com.manzhushaka.common.exception.BizException;
import com.manzhushaka.system.vo.LabelValueOption;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class ExportTaskTemplateRegistry {

    private final Map<String, AbstractExportTaskTemplate<?>> templates = new LinkedHashMap<>();

    public ExportTaskTemplateRegistry(List<AbstractExportTaskTemplate<?>> templates) {
        for (AbstractExportTaskTemplate<?> template : templates) {
            this.templates.put(template.bizType(), template);
        }
    }

    public AbstractExportTaskTemplate<?> getRequired(String bizType) {
        AbstractExportTaskTemplate<?> template = templates.get(bizType);
        if (template == null) {
            throw new BizException(404, "未找到导出场景");
        }
        return template;
    }

    public List<LabelValueOption> options() {
        return templates.values().stream()
            .map(template -> new LabelValueOption(template.bizLabel(), template.bizType()))
            .toList();
    }
}
