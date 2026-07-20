import request from '@/utils/request'

// 查询兑换记录列表（按券名/用户/状态/核销码/兑换时间筛选）
export function listExchange(query) {
  return request({
    url: '/iip/exchange/list',
    method: 'get',
    params: query
  })
}

// 查询兑换记录详细
export function getExchange(recordId) {
  return request({
    url: '/iip/exchange/' + recordId,
    method: 'get'
  })
}

// 作废未使用券并退回兑换积分
export function voidExchange(recordId, voidReason) {
  return request({
    url: '/iip/exchange/' + recordId + '/void',
    method: 'put',
    data: { voidReason }
  })
}

// 导出兑换记录列表。
// 注意：后端 /iip/exchange/export 为 GET 导出，全局 download() 固定走 POST 无法复用，
// 故此处用 request + responseType:'blob' 返回原始数据，由页面自行校验并保存文件。
export function exportExchange(query) {
  return request({
    url: '/iip/exchange/export',
    method: 'get',
    params: query,
    responseType: 'blob',
    timeout: 60000
  })
}
