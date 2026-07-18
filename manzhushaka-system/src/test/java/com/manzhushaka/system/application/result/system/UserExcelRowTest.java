package com.manzhushaka.system.application.result.system;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.List;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import com.manzhushaka.common.utils.poi.ExcelUtil;

/**
 * 用户 Excel 行测试。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
class UserExcelRowTest
{
    /** Excel 导入应通过无参构造和 setter 填充用户行。 */
    @Test
    void importExcelShouldPopulateMutableRow() throws Exception
    {
        byte[] content;
        try (XSSFWorkbook workbook = new XSSFWorkbook();
                ByteArrayOutputStream output = new ByteArrayOutputStream())
        {
            org.apache.poi.ss.usermodel.Row header = workbook.createSheet("用户数据").createRow(0);
            header.createCell(0).setCellValue("部门编号");
            header.createCell(1).setCellValue("登录名称");
            header.createCell(2).setCellValue("用户名称");
            org.apache.poi.ss.usermodel.Row data = workbook.getSheetAt(0).createRow(1);
            data.createCell(0).setCellValue(103L);
            data.createCell(1).setCellValue("tester");
            data.createCell(2).setCellValue("测试用户");
            workbook.write(output);
            content = output.toByteArray();
        }

        List<UserExcelRow> rows = new ExcelUtil<>(UserExcelRow.class)
                .importExcel(new ByteArrayInputStream(content));

        assertThat(rows).hasSize(1);
        assertThat(rows.get(0).getDeptId()).isEqualTo(103L);
        assertThat(rows.get(0).getUserName()).isEqualTo("tester");
        assertThat(rows.get(0).getNickName()).isEqualTo("测试用户");
    }
}
