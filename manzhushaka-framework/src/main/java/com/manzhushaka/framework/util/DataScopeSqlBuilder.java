package com.manzhushaka.framework.util;

import com.manzhushaka.common.context.LoginUser;
import com.manzhushaka.common.enums.DataScopeType;

/**
 * 提供 DataScopeSqlBuilder 工具能力。
 */
public final class DataScopeSqlBuilder {

    /**
     * 创建 DataScopeSqlBuilder 实例。
     */
    private DataScopeSqlBuilder() {
    }

    /**
     * 构建 build 结果。
     *
     * @param loginUser loginUser 参数
     * @param tableAlias tableAlias 参数
     * @param deptColumn deptColumn 参数
     * @param userColumn userColumn 参数
     * @return 处理结果
     */
    public static DataScopeCondition build(LoginUser loginUser, String tableAlias, String deptColumn, String userColumn) {
        DataScopeType scopeType = loginUser.getDataScopes()
            .stream()
            .reduce(DataScopeType.SELF, DataScopeType::max);

        String prefix = " AND " + tableAlias + ".";
        String sqlSegment = switch (scopeType) {
            case ALL -> "";
            case DEPT -> prefix + deptColumn + " = " + loginUser.getDeptId();
            case DEPT_AND_CHILD ->
                prefix + deptColumn + " IN (SELECT id FROM sys_dept WHERE id = " + loginUser.getDeptId()
                    + " OR ancestor_path LIKE '%," + loginUser.getDeptId() + ",%')";
            case SELF -> prefix + userColumn + " = " + loginUser.getUserId();
        };
        return new DataScopeCondition(scopeType, sqlSegment);
    }
}
