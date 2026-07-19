/**
 * 全局配置（集中管理，部署时只需改这里）。
 *
 * baseURL：H5 开发模式根据当前页面主机名自动推导后端地址，避免局域网 IP 变化后失效。
 * 非 H5 或生产构建默认使用 RuoYi 后端 http://localhost:8080。
 */

const devH5BaseURL = import.meta.env.DEV && typeof window !== 'undefined'
  ? 'http://' + window.location.hostname + ':8080'
  : 'http://localhost:8080'

const config = {
  /** 后端服务基础地址 */
  baseURL: devH5BaseURL,
  /** 通用文件上传接口（需登录，返回 {code,msg,url,fileName,newFileName}） */
  uploadPath: '/common/upload',
  /** 开发模式：未配置真实平台 appid 时允许使用 mock code 登录 */
  mockLoginEnabled: true,
  /** 请求超时时间（毫秒） */
  timeout: 15000
}

/**
 * 将后端返回的文件相对路径（/profile/upload/...）补全为可访问的完整 URL。
 *
 * @param {string} path 后端返回的 url 或 fileName
 * @returns {string} 完整可访问地址
 */
export function resolveFileUrl(path) {
  if (!path) {
    return ''
  }
  if (/^https?:\/\//.test(path)) {
    return path
  }
  return config.baseURL + path
}

export default config
