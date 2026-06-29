import request from '@/utils/request'

// 查询慢 SQL 日志列表
export function listSlowSql(query) {
  return request({
    url: '/monitor/slowSql/list',
    method: 'get',
    params: query
  })
}

// 查询慢 SQL 日志详细
export function getSlowSql(slowSqlId) {
  return request({
    url: '/monitor/slowSql/' + slowSqlId,
    method: 'get'
  })
}

// 删除慢 SQL 日志
export function delSlowSql(slowSqlId) {
  return request({
    url: '/monitor/slowSql/' + slowSqlId,
    method: 'delete'
  })
}

// 清空慢 SQL 日志
export function cleanSlowSql() {
  return request({
    url: '/monitor/slowSql/clean',
    method: 'delete'
  })
}
