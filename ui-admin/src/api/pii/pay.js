import request from '@/utils/request'

export function getQrcodeConfig(code) {
  return request({ url: '/anon/pii/qrcode/' + code, method: 'get' })
}

export function precreate(data) {
  return request({ url: '/anon/pii/pay/precreate', method: 'post', data })
}

export function getOrder(outTradeNo, token) {
  return request({ url: '/anon/pii/order/' + outTradeNo, method: 'get', params: { token } })
}

export function downloadInvoice(outTradeNo, token) {
  return request({
    url: '/anon/pii/invoice/' + outTradeNo + '/download',
    method: 'get',
    params: { token },
    responseType: 'blob'
  })
}
