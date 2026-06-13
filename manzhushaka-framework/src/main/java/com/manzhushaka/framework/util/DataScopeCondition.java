package com.manzhushaka.framework.util;

import com.manzhushaka.common.enums.DataScopeType;

/**
 * 承载 DataScopeCondition 数据。
 */
public record DataScopeCondition(DataScopeType scopeType, String sqlSegment) {
}
