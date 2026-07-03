import request from '@/utils/request'

export function listMerchant(query) {
  return request({ url: '/pii/merchant/list', method: 'get', params: query })
}

export function getMerchant(id) {
  return request({ url: '/pii/merchant/' + id, method: 'get' })
}

export function addMerchant(data) {
  return request({ url: '/pii/merchant', method: 'post', data })
}

export function updateMerchant(data) {
  return request({ url: '/pii/merchant', method: 'put', data })
}

export function delMerchant(ids) {
  return request({ url: '/pii/merchant/' + ids, method: 'delete' })
}

export function changeMerchantStatus(id, status) {
  return request({ url: '/pii/merchant/changeStatus', method: 'put', data: { id, status } })
}
