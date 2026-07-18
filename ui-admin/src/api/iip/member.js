import request from '@/utils/request'

// 查询小程序用户列表
export function listMember(query) {
  return request({
    url: '/iip/member/list',
    method: 'get',
    params: query
  })
}

// 查询小程序用户详细
export function getMember(memberId) {
  return request({
    url: '/iip/member/getInfo/' + memberId,
    method: 'get'
  })
}

// 修改小程序用户状态（0正常 1停用）
export function changeMemberStatus(memberId, status) {
  const data = {
    memberId,
    status
  }
  return request({
    url: '/iip/member/status',
    method: 'put',
    data: data
  })
}

// 导出小程序用户列表（后端为 GET 接口，返回 Excel 二进制流，页面端按 blob 保存）
export function exportMember(query) {
  return request({
    url: '/iip/member/export',
    method: 'get',
    params: query,
    responseType: 'blob'
  })
}
