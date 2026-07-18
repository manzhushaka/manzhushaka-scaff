package com.manzhushaka.iip.application.merchant.service.impl;

import java.util.Date;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.manzhushaka.common.exception.ServiceException;
import com.manzhushaka.common.utils.StringUtils;
import com.manzhushaka.iip.application.merchant.command.AuditMerchantCommand;
import com.manzhushaka.iip.application.merchant.command.MerchantApplyCommand;
import com.manzhushaka.iip.application.merchant.command.MerchantVerifyCommand;
import com.manzhushaka.iip.application.merchant.command.SaveMerchantCommand;
import com.manzhushaka.iip.application.merchant.query.MerchantQuery;
import com.manzhushaka.iip.application.merchant.result.MerchantResult;
import com.manzhushaka.iip.application.merchant.result.MerchantVerifyResult;
import com.manzhushaka.iip.application.merchant.result.VerifyRecordResult;
import com.manzhushaka.iip.application.merchant.service.MerchantAppService;
import com.manzhushaka.iip.domain.IipCouponRecord;
import com.manzhushaka.iip.domain.IipMerchant;
import com.manzhushaka.iip.service.IIipMerchantService;

/**
 * 商户应用服务实现。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
@Service
public class MerchantAppServiceImpl implements MerchantAppService
{
    @Autowired
    private IIipMerchantService merchantService;

    @Override
    public List<MerchantResult> listMerchants(MerchantQuery query)
    {
        return merchantService.selectIipMerchantList(toEntity(query)).stream()
                .map(this::toResult)
                .toList();
    }

    @Override
    public MerchantResult getMerchant(Long merchantId)
    {
        return toResult(merchantService.selectIipMerchantById(merchantId));
    }

    @Override
    @Transactional
    public int createMerchant(SaveMerchantCommand command, String operatorUsername)
    {
        IipMerchant merchant = toEntity(command);
        merchant.setCreateBy(operatorUsername);
        return merchantService.insertIipMerchant(merchant);
    }

    @Override
    @Transactional
    public int updateMerchant(SaveMerchantCommand command, String operatorUsername)
    {
        IipMerchant merchant = toEntity(command);
        merchant.setUpdateBy(operatorUsername);
        return merchantService.updateIipMerchant(merchant);
    }

    @Override
    @Transactional
    public void deleteMerchants(Long[] merchantIds)
    {
        merchantService.deleteIipMerchantByIds(merchantIds);
    }

    @Override
    @Transactional
    public void auditMerchant(AuditMerchantCommand command, String operatorUsername)
    {
        if (Boolean.FALSE.equals(command.approve()) && StringUtils.isEmpty(command.auditRemark()))
        {
            throw new ServiceException("驳回商户时必须填写审核备注");
        }
        int rows = merchantService.auditMerchant(command.merchantId(), Boolean.TRUE.equals(command.approve()),
                command.auditRemark(), operatorUsername);
        if (rows == 0)
        {
            throw new ServiceException("审核失败：商户不存在或不处于待审核状态，请刷新后重试");
        }
    }

    @Override
    @Transactional
    public Long applyMerchant(MerchantApplyCommand command, Long memberId)
    {
        IipMerchant merchant = new IipMerchant();
        merchant.setMerchantName(command.merchantName());
        merchant.setCategory(command.category());
        merchant.setContactName(command.contactName());
        merchant.setContactPhone(command.contactPhone());
        merchant.setAddress(command.address());
        merchant.setBusinessLicense(command.businessLicense());
        merchant.setMemberId(memberId);
        return merchantService.applyMerchant(merchant);
    }

    @Override
    public MerchantResult getMyMerchant(Long memberId)
    {
        return toResult(merchantService.selectMerchantByMemberId(memberId));
    }

    @Override
    public MerchantResult getVerifiableMerchant(Long memberId)
    {
        return toResult(merchantService.getVerifiableMerchant(memberId));
    }

    @Override
    @Transactional
    public MerchantVerifyResult verifyCoupon(MerchantVerifyCommand command, Long memberId, String operatorName)
    {
        IipCouponRecord record = merchantService.verifyCoupon(memberId, command.verifyCode(), operatorName);
        return new MerchantVerifyResult(record.getRecordId(), record.getCouponName(), record.getCouponType(),
                record.getPointsCost(), record.getVerifyCode(), new Date());
    }

    @Override
    public List<VerifyRecordResult> listVerifyRecords(Long merchantId)
    {
        return merchantService.selectVerifyRecordsByMerchantId(merchantId).stream()
                .map(this::toVerifyRecordResult)
                .toList();
    }

    private IipMerchant toEntity(MerchantQuery query)
    {
        IipMerchant merchant = new IipMerchant();
        if (query == null)
        {
            return merchant;
        }
        merchant.setMerchantNo(query.merchantNo());
        merchant.setMerchantName(query.merchantName());
        merchant.setCategory(query.category());
        merchant.setStatus(query.status());
        putDateRange(merchant, query.beginTime(), query.endTime());
        return merchant;
    }

    private IipMerchant toEntity(SaveMerchantCommand command)
    {
        IipMerchant merchant = new IipMerchant();
        merchant.setMerchantId(command.merchantId());
        merchant.setMerchantName(command.merchantName());
        merchant.setCategory(command.category());
        merchant.setCity(command.city());
        merchant.setContactName(command.contactName());
        merchant.setContactPhone(command.contactPhone());
        merchant.setAddress(command.address());
        merchant.setDescription(command.description());
        merchant.setLogo(command.logo());
        merchant.setBusinessHours(command.businessHours());
        merchant.setLongitude(command.longitude());
        merchant.setLatitude(command.latitude());
        merchant.setBusinessLicense(command.businessLicense());
        merchant.setMemberId(command.memberId());
        merchant.setStatus(command.status());
        merchant.setRemark(command.remark());
        return merchant;
    }

    private void putDateRange(IipMerchant merchant, String beginTime, String endTime)
    {
        if (beginTime != null)
        {
            merchant.getParams().put("beginTime", beginTime);
        }
        if (endTime != null)
        {
            merchant.getParams().put("endTime", endTime);
        }
    }

    private MerchantResult toResult(IipMerchant merchant)
    {
        if (merchant == null)
        {
            return null;
        }
        return new MerchantResult(merchant.getMerchantId(), merchant.getMerchantNo(), merchant.getMerchantName(),
                merchant.getCategory(), merchant.getCity(), merchant.getContactName(), merchant.getContactPhone(),
                merchant.getAddress(), merchant.getDescription(), merchant.getLogo(), merchant.getBusinessHours(),
                merchant.getLongitude(), merchant.getLatitude(), merchant.getBusinessLicense(),
                merchant.getMemberId(), merchant.getStatus(), merchant.getAuditBy(), merchant.getAuditTime(),
                merchant.getAuditRemark(), merchant.getCreateBy(), merchant.getCreateTime(), merchant.getUpdateBy(),
                merchant.getUpdateTime(), merchant.getRemark());
    }

    private VerifyRecordResult toVerifyRecordResult(IipCouponRecord record)
    {
        return new VerifyRecordResult(record.getRecordId(), record.getCouponName(), record.getCouponType(),
                record.getMemberId(), record.getPointsCost(), record.getVerifyCode(), record.getVerifyTime(),
                record.getVerifyBy());
    }
}
