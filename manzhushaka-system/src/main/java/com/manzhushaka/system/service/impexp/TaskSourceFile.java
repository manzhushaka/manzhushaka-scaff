package com.manzhushaka.system.service.impexp;

/**
 * 承载 TaskSourceFile 数据。
 */
public record TaskSourceFile(String fileName, byte[] content) {
}
