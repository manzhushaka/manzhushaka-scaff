/**
 * 平台适配层：收敛微信 / 支付宝 / 云闪付三端差异。
 *
 * 编译期通过 #ifdef MP-WEIXIN / MP-ALIPAY 确定默认平台；H5 开发环境按 wechat 协议 mock。
 *
 * 云闪付（unionpay）接入说明：
 * 云闪付小程序容器兼容支付宝小程序语法，本期复用 mp-alipay 编译产物接入：
 * 1. 生产接入时，将 manifest.json 中 mp-alipay 的 appid 替换为云闪付小程序分配的 appid，
 *    并将 uni.login 的 provider 替换为云闪付容器要求的 provider 值（见 doLogin 内注释）；
 * 2. 后端在 MiniappLoginService 中配置云闪付 code2session 的 appid/密钥；
 * 3. 本期未配置真实密钥，通过 setPlatformOverride('unionpay') 在适配层把 platform 切换为
 *    'unionpay'，登录请求即以云闪付身份进入后端 mock 通道（mock_unionpay_{code}），
 *    无需重新编译即可演示云闪付全链路。
 */

/** 平台调试覆盖 storage key */
const PLATFORM_OVERRIDE_KEY = 'iip_platform_override'

/** 支持的平台标识 */
const PLATFORMS = ['wechat', 'alipay', 'unionpay']

/**
 * 获取当前平台标识（优先取调试覆盖，其次取编译期平台）。
 *
 * @returns {'wechat'|'alipay'|'unionpay'} 平台标识
 */
export function getPlatform() {
  const override = uni.getStorageSync(PLATFORM_OVERRIDE_KEY)
  if (PLATFORMS.indexOf(override) > -1) {
    return override
  }
  let platform = 'wechat'
  // #ifdef MP-WEIXIN
  platform = 'wechat'
  // #endif
  // #ifdef MP-ALIPAY
  platform = 'alipay'
  // #endif
  // #ifdef H5
  // H5 开发环境没有真实平台容器，默认按微信协议走后端 mock 通道
  platform = 'wechat'
  // #endif
  return platform
}

/**
 * 平台显示名。
 *
 * @param {string} [platform] 平台标识，默认取当前平台
 * @returns {string} 中文显示名
 */
export function getPlatformName(platform) {
  const names = {
    wechat: '微信',
    alipay: '支付宝',
    unionpay: '云闪付'
  }
  return names[platform || getPlatform()] || '微信'
}

/**
 * 设置平台调试覆盖（开发调试用，生产不需要）。
 *
 * @param {'wechat'|'alipay'|'unionpay'} platform 目标平台
 */
export function setPlatformOverride(platform) {
  if (PLATFORMS.indexOf(platform) > -1) {
    uni.setStorageSync(PLATFORM_OVERRIDE_KEY, platform)
  }
}

/**
 * 清除平台调试覆盖，恢复编译期默认平台。
 */
export function clearPlatformOverride() {
  uni.removeStorageSync(PLATFORM_OVERRIDE_KEY)
}

/**
 * 调起平台登录，换取登录 code（封装 uni.login）。
 *
 * 云闪付生产接入：当前复用支付宝容器，provider 取 'alipay'；接入真实云闪付容器时，
 * 将下方 unionpay 分支的 provider 替换为云闪付容器要求的值即可。
 *
 * @returns {Promise<{code: string}>} 登录 code（H5 环境返回空串，由调用方走 mock code）
 */
export function doLogin() {
  return new Promise((resolve, reject) => {
    let provider = 'weixin'
    // #ifdef MP-WEIXIN
    provider = 'weixin'
    // #endif
    // #ifdef MP-ALIPAY
    provider = 'alipay'
    // #endif
    if (getPlatform() === 'unionpay') {
      // 云闪付容器兼容支付宝语法；生产接入云闪付容器时替换此 provider
      provider = 'alipay'
    }
    // #ifdef H5
    // H5 无 uni.login 能力，返回空 code，由登录页回退到 mock code 模式
    resolve({ code: '' })
    return
    // #endif
    uni.login({
      provider,
      success: (res) => {
        resolve({ code: res.code || '' })
      },
      fail: (err) => {
        reject(err)
      }
    })
  })
}
