/**
 * 手绘风 inline SVG 图标集。
 *
 * 小程序端不支持在模板中直接内联 <svg> 标签，这里统一把手绘 SVG 编码为
 * data URI，作为 view 的 background-image 使用（微信/支付宝/H5 三端均可渲染），
 * 避免引入图片资源或 emoji 图标。图标为描边风格（stroke），线条圆润、低饱和。
 */

/**
 * 将 SVG 字符串编码为 CSS background-image 可用的 url()。
 *
 * @param {string} svg SVG 源码
 * @returns {string} url("data:image/svg+xml,...") 形式
 */
function svgUri(svg) {
  return 'url("data:image/svg+xml;charset=UTF-8,' + encodeURIComponent(svg) + '")'
}

/**
 * 生成描边图标。
 *
 * @param {string} body svg 内部 path/shape 片段
 * @param {string} color 描边颜色
 * @returns {string} background-image 值
 */
function build(body, color) {
  const svg =
    '<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 48 48" fill="none" stroke="' +
    color +
    '" stroke-width="2.6" stroke-linecap="round" stroke-linejoin="round">' +
    body +
    '</svg>'
  return svgUri(svg)
}

const PRIMARY = '#ff6a2a'
const MUTED = '#9a8f83'
const WHITE = '#ffffff'
const SUCCESS = '#1f8a5b'

