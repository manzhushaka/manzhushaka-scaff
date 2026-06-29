package com.manzhushaka.web.vo.monitor;

/**
 * 运行日志文件展示对象。
 *
 * @author manzhushaka
 * @date 2026-06-29
 */
public class RuntimeLogFileVO {

    /** 文件名 */
    private String fileName;

    /** 文件大小 */
    private Long fileSize;

    /** 修改时间 */
    private String updateTime;

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public void setFileSize(Long fileSize) {
        this.fileSize = fileSize;
    }

    public String getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(String updateTime) {
        this.updateTime = updateTime;
    }
}
