/**
 * 发票接口（对齐 MiniappInvoiceController）。
 */
import { request } from '@/common/request.js'

/**
 * 提交发票。
 *
 * @param {object} data { merchantId?, merchantName, invoiceCode?, invoiceNo,
 *   invoiceDate?: 'yyyy-MM-dd', amount, imageUrl }
 * @returns {Promise<object>} InvoiceResult
 */
export async function submitInvoice(data) {
  const res = await request({ url: '/miniapp/invoice/submit', method: 'POST', data })
  return res.data
}

/**
 * 当前用户发票列表（按创建时间倒序）。
 *
 * @param {string} [status] 0待审核 1已通过 2已驳回，不传为全部
 * @returns {Promise<Array>} InvoiceResult[]
 */
export async function getInvoiceList(status) {
  const res = await request({ url: '/miniapp/invoice/list', data: { status } })
  return res.data || []
}

/**
 * 当前用户发票详情（仅本人可查）。
 *
 * @param {number|string} invoiceId 发票ID
 * @returns {Promise<object>} InvoiceResult
 */
export async function getInvoiceDetail(invoiceId) {
  const res = await request({ url: '/miniapp/invoice/' + invoiceId })
  return res.data
}
