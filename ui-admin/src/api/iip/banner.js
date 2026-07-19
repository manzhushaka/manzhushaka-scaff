import request from '@/utils/request'

// 查询 banner 列表
export function listBanner(query) {
  return request({
    url: '/iip/banner/list',
    method: 'get',
    params: query
  })
}

// 查询 banner 详细（RuoYi 风格：GET /iip/banner/{bannerId}）
export function getBanner(bannerId) {
  return request({
    url: '/iip/banner/' + bannerId,
    method: 'get'
  })
}

// 新增 banner
export function addBanner(data) {
  return request({
    url: '/iip/banner',
    method: 'post',
    data: data
  })
}

// 修改 banner
export function updateBanner(data) {
  return request({
    url: '/iip/banner',
    method: 'put',
    data: data
  })
}

// 删除 banner
export function delBanner(bannerIds) {
  return request({
    url: '/iip/banner/' + bannerIds,
    method: 'delete'
  })
}
