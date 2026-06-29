package com.manzhushaka.system.domain;

/**
 * 消息队列主台账状态枚举。
 *
 * @author manzhushaka
 * @date 2026-06-29
 */
public enum SysMqMessageStatusEnum
{
    PROCESSING("0", "执行中"),
    SUCCESS("1", "成功"),
    FAILED("2", "失败"),
    SKIPPED("3", "已跳过"),
    DEAD_LETTER("4", "死信");

    private final String code;
    private final String info;

    SysMqMessageStatusEnum(String code, String info)
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