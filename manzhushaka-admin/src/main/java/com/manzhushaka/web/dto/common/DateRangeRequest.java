package com.manzhushaka.web.dto.common;

import java.util.HashMap;
import java.util.Map;

/**
 * 日期范围查询请求基类。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
public class DateRangeRequest
{
    private Map<String, Object> params = new HashMap<>();

    public Map<String, Object> getParams()
    {
        return params;
    }

    public void setParams(Map<String, Object> params)
    {
        this.params = params == null ? new HashMap<>() : params;
    }

    public String getBeginTime()
    {
        return getParamAsString("beginTime");
    }

    public String getEndTime()
    {
        return getParamAsString("endTime");
    }

    private String getParamAsString(String name)
    {
        Object value = params.get(name);
        return value == null ? null : value.toString();
    }
}
