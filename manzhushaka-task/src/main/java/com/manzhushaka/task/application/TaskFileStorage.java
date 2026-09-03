package com.manzhushaka.task.application;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.UUID;

import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.manzhushaka.common.config.ManzhushakaConfig;

/**
 * 异步任务私有文件存储。
 *
 * @author manzhushaka
 * @date 2026-09-03
 */
@Component
public class TaskFileStorage
{
    private static final String ROOT_DIRECTORY = "async-task";

    /** 保存导入文件并返回受控文件键。 */
    public String storeImport(MultipartFile file) throws IOException
    {
        if (file == null || file.isEmpty())
        {
            throw new IOException("导入文件不能为空");
        }
        String originalName = StringUtils.defaultIfBlank(file.getOriginalFilename(), "import.xlsx");
        String extension = StringUtils.substringAfterLast(originalName, ".").toLowerCase(Locale.ROOT);
        if (!"xls".equals(extension) && !"xlsx".equals(extension))
        {
            throw new IOException("仅支持上传 xls、xlsx 格式文件");
        }
        String fileKey = "import/" + UUID.randomUUID() + "." + extension;
        Path target = resolve(fileKey);
        Files.createDirectories(target.getParent());
        try (InputStream inputStream = file.getInputStream())
        {
            Files.copy(inputStream, target, StandardCopyOption.REPLACE_EXISTING);
        }
        return fileKey;
    }

    /** 创建导出文件键。 */
    public String newExportKey()
    {
        return "export/" + UUID.randomUUID() + ".xlsx";
    }

    /** 解析并校验文件键。 */
    public Path resolve(String fileKey)
    {
        if (StringUtils.isBlank(ManzhushakaConfig.getProfile()) || StringUtils.isBlank(fileKey)
                || fileKey.contains("..") || fileKey.startsWith("/") || fileKey.contains("\\"))
        {
            throw new IllegalArgumentException("非法任务文件键");
        }
        Path root = Path.of(ManzhushakaConfig.getProfile(), ROOT_DIRECTORY).toAbsolutePath().normalize();
        Path path = root.resolve(fileKey).normalize();
        if (!path.startsWith(root))
        {
            throw new IllegalArgumentException("非法任务文件路径");
        }
        return path;
    }

    /** 尽力删除任务文件。 */
    public void deleteQuietly(String fileKey)
    {
        try
        {
            Files.deleteIfExists(resolve(fileKey));
        }
        catch (IOException | IllegalArgumentException ignored)
        {
            // 清理失败不覆盖任务最终状态。
        }
    }
}
