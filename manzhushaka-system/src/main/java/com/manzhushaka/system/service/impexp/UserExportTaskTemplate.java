package com.manzhushaka.system.service.impexp;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.manzhushaka.db.system.entity.SysImportExportTask;
import com.manzhushaka.db.system.entity.SysUser;
import com.manzhushaka.db.system.mapper.SysImportExportTaskMapper;
import com.manzhushaka.db.system.mapper.SysUserMapper;
import com.manzhushaka.framework.storage.BosStorageProperties;
import com.manzhushaka.framework.storage.ObjectStorageService;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class UserExportTaskTemplate extends AbstractExportTaskTemplate {

    private final SysUserMapper userMapper;

    public UserExportTaskTemplate(
        SysImportExportTaskMapper taskMapper,
        ObjectStorageService storageService,
        BosStorageProperties properties,
        SysUserMapper userMapper
    ) {
        super(taskMapper, storageService, properties.getBasePath());
        this.userMapper = userMapper;
    }

    @Override
    public String bizType() {
        return "SYS_USER_EXPORT";
    }

    @Override
    public String bizLabel() {
        return "系统用户导出示例";
    }

    @Override
    protected String defaultTaskName() {
        return "系统用户导出示例";
    }

    @Override
    protected TaskExecutionResult executeExport(SysImportExportTask task) {
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
}
