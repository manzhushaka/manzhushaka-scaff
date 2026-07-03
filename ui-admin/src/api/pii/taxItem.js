import request from '@/utils/request'

export function listTaxItem(query) {
  return request({ url: '/pii/taxItem/list', method: 'get', params: query })
}

export function getTaxItem(id) {
  return request({ url: '/pii/taxItem/' + id, method: 'get' })
}

export function addTaxItem(data) {
  return request({ url: '/pii/taxItem', method: 'post', data })
}

export function updateTaxItem(data) {
  return request({ url: '/pii/taxItem', method: 'put', data })
}

export function delTaxItem(ids) {
  return request({ url: '/pii/taxItem/' + ids, method: 'delete' })
}

export function changeTaxItemStatus(id, status) {
  return request({ url: '/pii/taxItem/changeStatus', method: 'put', data: { id, status } })
}
