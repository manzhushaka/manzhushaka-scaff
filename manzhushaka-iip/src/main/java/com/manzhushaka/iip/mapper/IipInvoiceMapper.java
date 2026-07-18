package com.manzhushaka.iip.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.manzhushaka.iip.domain.IipInvoice;

/**
 * 发票 数据层
 * 
 * @author manzhushaka
 * @date 2026-07-18
 */
public interface IipInvoiceMapper
{
    /**
     * 通过ID查询发票
     * 
     * @param invoiceId 发票ID
     * @return 发票信息
     */
    public IipInvoice selectIipInvoiceById(Long invoiceId);

    /**
     * 查询发票列表
     * 
     * @param iipInvoice 查询条件
     * @return 发票集合
     */
    public List<IipInvoice> selectIipInvoiceList(IipInvoice iipInvoice);

    /**
     * 新增发票
     * 
     * @param iipInvoice 发票信息
     * @return 结果
     */
    public int insertIipInvoice(IipInvoice iipInvoice);

    /**
     * 修改发票
     * 
     * @param iipInvoice 发票信息
     * @return 结果
     */
    public int updateIipInvoice(IipInvoice iipInvoice);

    /**
     * 通过ID删除发票
     * 
     * @param invoiceId 发票ID
     * @return 结果
     */
    public int deleteIipInvoiceById(Long invoiceId);

    /**
     * 批量删除发票
     * 
     * @param invoiceIds 需要删除的发票ID
     * @return 结果
     */
    public int deleteIipInvoiceByIds(Long[] invoiceIds);

    /**
     * 按发票代码与发票号码查询（全局唯一防重复上传）
     *
     * @param invoiceCode 发票代码
     * @param invoiceNo 发票号码
     * @return 发票信息，不存在时返回null
     */
    public IipInvoice selectByCodeAndNo(@Param("invoiceCode") String invoiceCode, @Param("invoiceNo") String invoiceNo);

    /**
     * 按用户查询发票列表（小程序端，状态可选，按创建时间倒序）
     *
     * @param iipInvoice 查询条件（memberId 必填，status 可选）
     * @return 发票集合
     */
    public List<IipInvoice> selectMemberInvoiceList(IipInvoice iipInvoice);

    /**
     * 审核状态条件更新（仅 status='0' 待审核可更新，返回 0 行表示已被审核）
     *
     * @param iipInvoice 审核更新内容
     * @return 影响行数
     */
    public int updateAuditStatus(IipInvoice iipInvoice);
}
