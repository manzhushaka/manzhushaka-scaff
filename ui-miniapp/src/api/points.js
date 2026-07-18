/**
 * 积分接口（对齐小程序端 /miniapp/points 契约）。
 *
 * 说明：后端该接口由积分域补充中，按约定契约对接——
 * 入参 { pageNum, pageSize, changeType? }，返回 TableDataInfo { code, msg, rows, total }，
 * rows 元素为 PointsRecordResult：
 * { recordId, memberId, changeType, points, balanceAfter, bizType, bizId,
 *   remaining, expireTime, createTime, remark }。
 * 备注：手工调整落库为 earn/consume 流水 + bizType=admin_adjust，
 * 「调整」筛选的后端映射以 /miniapp/points/records 实际实现为准。
 */
import { request } from '@/common/request.js'

/**
 * 当前用户积分流水分页查询。
 *
 * @param {object} params { pageNum, pageSize, changeType?: 'earn'|'consume'|'expire'|'adjust' }
 * @returns {Promise<{rows: Array, total: number}>} rows/total 分页结构
 */
export async function getPointsRecords(params) {
  const res = await request({ url: '/miniapp/points/records', data: params })
  return { rows: res.rows || [], total: res.total || 0 }
}
