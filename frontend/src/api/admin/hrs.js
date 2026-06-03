import request from '@/utils/request'

export function getHrs(params) {
  return request({ url: '/admin/hrs', method: 'get', params })
}

export function createHr(data) {
  return request({ url: '/admin/hrs', method: 'post', data })
}

export function updateHr(id, data) {
  return request({ url: `/admin/hrs/${id}`, method: 'put', data })
}

export function deleteHr(id) {
  return request({ url: `/admin/hrs/${id}`, method: 'delete' })
}