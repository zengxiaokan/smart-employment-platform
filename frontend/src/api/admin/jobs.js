import request from '@/utils/request'

export function getJobs(params) {
  return request({ url: '/admin/jobs', method: 'get', params })
}

export function createJob(data) {
  return request({ url: '/admin/jobs', method: 'post', data })
}

export function updateJob(id, data) {
  return request({ url: `/admin/jobs/${id}`, method: 'put', data })
}

export function auditJob(id, status) {
  return request({ url: `/admin/jobs/${id}/audit`, method: 'put', data: { status } })
}

export function toggleJobStatus(id, status) {
  return request({ url: `/admin/jobs/${id}/status`, method: 'put', data: { status } })
}

export function deleteJob(id) {
  return request({ url: `/admin/jobs/${id}`, method: 'delete' })
}
