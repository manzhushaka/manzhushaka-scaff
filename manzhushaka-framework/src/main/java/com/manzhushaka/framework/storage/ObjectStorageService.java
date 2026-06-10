package com.manzhushaka.framework.storage;

public interface ObjectStorageService {

    void putObject(String objectKey, byte[] content, String contentType);

    byte[] getObjectContent(String objectKey);

    String generateDownloadUrl(String objectKey, String downloadFileName);
}
