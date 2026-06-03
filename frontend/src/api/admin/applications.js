import request from '@/utils/request'

export function getApplications(params) {
  return request({ url: '/admin/applications', method: 'get', params })
}

export function getApplicationDetail(id) {
  return request({ url: `/admin/applications/${id}`, method: 'get' })
}

export function deleteApplication(id) {
  return request({ url: `/admin/applications/${id}`, method: 'delete' })
}
