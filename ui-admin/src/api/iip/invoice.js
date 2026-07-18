import request from '@/utils/request'

// 查询发票列表
export function listInvoice(query) {
  return request({
    url: '/iip/invoice/list',
    method: 'get',
    params: query
  })
}

// 查询发票详细
export function getInvoice(invoiceId) {
  return request({
    url: '/iip/invoice/getInfo/' + invoiceId,
    method: 'get'
  })
}

// 审核发票（pass 为 true 通过并按当前活动比例发放积分，false 驳回，驳回时 auditRemark 必填）
export function auditInvoice(data) {
  return request({
    url: '/iip/invoice/audit',
    method: 'put',
    data: data
  })
}

// 导出发票列表（后端为 GET 接口，返回 Excel 二进制流，页面端按 blob 保存）
export function exportInvoice(query) {
  return request({
    url: '/iip/invoice/export',
    method: 'get',
    params: query,
    responseType: 'blob'
  })
}
