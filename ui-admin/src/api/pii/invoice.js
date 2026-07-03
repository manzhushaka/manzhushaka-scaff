import request from '@/utils/request'

export function listInvoice(query) {
  return request({ url: '/pii/invoice/list', method: 'get', params: query })
}

export function getInvoice(orderId) {
  return request({ url: '/pii/invoice/' + orderId, method: 'get' })
}
