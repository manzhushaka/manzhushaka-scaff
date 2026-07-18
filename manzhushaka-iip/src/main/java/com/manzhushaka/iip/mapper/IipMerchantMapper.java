package com.manzhushaka.iip.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Param;
import com.manzhushaka.iip.domain.IipMerchant;

/**
 * 商户 数据层
 * 
 * @author manzhushaka
 * @date 2026-07-18
 */
public interface IipMerchantMapper
{
    /**
     * 通过ID查询商户
     * 
     * @param merchantId 商户ID
     * @return 商户信息
     */
    public IipMerchant selectIipMerchantById(Long merchantId);

    /**
     * 查询商户列表
     * 
     * @param iipMerchant 查询条件
     * @return 商户集合
     */
    public List<IipMerchant> selectIipMerchantList(IipMerchant iipMerchant);

    /**
     * 新增商户
     * 
     * @param iipMerchant 商户信息
     * @return 结果
     */
    public int insertIipMerchant(IipMerchant iipMerchant);

    /**
     * 修改商户
     * 
     * @param iipMerchant 商户信息
     * @return 结果
     */
    public int updateIipMerchant(IipMerchant iipMerchant);

    /**
     * 通过ID删除商户
     * 
     * @param merchantId 商户ID
     * @return 结果
     */
    public int deleteIipMerchantById(Long merchantId);

    /**
     * 批量删除商户
     * 
     * @param merchantIds 需要删除的商户ID
     * @return 结果
     */
    public int deleteIipMerchantByIds(Long[] merchantIds);

    /**
     * 按绑定用户ID查询商户（小程序商户能力校验）
     * 
     * @param memberId 用户ID
     * @return 商户信息，不存在时返回null
     */
    public IipMerchant selectByMemberId(Long memberId);

    /**
     * 按商户编号查询商户（merchant_no 唯一性校验）
     * 
     * @param merchantNo 商户编号
     * @return 商户信息，不存在时返回null
     */
    public IipMerchant selectByMerchantNo(String merchantNo);

    /**
     * 查询指定编号前缀下的最大商户编号（merchant_no 序号生成）
     * 
     * @param prefix 编号前缀，格式为 M+yyyyMM
     * @return 最大商户编号，无记录时返回null
     */
    public String selectMaxMerchantNo(@Param("prefix") String prefix);

    /**
     * 审核商户（仅待审核 status='2' 可更新，条件守护保证幂等）
     * 
     * @param merchantId 商户ID
     * @param status 目标状态（0正常 1停用）
     * @param auditBy 审核人
     * @param auditRemark 审核备注，不允许为null（无备注传空串）
     * @return 影响行数，0 表示商户不存在或非待审核状态
     */
    public int updateAuditStatus(@Param("merchantId") Long merchantId, @Param("status") String status,
            @Param("auditBy") String auditBy, @Param("auditRemark") String auditRemark);
}
