import request from '@/utils/request'

// 查询请求日志列表
export function listRequestLog(query) {
  return request({
    url: '/monitor/requestLog/list',
    method: 'get',
    params: query
  })
}

// 查询请求日志详细
export function getRequestLog(requestId) {
  return request({
    url: '/monitor/requestLog/' + requestId,
    method: 'get'
  })
}

// 删除请求日志
export function delRequestLog(requestId) {
  return request({
    url: '/monitor/requestLog/' + requestId,
    method: 'delete'
  })
}

// 清空请求日志
export function cleanRequestLog() {
  return request({
    url: '/monitor/requestLog/clean',
    method: 'delete'
  })
}
