package com.manzhushaka.iip.application.invoice.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.manzhushaka.common.exception.ServiceException;
import com.manzhushaka.iip.application.invoice.command.AuditInvoiceCommand;
import com.manzhushaka.iip.application.invoice.command.SubmitInvoiceCommand;
import com.manzhushaka.iip.application.invoice.query.InvoiceQuery;
import com.manzhushaka.iip.application.invoice.result.InvoiceResult;
import com.manzhushaka.iip.application.invoice.service.InvoiceAppService;
import com.manzhushaka.iip.domain.IipInvoice;
import com.manzhushaka.iip.service.IIipInvoiceService;

/**
 * 发票应用服务实现。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
@Service
public class InvoiceAppServiceImpl implements InvoiceAppService
{
    @Autowired
    private IIipInvoiceService invoiceService;

    @Override
    public List<InvoiceResult> listInvoices(InvoiceQuery query)
    {
        return invoiceService.selectInvoiceList(toEntity(query)).stream()
                .map(this::toResult)
                .toList();
    }

    @Override
    public InvoiceResult getInvoice(Long invoiceId)
    {
        return toResult(invoiceService.selectInvoiceById(invoiceId));
    }

    @Override
    @Transactional
    public void auditInvoice(AuditInvoiceCommand command, String auditBy)
    {
        invoiceService.auditInvoice(command.invoiceId(), Boolean.TRUE.equals(command.pass()),
                command.auditRemark(), auditBy);
    }

    @Override
    public List<InvoiceResult> listMemberInvoices(Long memberId, String status)
    {
        return invoiceService.selectMemberInvoiceList(memberId, status).stream()
                .map(this::toResult)
                .toList();
    }

    @Override
    public InvoiceResult getMemberInvoice(Long memberId, Long invoiceId)
    {
        IipInvoice invoice = invoiceService.selectInvoiceById(invoiceId);
        if (invoice == null)
        {
            throw new ServiceException("发票不存在");
        }
        if (!memberId.equals(invoice.getMemberId()))
        {
            throw new ServiceException("无权查看");
        }
        return toResult(invoice);
    }

    @Override
    @Transactional
    public Long submitInvoice(SubmitInvoiceCommand command, Long memberId)
    {
        IipInvoice invoice = new IipInvoice();
        invoice.setMemberId(memberId);
        invoice.setMerchantId(command.merchantId());
        invoice.setMerchantName(command.merchantName());
        invoice.setInvoiceCode(command.invoiceCode());
        invoice.setInvoiceNo(command.invoiceNo());
        invoice.setInvoiceDate(command.invoiceDate());
        invoice.setAmount(command.amount());
        invoice.setImageUrl(command.imageUrl());
        invoice.setCreateBy("m_" + memberId);
        return invoiceService.submitInvoice(invoice);
    }

    /**
     * 查询条件转实体（含上传时间范围参数）。
     *
     * @param query 查询条件
     * @return 发票实体
     */
    private IipInvoice toEntity(InvoiceQuery query)
    {
        IipInvoice invoice = new IipInvoice();
        if (query == null)
        {
            return invoice;
        }
        invoice.setStatus(query.status());
        invoice.setInvoiceNo(query.invoiceNo());
        invoice.setMerchantName(query.merchantName());
        if (query.beginTime() != null)
        {
            invoice.getParams().put("beginTime", query.beginTime());
        }
        if (query.endTime() != null)
        {
            invoice.getParams().put("endTime", query.endTime());
        }
        return invoice;
    }

    /**
     * 实体转结果。
     *
     * @param invoice 发票实体
     * @return 发票结果，入参为null时返回null
     */
    private InvoiceResult toResult(IipInvoice invoice)
    {
        if (invoice == null)
        {
            return null;
        }
        return new InvoiceResult(invoice.getInvoiceId(), invoice.getMemberId(), invoice.getMerchantId(),
                invoice.getMerchantName(), invoice.getInvoiceCode(), invoice.getInvoiceNo(),
                invoice.getInvoiceDate(), invoice.getAmount(), invoice.getImageUrl(), invoice.getStatus(),
                invoice.getPoints(), invoice.getActivityId(), invoice.getPointsRuleId(),
                invoice.getPointsRatioSnapshot(), invoice.getPointsRuleSnapshot(), invoice.getAuditBy(),
                invoice.getAuditTime(), invoice.getAuditRemark(), invoice.getCreateTime(), invoice.getRemark());
    }
}
