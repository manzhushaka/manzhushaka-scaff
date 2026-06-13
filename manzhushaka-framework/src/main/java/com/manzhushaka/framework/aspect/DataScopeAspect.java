package com.manzhushaka.framework.aspect;

import com.manzhushaka.common.annotation.DataScope;
import com.manzhushaka.common.context.DataScopeContext;
import com.manzhushaka.common.context.LoginUser;
import com.manzhushaka.common.context.LoginUserContext;
import com.manzhushaka.framework.util.DataScopeCondition;
import com.manzhushaka.framework.util.DataScopeSqlBuilder;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

/**
 * 处理 DataScopeAspect 切面逻辑。
 */
@Aspect
@Component
public class DataScopeAspect {

    /**
     * 处理 around 流程。
     *
     * @param joinPoint joinPoint 参数
     * @param dataScope dataScope 参数
     * @return 处理结果
     */
    @Around("@annotation(dataScope)")
    public Object around(ProceedingJoinPoint joinPoint, DataScope dataScope) throws Throwable {
        LoginUser loginUser = LoginUserContext.get();
        if (loginUser != null) {
            DataScopeCondition condition = DataScopeSqlBuilder.build(
                loginUser,
                dataScope.tableAlias(),
                dataScope.deptColumn(),
                dataScope.userColumn()
            );
            DataScopeContext.set(condition.sqlSegment());
        }
        try {
            return joinPoint.proceed();
        } finally {
            DataScopeContext.clear();
        }
    }
}
