import request from '@/utils/request'

// 查询运行日志文件
export function listRuntimeLogFiles() {
  return request({
    url: '/monitor/runtimeLog/files',
    method: 'get'
  })
}

// 查询运行日志内容
export function listRuntimeLog(query) {
  return request({
    url: '/monitor/runtimeLog/list',
    method: 'get',
    params: query
  })
}
