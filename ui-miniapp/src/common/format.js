/**
 * 展示格式化工具。
 *
 * 后端日期时间经全局 Jackson 配置序列化为 "yyyy-MM-dd HH:mm:ss"（GMT+8）字符串，
 * 前端直接按字符串截取展示，不做时区换算。
 */

/**
 * 取日期部分（yyyy-MM-dd）。
 *
 * @param {string} value 后端日期时间字符串
 * @returns {string} yyyy-MM-dd 或空串
 */
export function fmtDate(value) {
  return value ? String(value).slice(0, 10) : ''
}

/**
 * 取到分钟（yyyy-MM-dd HH:mm）。
 *
 * @param {string} value 后端日期时间字符串
 * @returns {string} yyyy-MM-dd HH:mm 或空串
 */
export function fmtMinute(value) {
  return value ? String(value).slice(0, 16) : ''
}

/**
 * 券类型显示名。
 *
 * @param {string} type ticket/virtual/full_reduction/discount
 * @returns {string} 中文类型名
 */
export function couponTypeName(type) {
  const names = {
    ticket: '门票',
    virtual: '虚拟物品',
    full_reduction: '满减券',
    discount: '折扣券'
  }
  return names[type] || '优惠券'
}

/**
 * 券品类显示名。
 *
 * @param {string} category general/scenic_ticket/hotel/dining/flight_package/duty_free
 * @returns {string} 中文品类名
 */
export function couponCategoryName(category) {
  const names = {
    general: '通用',
    scenic_ticket: '门票',
    hotel: '酒店',
    dining: '餐饮',
    flight_package: '机票+',
    duty_free: '免税'
  }
  return names[category] || '通用'
}

/**
 * 赞助方文案（platform 平台券不展示赞助方）。
 *
 * @param {string} sponsorType platform/bank/merchant
 * @param {string} sponsorName 赞助方名称
 * @returns {string} 赞助方文案，平台或无名称时返回空串
 */
export function couponSponsorText(sponsorType, sponsorName) {
  if (!sponsorName) {
    return ''
  }
  if (sponsorType === 'bank') {
    return '银行赞助·' + sponsorName
  }
  if (sponsorType === 'merchant') {
    return '商户赞助·' + sponsorName
  }
  return ''
}

/**
 * 积分流水业务来源文案。
 *
 * @param {string} bizType invoice_audit/coupon_exchange/admin_adjust/point_expire
 * @returns {string} 中文来源文案
 */
export function pointsBizName(bizType) {
  const names = {
    invoice_audit: '发票审核',
    coupon_exchange: '兑换优惠券',
    admin_adjust: '管理员调整',
    point_expire: '积分过期'
  }
  return names[bizType] || '积分变动'
}
