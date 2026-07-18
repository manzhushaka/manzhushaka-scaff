package com.manzhushaka.system.application.result.system;

import java.util.List;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * 动态路由结果。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
@JsonInclude(JsonInclude.Include.NON_EMPTY)
public record RouterResult(String name, String path, boolean hidden, String redirect,
        String component, String query, Boolean alwaysShow, MetaResult meta,
        List<RouterResult> children)
{
}
