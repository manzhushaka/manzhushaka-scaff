package com.manzhushaka.system.mapper;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.Test;

class SysDeptMapperXmlTest {

    @Test
    void selectDeptListShouldFilterByDeptType() throws Exception {
        String mapper = Files.readString(Path.of("src/main/resources/mapper/system/SysDeptMapper.xml"),
                StandardCharsets.UTF_8);

        String selectDeptList = mapper.substring(mapper.indexOf("<select id=\"selectDeptList\""),
                mapper.indexOf("<select id=\"selectDeptListByRoleId\""));
        assertThat(selectDeptList).contains("dept_type = #{deptType}");
    }
}
