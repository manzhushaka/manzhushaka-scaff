package com.manzhushaka.framework.mybatis;

import java.util.Date;
import java.util.Properties;

import com.manzhushaka.common.utils.ExceptionUtil;
import com.manzhushaka.common.utils.StringUtils;
import com.manzhushaka.framework.datasource.DynamicDataSourceContextHolder;
import com.manzhushaka.framework.manager.AsyncManager;
import com.manzhushaka.framework.manager.factory.AsyncFactory;
import com.manzhushaka.framework.web.command.SlowSqlLogRecord;
import org.apache.ibatis.cache.CacheKey;
import org.apache.ibatis.executor.Executor;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * 慢 SQL 拦截器。
 *
 * @author manzhushaka
 * @date 2026-06-29
 */
@Component
@Intercepts({
        @Signature(type = Executor.class, method = "query",
                args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class}),
        @Signature(type = Executor.class, method = "query",
                args = {MappedStatement.class, Object.class, RowBounds.class, ResultHandler.class, CacheKey.class, BoundSql.class}),
        @Signature(type = Executor.class, method = "update", args = {MappedStatement.class, Object.class})
})
public class SlowSqlInterceptor implements Interceptor
{
    private static final String SLOW_SQL_LOG_TABLE = "sys_slow_sql_log";

    private static final int TEXT_MAX_LENGTH = 4000;

    @Value("${spring.datasource.druid.filter.stat.slow-sql-millis:${DRUID_FILTER_STAT_SLOW_SQL_MILLIS:1000}}")
    private long slowSqlMillis = 1000L;

    public SlowSqlInterceptor()
    {
    }

    public SlowSqlInterceptor(long slowSqlMillis)
    {
        this.slowSqlMillis = slowSqlMillis;
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable
    {
        long startTime = System.currentTimeMillis();
        Throwable throwable = null;
        try
        {
            return invocation.proceed();
        }
        catch (Throwable ex)
        {
            throwable = ex;
            throw ex;
        }
        finally
        {
            recordIfSlow(invocation, System.currentTimeMillis() - startTime, throwable);
        }
    }

    @Override
    public Object plugin(Object target)
    {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties)
    {
        // 使用 Spring 配置注入阈值。
    }

    /**
     * 判断是否达到慢 SQL 阈值。
     *
     * @param costTime 消耗时间
     * @return true 表示需要记录
     */
    public boolean shouldRecord(long costTime)
    {
        return costTime >= slowSqlMillis;
    }

    /**
     * 判断 SQL 是否应排除。
     *
     * @param mapperId Mapper 方法 ID
     * @param sqlText SQL 文本
     * @return true 表示排除
     */
    public boolean shouldExclude(String mapperId, String sqlText)
    {
        String lowerMapperId = mapperId == null ? "" : mapperId.toLowerCase();
        String lowerSqlText = sqlText == null ? "" : sqlText.toLowerCase();
        return lowerMapperId.contains("sysslowsqllogmapper") || lowerSqlText.contains(SLOW_SQL_LOG_TABLE);
    }

    /**
     * 按条件记录慢 SQL。
     *
     * @param invocation MyBatis 调用
     * @param costTime 消耗时间
     * @param throwable 异常
     */
    private void recordIfSlow(Invocation invocation, long costTime, Throwable throwable)
    {
        if (!shouldRecord(costTime) || invocation.getArgs().length == 0
                || !(invocation.getArgs()[0] instanceof MappedStatement mappedStatement))
        {
            return;
        }
        BoundSql boundSql = resolveBoundSql(invocation, mappedStatement);
        String sqlText = boundSql == null ? null : normalizeSql(boundSql.getSql());
        String mapperId = mappedStatement.getId();
        if (shouldExclude(mapperId, sqlText))
        {
            return;
        }
        SlowSqlLogRecord record = SlowSqlLogRecord.builder()
                .mapperId(StringUtils.substring(mapperId, 0, 255))
                .sqlText(StringUtils.substring(sqlText, 0, TEXT_MAX_LENGTH))
                .dataSourceName(StringUtils.substring(DynamicDataSourceContextHolder.getDataSourceType(), 0, 64))
                .costTime(costTime)
                .errorMsg(resolveErrorMessage(throwable))
                .executeTime(new Date())
                .build();
        AsyncManager.me().execute(AsyncFactory.recordSlowSql(record));
    }

    /**
     * 解析 BoundSql。
     *
     * @param invocation MyBatis 调用
     * @param mappedStatement Mapper 声明
     * @return BoundSql
     */
    private BoundSql resolveBoundSql(Invocation invocation, MappedStatement mappedStatement)
    {
        Object[] args = invocation.getArgs();
        if (args.length >= 6 && args[5] instanceof BoundSql boundSql)
        {
            return boundSql;
        }
        Object parameter = args.length > 1 ? args[1] : null;
        return mappedStatement.getBoundSql(parameter);
    }

    /**
     * 规整 SQL 文本。
     *
     * @param sql SQL
     * @return 规整后的 SQL
     */
    private String normalizeSql(String sql)
    {
        if (sql == null)
        {
            return null;
        }
        return sql.replaceAll("\\s+", " ").trim();
    }

    /**
     * 解析异常摘要。
     *
     * @param throwable 异常
     * @return 异常摘要
     */
    private String resolveErrorMessage(Throwable throwable)
    {
        if (throwable == null)
        {
            return null;
        }
        return StringUtils.substring(ExceptionUtil.getExceptionMessage(throwable), 0, 2000);
    }
}
