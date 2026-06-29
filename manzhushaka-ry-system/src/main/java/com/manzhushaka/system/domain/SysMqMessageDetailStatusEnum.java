package com.manzhushaka.system.domain;

/**
 * 消息队列执行明细状态枚举。
 *
 * @author manzhushaka
 * @date 2026-06-29
 */
public enum SysMqMessageDetailStatusEnum
{
    PROCESSING("0", "执行中"),
    SUCCESS("1", "成功"),
    FAILED("2", "失败"),
    SKIPPED("3", "已跳过");

    private final String code;
    private final String info;

    SysMqMessageDetailStatusEnum(String code, String info)
    {
        this.code = code;
        this.info = info;
    }

    public String getCode()
    {
        return code;
    }

    public String getInfo()
    {
        return info;
    }
}