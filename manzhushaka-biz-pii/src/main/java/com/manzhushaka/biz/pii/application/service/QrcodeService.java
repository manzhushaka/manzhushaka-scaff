package com.manzhushaka.biz.pii.application.service;

import com.manzhushaka.biz.pii.application.command.ChangeQrcodeStatusCommand;
import com.manzhushaka.biz.pii.application.command.CreateQrcodeCommand;
import com.manzhushaka.biz.pii.application.command.UpdateQrcodeCommand;
import com.manzhushaka.biz.pii.application.query.QrcodePageQuery;
import com.manzhushaka.biz.pii.application.result.QrcodeResult;

import java.util.List;

public interface QrcodeService {
    Long create(CreateQrcodeCommand command);
    int update(UpdateQrcodeCommand command);
    int delete(Long id);
    int changeStatus(ChangeQrcodeStatusCommand command);
    QrcodeResult get(Long id);
    List<QrcodeResult> page(QrcodePageQuery query);
}
