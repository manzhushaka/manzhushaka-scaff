package com.manzhushaka.system.infrastructure.dict;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;
import com.manzhushaka.common.spi.DictResolver;
import com.manzhushaka.common.utils.StringUtils;
import com.manzhushaka.system.infrastructure.persistence.entity.SysDictData;

/**
 * 系统字典解析器实现
 * <p>
 * 实现 {@link DictResolver} SPI，通过 {@link SystemDictCacheSupport} 获取字典缓存数据。
 * 替代原先 {@code com.manzhushaka.common.utils.DictUtils} 中的查询逻辑。
 * </p>
 *
 * @author manzhushaka
 */
@Component
public class SystemDictResolver implements DictResolver
{
    @Override
    public String getDictLabel(String dictType, String dictValue, String separator)
    {
        List<SysDictData> datas = SystemDictCacheSupport.getDictCache(dictType);
        if (datas == null || StringUtils.isEmpty(dictValue))
        {
            return StringUtils.EMPTY;
        }
        Map<String, String> dictMap = datas.stream()
                .collect(HashMap::new, (map, dict) -> map.put(dict.getDictValue(), dict.getDictLabel()), Map::putAll);
        if (!StringUtils.contains(dictValue, separator))
        {
            return dictMap.getOrDefault(dictValue, StringUtils.EMPTY);
        }
        StringBuilder labelBuilder = new StringBuilder();
        for (String seperatedValue : dictValue.split(separator))
        {
            if (dictMap.containsKey(seperatedValue))
            {
                labelBuilder.append(dictMap.get(seperatedValue)).append(separator);
            }
        }
        return StringUtils.removeEnd(labelBuilder.toString(), separator);
    }

    @Override
    public String getDictValue(String dictType, String dictLabel, String separator)
    {
        List<SysDictData> datas = SystemDictCacheSupport.getDictCache(dictType);
        if (datas == null || StringUtils.isEmpty(dictLabel))
        {
            return StringUtils.EMPTY;
        }
        Map<String, String> dictMap = datas.stream()
                .collect(HashMap::new, (map, dict) -> map.put(dict.getDictLabel(), dict.getDictValue()), Map::putAll);
        if (!StringUtils.contains(dictLabel, separator))
        {
            return dictMap.getOrDefault(dictLabel, StringUtils.EMPTY);
        }
        StringBuilder valueBuilder = new StringBuilder();
        for (String seperatedValue : dictLabel.split(separator))
        {
            if (dictMap.containsKey(seperatedValue))
            {
                valueBuilder.append(dictMap.get(seperatedValue)).append(separator);
            }
        }
        return StringUtils.removeEnd(valueBuilder.toString(), separator);
    }

    @Override
    public String getDictLabels(String dictType)
    {
        StringBuilder propertyString = new StringBuilder();
        List<SysDictData> datas = SystemDictCacheSupport.getDictCache(dictType);
        if (datas == null)
        {
            return StringUtils.EMPTY;
        }
        for (SysDictData dict : datas)
        {
            propertyString.append(dict.getDictLabel()).append(SEPARATOR);
        }
        return StringUtils.stripEnd(propertyString.toString(), SEPARATOR);
    }

    @Override
    public String getDictValues(String dictType)
    {
        StringBuilder propertyString = new StringBuilder();
        List<SysDictData> datas = SystemDictCacheSupport.getDictCache(dictType);
        if (datas == null)
        {
            return StringUtils.EMPTY;
        }
        for (SysDictData dict : datas)
        {
            propertyString.append(dict.getDictValue()).append(SEPARATOR);
        }
        return StringUtils.stripEnd(propertyString.toString(), SEPARATOR);
    }
}