import request from '@/utils/request'

// 查询消息队列台账列表
export function listMqLog(query) {
  return request({
    url: '/monitor/mqLog/list',
    method: 'get',
    params: query
  })
}

// 查询消息队列台账详细
export function getMqLog(messageLogId) {
  return request({
    url: '/monitor/mqLog/' + messageLogId,
    method: 'get'
  })
}

// 查询消息队列执行明细
export function listMqLogDetail(messageLogId) {
  return request({
    url: '/monitor/mqLog/' + messageLogId + '/details',
    method: 'get'
  })
}

// 删除消息队列台账
export function delMqLog(messageLogId) {
  return request({
    url: '/monitor/mqLog/' + messageLogId,
    method: 'delete'
  })
}

// 清空消息队列台账
export function cleanMqLog() {
  return request({
    url: '/monitor/mqLog/clean',
    method: 'delete'
  })
}