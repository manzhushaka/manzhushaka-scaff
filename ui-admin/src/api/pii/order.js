import request from '@/utils/request'

export function listOrder(query) {
  return request({ url: '/pii/order/list', method: 'get', params: query })
}

export function getOrder(id) {
  return request({ url: '/pii/order/' + id, method: 'get' })
}
