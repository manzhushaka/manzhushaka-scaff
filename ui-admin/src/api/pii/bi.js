import request from '@/utils/request'

export function getBiData(query) {
  return request({ url: '/pii/bi/data', method: 'get', params: query })
}

export function getBiDeptAggregate(query) {
  return request({ url: '/pii/bi/dept/aggregate', method: 'get', params: query })
}
