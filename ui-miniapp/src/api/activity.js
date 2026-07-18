/**
 * 活动接口（对齐 MiniappActivityController）。
 */
import { request } from '@/common/request.js'

/**
 * 查询当前生效活动。
 * data 为 CurrentActivityResult（含 merchantCount/couponCount/coupons），无活动时 data 为 null。
 *
 * @returns {Promise<object|null>} CurrentActivityResult 或 null
 */
export async function getCurrentActivity() {
  const res = await request({ url: '/miniapp/activity/current' })
  return res.data || null
}

/**
 * 查询全部生效活动列表（按优先级排序，含 regionType/city/regionName/priority）。
 *
 * @returns {Promise<Array>} 生效活动列表，无活动时返回空数组
 */
export async function getActivityList() {
  const res = await request({ url: '/miniapp/activity/list' })
  return res.data || []
}
