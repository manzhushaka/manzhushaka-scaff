package com.manzhushaka.framework.storage;

/**
 * 定义 ObjectStorageService。
 */
public interface ObjectStorageService {

    /**
     * 执行 put Object 逻辑。
     *
     * @param objectKey objectKey 参数
     * @param content content 参数
     * @param contentType contentType 参数
     */
    void putObject(String objectKey, byte[] content, String contentType);

    /**
     * 返回 objectContent。
     *
     * @param objectKey objectKey 参数
     * @return 字段值
     */
    byte[] getObjectContent(String objectKey);

    /**
     * 生成下载地址。
     *
     * @param objectKey objectKey 参数
     * @param downloadFileName downloadFileName 参数
     * @return 创建结果
     */
    String generateDownloadUrl(String objectKey, String downloadFileName);
}
