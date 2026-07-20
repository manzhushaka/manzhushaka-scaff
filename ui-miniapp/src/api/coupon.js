/**
 * 券接口（对齐 MiniappCouponController）。
 */
import { request } from '@/common/request.js'

/**
 * 积分商城券列表（上架且在兑换窗口内）。
 *
 * @param {string} [category] 券品类（general/scenic_ticket/hotel/dining/flight_package/duty_free），不传为全部
 * @returns {Promise<Array>} CouponMallItemResult[]
 */
export async function getMallCoupons(category) {
  const data = category ? { category } : {}
  const res = await request({ url: '/miniapp/coupon/mall', data })
  return res.data || []
}

/**
 * 券详情（含当前用户已兑数量 exchangedCount）。
 *
 * 券绑定商户时额外返回商家信息：merchantName 商户名称、merchantLogo 商户logo图片、
 * merchantDescription 商家介绍、merchantAddress 商户地址、merchantPhone 联系电话、
 * merchantBusinessHours 营业时间、merchantLongitude 商户经度、merchantLatitude 商户纬度；
 * 券未绑定商户或商户不存在时，以上 8 个字段均为 null；
 * 经纬度为数字（可能为 null），两者都有值时前端可唤起地图导航。
 *
 * @param {number|string} couponId 券ID
 * @returns {Promise<object>} CouponDetailResult
 */
export async function getCouponDetail(couponId) {
  const res = await request({ url: '/miniapp/coupon/' + couponId })
  return res.data
}

/**
 * 兑换券（返回含核销码的券实例 ExchangeResult）。
 *
 * @param {number|string} couponId 券ID
 * @returns {Promise<object>} ExchangeResult
 */
export async function exchangeCoupon(couponId) {
  const res = await request({ url: '/miniapp/coupon/exchange', method: 'POST', data: { couponId } })
  return res.data
}

/**
 * 我的券列表。
 *
 * @param {string} [status] 0未使用 1已使用 2已过期 3已作废，不传为全部
 * @returns {Promise<Array>} MyCouponResult[]
 */
export async function getMyCoupons(status) {
  const res = await request({ url: '/miniapp/coupon/mine', data: { status } })
  return res.data || []
}
