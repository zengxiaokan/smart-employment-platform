import request from '@/utils/request'

export function getLogs(params) {
  return request({ url: '/admin/logs', method: 'get', params })
}

export function getLogDetail(id) {
  return request({ url: `/admin/logs/${id}`, method: 'get' })
}

export function updateLogRemark(id, remark) {
  return request({ url: `/admin/logs/${id}/remark`, method: 'put', data: { remark } })
}

export function getTodayStats() {
  return request({ url: '/admin/logs/stats/today', method: 'get' })
}
