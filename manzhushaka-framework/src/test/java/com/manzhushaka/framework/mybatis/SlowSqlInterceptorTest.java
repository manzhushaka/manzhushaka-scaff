package com.manzhushaka.framework.mybatis;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 慢 SQL 拦截器测试。
 *
 * @author manzhushaka
 * @date 2026-06-29
 */
class SlowSqlInterceptorTest {

    /**
     * 写慢 SQL 日志自身的 SQL 应被排除，避免递归写入。
     */
    @Test
    void shouldExcludeSlowSqlLogStatements() {
        SlowSqlInterceptor interceptor = new SlowSqlInterceptor();

        assertThat(interceptor.shouldExclude("com.manzhushaka.system.mapper.SysSlowSqlLogMapper.insertSlowSqlLog",
                "insert into sys_slow_sql_log(sql_text) values (?)")).isTrue();
        assertThat(interceptor.shouldExclude("com.manzhushaka.system.mapper.SysUserMapper.selectUserList",
                "select * from sys_user")).isFalse();
    }

    /**
     * 只有达到阈值的 SQL 才应记录。
     */
    @Test
    void shouldRecordOnlyWhenCostReachesThreshold() {
        SlowSqlInterceptor interceptor = new SlowSqlInterceptor(1000L);

        assertThat(interceptor.shouldRecord(999L)).isFalse();
        assertThat(interceptor.shouldRecord(1000L)).isTrue();
    }
}
