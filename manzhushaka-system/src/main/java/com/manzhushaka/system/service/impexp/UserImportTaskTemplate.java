package com.manzhushaka.system.service.impexp;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.manzhushaka.common.exception.BizException;
import com.manzhushaka.db.system.entity.SysImportExportTask;
import com.manzhushaka.db.system.mapper.SysImportExportTaskMapper;
import com.manzhushaka.framework.storage.BosStorageProperties;
import com.manzhushaka.framework.storage.ObjectStorageService;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Set;

@Component
public class UserImportTaskTemplate extends AbstractImportTaskTemplate<UserImportTaskTemplate.Command> {

    public UserImportTaskTemplate(
        SysImportExportTaskMapper taskMapper,
        ObjectStorageService storageService,
        BosStorageProperties properties,
        ObjectMapper objectMapper
    ) {
        super(taskMapper, storageService, objectMapper, Command.class, properties.getBasePath());
    }

    @Override
    public String bizType() {
        return "SYS_USER_IMPORT";
    }

    @Override
    public String bizLabel() {
        return "系统用户导入校验示例";
    }

    @Override
    protected String defaultTaskName() {
        return "系统用户导入校验示例";
    }

    @Override
    protected void validateSubmit(Command command) {
        if (!StringUtils.hasText(command.getFileName())) {
            throw new BizException(400, "导入文件名不能为空");
        }
        if (command.getContent().length == 0) {
            throw new BizException(400, "导入文件不能为空");
        }
    }

    @Override
    protected TaskExecutionResult executeImport(SysImportExportTask task, Command command, TaskSourceFile sourceFile) {
        String csv = new String(sourceFile.content(), StandardCharsets.UTF_8);
        String[] lines = csv.replace("\r\n", "\n").split("\n");
        if (lines.length == 0 || !"username,nickname,deptId,status".equals(lines[0].trim())) {
            throw new BizException(400, "导入模板表头不正确，应为 username,nickname,deptId,status");
        }

        StringBuilder report = new StringBuilder("rowNumber,username,result,message\n");
        Set<String> usernames = new HashSet<>();
        int total = 0;
        int success = 0;
        int fail = 0;

        for (int index = 1; index < lines.length; index++) {
            String line = lines[index].trim();
            if (!StringUtils.hasText(line)) {
                continue;
            }
            total++;
            String[] columns = line.split(",", -1);
            String username = columns.length > 0 ? columns[0].trim() : "";
            String nickname = columns.length > 1 ? columns[1].trim() : "";
            String deptId = columns.length > 2 ? columns[2].trim() : "";
            String status = columns.length > 3 ? columns[3].trim() : "";
            String message = validateRow(usernames, username, nickname, deptId, status);
            boolean passed = !StringUtils.hasText(message);
            if (passed) {
                success++;
                report.append(index + 1).append(',').append(username).append(",SUCCESS,校验通过\n");
            } else {
                fail++;
                report.append(index + 1).append(',').append(username).append(",FAIL,").append(message).append('\n');
            }
        }

        return TaskExecutionResult.success(
            total,
            success,
            fail,
            "导入校验完成，成功 " + success + " 条，失败 " + fail + " 条",
            new TaskFileArtifact("sys-user-import-report.csv", "text/csv", report.toString().getBytes(StandardCharsets.UTF_8))
        );
    }

    private String validateRow(Set<String> usernames, String username, String nickname, String deptId, String status) {
        if (!StringUtils.hasText(username)) {
            return "用户名不能为空";
        }
        if (!usernames.add(username)) {
            return "用户名在文件中重复";
        }
        if (!StringUtils.hasText(nickname)) {
            return "昵称不能为空";
        }
        if (StringUtils.hasText(deptId) && !deptId.chars().allMatch(Character::isDigit)) {
            return "deptId 必须为数字";
        }
        if (!"0".equals(status) && !"1".equals(status)) {
            return "status 仅支持 0 或 1";
        }
        return "";
    }

    public static class Command extends ImportTaskSubmitCommand {
    }
}
