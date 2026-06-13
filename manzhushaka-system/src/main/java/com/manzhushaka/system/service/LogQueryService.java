package com.manzhushaka.system.service;

import com.manzhushaka.system.dto.log.LoginLogQuery;
import com.manzhushaka.system.dto.log.MqMessageQuery;
import com.manzhushaka.system.dto.log.OpLogQuery;
import com.manzhushaka.system.vo.PageResult;
import com.manzhushaka.system.vo.log.LoginLogVO;
import com.manzhushaka.system.vo.log.MqMessageVO;
import com.manzhushaka.system.vo.log.OpLogVO;

/**
 * 定义 LogQueryService 服务能力。
 */
public interface LogQueryService {
    /**
     * 查询 page Login Logs 结果。
     *
     * @param query 查询条件
     * @return 查询结果
     */
    PageResult<LoginLogVO> pageLoginLogs(LoginLogQuery query);

    /**
     * 查询 page Op Logs 结果。
     *
     * @param query 查询条件
     * @return 查询结果
     */
    PageResult<OpLogVO> pageOpLogs(OpLogQuery query);

    /**
     * 查询 page Mq Messages 结果。
     *
     * @param query 查询条件
     * @return 查询结果
     */
    PageResult<MqMessageVO> pageMqMessages(MqMessageQuery query);
}
