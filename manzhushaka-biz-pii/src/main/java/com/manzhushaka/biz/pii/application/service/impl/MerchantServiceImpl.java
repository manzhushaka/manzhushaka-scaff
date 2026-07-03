package com.manzhushaka.biz.pii.application.service.impl;

import com.manzhushaka.biz.pii.application.command.ChangeMerchantStatusCommand;
import com.manzhushaka.biz.pii.application.command.CreateMerchantCommand;
import com.manzhushaka.biz.pii.application.command.UpdateMerchantCommand;
import com.manzhushaka.biz.pii.application.query.MerchantPageQuery;
import com.manzhushaka.biz.pii.application.result.MerchantResult;
import com.manzhushaka.biz.pii.application.service.MerchantService;
import com.manzhushaka.biz.pii.domain.model.MerchantProfile;
import com.manzhushaka.biz.pii.domain.repository.MerchantProfileRepository;
import com.manzhushaka.common.exception.ServiceException;
import com.manzhushaka.common.utils.StringUtils;
import com.manzhushaka.common.utils.security.PasswordUtils;
import com.manzhushaka.system.infrastructure.persistence.entity.SysDept;
import com.manzhushaka.system.infrastructure.persistence.entity.SysUser;
import com.manzhushaka.system.service.ISysDeptService;
import com.manzhushaka.system.service.ISysUserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class MerchantServiceImpl implements MerchantService {

    private final MerchantProfileRepository merchantProfileRepository;
    private final ISysDeptService deptService;
    private final ISysUserService userService;

    public MerchantServiceImpl(MerchantProfileRepository merchantProfileRepository,
                               ISysDeptService deptService,
                               ISysUserService userService) {
        this.merchantProfileRepository = merchantProfileRepository;
        this.deptService = deptService;
        this.userService = userService;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(CreateMerchantCommand command) {
        ensureUmsUnique(command.umsMerchantId(), command.umsTerminalId(), null);
        SysUser admin = new SysUser();
        admin.setUserName(command.adminUserName());
        if (!userService.checkUserNameUnique(admin)) {
            throw new ServiceException("商户管理员账号已存在");
        }

        SysDept dept = new SysDept();
        dept.setParentId(command.parentDeptId());
        dept.setDeptName(command.merchantName());
        dept.setOrderNum(0);
        dept.setStatus("0");
        dept.setDeptType("merchant");
        deptService.insertDept(dept);

        admin.setDeptId(dept.getDeptId());
        admin.setNickName(command.merchantName() + "管理员");
        admin.setPhonenumber(command.adminPhone());
        admin.setEmail(command.adminEmail());
        admin.setPassword(PasswordUtils.encrypt(command.adminPassword()));
        admin.setStatus("0");
        userService.insertUser(admin);

        MerchantProfile profile = new MerchantProfile();
        profile.setDeptId(dept.getDeptId());
        profile.setMerchantName(command.merchantName());
        profile.setCreateTime(LocalDateTime.now());
        fillProfile(profile, command.umsMerchantId(), command.umsTerminalId(), command.umsPaySignKey(),
                command.umsInvoiceSignKey(), command.invoiceMsgSrc(), command.status(), command.remark());
        return merchantProfileRepository.insert(profile);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int update(UpdateMerchantCommand command) {
        MerchantProfile profile = merchantProfileRepository.findById(command.id())
                .orElseThrow(() -> new ServiceException("商户不存在"));
        ensureUmsUnique(command.umsMerchantId(), command.umsTerminalId(), command.id());
        profile.setMerchantName(command.merchantName());
        profile.setUpdateTime(LocalDateTime.now());
        fillProfile(profile, command.umsMerchantId(), command.umsTerminalId(), command.umsPaySignKey(),
                command.umsInvoiceSignKey(), command.invoiceMsgSrc(), command.status(), command.remark());
        return merchantProfileRepository.updateById(profile);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int delete(Long id) {
        merchantProfileRepository.findById(id).orElseThrow(() -> new ServiceException("商户不存在"));
        return merchantProfileRepository.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int changeStatus(ChangeMerchantStatusCommand command) {
        merchantProfileRepository.findById(command.id()).orElseThrow(() -> new ServiceException("商户不存在"));
        return merchantProfileRepository.updateStatus(command.id(), command.status());
    }

    @Override
    public MerchantResult get(Long id) {
        return merchantProfileRepository.findById(id).map(this::toResult)
                .orElseThrow(() -> new ServiceException("商户不存在"));
    }

    @Override
    public List<MerchantResult> page(MerchantPageQuery query) {
        return merchantProfileRepository.findList(query.merchantName(), query.umsMerchantId(), query.status())
                .stream().map(this::toResult).collect(Collectors.toList());
    }

    private MerchantResult toResult(MerchantProfile profile) {
        MerchantResult result = MerchantResult.from(profile);
        if (profile.getDeptId() == null) {
            return result;
        }
        SysDept merchantDept = deptService.selectDeptById(profile.getDeptId());
        if (merchantDept == null) {
            return result;
        }
        Long parentDeptId = merchantDept.getParentId();
        String regionName = merchantDept.getParentName();
        if (StringUtils.isBlank(regionName) && parentDeptId != null && parentDeptId > 0) {
            SysDept regionDept = deptService.selectDeptById(parentDeptId);
            if (regionDept != null) {
                regionName = regionDept.getDeptName();
            }
        }
        return result.withRegion(parentDeptId, regionName);
    }

    private void ensureUmsUnique(String umsMerchantId, String umsTerminalId, Long currentId) {
        merchantProfileRepository.findByUmsMerchantAndTerminal(umsMerchantId, umsTerminalId).ifPresent(existing -> {
            if (currentId == null || !currentId.equals(existing.getId())) {
                throw new ServiceException("银商商户参数已存在");
            }
        });
    }

    private void fillProfile(MerchantProfile profile, String umsMerchantId, String umsTerminalId,
                             String umsPaySignKey, String umsInvoiceSignKey, String invoiceMsgSrc,
                             Integer status, String remark) {
        profile.setUmsMerchantId(umsMerchantId);
        profile.setUmsTerminalId(umsTerminalId);
        if (StringUtils.isNotBlank(umsPaySignKey)) {
            profile.setUmsPaySignKeyEnc(umsPaySignKey);
        }
        if (StringUtils.isNotBlank(umsInvoiceSignKey)) {
            profile.setUmsInvoiceSignKeyEnc(umsInvoiceSignKey);
        }
        profile.setInvoiceMsgSrc(invoiceMsgSrc);
        profile.setStatus(status == null ? 1 : status);
        profile.setRemark(remark);
    }
}
