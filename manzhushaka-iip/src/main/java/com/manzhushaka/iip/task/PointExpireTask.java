package com.manzhushaka.iip.task;

import java.util.Date;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import com.manzhushaka.iip.domain.IipPointsAccount;
import com.manzhushaka.iip.domain.IipPointsRecord;
import com.manzhushaka.iip.mapper.IipPointsAccountMapper;
import com.manzhushaka.iip.mapper.IipPointsRecordMapper;

/**
 * 积分过期结转定时任务
 * 
 * @author manzhushaka
 * @date 2026-07-18
 */
@Component("pointExpireTask")
public class PointExpireTask
{
    private static final Logger log = LoggerFactory.getLogger(PointExpireTask.class);

    /** 变动类型：过期 */
    private static final String CHANGE_TYPE_EXPIRE = "expire";

    /** 业务来源：积分过期 */
    private static final String BIZ_TYPE_POINT_EXPIRE = "point_expire";

    @Autowired
    private IipPointsRecordMapper pointsRecordMapper;

    @Autowired
    private IipPointsAccountMapper pointsAccountMapper;

    /**
     * 扫描已到期的 earn 批次，将批次剩余积分结转为过期
     */
    @Transactional
    public void expire()
    {
        List<IipPointsRecord> expiredList = pointsRecordMapper.selectExpiredEarnList(new Date());
        if (expiredList.isEmpty())
        {
            log.info("积分过期结转完成：无到期批次");
            return;
        }
        int batchCount = 0;
        int totalPoints = 0;
        for (IipPointsRecord batch : expiredList)
        {
            Integer remaining = batch.getRemaining();
            if (remaining == null || remaining <= 0)
            {
                continue;
            }
            pointsRecordMapper.clearRemaining(batch.getRecordId());
            int rows = pointsAccountMapper.expirePoints(batch.getMemberId(), remaining);
            if (rows == 0)
            {
                log.warn("积分过期结转跳过：账户可用余额不足，memberId={}，recordId={}，remaining={}",
                        batch.getMemberId(), batch.getRecordId(), remaining);
                continue;
            }
            IipPointsAccount account = pointsAccountMapper.selectByMemberId(batch.getMemberId());
            IipPointsRecord record = new IipPointsRecord();
            record.setMemberId(batch.getMemberId());
            record.setChangeType(CHANGE_TYPE_EXPIRE);
            record.setPoints(remaining);
            record.setBalanceAfter(account == null ? 0 : account.getAvailablePoints());
            record.setBizType(BIZ_TYPE_POINT_EXPIRE);
            record.setBizId(String.valueOf(batch.getRecordId()));
            record.setRemark("积分批次过期结转");
            pointsRecordMapper.insertIipPointsRecord(record);
            batchCount++;
            totalPoints += remaining;
        }
        log.info("积分过期结转完成：处理批次 {} 个，结转积分 {} 点", batchCount, totalPoints);
    }
}
