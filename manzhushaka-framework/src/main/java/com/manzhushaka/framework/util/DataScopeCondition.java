package com.manzhushaka.framework.util;

import com.manzhushaka.common.enums.DataScopeType;

public record DataScopeCondition(DataScopeType scopeType, String sqlSegment) {
}