const icons = {
  /* 参与流程四步 */
  stepInvoice: build(
    '<path d="M14 6h14l8 8v26a2 2 0 0 1-2 2H14a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2z"/><path d="M28 6v8h8"/><path d="M18 24h12M18 30h12M18 36h7"/>',
    PRIMARY
  ),
  stepPoints: build(
    '<path d="M24 7l4.6 9.4 10.4 1.5-7.5 7.3 1.8 10.3L24 30.4l-9.3 4.9 1.8-10.3L9 17.9l10.4-1.5z"/>',
    PRIMARY
  ),
  stepCoupon: build(
    '<rect x="7" y="13" width="34" height="22" rx="3"/><path d="M7 20h34"/><path d="M17 27h8M17 31h5"/>',
    PRIMARY
  ),
  stepVerify: build(
    '<rect x="8" y="8" width="13" height="13" rx="2"/><rect x="27" y="8" width="13" height="13" rx="2"/><rect x="8" y="27" width="13" height="13" rx="2"/><path d="M29 33.5l4 4 7.5-8"/>',
    PRIMARY
  ),
  /* 功能入口 */
  invoice: build(
    '<path d="M14 6h14l8 8v26a2 2 0 0 1-2 2H14a2 2 0 0 1-2-2V8a2 2 0 0 1 2-2z"/><path d="M28 6v8h8"/><path d="M18 24h12M18 30h12"/>',
    PRIMARY
  ),
  coupon: build(
    '<rect x="7" y="13" width="34" height="22" rx="3"/><path d="M7 20h34"/><path d="M17 27h8"/>',
    PRIMARY
  ),
  shop: build(
    '<path d="M9 18l2.5-9h25L39 18"/><path d="M9 18h30v4a5 5 0 0 1-10 0 5 5 0 0 1-10 0 5 5 0 0 1-10 0z"/><path d="M12 27v13h24V27"/><path d="M19 40v-8h10v8"/>',
    PRIMARY
  ),
  upload: build(
    '<path d="M24 32V12"/><path d="M15 21l9-9 9 9"/><path d="M10 36h28"/>',
    PRIMARY
  ),
  arrowRight: build('<path d="M18 10l14 14-14 14"/>', MUTED),
  /* 商家信息（地址定位、联系电话） */
  location: build(
    '<path d="M42 20c0 14-18 26-18 26S6 34 6 20a18 18 0 0 1 36 0z"/><circle cx="24" cy="20" r="6"/>',
    MUTED
  ),
  phone: build(
    '<path d="M44 33.8v6a4 4 0 0 1-4.4 4 39.6 39.6 0 0 1-17.2-6.1 39 39 0 0 1-12-12A39.6 39.6 0 0 1 4.2 8.4 4 4 0 0 1 8.2 4h6a4 4 0 0 1 4 3.4c.3 2 .8 3.8 1.4 5.6a4 4 0 0 1-.9 4.2l-2.5 2.6a32 32 0 0 0 12 12l2.5-2.6a4 4 0 0 1 4.2-.9c1.8.6 3.7 1.1 5.6 1.4a4 4 0 0 1 3.5 4z"/>',
    PRIMARY
  ),
  /* 用户与登录 */
  user: build(
    '<circle cx="24" cy="17" r="8"/><path d="M9 41c2.5-8 8.5-12 15-12s12.5 4 15 12"/>',
    MUTED
  ),
  wechat: build(
    '<path d="M20 9c-7.7 0-14 4.9-14 11 0 3.4 1.9 6.4 4.9 8.4L9.5 33l5.3-2.7c1.6.5 3.4.7 5.2.7 7.7 0 14-4.9 14-11S27.7 9 20 9z"/><path d="M31 20c5.5.8 10 4.6 10 9.5 0 2.8-1.5 5.3-4 7l1.2 3.8-4.4-2.2c-1.2.4-2.5.6-3.8.6-4.4 0-8.3-2-10.3-5"/>',
    WHITE
  ),
  alipay: build(
    '<rect x="6" y="10" width="36" height="28" rx="4"/><path d="M6 22h36"/><path d="M13 30h6M13 34h10"/>',
    WHITE
  ),
  unionpay: build(
    '<rect x="6" y="10" width="36" height="28" rx="4"/><path d="M6 19h36v7H6z" fill="' + WHITE + '" stroke="none"/><path d="M13 32h8"/>',
    WHITE
  ),
  /* 操作 */
  scan: build(
    '<path d="M8 15V9a1 1 0 0 1 1-1h6M33 8h6a1 1 0 0 1 1 1v6M40 33v6a1 1 0 0 1-1 1h-6M15 40H9a1 1 0 0 1-1-1v-6"/><path d="M10 24h28"/>',
    PRIMARY
  ),
  scanWhite: build(
    '<path d="M8 15V9a1 1 0 0 1 1-1h6M33 8h6a1 1 0 0 1 1 1v6M40 33v6a1 1 0 0 1-1 1h-6M15 40H9a1 1 0 0 1-1-1v-6"/><path d="M10 24h28"/>',
    WHITE
  ),
  copy: build(
    '<rect x="15" y="15" width="24" height="24" rx="3"/><path d="M33 15V12a3 3 0 0 0-3-3H12a3 3 0 0 0-3 3v18a3 3 0 0 0 3 3h3"/>',
    PRIMARY
  ),
  check: build('<path d="M10 25l9 9 19-19"/>', SUCCESS),
  checkWhite: build('<path d="M10 25l9 9 19-19"/>', WHITE),
  camera: build(
    '<path d="M8 16h6l3-5h14l3 5h6a2 2 0 0 1 2 2v18a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2V18a2 2 0 0 1 2-2z"/><circle cx="24" cy="27" r="7"/>',
    PRIMARY
  ),
  ticket: build(
    '<path d="M8 14a2 2 0 0 1 2-2h28a2 2 0 0 1 2 2v6a4 4 0 0 0 0 8v6a2 2 0 0 1-2 2H10a2 2 0 0 1-2-2v-6a4 4 0 0 0 0-8z"/><path d="M26 14v4M26 22v4M26 30v4"/>',
    MUTED
  ),
  /* 空状态 */
  empty: build(
    '<path d="M10 14h28v22a2 2 0 0 1-2 2H12a2 2 0 0 1-2-2z"/><path d="M10 14l4-7h20l4 7"/><path d="M19 25h10"/>',
    MUTED
  ),
  clock: build('<circle cx="24" cy="24" r="16"/><path d="M24 14v10l7 4"/>', MUTED)
}

export default icons
