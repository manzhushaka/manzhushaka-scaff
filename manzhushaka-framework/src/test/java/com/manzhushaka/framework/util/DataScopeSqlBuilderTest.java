package com.manzhushaka.framework.util;

import com.manzhushaka.common.context.LoginUser;
import com.manzhushaka.common.enums.DataScopeType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DataScopeSqlBuilderTest {

    @Test
    void shouldPreferWidestScopeAcrossRoles() {
        LoginUser loginUser = new LoginUser();
        loginUser.setUserId(7L);
        loginUser.setDeptId(12L);
        loginUser.setDataScopes(List.of(DataScopeType.SELF, DataScopeType.DEPT_AND_CHILD, DataScopeType.DEPT));

        DataScopeCondition condition = DataScopeSqlBuilder.build(loginUser, "u", "dept_id", "id");

        assertEquals(DataScopeType.DEPT_AND_CHILD, condition.scopeType());
        assertTrue(condition.sqlSegment().contains("ancestor_path"));
    }

    @Test
    void shouldBuildSelfScopeFilter() {
        LoginUser loginUser = new LoginUser();
        loginUser.setUserId(99L);
        loginUser.setDeptId(18L);
        loginUser.setDataScopes(List.of(DataScopeType.SELF));

        DataScopeCondition condition = DataScopeSqlBuilder.build(loginUser, "u", "dept_id", "id");

        assertEquals(" AND u.id = 99", condition.sqlSegment());
    }
}
