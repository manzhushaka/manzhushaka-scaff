/**
 * 统一请求封装（基于 uni.request）。
 *
 * 契约（以后端真实代码为准）：
 * - 鉴权：除 POST /miniapp/auth/login 外，/miniapp/** 与 /common/upload 均需
 *   请求头 Authorization: Bearer {token}（复用 RuoYi JwtAuthenticationTokenFilter）。
 * - AjaxResult 包装：{ code, msg, data }，code=200 成功，code=401 未授权；
 *   登录与资料接口将 token/member 直接放在响应体顶层（{code,msg,token,member}），
 *   分页接口返回 TableDataInfo（{code,msg,rows,total}）。本封装统一 resolve 整个响应体，
 *   由调用方按需取 data / rows / token / member。
 * - 失败处理：业务错误统一 toast msg；401 清空本地登录态并跳登录页（游客无 token
 *   访问受限接口时不跳转，由页面自行降级展示）。
 */
import config from './config.js'

/** 登录态 storage key（与 store/user.js 保持一致） */
export const TOKEN_KEY = 'iip_token'
export const MEMBER_KEY = 'iip_member'

/** AjaxResult 成功码（RuoYi HttpStatus.SUCCESS） */
const SUCCESS_CODE = 200
/** AjaxResult 未授权码（RuoYi HttpStatus.UNAUTHORIZED） */
const UNAUTHORIZED_CODE = 401

/** 防止并发 401 重复跳登录 */
let loginRedirecting = false

/** 401 回调（由 store 注册，用于同步清理内存登录态） */
let unauthorizedHandler = null

/**
 * 注册 401 处理器（token 失效时调用，先于跳登录）。
 *
 * @param {Function} fn 清理登录态的回调
 */
export function setUnauthorizedHandler(fn) {
  unauthorizedHandler = fn
}

/**
 * 获取本地 token。
 *
 * @returns {string} token 或空串
 */
export function getToken() {
  return uni.getStorageSync(TOKEN_KEY) || ''
}

/**
 * 清空登录态并跳转登录页（token 失效场景）。
 */
export function redirectToLogin() {
  uni.removeStorageSync(TOKEN_KEY)
  uni.removeStorageSync(MEMBER_KEY)
  if (typeof unauthorizedHandler === 'function') {
    unauthorizedHandler()
  }
  if (loginRedirecting) {
    return
  }
  loginRedirecting = true
  uni.navigateTo({
    url: '/pages/login/index',
    complete: () => {
      setTimeout(() => {
        loginRedirecting = false
      }, 800)
    }
  })
}

/**
 * 统一 toast 业务错误。
 *
 * @param {string} msg 错误提示
 */
function toastError(msg) {
  uni.showToast({
    title: msg || '请求失败，请稍后重试',
    icon: 'none',
    duration: 2500
  })
}

/**
 * 发起请求。
 *
 * @param {object} options { url, method, data, header, timeout }
 * @returns {Promise<object>} resolve 整个响应体 { code, msg, data?/rows?/token?/member? }
 */
export function request(options) {
  return new Promise((resolve, reject) => {
    const token = getToken()
    const header = Object.assign({ 'Content-Type': 'application/json' }, options.header || {})
    if (token) {
      header.Authorization = 'Bearer ' + token
    }
    uni.request({
      url: config.baseURL + options.url,
      method: options.method || 'GET',
      data: options.data || {},
      header,
      timeout: options.timeout || config.timeout,
      success: (res) => {
        const body = res.data || {}
        if (body.code === SUCCESS_CODE) {
          resolve(body)
          return
        }
        if (body.code === UNAUTHORIZED_CODE || res.statusCode === UNAUTHORIZED_CODE) {
          if (token) {
            // 已登录但 token 失效：清登录态并跳登录
            redirectToLogin()
          }
          reject(body)
          return
        }
        toastError(body.msg)
        reject(body)
      },
      fail: () => {
        toastError('网络异常，请检查网络后重试')
        reject(new Error('network error'))
      }
    })
  })
}

/**
 * 上传文件到 POST /common/upload（需登录）。
 *
 * @param {string} filePath uni.chooseImage 返回的临时文件路径
 * @returns {Promise<object>} resolve 响应体 { code, msg, url, fileName, newFileName }
 */
export function uploadFile(filePath) {
  return new Promise((resolve, reject) => {
    const token = getToken()
    const header = {}
    if (token) {
      header.Authorization = 'Bearer ' + token
    }
    uni.showLoading({ title: '上传中', mask: true })
    uni.uploadFile({
      url: config.baseURL + config.uploadPath,
      filePath,
      name: 'file',
      header,
      success: (res) => {
        let body = {}
        try {
          body = JSON.parse(res.data)
        } catch (e) {
          body = { code: -1, msg: '上传响应解析失败' }
        }
        if (body.code === SUCCESS_CODE) {
          resolve(body)
          return
        }
        if (body.code === UNAUTHORIZED_CODE) {
          redirectToLogin()
          reject(body)
          return
        }
        toastError(body.msg || '上传失败')
        reject(body)
      },
      fail: () => {
        toastError('上传失败，请检查网络后重试')
        reject(new Error('upload error'))
      },
      complete: () => {
        uni.hideLoading()
      }
    })
  })
}
