package com.manzhushaka.framework.storage;

import com.baidubce.auth.DefaultBceCredentials;
import com.baidubce.http.HttpMethodName;
import com.baidubce.services.bos.BosClient;
import com.baidubce.services.bos.BosClientConfiguration;
import com.baidubce.services.bos.model.GeneratePresignedUrlRequest;
import com.baidubce.services.bos.model.ObjectMetadata;
import com.baidubce.services.bos.model.ResponseHeaderOverrides;
import com.manzhushaka.common.exception.BizException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Service
public class BosObjectStorageService implements ObjectStorageService {

    private final BosStorageProperties properties;

    public BosObjectStorageService(BosStorageProperties properties) {
        this.properties = properties;
    }

    @Override
    public void putObject(String objectKey, byte[] content, String contentType) {
        validateConfiguration();
        if (!StringUtils.hasText(objectKey)) {
            throw new BizException(400, "对象存储路径不能为空");
        }
        byte[] payload = content == null ? new byte[0] : content;
        ObjectMetadata metadata = new ObjectMetadata();
        metadata.setContentLength(payload.length);
        metadata.setContentType(StringUtils.hasText(contentType) ? contentType : "application/octet-stream");
        BosClient client = createClient();
        try {
            client.putObject(properties.getBucket(), objectKey, payload, metadata);
        } catch (Exception exception) {
            throw new BizException(500, "上传 BOS 文件失败: " + exception.getMessage());
        } finally {
            client.shutdown();
        }
    }

    @Override
    public byte[] getObjectContent(String objectKey) {
        validateConfiguration();
        BosClient client = createClient();
        try {
            return client.getObjectContent(properties.getBucket(), objectKey);
        } catch (Exception exception) {
            throw new BizException(500, "读取 BOS 文件失败: " + exception.getMessage());
        } finally {
            client.shutdown();
        }
    }

    @Override
    public String generateDownloadUrl(String objectKey, String downloadFileName) {
        validateConfiguration();
        GeneratePresignedUrlRequest request = new GeneratePresignedUrlRequest(properties.getBucket(), objectKey, HttpMethodName.GET);
        request.setExpiration(Math.max(properties.getDownloadExpireSeconds(), 60));
        ResponseHeaderOverrides headers = new ResponseHeaderOverrides();
        headers.setContentDisposition(buildContentDisposition(downloadFileName));
        request.setResponseHeaders(headers);

        BosClient client = createClient();
        try {
            return client.generatePresignedUrl(request).toString();
        } catch (Exception exception) {
            throw new BizException(500, "生成 BOS 下载链接失败: " + exception.getMessage());
        } finally {
            client.shutdown();
        }
    }

    private BosClient createClient() {
        BosClientConfiguration configuration = new BosClientConfiguration()
            .withEndpoint(properties.getEndpoint())
            .withCredentials(new DefaultBceCredentials(properties.getAccessKeyId(), properties.getSecretAccessKey()));
        return new BosClient(configuration);
    }

    private void validateConfiguration() {
        if (!StringUtils.hasText(properties.getEndpoint())
            || !StringUtils.hasText(properties.getBucket())
            || !StringUtils.hasText(properties.getAccessKeyId())
            || !StringUtils.hasText(properties.getSecretAccessKey())) {
            throw new BizException(500, "BOS 配置不完整，请检查 endpoint、bucket 和访问密钥");
        }
    }

    private String buildContentDisposition(String fileName) {
        String normalized = StringUtils.hasText(fileName) ? fileName.trim() : "download.bin";
        String encoded = URLEncoder.encode(normalized, StandardCharsets.UTF_8).replace("+", "%20");
        return "attachment; filename=\"download.bin\"; filename*=UTF-8''" + encoded;
    }
}
