package com.manzhushaka.iip.application.invoice.service;

import java.util.List;
import com.manzhushaka.iip.application.invoice.command.AuditInvoiceCommand;
import com.manzhushaka.iip.application.invoice.command.SubmitInvoiceCommand;
import com.manzhushaka.iip.application.invoice.query.InvoiceQuery;
import com.manzhushaka.iip.application.invoice.result.InvoiceResult;

/**
 * 发票应用服务。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
public interface InvoiceAppService
{
    /**
     * 查询发票列表（管理端）。
     *
     * @param query 查询条件
     * @return 发票列表
     */
    List<InvoiceResult> listInvoices(InvoiceQuery query);

    /**
     * 查询发票详情（管理端）。
     *
     * @param invoiceId 发票ID
     * @return 发票详情，不存在时返回null
     */
    InvoiceResult getInvoice(Long invoiceId);

    /**
     * 审核发票（管理端）。
     *
     * @param command 审核命令
     * @param auditBy 审核人账号
     */
    void auditInvoice(AuditInvoiceCommand command, String auditBy);

    /**
     * 查询当前用户的发票列表（小程序端，状态可选，按创建时间倒序）。
     *
     * @param memberId 用户ID
     * @param status 状态（0待审核 1已通过 2已驳回），null 或空表示全部
     * @return 发票列表
     */
    List<InvoiceResult> listMemberInvoices(Long memberId, String status);

    /**
     * 查询当前用户的发票详情（小程序端，仅本人可查）。
     *
     * @param memberId 用户ID
     * @param invoiceId 发票ID
     * @return 发票详情
     */
    InvoiceResult getMemberInvoice(Long memberId, Long invoiceId);

    /**
     * 当前用户提交发票（小程序端）。
     *
     * @param command 提交命令
     * @param memberId 用户ID
     * @return 发票ID
     */
    Long submitInvoice(SubmitInvoiceCommand command, Long memberId);
}
