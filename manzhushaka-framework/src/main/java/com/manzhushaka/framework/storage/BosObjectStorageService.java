package com.manzhushaka.framework.storage;

import com.baidubce.auth.DefaultBceCredentials;
import com.baidubce.http.HttpMethodName;
import com.baidubce.services.bos.BosClient;
import com.baidubce.services.bos.BosClientConfiguration;
import com.baidubce.services.bos.model.GeneratePresignedUrlRequest;
import com.baidubce.services.bos.model.ObjectMetadata;
import com.baidubce.services.bos.model.ResponseHeaderOverrides;
import com.manzhushaka.common.exception.BizException;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

/**
 * 定义 BosObjectStorageService。
 */
@Service
public class BosObjectStorageService implements ObjectStorageService {

    private final BosStorageProperties properties;
    private volatile BosClient client;

    /**
     * 创建 BosObjectStorageService 实例。
     *
     * @param properties properties 参数
     */
    public BosObjectStorageService(BosStorageProperties properties) {
        this.properties = properties;
    }

    /**
     * 执行 put Object 逻辑。
     *
     * @param objectKey objectKey 参数
     * @param content content 参数
     * @param contentType contentType 参数
     */
    @Override
    public void putObject(String objectKey, byte[] content, String contentType) {
        validateConfiguration();
        String normalizedObjectKey = normalizeObjectKey(objectKey);
        byte[] payload = content == null ? new byte[0] : content;
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(payload.length);
        metadata.setContentType(StringUtils.hasText(contentType) ? contentType : "application/octet-stream");
        try {
            getClient().putObject(properties.getBucket(), normalizedObjectKey, payload, metadata);
        } catch (Exception exception) {
            throw new BizException(500, "上传 BOS 文件失败: " + exception.getMessage());
        }
    }

    /**
     * 返回 objectContent。
     *
     * @param objectKey objectKey 参数
     * @return 字段值
     */
    @Override
    public byte[] getObjectContent(String objectKey) {
        validateConfiguration();
        String normalizedObjectKey = normalizeObjectKey(objectKey);
        try {
            return getClient().getObjectContent(properties.getBucket(), normalizedObjectKey);
        } catch (Exception exception) {
            throw new BizException(500, "读取 BOS 文件失败: " + exception.getMessage());
        }
    }

    /**
     * 生成下载地址。
     *
     * @param objectKey objectKey 参数
     * @param downloadFileName downloadFileName 参数
     * @return 创建结果
     */
    @Override
    public String generateDownloadUrl(String objectKey, String downloadFileName) {
        validateConfiguration();
        String normalizedObjectKey = normalizeObjectKey(objectKey);
        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(
            properties.getBucket(),
            normalizedObjectKey,
            HttpMethodName.GET
        );
        request.setExpiration(Math.max(properties.getDownloadExpireSeconds(), 60));
        ResponseHeaderOverrides headers = new ResponseHeaderOverrides();
        headers.setContentDisposition(buildContentDisposition(downloadFileName));
        request.setResponseHeaders(headers);

        try {
            return getClient().generatePresignedUrl(request).toString();
        } catch (Exception exception) {
            throw new BizException(500, "生成 BOS 下载链接失败: " + exception.getMessage());
        }
    }

    /**
     * 获取复用的 BOS 客户端实例。
     *
     * @return BOS 客户端
     */
    private BosClient getClient() {
        BosClient localClient = client;
        if (localClient != null) {
            return localClient;
        }
        /**
         * 更新 synchronized 数据。
         *
         * @param this this 参数
         * @return 处理结果
         */
        synchronized (this) {
            if (client == null) {
                BosClientConfiguration configuration = new BosClientConfiguration()
                    .withEndpoint(properties.getEndpoint())
                    .withCredentials(new DefaultBceCredentials(properties.getAccessKeyId(), properties.getSecretAccessKey()));
                client = new BosClient(configuration);
            }
            return client;
        }
    }

    /**
     * 校验对象存储配置是否完整。
     */
    private void validateConfiguration() {
        if (!StringUtils.hasText(properties.getEndpoint())
            || !StringUtils.hasText(properties.getBucket())
            || !StringUtils.hasText(properties.getAccessKeyId())
            || !StringUtils.hasText(properties.getSecretAccessKey())) {
            throw new BizException(500, "BOS 配置不完整，请检查 endpoint、bucket 和访问密钥");
        }
    }

    /**
     * 规范化并校验对象路径必须落在受控前缀下。
     *
     * @param objectKey 原始对象路径
     * @return 规范化后的对象路径
     */
    private String normalizeObjectKey(String objectKey) {
        if (!StringUtils.hasText(objectKey)) {
            throw new BizException(400, "对象存储路径不能为空");
        }
        String normalized = objectKey.trim();
        while (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        String basePath = normalizeBasePath();
        if (!normalized.equals(basePath) && !normalized.startsWith(basePath + "/")) {
            throw new BizException(403, "无权访问该对象");
        }
        return normalized;
    }

    /**
     * 获取标准化后的受控根路径。
     *
     * @return BOS 根路径
     */
    private String normalizeBasePath() {
        String basePath = StringUtils.hasText(properties.getBasePath()) ? properties.getBasePath().trim() : "import-export";
        while (basePath.startsWith("/")) {
            basePath = basePath.substring(1);
        }
        while (basePath.endsWith("/")) {
            basePath = basePath.substring(0, basePath.length() - 1);
        }
        return basePath;
    }

    /**
     * 构建下载响应头中的文件名。
     *
     * @param fileName 下载文件名
     * @return Content-Disposition 响应头值
     */
    private String buildContentDisposition(String fileName) {
        String normalized = StringUtils.hasText(fileName) ? fileName.trim() : "download.bin";
        String encoded = URLEncoder.encode(normalized, StandardCharsets.UTF_8).replace("+", "%20");
        return "attachment; filename=\"download.bin\"; filename*=UTF-8''" + encoded;
    }

    /**
     * 关闭复用的 BOS 客户端。
     */
    @PreDestroy
    public void shutdown() {
        BosClient localClient = client;
        if (localClient != null) {
            localClient.shutdown();
            client = null;
        }
    }
}
