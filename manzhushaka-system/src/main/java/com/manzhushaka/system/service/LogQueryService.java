package com.manzhushaka.system.service;

import com.manzhushaka.system.dto.log.LoginLogQuery;
import com.manzhushaka.system.dto.log.MqMessageQuery;
import com.manzhushaka.system.dto.log.OpLogQuery;
import com.manzhushaka.system.vo.PageResult;
import com.manzhushaka.system.vo.log.LoginLogVO;
import com.manzhushaka.system.vo.log.MqMessageVO;
import com.manzhushaka.system.vo.log.OpLogVO;

public interface LogQueryService {
    PageResult<LoginLogVO> pageLoginLogs(LoginLogQuery query);

    PageResult<OpLogVO> pageOpLogs(OpLogQuery query);

    PageResult<MqMessageVO> pageMqMessages(MqMessageQuery query);
}
