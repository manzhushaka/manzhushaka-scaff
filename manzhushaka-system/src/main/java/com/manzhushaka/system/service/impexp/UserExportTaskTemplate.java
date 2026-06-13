package com.manzhushaka.system.service.impexp;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.manzhushaka.db.system.entity.SysImportExportTask;
import com.manzhushaka.db.system.entity.SysUser;
import com.manzhushaka.db.system.mapper.SysImportExportTaskMapper;
import com.manzhushaka.db.system.mapper.SysUserMapper;
import com.manzhushaka.framework.storage.BosStorageProperties;
import com.manzhushaka.framework.storage.ObjectStorageService;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 定义 UserExportTaskTemplate。
 */
@Component
public class UserExportTaskTemplate extends AbstractExportTaskTemplate<UserExportTaskTemplate.Command> {

    private final SysUserMapper userMapper;

    public UserExportTaskTemplate(
        SysImportExportTaskMapper taskMapper,
        ObjectStorageService storageService,
        BosStorageProperties properties,
        ObjectMapper objectMapper,
        SysUserMapper userMapper
    ) {
        super(taskMapper, storageService, objectMapper, Command.class, properties.getBasePath());
        this.userMapper = userMapper;
    }

    /**
     * 执行 biz Type 逻辑。
     *
     * @return 处理结果
     */
    @Override
    public String bizType() {
        return "SYS_USER_EXPORT";
    }

    /**
     * 执行 biz Label 逻辑。
     *
     * @return 处理结果
     */
    @Override
    public String bizLabel() {
        return "系统用户导出示例";
    }

    /**
     * 执行 default Task Name 逻辑。
     *
     * @return 处理结果
     */
    @Override
    protected String defaultTaskName() {
        return "系统用户导出示例";
    }

    /**
     * 执行 execute Export 操作。
     *
     * @param task task 参数
     * @param command command 参数
     * @return 处理结果
     */
    @Override
    protected TaskExecutionResult executeExport(SysImportExportTask task, Command command) {
        List<SysUser> users = userMapper.selectList(new LambdaQueryWrapper<SysUser>()
            .eq(SysUser::getDeleted, 0)
            .orderByAsc(SysUser::getId));
        StringBuilder builder = new StringBuilder("username,nickname,deptId,status\n");
        for (SysUser user : users) {
            builder.append(csv(user.getUsername())).append(',')
                .append(csv(user.getNickname())).append(',')
                .append(user.getDeptId() == null ? "" : user.getDeptId()).append(',')
                .append(user.getStatus() == null ? "" : user.getStatus())
                .append('\n');
        }
        byte[] content = builder.toString().getBytes(StandardCharsets.UTF_8);
        return TaskExecutionResult.success(
            users.size(),
            users.size(),
            0,
            "导出完成，共生成 " + users.size() + " 条记录",
            new TaskFileArtifact("sys-users-export.csv", "text/csv", content)
        );
    }

    /**
     * 执行 csv 逻辑。
     *
     * @param value 字段值
     * @return 处理结果
     */
    private String csv(String value) {
        if (value == null) {
            return "";
        }
        String normalized = value.replace("\"", "\"\"");
        if (normalized.contains(",") || normalized.contains("\"") || normalized.contains("\n")) {
            return "\"" + normalized + "\"";
        }
        return normalized;
    }

    public static class Command extends ExportTaskSubmitCommand {
    }
}
