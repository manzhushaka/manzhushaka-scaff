package com.manzhushaka.db.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.jdbc.datasource.init.ScriptUtils;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Arrays;
import java.util.Comparator;

/**
 * 配置 DatabasePatchRunner 相关组件。
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class DatabasePatchRunner implements ApplicationRunner {
    static final String DEFAULT_LOCATION_PATTERN = "classpath*:db/patch/*_patch.sql";

    /**
     * 返回 logger。
     *
     * @param DatabasePatchRunner.class DatabasePatchRunner.class 参数
     * @return 字段值
     */
    private static final Logger log = LoggerFactory.getLogger(DatabasePatchRunner.class);

    private final DataSource dataSource;
    private final PathMatchingResourcePatternResolver resourceResolver;
    private final String locationPattern;

    /**
     * 创建 DatabasePatchRunner 实例。
     *
     * @param dataSource dataSource 参数
     */
    @Autowired
    public DatabasePatchRunner(DataSource dataSource) {
        this(dataSource, new PathMatchingResourcePatternResolver(), DEFAULT_LOCATION_PATTERN);
    }

    DatabasePatchRunner(
        DataSource dataSource,
        PathMatchingResourcePatternResolver resourceResolver,
        String locationPattern
    ) {
        this.dataSource = dataSource;
        this.resourceResolver = resourceResolver;
        this.locationPattern = locationPattern;
    }

    /**
     * 执行 run 操作。
     *
     * @param args args 参数
     */
    @Override
    public void run(ApplicationArguments args) throws Exception {
        applyPatches();
    }

    /**
     * 更新 apply Patches 数据。
     */
    void applyPatches() throws Exception {
        Resource[] resources = resourceResolver.getResources(locationPattern);
        if (resources.length == 0) {
            log.debug("No database patch scripts found for pattern {}", locationPattern);
            return;
        }

        Arrays.sort(resources, Comparator.comparing(Resource::getFilename, Comparator.nullsLast(String::compareTo)));

        Connection connection = DataSourceUtils.getConnection(dataSource);
        try {
            for (Resource resource : resources) {
                log.info("Applying database patch script {}", resource.getFilename());
                ScriptUtils.executeSqlScript(connection, resource);
            }
        } finally {
            DataSourceUtils.releaseConnection(connection, dataSource);
        }
    }
}
