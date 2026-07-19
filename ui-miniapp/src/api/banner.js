/**
 * 首页 Banner 轮播接口（对齐 MiniappBannerController）。
 */
import { request } from '@/common/request.js'

/**
 * 查询启用中的首页 banner 列表（按 sort 升序，无需登录）。
 *
 * @returns {Promise<Array>} banner 列表 [{bannerId,title,imageUrl,linkType,linkValue,sort}]，无数据返回空数组
 */
export async function listBanners() {
  const res = await request({ url: '/miniapp/banner/list' })
  return res.data || []
}
