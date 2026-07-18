package com.manzhushaka.web.converter.system;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.Test;
import com.manzhushaka.system.application.query.DictTypeQuery;
import com.manzhushaka.system.application.query.RoleListQuery;
import com.manzhushaka.system.application.query.UserListQuery;
import com.manzhushaka.web.converter.system.role.RoleAdminConverter;
import com.manzhushaka.web.converter.system.user.UserAdminConverter;
import com.manzhushaka.web.dto.system.DictTypeRequest;
import com.manzhushaka.web.dto.system.role.RoleListRequest;
import com.manzhushaka.web.dto.system.user.UserListRequest;

/**
 * 日期范围请求转换测试。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
class DateRangeAdminConverterTest
{
    private static final String BEGIN_TIME = "2026-07-01 00:00:00";
    private static final String END_TIME = "2026-07-18 23:59:59";

    /** 用户列表应读取前端 params 日期范围。 */
    @Test
    void userQueryShouldReadNestedDateRange()
    {
        UserListRequest request = new UserListRequest();
        request.setParams(dateRange());

        UserListQuery query = UserAdminConverter.toUserListQuery(request);

        assertThat(query.beginTime()).isEqualTo(BEGIN_TIME);
        assertThat(query.endTime()).isEqualTo(END_TIME);
    }

    /** 角色列表应读取前端 params 日期范围。 */
    @Test
    void roleQueryShouldReadNestedDateRange()
    {
        RoleListRequest request = new RoleListRequest();
        request.setParams(dateRange());

        RoleListQuery query = RoleAdminConverter.toRoleListQuery(request);

        assertThat(query.beginTime()).isEqualTo(BEGIN_TIME);
        assertThat(query.endTime()).isEqualTo(END_TIME);
    }

    /** 字典类型列表应读取前端 params 日期范围。 */
    @Test
    void dictTypeQueryShouldReadNestedDateRange()
    {
        DictTypeRequest request = new DictTypeRequest();
        request.setParams(dateRange());

        DictTypeQuery query = DictAdminConverter.toQuery(request);

        assertThat(query.beginTime()).isEqualTo(BEGIN_TIME);
        assertThat(query.endTime()).isEqualTo(END_TIME);
    }

    /**
     * 构造前端日期范围参数。
     *
     * @return 日期范围参数
     */
    private Map<String, Object> dateRange()
    {
        return Map.of("beginTime", BEGIN_TIME, "endTime", END_TIME);
    }
}
