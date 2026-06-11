package com.manzhushaka.db.config;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.manzhushaka.db.monitor.SlowSqlMonitorInterceptor;
import com.manzhushaka.db.monitor.SlowSqlMonitorStore;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.beans.factory.annotation.Value;

import java.time.LocalDateTime;

@Configuration
public class MybatisPlusConfig {

    /**
     * 配置 MyBatis-Plus 主拦截器。
     *
     * @return MyBatis-Plus 拦截器
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor());
        return interceptor;
    }

    /**
     * 创建慢 SQL 监控存储器。
     *
     * @param capacity 缓冲区容量
     * @param thresholdMs 慢 SQL 阈值
     * @return 慢 SQL 存储器
     */
    @Bean
    public SlowSqlMonitorStore slowSqlMonitorStore(
        @Value("${manzhushaka.monitor.slow-sql.capacity:120}") int capacity,
        @Value("${manzhushaka.monitor.slow-sql.threshold-ms:500}") long thresholdMs
    ) {
        return new SlowSqlMonitorStore(capacity, thresholdMs);
    }

    /**
     * 创建慢 SQL 监控拦截器。
     *
     * @param store 慢 SQL 存储器
     * @return 慢 SQL 监控拦截器
     */
    @Bean
    public SlowSqlMonitorInterceptor slowSqlMonitorInterceptor(SlowSqlMonitorStore store) {
        return new SlowSqlMonitorInterceptor(store);
    }

    /**
     * 配置自动填充处理器。
     *
     * @return 自动填充处理器
     */
    @Bean
    public MetaObjectHandler metaObjectHandler() {
        return new MetaObjectHandler() {
            /**
             * 填充新增场景的通用时间字段。
             *
             * @param metaObject 元对象
             */
            @Override
            public void insertFill(MetaObject metaObject) {
                strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
                strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
            }

            /**
             * 填充更新场景的通用时间字段。
             *
             * @param metaObject 元对象
             */
            @Override
            public void updateFill(MetaObject metaObject) {
                strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
            }
        };
    }
}
