import request from '@/utils/request'

export function listQrcode(query) {
  return request({ url: '/pii/qrcode/list', method: 'get', params: query })
}

export function getQrcode(id) {
  return request({ url: '/pii/qrcode/' + id, method: 'get' })
}

export function addQrcode(data) {
  return request({ url: '/pii/qrcode', method: 'post', data })
}

export function updateQrcode(data) {
  return request({ url: '/pii/qrcode', method: 'put', data })
}

export function delQrcode(ids) {
  return request({ url: '/pii/qrcode/' + ids, method: 'delete' })
}

export function changeQrcodeStatus(id, status) {
  return request({ url: '/pii/qrcode/changeStatus', method: 'put', data: { id, status } })
}
