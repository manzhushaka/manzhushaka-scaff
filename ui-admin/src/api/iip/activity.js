import request from '@/utils/request'

// 查询活动列表
export function listActivity(query) {
  return request({
    url: '/iip/activity/list',
    method: 'get',
    params: query
  })
}

// 查询活动详细（RuoYi 风格：GET /iip/activity/{activityId}）
export function getActivity(activityId) {
  return request({
    url: '/iip/activity/' + activityId,
    method: 'get'
  })
}

// 新增活动
export function addActivity(data) {
  return request({
    url: '/iip/activity',
    method: 'post',
    data: data
  })
}

// 修改活动
export function updateActivity(data) {
  return request({
    url: '/iip/activity',
    method: 'put',
    data: data
  })
}

// 删除活动（进行中的活动后端禁止删除，会返回错误提示）
export function delActivity(activityIds) {
  return request({
    url: '/iip/activity/' + activityIds,
    method: 'delete'
  })
}

// 查询活动已配置商户列表
export function listActivityMerchants(activityId) {
  return request({
    url: '/iip/activity/merchants/' + activityId,
    method: 'get'
  })
}

// 新增活动商户配置
export function addActivityMerchant(data) {
  return request({
    url: '/iip/activity/merchants',
    method: 'post',
    data: data
  })
}

// 移除活动商户配置
export function removeActivityMerchant(id) {
  return request({
    url: '/iip/activity/merchants/' + id,
    method: 'delete'
  })
}

// 查询活动已配置券列表
export function listActivityCoupons(activityId) {
  return request({
    url: '/iip/activity/coupons/' + activityId,
    method: 'get'
  })
}

// 新增活动券配置
export function addActivityCoupon(data) {
  return request({
    url: '/iip/activity/coupons',
    method: 'post',
    data: data
  })
}

// 修改活动券发行上限
export function updateActivityCoupon(data) {
  return request({
    url: '/iip/activity/coupons',
    method: 'put',
    data: data
  })
}

// 移除活动券配置
export function removeActivityCoupon(id) {
  return request({
    url: '/iip/activity/coupons/' + id,
    method: 'delete'
  })
}

// 商户下拉数据源。
// 注意：@/api/iip/merchant.js 归 F1 代理并行开发，为避免并行期间跨任务 import
// 造成文件依赖与合并冲突，本文件按协作纪律自写轻量选项查询，禁止 import merchant.js。
export function listMerchantOptions() {
  return request({
    url: '/iip/merchant/list',
    method: 'get',
    params: { pageNum: 1, pageSize: 999 }
  })
}

// 券下拉数据源。
// 注意：@/api/iip/coupon.js 归 F2 代理并行开发，同理自写，禁止 import coupon.js。
export function listCouponOptions() {
  return request({
    url: '/iip/coupon/list',
    method: 'get',
    params: { pageNum: 1, pageSize: 999 }
  })
}
