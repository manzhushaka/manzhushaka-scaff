package com.manzhushaka.biz.pii.infrastructure.persistence.support;

import com.manzhushaka.biz.pii.domain.model.MerchantProfile;
import com.manzhushaka.biz.pii.domain.repository.MerchantProfileRepository;
import com.manzhushaka.framework.security.context.SecurityContextHelper;
import com.manzhushaka.framework.security.model.LoginPrincipal;
import org.apache.ibatis.executor.statement.StatementHandler;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.plugin.Interceptor;
import org.apache.ibatis.plugin.Intercepts;
import org.apache.ibatis.plugin.Invocation;
import org.apache.ibatis.plugin.Plugin;
import org.apache.ibatis.plugin.Signature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Intercepts({@Signature(type = StatementHandler.class, method = "prepare", args = {Connection.class, Integer.class})})
public class PiiTenantInterceptor implements Interceptor {
    private static final Pattern TENANT_TABLES = Pattern.compile(
            "\\b(pii_pay_order|pii_refund_record|pii_pay_qrcode)\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern HAS_WHERE = Pattern.compile("\\bwhere\\b", Pattern.CASE_INSENSITIVE);
    private static final Pattern HAS_MERCHANT_FILTER = Pattern.compile("\\bmerchant_id\\s*=", Pattern.CASE_INSENSITIVE);
    private static final Pattern TAIL_CLAUSE = Pattern.compile(
            "\\s+(order\\s+by|group\\s+by|having|limit|for\\s+update)\\b", Pattern.CASE_INSENSITIVE);

    private final MerchantProfileRepository merchantProfileRepository;

    public PiiTenantInterceptor(MerchantProfileRepository merchantProfileRepository) {
        this.merchantProfileRepository = merchantProfileRepository;
    }

    @Override
    public Object intercept(Invocation invocation) throws Throwable {
        StatementHandler statementHandler = (StatementHandler) invocation.getTarget();
        BoundSql boundSql = statementHandler.getBoundSql();
        Long merchantId = currentMerchantId();
        if (merchantId == null) {
            return invocation.proceed();
        }

        String newSql = injectTenantFilter(boundSql.getSql(), merchantId);
        if (!newSql.equals(boundSql.getSql())) {
            replaceSql(boundSql, newSql);
        }
        return invocation.proceed();
    }

    String injectTenantFilter(String sql, Long merchantId) {
        if (sql == null || merchantId == null) {
            return sql;
        }
        String normalized = sql.trim().toLowerCase(Locale.ENGLISH);
        if (normalized.startsWith("insert") || !TENANT_TABLES.matcher(sql).find()
                || HAS_MERCHANT_FILTER.matcher(sql).find()) {
            return sql;
        }

        Matcher tailMatcher = TAIL_CLAUSE.matcher(sql);
        int tailIndex = tailMatcher.find() ? tailMatcher.start() : sql.length();
        String head = sql.substring(0, tailIndex);
        String tail = sql.substring(tailIndex);
        String condition = "merchant_id = " + merchantId;
        if (HAS_WHERE.matcher(head).find()) {
            return head + " and " + condition + tail;
        }
        return head + " where " + condition + tail;
    }

    private Long currentMerchantId() {
        LoginPrincipal principal = SecurityContextHelper.getPrincipalQuietly();
        if (principal == null || principal.isAdmin() || hasRole(principal.getRoleKeys(), "operator")) {
            return null;
        }
        Long deptId = principal.getDeptId();
        if (deptId == null) {
            return null;
        }
        return merchantProfileRepository.findByDeptId(deptId)
                .map(MerchantProfile::getId)
                .orElse(null);
    }

    private boolean hasRole(Set<String> roleKeys, String roleKey) {
        return roleKeys != null && roleKeys.contains(roleKey);
    }

    private void replaceSql(BoundSql boundSql, String newSql) {
        try {
            Field sqlField = BoundSql.class.getDeclaredField("sql");
            sqlField.setAccessible(true);
            sqlField.set(boundSql, newSql);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("替换 MyBatis BoundSql 失败", e);
        }
    }

    @Override
    public Object plugin(Object target) {
        return Plugin.wrap(target, this);
    }

    @Override
    public void setProperties(Properties properties) {
        // no properties
    }
}
