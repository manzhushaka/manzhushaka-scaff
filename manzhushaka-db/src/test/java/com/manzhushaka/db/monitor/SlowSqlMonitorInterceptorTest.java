package com.manzhushaka.db.monitor;

import org.apache.ibatis.builder.StaticSqlSource;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.RowBounds;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SlowSqlMonitorInterceptorTest {

    /**
     * 验证慢 SQL 拦截器会记录超过阈值的查询。
     *
     * @throws Throwable 拦截调用异常
     */
    @Test
    void shouldRecordQueryWhenCostExceedsThreshold() throws Throwable {
        SlowSqlMonitorStore store = new SlowSqlMonitorStore(8);
        SlowSqlMonitorInterceptor interceptor = new SlowSqlMonitorInterceptor(store, 1L);
        Executor executor = mock(Executor.class);
        MappedStatement statement = buildStatement("com.manzhushaka.db.system.mapper.UserMapper.selectById", "SELECT * FROM sys_user WHERE id = ?");

        when(executor.query(eq(statement), eq(1L), eq(RowBounds.DEFAULT), eq(null))).thenAnswer(invocation -> {
            Thread.sleep(5L);
            return List.of("ok");
        });

        Invocation invocation = new Invocation(executor, queryMethod(), new Object[] {statement, 1L, RowBounds.DEFAULT, null});

        Object result = interceptor.intercept(invocation);

        assertEquals(List.of("ok"), result);
        assertEquals(1, store.listRecent(10).size());
        assertEquals(statement.getId(), store.listRecent(10).get(0).getStatementId());
        assertTrue(store.listRecent(10).get(0).getCostMs() >= 1L);
    }

    /**
     * 验证慢 SQL 拦截器会忽略低于阈值的查询。
     *
     * @throws Throwable 拦截调用异常
     */
    @Test
    void shouldIgnoreQueryWhenCostBelowThreshold() throws Throwable {
        SlowSqlMonitorStore store = new SlowSqlMonitorStore(8);
        SlowSqlMonitorInterceptor interceptor = new SlowSqlMonitorInterceptor(store, 200L);
        Executor executor = mock(Executor.class);
        MappedStatement statement = buildStatement("com.manzhushaka.db.system.mapper.UserMapper.selectPage", "SELECT * FROM sys_user");

        when(executor.query(any(), any(), any(), any())).thenReturn(List.of());

        Invocation invocation = new Invocation(executor, queryMethod(), new Object[] {statement, null, RowBounds.DEFAULT, null});

        interceptor.intercept(invocation);

        assertTrue(store.listRecent(10).isEmpty());
    }

    /**
     * 构造最小可用的 MyBatis MappedStatement。
     *
     * @param statementId 语句标识
     * @param sql 原始 SQL
     * @return MappedStatement
     */
    private MappedStatement buildStatement(String statementId, String sql) {
        Configuration configuration = new Configuration();
        return new MappedStatement.Builder(configuration, statementId, new StaticSqlSource(configuration, sql), SqlCommandType.SELECT)
            .build();
    }

    /**
     * 读取 Executor 查询方法反射对象。
     *
     * @return 查询方法
     * @throws NoSuchMethodException 方法不存在时抛出
     */
    private Method queryMethod() throws NoSuchMethodException {
        return Executor.class.getMethod("query", MappedStatement.class, Object.class, RowBounds.class, org.apache.ibatis.session.ResultHandler.class);
    }
}
