import request from '@/utils/request'

// 查询商户列表
export function listMerchant(query) {
  return request({
    url: '/iip/merchant/list',
    method: 'get',
    params: query
  })
}

// 查询商户详细
export function getMerchant(merchantId) {
  return request({
    url: '/iip/merchant/getInfo/' + merchantId,
    method: 'get'
  })
}

// 新增商户
export function addMerchant(data) {
  return request({
    url: '/iip/merchant',
    method: 'post',
    data: data
  })
}

// 修改商户
export function updateMerchant(data) {
  return request({
    url: '/iip/merchant',
    method: 'put',
    data: data
  })
}

// 删除商户
export function delMerchant(merchantIds) {
  return request({
    url: '/iip/merchant/' + merchantIds,
    method: 'delete'
  })
}

// 审核商户（approve 为 true 通过置正常，false 驳回置停用，驳回时 auditRemark 必填）
export function auditMerchant(data) {
  return request({
    url: '/iip/merchant/audit',
    method: 'put',
    data: data
  })
}

// 导出商户列表（后端为 GET 接口，返回 Excel 二进制流，页面端按 blob 保存）
export function exportMerchant(query) {
  return request({
    url: '/iip/merchant/export',
    method: 'get',
    params: query,
    responseType: 'blob'
  })
}
