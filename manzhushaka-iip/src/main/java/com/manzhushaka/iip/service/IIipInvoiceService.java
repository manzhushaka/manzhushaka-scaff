package com.manzhushaka.iip.service;

import java.util.List;
import com.manzhushaka.iip.domain.IipInvoice;

/**
 * 发票 服务层
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
public interface IIipInvoiceService
{
    /**
     * 通过ID查询发票
     *
     * @param invoiceId 发票ID
     * @return 发票信息，不存在时返回null
     */
    public IipInvoice selectInvoiceById(Long invoiceId);

    /**
     * 查询发票列表（管理端，支持状态/发票号码/商户名称/上传时间筛选）
     *
     * @param iipInvoice 查询条件
     * @return 发票集合
     */
    public List<IipInvoice> selectInvoiceList(IipInvoice iipInvoice);

    /**
     * 按用户查询发票列表（小程序端，状态可选，按创建时间倒序）
     *
     * @param memberId 用户ID
     * @param status 状态（0待审核 1已通过 2已驳回），null 或空表示全部
     * @return 发票集合
     */
    public List<IipInvoice> selectMemberInvoiceList(Long memberId, String status);

    /**
     * 用户提交发票（校验必填与金额，防重复上传，初始状态为待审核）
     *
     * @param iipInvoice 发票信息（memberId 由调用方按登录用户写入）
     * @return 发票ID
     * @throws com.manzhushaka.common.exception.ServiceException 校验失败或重复上传时抛出
     */
    public Long submitInvoice(IipInvoice iipInvoice);

    /**
     * 审核发票（仅待审核可审；通过时按当前活动比例发放积分，驳回必须填写原因）
     *
     * @param invoiceId 发票ID
     * @param pass true 通过，false 驳回
     * @param auditRemark 审核备注（驳回必填原因）
     * @param auditBy 审核人账号
     * @throws com.manzhushaka.common.exception.ServiceException 发票不存在、已审核、金额过低或驳回缺原因时抛出
     */
    public void auditInvoice(Long invoiceId, boolean pass, String auditRemark, String auditBy);
}
