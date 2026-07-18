package com.manzhushaka.iip.application.overview;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.manzhushaka.iip.mapper.IipOverviewMapper;

/**
 * 数据概览应用服务实现。
 *
 * @author manzhushaka
 * @date 2026-07-18
 */
@Service
public class OverviewAppServiceImpl implements OverviewAppService
{
    /** 发票状态：待审核 */
    private static final String INVOICE_STATUS_PENDING = "0";

    /** 发票状态：已通过 */
    private static final String INVOICE_STATUS_APPROVED = "1";

    /** 发票状态：已驳回 */
    private static final String INVOICE_STATUS_REJECTED = "2";

    @Autowired
    private IipOverviewMapper iipOverviewMapper;

    @Override
    public SummaryResult getSummary()
    {
        long pendingInvoiceCount = 0L;
        long approvedInvoiceCount = 0L;
        long rejectedInvoiceCount = 0L;
        for (Map<String, Object> row : iipOverviewMapper.countInvoicesByStatus())
        {
            String status = String.valueOf(row.get("status"));
            long cnt = toLong(row.get("cnt"));
            switch (status)
            {
                case INVOICE_STATUS_PENDING:
                    pendingInvoiceCount = cnt;
                    break;
                case INVOICE_STATUS_APPROVED:
                    approvedInvoiceCount = cnt;
                    break;
                case INVOICE_STATUS_REJECTED:
                    rejectedInvoiceCount = cnt;
                    break;
                default:
                    // 未知状态不纳入三个统计口径，向前兼容后续新增状态
                    break;
            }
        }
        return new SummaryResult(zeroIfNull(iipOverviewMapper.countMembers()),
                zeroIfNull(iipOverviewMapper.countMerchants()),
                zeroIfNull(iipOverviewMapper.countPendingMerchants()),
                pendingInvoiceCount, approvedInvoiceCount, rejectedInvoiceCount,
                zeroIfNull(iipOverviewMapper.sumPointsIssued()),
                zeroIfNull(iipOverviewMapper.sumPointsConsumed()),
                zeroIfNull(iipOverviewMapper.countCouponExchanges()),
                zeroIfNull(iipOverviewMapper.countVerifiedCoupons()),
                zeroIfNull(iipOverviewMapper.countActiveActivities()));
    }

    @Override
    public TrendResult getTrend()
    {
        return new TrendResult(toDayCounts(iipOverviewMapper.invoiceTrend()),
                toDayCounts(iipOverviewMapper.pointsTrend()),
                toDayCounts(iipOverviewMapper.exchangeTrend()));
    }

    /**
     * 将 Map 行转换为单日统计结果。
     *
     * @param rows 趋势查询行（含 day 与 cnt 键）
     * @return 单日统计列表
     */
    private List<DayCount> toDayCounts(List<Map<String, Object>> rows)
    {
        List<DayCount> dayCounts = new ArrayList<>(rows.size());
        for (Map<String, Object> row : rows)
        {
            dayCounts.add(new DayCount(String.valueOf(row.get("day")), toLong(row.get("cnt"))));
        }
        return dayCounts;
    }

    /**
     * 将统计值安全转为 long，count 返回 Long、sum 返回 BigDecimal 时均可兼容。
     *
     * @param value 统计值
     * @return long 数值，非数字时返回 0
     */
    private long toLong(Object value)
    {
        if (value instanceof Number number)
        {
            return number.longValue();
        }
        return 0L;
    }

    /**
     * Long 防空转换，null 返回 0。
     *
     * @param value 统计值
     * @return 非 null 数值
     */
    private Long zeroIfNull(Long value)
    {
        return value == null ? 0L : value;
    }
}
