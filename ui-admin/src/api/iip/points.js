import request from '@/utils/request'

// 查询积分账户列表（按用户ID/昵称关键字筛选）
export function listPointsAccount(query) {
  return request({
    url: '/iip/points/account/list',
    method: 'get',
    params: query
  })
}

// 查询积分流水列表（按用户ID/变动类型/业务类型/时间筛选）
export function listPointsRecord(query) {
  return request({
    url: '/iip/points/record/list',
    method: 'get',
    params: query
  })
}

// 手工调整积分（正数=发放，负数=扣减；扣减超过可用余额时后端返回错误提示）
export function adjustPoints(data) {
  return request({
    url: '/iip/points/adjust',
    method: 'post',
    data: data
  })
}
