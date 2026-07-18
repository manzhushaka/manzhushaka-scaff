/**
 * 商户接口（对齐 MiniappMerchantController）。
 */
import { request } from '@/common/request.js'

/**
 * 提交商户入驻申请（申请状态为待审核）。
 *
 * @param {object} data { merchantName, category, contactName, contactPhone, address, businessLicense }
 * @returns {Promise<object>} MerchantResult
 */
export async function applyMerchant(data) {
  const res = await request({ url: '/miniapp/merchant/apply', method: 'POST', data })
  return res.data
}

/**
 * 查询当前用户绑定的商户信息（未申请过时返回 null）。
 *
 * @returns {Promise<object|null>} MerchantResult 或 null
 */
export async function getMerchantInfo() {
  const res = await request({ url: '/miniapp/merchant/info' })
  return res.data || null
}

/**
 * 核销用户券（仅状态正常的商户可操作）。
 *
 * @param {string} verifyCode 核销码
 * @returns {Promise<object>} MerchantVerifyResult { recordId, couponName, couponType, pointsCost, verifyCode, verifyTime }
 */
export async function verifyCoupon(verifyCode) {
  const res = await request({ url: '/miniapp/merchant/verify', method: 'POST', data: { verifyCode } })
  return res.data
}

/**
 * 本商户核销记录分页查询（TableDataInfo { rows, total }）。
 *
 * @param {object} params { pageNum, pageSize }
 * @returns {Promise<{rows: Array, total: number}>} rows 为 VerifyRecordResult[]
 */
export async function getVerifyRecords(params) {
  const res = await request({ url: '/miniapp/merchant/verify/records', data: params })
  return { rows: res.rows || [], total: res.total || 0 }
}
