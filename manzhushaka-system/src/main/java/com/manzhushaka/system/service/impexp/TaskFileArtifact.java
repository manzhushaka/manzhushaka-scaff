package com.manzhushaka.system.service.impexp;

/**
 * 承载 TaskFileArtifact 数据。
 */
public record TaskFileArtifact(String fileName, String contentType, byte[] content) {
}
