/**
 * 认证接口（对齐 MiniappAuthController）。
 */
import { request } from '@/common/request.js'

/**
 * 小程序登录（匿名接口）。
 * 响应体顶层：{ code, msg, token, member }（token/member 不在 data 内）。
 *
 * @param {object} data { platform: 'wechat'|'alipay'|'unionpay', code, nickname?, avatar? }
 * @returns {Promise<object>} 整个响应体，含 token 与 member（MemberProfileResult）
 */
export function login(data) {
  return request({ url: '/miniapp/auth/login', method: 'POST', data })
}

/**
 * 获取当前登录用户资料。
 * 响应体顶层：{ code, msg, member }。
 *
 * @returns {Promise<object>} 整个响应体，含 member（MemberProfileResult）
 */
export function getProfile() {
  return request({ url: '/miniapp/member/profile' })
}
