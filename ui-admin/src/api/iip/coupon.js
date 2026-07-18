import request from '@/utils/request'

// 查询券列表（按名称/类型/状态筛选）
export function listCoupon(query) {
  return request({
    url: '/iip/coupon/list',
    method: 'get',
    params: query
  })
}

// 查询券详细
export function getCoupon(couponId) {
  return request({
    url: '/iip/coupon/' + couponId,
    method: 'get'
  })
}

// 新增券
export function addCoupon(data) {
  return request({
    url: '/iip/coupon',
    method: 'post',
    data: data
  })
}

// 修改券（已有兑换记录的券，后端禁止修改兑换规则：所需积分与有效期相关字段）
export function updateCoupon(data) {
  return request({
    url: '/iip/coupon',
    method: 'put',
    data: data
  })
}

// 删除券（已有兑换记录的券后端禁止删除，会返回错误提示）
export function delCoupon(couponIds) {
  return request({
    url: '/iip/coupon/' + couponIds,
    method: 'delete'
  })
}

// 导出券列表。
// 注意：后端 /iip/coupon/export 为 GET 导出，全局 download() 固定走 POST 无法复用，
// 故此处用 request + responseType:'blob' 返回原始数据，由页面自行校验并保存文件。
export function exportCoupon(query) {
  return request({
    url: '/iip/coupon/export',
    method: 'get',
    params: query,
    responseType: 'blob',
    timeout: 60000
  })
}

// 商户下拉数据源（可选绑定商户，不选=平台通用券）。
// 注意：@/api/iip/merchant.js 归 F1 代理并行开发，为避免并行期间跨任务 import
// 造成文件依赖与合并冲突，本文件按协作纪律自写轻量选项查询（仅取正常状态商户），禁止 import merchant.js。
export function listMerchantOptions() {
  return request({
    url: '/iip/merchant/list',
    method: 'get',
    params: { pageNum: 1, pageSize: 999, status: '0' }
  })
}
