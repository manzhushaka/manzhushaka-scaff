package com.manzhushaka.db.monitor;

import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;

import java.util.Collection;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

@Intercepts({
    @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class}),
    @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, org.apache.ibatis.session.RowBounds.class, org.apache.ibatis.session.ResultHandler.class}),
    @Signature(type = Executor.class, method = "query", args = {MappedStatement.class, Object.class, org.apache.ibatis.session.RowBounds.class, org.apache.ibatis.session.ResultHandler.class, org.apache.ibatis.cache.CacheKey.class, BoundSql.class})
})
/**
 * 定义 SlowSqlMonitorInterceptor。
 */
public class SlowSqlMonitorInterceptor implements Interceptor {

    private final SlowSqlMonitorStore store;
    private final long thresholdMs;

    /**
     * 使用存储器默认阈值创建拦截器。
     *
     * @param store 慢 SQL 存储器
     */
    public SlowSqlMonitorInterceptor(SlowSqlMonitorStore store) {
        this(store, store == null ? 500L : store.getThresholdMs());
    }

    /**
     * 创建慢 SQL 拦截器。
     *
     * @param store 慢 SQL 存储器
     * @param thresholdMs 慢 SQL 阈值
     */
    public SlowSqlMonitorInterceptor(SlowSqlMonitorStore store, long thresholdMs) {
        this.store = store;
        this.thresholdMs = thresholdMs > 0 ? thresholdMs : 500L;
    }

    /**
     * 拦截 MyBatis 查询或更新调用并记录慢 SQL。
     *
     * @param invocation MyBatis 调用上下文
     * @return 原始调用结果
     * @throws Throwable 底层调用异常
     */
    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        long start = System.nanoTime();
        Object result = invocation.proceed();
        long costMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
        if (store == null || costMs < thresholdMs) {
            return result;
        }
        MappedStatement statement = (MappedStatement) invocation.getArgs()[0];
        Object parameterObject = invocation.getArgs().length > 1 ? invocation.getArgs()[1] : null;
        BoundSql boundSql = resolveBoundSql(statement, parameterObject, invocation.getArgs());
        store.record(statement.getId(), boundSql == null ? null : boundSql.getSql(), costMs, resolveResultSize(result));
        return result;
    }

    /**
     * 包装目标对象。
     *
     * @param target 目标对象
     * @return 插件代理
     */
    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    /**
     * 接收插件属性配置。
     *
     * @param properties 插件属性
     */
    @Override
    public void setProperties(Properties properties) {
        // 当前实现无需额外属性。
    }

    /**
     * 解析本次调用的 BoundSql。
     *
     * @param statement MappedStatement
     * @param parameterObject 参数对象
     * @param args 调用参数
     * @return BoundSql
     */
    private BoundSql resolveBoundSql(MappedStatement statement, Object parameterObject, Object[] args) {
        if (args.length >= 6 && args[5] instanceof BoundSql boundSql) {
            return boundSql;
        }
        return statement.getBoundSql(parameterObject);
    }

    /**
     * 解析返回结果规模。
     *
     * @param result 返回结果
     * @return 结果规模
     */
    private Integer resolveResultSize(Object result) {
        if (result instanceof Collection<?> collection) {
            return collection.size();
        }
        if (result instanceof Number number) {
            return number.intValue();
        }
        return null;
    }
}
