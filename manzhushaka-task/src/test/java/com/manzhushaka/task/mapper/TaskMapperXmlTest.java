package com.manzhushaka.task.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.Test;

/** 异步任务 Mapper XML 契约测试。 */
class TaskMapperXmlTest
{
    @Test
    void importTerminalUpdatesShouldRequireLeaseToken() throws Exception
    {
        assertTerminalUpdatesRequireLeaseToken("ImportTaskMapper.xml", ImportTaskMapper.class.getName());
    }

    @Test
    void exportTerminalUpdatesShouldRequireLeaseToken() throws Exception
    {
        assertTerminalUpdatesRequireLeaseToken("ExportTaskMapper.xml", ExportTaskMapper.class.getName());
    }

    private void assertTerminalUpdatesRequireLeaseToken(String fileName, String namespace) throws Exception
    {
        Path path = Path.of("src/main/resources/mapper/task", fileName);
        Configuration configuration = new Configuration();
        try (InputStream inputStream = Files.newInputStream(path))
        {
            new XMLMapperBuilder(inputStream, configuration, path.toString(),
                    configuration.getSqlFragments()).parse();
        }

        for (String statement : new String[] {"markSuccess", "markFailure", "markCancelled"})
        {
            BoundSql boundSql = configuration.getMappedStatement(namespace + "." + statement)
                    .getBoundSql(Map.of());
            String sql = boundSql.getSql().replaceAll("\\s+", " ").trim();
            assertThat(sql).contains("lease_token = ?");
        }
    }
}
