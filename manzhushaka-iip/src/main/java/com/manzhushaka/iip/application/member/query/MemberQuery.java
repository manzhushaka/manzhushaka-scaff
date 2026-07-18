package com.manzhushaka.iip.application.member.query;

/**
 * 小程序用户查询条件。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
public record MemberQuery(String keyword, String phone, String status, String beginTime, String endTime)
{
}
