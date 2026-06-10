package com.manzhushaka.db.config;

import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DatabasePatchRunnerTest {

    @Test
    void canBeInstantiatedBySpringWithDataSourceOnly() {
        try (AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext()) {
            context.register(TestDataSourceConfiguration.class);
            context.register(DatabasePatchRunner.class);

            context.refresh();

            assertTrue(context.containsBeanDefinition("databasePatchRunner"));
            assertEquals(DatabasePatchRunner.class, context.getBean(DatabasePatchRunner.class).getClass());
        }
    }

    @Test
    void discoversImportExportTaskPatchFromClasspath() throws Exception {
        Resource[] resources = new PathMatchingResourcePatternResolver()
            .getResources("classpath*:db/patch/*_patch.sql");

        assertTrue(
            Arrays.stream(resources)
                .map(Resource::getFilename)
                .anyMatch("20260610_import_export_task_patch.sql"::equals),
            "expected import/export task patch on classpath"
        );
    }

    @Test
    void appliesConfiguredPatchScripts() throws Exception {
        DataSource dataSource = new DriverManagerDataSource(
            "jdbc:h2:mem:db_patch_runner;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
            "sa",
            ""
        );
        PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        Class<?> runnerType = Class.forName("com.manzhushaka.db.config.DatabasePatchRunner");
        Object runner = runnerType.getDeclaredConstructor(DataSource.class, PathMatchingResourcePatternResolver.class, String.class)
            .newInstance(dataSource, resolver, "classpath:db/test-patch/*_patch.sql");
        Method applyPatches = runnerType.getDeclaredMethod("applyPatches");
        applyPatches.setAccessible(true);

        applyPatches.invoke(runner);
        applyPatches.invoke(runner);

        try (Connection connection = dataSource.getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery("SELECT COUNT(*) FROM demo_patch_table")) {
            assertTrue(resultSet.next());
            assertEquals(1, resultSet.getInt(1));
        }
    }

    @Configuration
    static class TestDataSourceConfiguration {

        @Bean
        DataSource dataSource() {
            return new DriverManagerDataSource(
                "jdbc:h2:mem:db_patch_runner_context;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
                "sa",
                ""
            );
        }
    }
}
