import request from '@/utils/request'

// 获取数据概览汇总指标
export function getSummary() {
  return request({
    url: '/iip/overview/summary',
    method: 'get'
  })
}

// 获取近7日发票、积分、兑换趋势
export function getTrend() {
  return request({
    url: '/iip/overview/trend',
    method: 'get'
  })
}
