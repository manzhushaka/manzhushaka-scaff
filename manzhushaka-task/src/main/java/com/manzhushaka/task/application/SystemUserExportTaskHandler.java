package com.manzhushaka.task.application;

import java.text.SimpleDateFormat;
import java.util.Base64;
import java.util.Date;
import java.util.List;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.springframework.stereotype.Component;

import com.manzhushaka.framework.security.context.PermissionContextHolder;
import com.manzhushaka.system.application.query.UserListQuery;
import com.manzhushaka.system.application.result.system.UserExportCursorRow;
import com.manzhushaka.system.application.service.SystemUserAppService;
import com.manzhushaka.task.infrastructure.persistence.entity.ExportTask;

import tools.jackson.databind.ObjectMapper;

/**
 * 系统用户异步流式导出处理器。
 *
 * @author manzhushaka
 * @date 2026-09-03
 */
@Component
public class SystemUserExportTaskHandler extends AbstractExportTaskHandler<UserExportCursorRow>
{
    public static final String HANDLER_TYPE = "SYSTEM_USER_EXPORT";
    private static final String DATA_SCOPE_PERMISSION = "monitor:exporttask:submit";
    private static final String[] HEADERS = {
            "用户序号", "登录名称", "用户名称", "用户邮箱", "手机号码", "用户性别",
            "账号状态", "部门名称", "部门负责人", "最后登录IP", "最后登录时间"
    };
    private static final String DATE_PATTERN = "yyyy-MM-dd HH:mm:ss";

    private final SystemUserAppService userAppService;
    private final ObjectMapper objectMapper;

    public SystemUserExportTaskHandler(SystemUserAppService userAppService, ObjectMapper objectMapper)
    {
        this.userAppService = userAppService;
        this.objectMapper = objectMapper;
    }

    @Override
    public String handlerType()
    {
        return HANDLER_TYPE;
    }

    @Override
    protected long countRows(TaskContext<?> context)
    {
        PermissionContextHolder.setContext(DATA_SCOPE_PERMISSION);
        return userAppService.countUserExportRows(query(context));
    }

    @Override
    protected String sheetName()
    {
        return "用户数据";
    }

    @Override
    protected void writeHeader(Row row)
    {
        for (int index = 0; index < HEADERS.length; index++)
        {
            row.createCell(index).setCellValue(HEADERS[index]);
        }
    }

    @Override
    protected List<UserExportCursorRow> fetchBatch(TaskContext<?> context, String cursor, int batchSize)
    {
        PermissionContextHolder.setContext(DATA_SCOPE_PERMISSION);
        CursorValue cursorValue = decodeCursor(cursor);
        return userAppService.listUserExportRows(query(context),
                cursorValue == null ? null : cursorValue.createTime(),
                cursorValue == null ? null : cursorValue.userId(), batchSize);
    }

    @Override
    protected void writeRow(Row row, UserExportCursorRow value)
    {
        setCell(row, 0, value.getUserId());
        setCell(row, 1, value.getUserName());
        setCell(row, 2, value.getNickName());
        setCell(row, 3, value.getEmail());
        setCell(row, 4, value.getPhonenumber());
        setCell(row, 5, convertSex(value.getSex()));
        setCell(row, 6, "0".equals(value.getStatus()) ? "正常" : "停用");
        setCell(row, 7, value.getDeptName());
        setCell(row, 8, value.getDeptLeader());
        setCell(row, 9, value.getLoginIp());
        setCell(row, 10, value.getLoginDate() == null ? null
                : new SimpleDateFormat(DATE_PATTERN).format(value.getLoginDate()));
    }

    @Override
    protected String nextCursor(UserExportCursorRow lastRow)
    {
        CursorValue cursor = new CursorValue(lastRow.getCreateTime(), lastRow.getUserId());
        return Base64.getUrlEncoder().withoutPadding().encodeToString(
                objectMapper.writeValueAsBytes(cursor));
    }

    private UserListQuery query(TaskContext<?> context)
    {
        ExportTask task = (ExportTask) context.task();
        return objectMapper.readValue(task.getQuerySnapshot(), UserListQuery.class);
    }

    private CursorValue decodeCursor(String cursor)
    {
        if (cursor == null)
        {
            return null;
        }
        return objectMapper.readValue(Base64.getUrlDecoder().decode(cursor), CursorValue.class);
    }

    private void setCell(Row row, int index, Object value)
    {
        Cell cell = row.createCell(index);
        if (value instanceof Number number)
        {
            cell.setCellValue(number.doubleValue());
        }
        else
        {
            cell.setCellValue(value == null ? "" : value.toString());
        }
    }

    private String convertSex(String sex)
    {
        if ("0".equals(sex))
        {
            return "男";
        }
        if ("1".equals(sex))
        {
            return "女";
        }
        return "未知";
    }

    private record CursorValue(Date createTime, Long userId)
    {
    }
}
