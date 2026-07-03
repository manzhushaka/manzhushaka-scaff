package com.manzhushaka.biz.pii.application.service.impl;

import com.manzhushaka.biz.pii.application.command.ChangeQrcodeStatusCommand;
import com.manzhushaka.biz.pii.application.command.CreateQrcodeCommand;
import com.manzhushaka.biz.pii.application.command.QrcodeTaxItemCommand;
import com.manzhushaka.biz.pii.application.command.UpdateQrcodeCommand;
import com.manzhushaka.biz.pii.application.query.QrcodePageQuery;
import com.manzhushaka.biz.pii.application.result.QrcodeResult;
import com.manzhushaka.biz.pii.application.result.QrcodeTaxItemResult;
import com.manzhushaka.biz.pii.application.service.QrcodeService;
import com.manzhushaka.biz.pii.domain.model.PayQrcode;
import com.manzhushaka.biz.pii.domain.model.PayQrcodeTaxItem;
import com.manzhushaka.biz.pii.domain.repository.PayQrcodeRepository;
import com.manzhushaka.biz.pii.domain.repository.PayQrcodeTaxItemRepository;
import com.manzhushaka.common.exception.ServiceException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class QrcodeServiceImpl implements QrcodeService {

    private final PayQrcodeRepository qrcodeRepository;
    private final PayQrcodeTaxItemRepository qrcodeTaxItemRepository;

    public QrcodeServiceImpl(PayQrcodeRepository qrcodeRepository,
                             PayQrcodeTaxItemRepository qrcodeTaxItemRepository) {
        this.qrcodeRepository = qrcodeRepository;
        this.qrcodeTaxItemRepository = qrcodeTaxItemRepository;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public Long create(CreateQrcodeCommand command) {
        ensureCodeUnique(command.qrcodeCode(), null);
        PayQrcode qrcode = new PayQrcode();
        fill(qrcode, command.merchantId(), command.qrcodeCode(), command.name(),
                command.status(), command.expireTime(), command.remark());
        qrcode.setCreateTime(LocalDateTime.now());
        Long id = qrcodeRepository.insert(qrcode);
        bindTaxItems(id, command.taxItems());
        return id;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int update(UpdateQrcodeCommand command) {
        PayQrcode qrcode = qrcodeRepository.findById(command.id())
                .orElseThrow(() -> new ServiceException("二维码不存在"));
        ensureCodeUnique(command.qrcodeCode(), command.id());
        fill(qrcode, command.merchantId(), command.qrcodeCode(), command.name(),
                command.status(), command.expireTime(), command.remark());
        qrcode.setUpdateTime(LocalDateTime.now());
        int updated = qrcodeRepository.updateById(qrcode);
        qrcodeTaxItemRepository.deleteByQrcodeId(command.id());
        bindTaxItems(command.id(), command.taxItems());
        return updated;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int delete(Long id) {
        qrcodeRepository.findById(id).orElseThrow(() -> new ServiceException("二维码不存在"));
        qrcodeTaxItemRepository.deleteByQrcodeId(id);
        return qrcodeRepository.deleteById(id);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public int changeStatus(ChangeQrcodeStatusCommand command) {
        qrcodeRepository.findById(command.id()).orElseThrow(() -> new ServiceException("二维码不存在"));
        return qrcodeRepository.updateStatus(command.id(), command.status());
    }

    @Override
    public QrcodeResult get(Long id) {
        return toResult(qrcodeRepository.findById(id).orElseThrow(() -> new ServiceException("二维码不存在")));
    }

    @Override
    public List<QrcodeResult> page(QrcodePageQuery query) {
        if (query.merchantId() == null) {
            return Collections.emptyList();
        }
        return qrcodeRepository.findByMerchantId(query.merchantId()).stream()
                .map(this::toResult).collect(Collectors.toList());
    }

    private void fill(PayQrcode qrcode, Long merchantId, String qrcodeCode, String name,
                      Integer status, LocalDateTime expireTime, String remark) {
        qrcode.setMerchantId(merchantId);
        qrcode.setQrcodeCode(qrcodeCode);
        qrcode.setQrcodeUrl("/pay?code=" + qrcodeCode);
        qrcode.setQrcodeImageUrl(toDataUrl(qrcode.getQrcodeUrl()));
        qrcode.setName(name);
        qrcode.setStatus(status == null ? 1 : status);
        qrcode.setExpireTime(expireTime);
        qrcode.setRemark(remark);
    }

    private void ensureCodeUnique(String qrcodeCode, Long currentId) {
        qrcodeRepository.findByCode(qrcodeCode).ifPresent(existing -> {
            if (currentId == null || !currentId.equals(existing.getId())) {
                throw new ServiceException("二维码编码已存在");
            }
        });
    }

    private void bindTaxItems(Long qrcodeId, List<QrcodeTaxItemCommand> taxItems) {
        if (taxItems == null || taxItems.isEmpty()) {
            return;
        }
        for (QrcodeTaxItemCommand taxItem : taxItems) {
            PayQrcodeTaxItem relation = new PayQrcodeTaxItem();
            relation.setQrcodeId(qrcodeId);
            relation.setTaxItemId(taxItem.taxItemId());
            relation.setDefaultAmount(taxItem.defaultAmount());
            qrcodeTaxItemRepository.insert(relation);
        }
    }

    private QrcodeResult toResult(PayQrcode qrcode) {
        List<QrcodeTaxItemResult> taxItems = qrcodeTaxItemRepository.findByQrcodeId(qrcode.getId()).stream()
                .map(QrcodeTaxItemResult::from).collect(Collectors.toList());
        return QrcodeResult.from(qrcode, taxItems);
    }

    private String toDataUrl(String content) {
        return "data:text/plain;base64," + Base64.getEncoder().encodeToString(content.getBytes(StandardCharsets.UTF_8));
    }
}
